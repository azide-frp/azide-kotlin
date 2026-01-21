package dev.azide.core.test_utils.collections.reactive_set

import dev.azide.core.collections.ReactiveSet
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.collections.reactive_set.FrozenTrackedSetVertex
import dev.azide.core.impl.collections.reactive_set.TrackedSetVertex.SetChange
import dev.azide.core.impl.collections.reactive_set.TrackedSetVertex.SetChangeObserver
import dev.azide.core.impl.collections.reactive_set.TrackedSetVertex.SetObserverHandle
import dev.azide.core.impl.collections.reactive_set.WarmTrackedSetVertex
import dev.azide.core.impl.collections.reactive_set.registerSetChangeObserver
import kotlin.jvm.JvmInline
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal object ReactiveSetTestUtils {
    fun <ElementT> createInputReactiveSet(
        initialElements: Set<ElementT>,
    ): TestInputReactiveSet<ElementT> =
        TestInputReactiveSet(
            initialElements = initialElements,
        )

    class ObservingVerifier<ElementT>(
        private val subjectVertex: WarmTrackedSetVertex<ElementT>,
    ) : SetChangeObserver<ElementT> {
        @JvmInline
        value class ReceivedChange<ElementT>(
            val receivedChange: SetChange<ElementT>?,
        )

        private var receivedChange: ReceivedChange<ElementT>? = null

        private var upstreamObserverHandle: SetObserverHandle? = Transactions.executeWithResult { propagationContext ->
            subjectVertex.registerSetChangeObserver(
                propagationContext = propagationContext,
                observer = this,
            )
        }

        /**
         * Verify that, under the given [inputStimulation], the subject reactive set changes from [expectedOldElements] to
         * [expectedChangedElements].
         */
        fun verifyChangesAsExpected(
            inputStimulation: dev.azide.core.test_utils.TestInputStimulation,
            expectedOldElements: Set<ElementT>,
            expectedChangedElements: Set<ElementT>,
        ) {
            verifyTransaction(
                inputStimulation = inputStimulation,
                expectedOldElements = expectedOldElements,
                expectedChangedElements = expectedChangedElements,
            ) { receivedChange ->
                assertEquals(
                    expected = ReceivedChange(
                        receivedChange = SetChange(
                            addedElements = expectedChangedElements.subtract(expectedOldElements),
                            removedElements = expectedOldElements.subtract(expectedChangedElements),
                        ),
                    ),
                    actual = receivedChange,
                    message = "Received change did not match expected change",
                )
            }
        }

        /**
         * Verify that, in spite of the given [inputStimulation], the subject reactive set does not change. This utility is
         * meant for verifying complete silence. If even a single emission notification is propagated by the subject
         * reactiveSet's vertex during the transaction (even if it's later corrected), the verification will fail.
         */
        fun verifyDoesNotChangeAtAll(
            inputStimulation: dev.azide.core.test_utils.TestInputStimulation,
            expectedUnaffectedElements: Set<ElementT>,
        ) {
            verifyTransaction(
                inputStimulation = inputStimulation,
                expectedOldElements = expectedUnaffectedElements,
                expectedChangedElements = expectedUnaffectedElements,
            ) { receivedChange ->
                assertEquals(
                    expected = null,
                    actual = receivedChange,
                    message = "Received change when none was expected",
                )
            }
        }

        /**
         * Verify that, in spite of the given [inputStimulation], the subject reactive set does not effectively change. This
         * utility is meant for testing change revoking. If not even a single change notification (later revoked) is
         * propagated by the subject reactive set's vertex during the transaction, the verification will fail.
         */
        fun verifyDoesNotChangeEffectively(
            inputStimulation: dev.azide.core.test_utils.TestInputStimulation,
            expectedUnaffectedElements: Set<ElementT>,
        ) {
            verifyTransaction(
                inputStimulation = inputStimulation,
                expectedOldElements = expectedUnaffectedElements,
                expectedChangedElements = expectedUnaffectedElements,
            ) { receivedChange ->
                assertEquals(
                    expected = ReceivedChange(
                        receivedChange = null,
                    ),
                    actual = receivedChange,
                    message = "Received change different from expected no-effect change",
                )
            }
        }

        private fun verifyTransaction(
            inputStimulation: dev.azide.core.test_utils.TestInputStimulation,
            expectedOldElements: Set<ElementT>,
            expectedChangedElements: Set<ElementT>,
            verifyReceivedChange: (ReceivedChange<ElementT>?) -> Unit,
        ) {
            assertIs<WarmTrackedSetVertex<ElementT>>(
                value = subjectVertex,
                message = "Subject reactive set vertex is already frozen",
            )

            val preSampledElements = Transactions.executeWithResult { propagationContext ->
                subjectVertex.getOldContentView(
                    propagationContext = propagationContext,
                ).toSet()
            }

            assertEquals(
                expected = expectedOldElements,
                actual = preSampledElements,
                message = "Pre-change sampled elements mismatch",
            )

            // Clear the change potentially received in separate transactions
            this.receivedChange = null

            val intraSampledElements = Transactions.executeWithResult { propagationContext ->
                inputStimulation.stimulate(
                    propagationContext = propagationContext,
                )

                subjectVertex.getOldContentView(
                    propagationContext = propagationContext,
                ).toSet()
            }

            verifyReceivedChange(this.receivedChange)

            // Clear the change, as it's not needed after the verification
            this.receivedChange = null

            assertEquals(
                expected = expectedOldElements,
                actual = intraSampledElements,
                message = "Intra-change sampled elements mismatch",
            )

            val postSampledElements = Transactions.executeWithResult { propagationContext ->
                subjectVertex.getOldContentView(
                    propagationContext = propagationContext,
                ).toSet()
            }

            assertEquals(
                expected = expectedChangedElements,
                actual = postSampledElements,
                message = "Post-change sampled elements mismatch",
            )
        }

        fun stop() {
            val upstreamObserverHandle =
                this.upstreamObserverHandle ?: throw IllegalStateException("Verifier is already stopped")

            subjectVertex.unregisterCollectionObserver(
                handle = upstreamObserverHandle,
            )

            this.upstreamObserverHandle = null
        }

        override fun handleChange(
            propagationContext: Transactions.PropagationContext,
            change: SetChange<ElementT>?,
        ) {
            receivedChange = ReceivedChange(
                receivedChange = change,
            )
        }
    }

    fun <ElementT> observeForVerification(
        subjectReactiveSet: ReactiveSet<ElementT>,
    ): ObservingVerifier<ElementT> {
        val subjectVertex = subjectReactiveSet.vertex as? WarmTrackedSetVertex<ElementT>
            ?: throw IllegalStateException("Subject reactive set vertex is already frozen")

        return ObservingVerifier(
            subjectVertex = subjectVertex,
        )
    }

    /**
     * Verify that the [subjectReactiveSet] is still warm and its elements match [expectedElements].
     */
    fun <ElementT> verifySampledElements(
        subjectReactiveSet: ReactiveSet<ElementT>,
        expectedElements: Set<ElementT>,
    ) {
        val subjectVertex = subjectReactiveSet.vertex

        assertIs<WarmTrackedSetVertex<ElementT>>(
            value = subjectVertex,
            message = "Subject reactive set vertex is not warm as expected",
        )

        val sampledElements = Transactions.executeWithResult { propagationContext ->
            subjectVertex.getOldContentView(
                propagationContext = propagationContext,
            ).toSet()
        }

        assertEquals(
            expected = expectedElements,
            actual = sampledElements,
            message = "Warm subject reactive set's elements did not match expected elements",
        )
    }

    /**
     * Verify that the [subjectReactiveSet] is frozen.
     */
    fun <ElementT> verifyFrozen(
        subjectReactiveSet: ReactiveSet<ElementT>,
        expectedFrozenElements: Set<ElementT>,
    ) {
        val subjectVertex = subjectReactiveSet.vertex

        assertIs<FrozenTrackedSetVertex<ElementT>>(
            value = subjectVertex,
            message = "Subject reactive set vertex is not frozen as expected",
        )

        val sampledElements = Transactions.executeWithResult { propagationContext ->
            subjectVertex.getOldContentView(
                propagationContext = propagationContext,
            ).toSet()
        }

        assertEquals(
            expected = expectedFrozenElements,
            actual = sampledElements,
            message = "Frozen subject reactive set's elements did not match expected elements",
        )
    }

    /**
     * A helper wrapper for [ObservingVerifier.verifyChangesAsExpected], which observes the [subjectReactiveSet] for the
     * purpose of a single change verification.
     */
    fun <ElementT> verifyChangesAsExpected(
        subjectReactiveSet: ReactiveSet<ElementT>,
        inputStimulation: dev.azide.core.test_utils.TestInputStimulation,
        expectedOldElements: Set<ElementT>,
        expectedChangedElements: Set<ElementT>,
    ) {
        val observingVerifier = observeForVerification(
            subjectReactiveSet = subjectReactiveSet,
        )

        observingVerifier.verifyChangesAsExpected(
            inputStimulation = inputStimulation,
            expectedOldElements = expectedOldElements,
            expectedChangedElements = expectedChangedElements,
        )

        observingVerifier.stop()
    }

    /**
     * A helper wrapper for [ObservingVerifier.verifyDoesNotChangeAtAll], which observes the [subjectReactiveSet] for the
     * purpose of a single change verification.
     */
    fun <ElementT> verifyDoesNotChangeAtAll(
        subjectReactiveSet: ReactiveSet<ElementT>,
        inputStimulation: dev.azide.core.test_utils.TestInputStimulation,
        expectedUnaffectedElements: Set<ElementT>,
    ) {
        val observingVerifier = observeForVerification(
            subjectReactiveSet = subjectReactiveSet,
        )

        observingVerifier.verifyDoesNotChangeAtAll(
            inputStimulation = inputStimulation,
            expectedUnaffectedElements = expectedUnaffectedElements,
        )

        observingVerifier.stop()
    }

    /**
     * A helper wrapper for [ObservingVerifier.verifyDoesNotChangeEffectively], which observes the [subjectReactiveSet]
     * for the purpose of a single change verification.
     */
    fun <ElementT> verifyDoesNotChangeEffectively(
        subjectReactiveSet: ReactiveSet<ElementT>,
        inputStimulation: dev.azide.core.test_utils.TestInputStimulation,
        expectedUnaffectedElements: Set<ElementT>,
    ) {
        val observingVerifier = observeForVerification(
            subjectReactiveSet = subjectReactiveSet,
        )

        observingVerifier.verifyDoesNotChangeEffectively(
            inputStimulation = inputStimulation,
            expectedUnaffectedElements = expectedUnaffectedElements,
        )

        observingVerifier.stop()
    }
}
