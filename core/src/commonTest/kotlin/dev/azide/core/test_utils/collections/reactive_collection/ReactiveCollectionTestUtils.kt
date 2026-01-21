package dev.azide.core.test_utils.collections.reactive_collection

import dev.azide.core.collections.ReactiveCollection
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.collections.reactive_collection.TrackedCollectionVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedCollectionVertex.CollectionChange
import dev.azide.core.impl.collections.reactive_collection.TrackedCollectionVertex.CollectionObserver
import dev.azide.core.impl.collections.reactive_collection.TrackedCollectionVertex.CollectionObserverHandle
import dev.azide.core.test_utils.TestInputStimulation
import dev.kmpx.collections.multi_sets.MultiSet
import dev.kmpx.collections.multi_sets.minus
import dev.kmpx.collections.multi_sets.toMultiSet
import kotlin.jvm.JvmInline
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal object ReactiveCollectionTestUtils {
    class ObservingVerifier<ElementT>(
        private val subjectVertex: TrackedCollectionVertex<ElementT>,
    ) : CollectionObserver<ElementT> {
        @JvmInline
        value class ReceivedChange<ElementT>(
            val receivedChange: CollectionChange<ElementT>?,
        )

        private var receivedChange: ReceivedChange<ElementT>? = null

        private var upstreamObserverHandle: CollectionObserverHandle? =
            Transactions.executeWithResult { propagationContext ->
                subjectVertex.registerCollectionObserver(
                    propagationContext = propagationContext,
                    observer = this,
                )
            }

        /**
         * Verify that, under the given [inputStimulation], the subject reactive collection changes from
         * [expectedOldElements] to [expectedChangedElements].
         */
        fun verifyChangesAsExpected(
            inputStimulation: TestInputStimulation,
            expectedOldElements: MultiSet<ElementT>,
            expectedChangedElements: MultiSet<ElementT>,
        ) {
            val expectedAddedElements = expectedChangedElements - expectedOldElements
            val expectedRemovedElements = expectedOldElements - expectedChangedElements

            verifyTransaction(
                inputStimulation = inputStimulation,
                expectedOldElements = expectedOldElements,
                expectedChangedElements = expectedChangedElements,
            ) { receivedChange ->
                val innerReceivedChange: CollectionChange<ElementT> = assertNotNull(
                    actual = receivedChange?.receivedChange,
                    message = "Expected a change but none was received",
                )

                assertEquals(
                    expected = expectedAddedElements,
                    actual = innerReceivedChange.addedElements.toMultiSet(),
                    message = "Added elements mismatch",
                )

                assertEquals(
                    expected = expectedRemovedElements,
                    actual = innerReceivedChange.removedElements.toMultiSet(),
                    message = "Removed elements mismatch",
                )
            }
        }

        /**
         * Verify that, in spite of the given [inputStimulation], the subject reactive collection does not change. This
         * utility is meant for verifying complete silence. If even a single emission notification is propagated by the
         * subject reactive collection's vertex during the transaction (even if it's later corrected), the verification
         * will fail.
         */
        fun verifyDoesNotChangeAtAll(
            inputStimulation: TestInputStimulation,
            expectedUnaffectedElements: MultiSet<ElementT>,
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
         * Verify that, in spite of the given [inputStimulation], the subject reactive collection does not effectively
         * change. This utility is meant for testing change revoking. If not even a single change notification (later
         * revoked) is propagated by the subject reactive collection's vertex during the transaction, the verification
         * will fail.
         */
        fun verifyDoesNotChangeEffectively(
            inputStimulation: TestInputStimulation,
            expectedUnaffectedElements: MultiSet<ElementT>,
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
            inputStimulation: TestInputStimulation,
            expectedOldElements: MultiSet<ElementT>,
            expectedChangedElements: MultiSet<ElementT>,
            verifyReceivedChange: (ReceivedChange<ElementT>?) -> Unit,
        ) {
            val preSampledElements = Transactions.executeWithResult { propagationContext ->
                subjectVertex.getOldContentView(
                    propagationContext = propagationContext,
                ).toMultiSet()
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
                ).toMultiSet()
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
                ).toMultiSet()
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
            change: CollectionChange<ElementT>?,
        ) {
            receivedChange = ReceivedChange(
                receivedChange = change,
            )
        }
    }

    fun <ElementT> observeForVerification(
        subjectReactiveCollection: ReactiveCollection<ElementT>,
    ): ObservingVerifier<ElementT> {
        val subjectVertex = subjectReactiveCollection.vertex

        return ObservingVerifier(
            subjectVertex = subjectVertex,
        )
    }

    /**
     * Verify that the [subjectReactiveCollection] is still warm and its elements match [expectedElements].
     */
    fun <ElementT> verifySampledElements(
        subjectReactiveCollection: ReactiveCollection<ElementT>,
        expectedElements: MultiSet<ElementT>,
    ) {
        val subjectVertex = subjectReactiveCollection.vertex

        val sampledElements = Transactions.executeWithResult { propagationContext ->
            subjectVertex.getOldContentView(
                propagationContext = propagationContext,
            ).toMultiSet()
        }

        assertEquals(
            expected = expectedElements,
            actual = sampledElements,
            message = "Subject reactive collection's elements did not match expected elements",
        )
    }

    /**
     * A helper wrapper for [ObservingVerifier.verifyChangesAsExpected], which observes the [subjectReactiveCollection] for the
     * purpose of a single change verification.
     */
    fun <ElementT> verifyChangesAsExpected(
        subjectReactiveCollection: ReactiveCollection<ElementT>,
        inputStimulation: TestInputStimulation,
        expectedOldElements: MultiSet<ElementT>,
        expectedChangedElements: MultiSet<ElementT>,
    ) {
        val observingVerifier = observeForVerification(
            subjectReactiveCollection = subjectReactiveCollection,
        )

        observingVerifier.verifyChangesAsExpected(
            inputStimulation = inputStimulation,
            expectedOldElements = expectedOldElements,
            expectedChangedElements = expectedChangedElements,
        )

        observingVerifier.stop()
    }

    /**
     * A helper wrapper for [ObservingVerifier.verifyDoesNotChangeAtAll], which observes the [subjectReactiveCollection] for the
     * purpose of a single change verification.
     */
    fun <ElementT> verifyDoesNotChangeAtAll(
        subjectReactiveCollection: ReactiveCollection<ElementT>,
        inputStimulation: TestInputStimulation,
        expectedUnaffectedElements: MultiSet<ElementT>,
    ) {
        val observingVerifier = observeForVerification(
            subjectReactiveCollection = subjectReactiveCollection,
        )

        observingVerifier.verifyDoesNotChangeAtAll(
            inputStimulation = inputStimulation,
            expectedUnaffectedElements = expectedUnaffectedElements,
        )

        observingVerifier.stop()
    }

    /**
     * A helper wrapper for [ObservingVerifier.verifyDoesNotChangeEffectively], which observes the [subjectReactiveCollection]
     * for the purpose of a single change verification.
     */
    fun <ElementT> verifyDoesNotChangeEffectively(
        subjectReactiveCollection: ReactiveCollection<ElementT>,
        inputStimulation: TestInputStimulation,
        expectedUnaffectedElements: MultiSet<ElementT>,
    ) {
        val observingVerifier = observeForVerification(
            subjectReactiveCollection = subjectReactiveCollection,
        )

        observingVerifier.verifyDoesNotChangeEffectively(
            inputStimulation = inputStimulation,
            expectedUnaffectedElements = expectedUnaffectedElements,
        )

        observingVerifier.stop()
    }
}
