package dev.azide.core.test_utils.collections.reactive_set

import dev.azide.core.collections.ReactiveSet
import dev.azide.core.impl.ListenableVertex.BoundListener
import dev.azide.core.impl.ListenableVertex.ListenerHandle
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.collections.reactive_collection.TrackedSetVertex
import dev.azide.core.impl.collections.reactive_set.SetChange
import dev.azide.core.impl.registerBoundListenerOnline
import dev.azide.core.test_utils.TestStimulation
import kotlin.jvm.JvmInline
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal object ReactiveSetTestUtils {
    fun <ElementT> createInputReactiveSet(
        initialElements: Set<ElementT>,
    ): TestInputReactiveSet<ElementT> = TestInputReactiveSet(
        initialElements = initialElements,
    )

    class ObservingVerifier<ElementT>(
        private val subjectVertex: TrackedSetVertex<ElementT>,
    ) : BoundListener {
        @JvmInline
        value class ReceivedChange<ElementT>(
            val receivedChange: SetChange<ElementT>?,
        )

        private var receivedChange: ReceivedChange<ElementT>? = null

        private var upstreamListenerHandle: ListenerHandle? = Transactions.executeWithResult { propagationContext ->
            subjectVertex.registerBoundListenerOnline(
                propagationContext = propagationContext,
                listener = this,
            )
        }

        /**
         * Verify that, under the given [inputStimulation], the subject reactive set changes from [expectedOldElements] to
         * [expectedChangedElements].
         */
        fun verifyChangesAsExpected(
            inputStimulation: TestStimulation,
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
            inputStimulation: TestStimulation,
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
            inputStimulation: TestStimulation,
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
            inputStimulation: TestStimulation,
            expectedOldElements: Set<ElementT>,
            expectedChangedElements: Set<ElementT>,
            verifyReceivedChange: (ReceivedChange<ElementT>?) -> Unit,
        ) {
            assertIs<TrackedSetVertex<ElementT>>(
                value = subjectVertex,
                message = "Subject reactive set vertex is already frozen",
            )

            val preSampledElements = Transactions.executeWithResult { propagationContext ->
                subjectVertex.getOldContentView(
                    processingContext = propagationContext,
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
                    processingContext = propagationContext,
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
                    processingContext = propagationContext,
                ).toSet()
            }

            assertEquals(
                expected = expectedChangedElements,
                actual = postSampledElements,
                message = "Post-change sampled elements mismatch",
            )
        }

        fun stop() {
            val upstreamListenerHandle =
                this.upstreamListenerHandle ?: throw IllegalStateException("Verifier is already stopped")

            subjectVertex.unregisterListener(
                handle = upstreamListenerHandle,
            )

            this.upstreamListenerHandle = null
        }

        override fun handle(
            propagationContext: Transactions.PropagationContext,
        ) {
            receivedChange = ReceivedChange(
                receivedChange = subjectVertex.ongoingChange,
            )
        }
    }

    fun <ElementT> observeForVerification(
        subjectReactiveSet: ReactiveSet<ElementT>,
    ): ObservingVerifier<ElementT> {
        val subjectVertex = subjectReactiveSet.trackedVertex as? TrackedSetVertex<ElementT>
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
        val subjectVertex = subjectReactiveSet.trackedVertex

        assertIs<TrackedSetVertex<ElementT>>(
            value = subjectVertex,
            message = "Subject reactive set vertex is not warm as expected",
        )

        val sampledElements = Transactions.executeWithResult { propagationContext ->
            subjectVertex.getOldContentView(
                processingContext = propagationContext,
            ).toSet()
        }

        assertEquals(
            expected = expectedElements,
            actual = sampledElements,
            message = "Warm subject reactive set's elements did not match expected elements",
        )
    }

    /**
     * A helper wrapper for [ObservingVerifier.verifyChangesAsExpected], which observes the [subjectReactiveSet] for the
     * purpose of a single change verification.
     */
    fun <ElementT> verifyChangesAsExpected(
        subjectReactiveSet: ReactiveSet<ElementT>,
        inputStimulation: TestStimulation,
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
        inputStimulation: TestStimulation,
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
        inputStimulation: TestStimulation,
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
