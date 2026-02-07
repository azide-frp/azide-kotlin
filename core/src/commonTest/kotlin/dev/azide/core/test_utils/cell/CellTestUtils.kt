package dev.azide.core.test_utils.cell

import dev.azide.core.Cell
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex.BoundListener
import dev.azide.core.impl.Vertex.Listener
import dev.azide.core.impl.Vertex.ListenerHandle
import dev.azide.core.impl.Vertex.ListenerStatus
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.cell.CellVertex.Update
import dev.azide.core.impl.registerBoundListenerOnline
import dev.azide.core.impl.registerListenerOnline
import dev.azide.core.test_utils.TestStimulation
import kotlin.jvm.JvmInline
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal object CellTestUtils {
    private object NoopListener : Listener {
        override fun handle(
            propagationContext: Transactions.PropagationContext,
        ): ListenerStatus = ListenerStatus.Reachable
    }

    class ObservingVerifier<ValueT>(
        propagationContext: Transactions.PropagationContext,
        private val subjectVertex: CellVertex<ValueT>,
    ) : BoundListener {
        @JvmInline
        value class ReceivedUpdate<ValueT>(
            val receivedUpdate: Update<ValueT>?,
        )

        private var receivedUpdate: ReceivedUpdate<ValueT>? = null

        private var upstreamListenerHandle: ListenerHandle? = subjectVertex.registerBoundListenerOnline(
            propagationContext = propagationContext,
            listener = this,
        )

        /**
         * Verify that, under the given [inputStimulation], the subject cell updates from [expectedOldValue] to
         * [expectedNewValue].
         */
        fun verifyUpdatesAsExpected(
            inputStimulation: TestStimulation,
            expectedOldValue: ValueT,
            expectedNewValue: ValueT,
        ) {
            verifyReceivedUpdate(
                inputStimulation = inputStimulation,
                expectedOldValue = expectedOldValue,
                expectedReceivedUpdate = ReceivedUpdate(
                    receivedUpdate = Update(
                        updatedValue = expectedNewValue,
                    ),
                ),
            )
        }

        /**
         * Verify that, in spite of the given [inputStimulation], the subject cell does not update. This utility is
         * meant for verifying complete silence. If even a single emission notification is propagated by the subject
         * cell's vertex during the transaction (even if it's later corrected), the verification will fail.
         */
        fun verifyDoesNotUpdateAtAll(
            inputStimulation: TestStimulation,
            expectedUnaffectedValue: ValueT,
        ) {
            verifyReceivedUpdate(
                inputStimulation = inputStimulation,
                expectedOldValue = expectedUnaffectedValue,
                expectedReceivedUpdate = null,
            )
        }

        /**
         * Verify that, in spite of the given [inputStimulation], the subject cell does not effectively update. This
         * utility is meant for testing update revoking. If not even a single update notification (later revoked) is
         * propagated by the subject cell's vertex during the transaction, the verification will fail.
         */
        fun verifyDoesNotUpdateEffectively(
            inputStimulation: TestStimulation,
            expectedUnaffectedValue: ValueT,
        ) {
            verifyReceivedUpdate(
                inputStimulation = inputStimulation,
                expectedOldValue = expectedUnaffectedValue,
                expectedReceivedUpdate = ReceivedUpdate(
                    receivedUpdate = null,
                ),
            )
        }

        private fun verifyReceivedUpdate(
            inputStimulation: TestStimulation,
            expectedOldValue: ValueT,
            expectedReceivedUpdate: ReceivedUpdate<ValueT>?,
        ) {
            assertIs<CellVertex<ValueT>>(
                value = subjectVertex,
                message = "Subject cell vertex is already frozen",
            )

            val preSampledValue = Transactions.executeWithResult { propagationContext ->
                subjectVertex.getOldValue(
                    propagationContext = propagationContext,
                )
            }

            assertEquals(
                expected = expectedOldValue,
                actual = preSampledValue,
                message = "Pre-update sampled value mismatch",
            )

            // Clear the update potentially received in separate transactions
            receivedUpdate = null

            val intraSampledValue = Transactions.executeWithResult(
                propagate = { propagationContext ->
                    inputStimulation.stimulate(
                        propagationContext = propagationContext,
                    )

                    subjectVertex.getOldValue(
                        propagationContext = propagationContext,
                    )
                },
            )

            assertEquals(
                expected = expectedReceivedUpdate,
                actual = receivedUpdate,
                message = "Received update mismatch",
            )

            // Clear the update, as it's not needed after the verification
            receivedUpdate = null

            assertEquals(
                expected = expectedOldValue,
                actual = intraSampledValue,
                message = "Intra-update sampled value mismatch",
            )

            val postSampledValue = Transactions.executeWithResult { propagationContext ->
                subjectVertex.getOldValue(
                    propagationContext = propagationContext,
                )
            }

            val expectedNewValue = when (val expectedUpdate = expectedReceivedUpdate?.receivedUpdate) {
                null -> expectedOldValue
                else -> expectedUpdate.updatedValue
            }

            assertEquals(
                expected = expectedNewValue,
                actual = postSampledValue,
                message = "Post-update sampled value mismatch",
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
            receivedUpdate = ReceivedUpdate(
                receivedUpdate = subjectVertex.ongoingUpdate,
            )
        }
    }

    fun <ValueT> observeForVerification(
        subjectCell: Cell<ValueT>,
    ): ObservingVerifier<ValueT> = Transactions.executeWithResult { propagationContext ->
        val subjectVertex = subjectCell.vertex

        ObservingVerifier(
            propagationContext = propagationContext,
            subjectVertex = subjectVertex,
        )
    }

    /**
     * Verify that the [subjectCell] is still warm and samples to [expectedValue], both when it's not observed and when
     * it is. This utility adds a temporary no-op listener to perform the active sampling.
     */
    fun <ValueT> verifyAtRest(
        subjectCell: Cell<ValueT>,
        expectedValue: ValueT,
    ) {
        val subjectVertex = subjectCell.vertex

        val passivelySampledValue = Transactions.executeWithResult { propagationContext ->
            subjectVertex.getOldValue(
                propagationContext = propagationContext,
            )
        }

        assertEquals(
            expected = expectedValue,
            actual = passivelySampledValue,
            message = "Passively sampled value of subject cell did not yield the expected value",
        )

        val activelySampledValue = Transactions.executeWithResult { propagationContext ->
            val listenerHandle = subjectVertex.registerListenerOnline(
                propagationContext = propagationContext,
                listener = NoopListener,
            )

            val sampledValue = subjectVertex.getOldValue(
                propagationContext = propagationContext,
            )

            subjectVertex.unregisterListener(
                handle = listenerHandle,
            )

            sampledValue
        }

        assertEquals(
            expected = expectedValue,
            actual = activelySampledValue,
            message = "Actively sampled value of subject cell did not yield the expected value",
        )
    }

    /**
     * A helper wrapper for [ObservingVerifier.verifyUpdatesAsExpected], which observes the [subjectCell] for the
     * purpose of a single update verification.
     */
    fun <ValueT> verifyUpdatesAsExpected(
        subjectCell: Cell<ValueT>,
        inputStimulation: TestStimulation,
        expectedOldValue: ValueT,
        expectedNewValue: ValueT,
    ) {
        val observingVerifier = observeForVerification(
            subjectCell = subjectCell,
        )

        observingVerifier.verifyUpdatesAsExpected(
            inputStimulation = inputStimulation,
            expectedOldValue = expectedOldValue,
            expectedNewValue = expectedNewValue,
        )

        observingVerifier.stop()
    }

    /**
     * A helper wrapper for [ObservingVerifier.verifyDoesNotUpdateAtAll], which observes the [subjectCell] for the
     * purpose of a single update verification.
     */
    fun <ValueT> verifyDoesNotUpdateAtAll(
        subjectCell: Cell<ValueT>,
        inputStimulation: TestStimulation,
        expectedUnaffectedValue: ValueT,
    ) {
        val observingVerifier = observeForVerification(
            subjectCell = subjectCell,
        )

        observingVerifier.verifyDoesNotUpdateAtAll(
            inputStimulation = inputStimulation,
            expectedUnaffectedValue = expectedUnaffectedValue,
        )

        observingVerifier.stop()
    }

    /**
     * A helper wrapper for [ObservingVerifier.verifyDoesNotUpdateEffectively], which observes the [subjectCell]
     * for the purpose of a single update verification.
     */
    fun <ValueT> verifyDoesNotUpdateEffectively(
        subjectCell: Cell<ValueT>,
        inputStimulation: TestStimulation,
        expectedUnaffectedValue: ValueT,
    ) {
        val observingVerifier = observeForVerification(
            subjectCell = subjectCell,
        )

        observingVerifier.verifyDoesNotUpdateEffectively(
            inputStimulation = inputStimulation,
            expectedUnaffectedValue = expectedUnaffectedValue,
        )

        observingVerifier.stop()
    }

    /**
     * Register a no-op listener on the [subjectCell].
     */
    fun <ValueT> registerNoopListener(
        subjectCell: Cell<ValueT>,
    ) {
        Transactions.execute { propagationContext ->
            subjectCell.vertex.registerListenerOnline(
                propagationContext = propagationContext,
                listener = NoopListener,
            )
        }
    }
}
