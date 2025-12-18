package dev.azide.core.test_utils.cell

import dev.azide.core.Cell
import dev.azide.core.MomentContext
import dev.azide.core.internal.Transactions
import dev.azide.core.internal.cell.CellVertex
import dev.azide.core.internal.cell.CellVertex.Observer
import dev.azide.core.internal.cell.CellVertex.ObserverHandle
import dev.azide.core.internal.cell.CellVertex.Update
import dev.azide.core.internal.cell.FrozenCellVertex
import dev.azide.core.internal.cell.WarmCellVertex
import dev.azide.core.test_utils.TestInputStimulation
import kotlin.jvm.JvmInline
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal object CellTestUtils {
    private object NoopObserver : Observer<Any?> {
        override fun handleUpdate(
            propagationContext: Transactions.PropagationContext,
            update: Update<Any?>?,
        ) {
        }
    }

    fun <ValueT> createInputCell(
        initialValue: ValueT,
    ): dev.azide.core.test_utils.cell.TestInputCell<ValueT> =
        TestInputCell(
            initialValue = initialValue,
        )

    /**
     * Spawn a stateful cell, not expecting it to update during spawn.
     */
    fun <ValueT : Any> spawnStatefulCell(
        spawn: context(MomentContext) () -> Cell<ValueT>,
    ): Cell<ValueT> = Transactions.execute { propagationContext ->
        val subjectCell = with(
            MomentContext(
                propagationContext = propagationContext,
            ),
        ) {
            spawn()
        }

        val ongoingUpdate = subjectCell.getVertex(
            propagationContext = propagationContext,
        ).ongoingUpdate

        assertNull(
            actual = ongoingUpdate,
            message = "Spawned subject cell has an ongoing update unexpectedly",
        )

        return@execute subjectCell
    }

    /**
     * Spawn a stateful cell, expecting it to update during spawn to [expectedUpdatedValue].
     */
    fun <ValueT> spawnStatefulCellExpectingUpdate(
        inputStimulation: TestInputStimulation? = null,
        expectedOldValue: ValueT,
        expectedUpdatedValue: ValueT,
        spawn: context(MomentContext) () -> Cell<ValueT>,
    ): Cell<ValueT> = Transactions.execute { propagationContext ->
        inputStimulation?.stimulate(
            propagationContext = propagationContext,
        )

        val subjectCell = with(
            MomentContext(
                propagationContext = propagationContext,
            ),
        ) {
            spawn()
        }

        val subjectVertex = subjectCell.getVertex(
            propagationContext = propagationContext,
        )

        val sampledOldValue = subjectVertex.getOldValue(
            propagationContext = propagationContext,
        )

        assertEquals(
            expected = expectedOldValue,
            actual = sampledOldValue,
            message = "Spawned subject cell's old value did not match expected old value",
        )

        val ongoingUpdate = subjectVertex.ongoingUpdate

        assertNotNull(
            actual = ongoingUpdate,
            message = "Spawned subject cell has no ongoing update unexpectedly",
        )

        assertEquals(
            expected = expectedUpdatedValue,
            actual = ongoingUpdate.updatedValue,
        )

        return@execute subjectCell
    }

    class ObservingVerifier<ValueT>(
        private val subjectVertex: CellVertex<ValueT>,
    ) : Observer<ValueT> {
        @JvmInline
        value class ReceivedUpdate<ValueT>(
            val receivedUpdate: Update<ValueT>?,
        )

        private var receivedUpdate: ReceivedUpdate<ValueT>? = null

        private var upstreamObserverHandle: ObserverHandle? = Transactions.execute { propagationContext ->
            subjectVertex.registerObserver(
                propagationContext = propagationContext,
                observer = this,
            )
        }

        /**
         * Verify that, under the given [inputStimulation], the subject cell updates from [expectedOldValue] to
         * [expectedNewValue].
         */
        fun verifyUpdatesAsExpected(
            inputStimulation: TestInputStimulation,
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
            inputStimulation: TestInputStimulation,
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
            inputStimulation: TestInputStimulation,
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
            inputStimulation: TestInputStimulation,
            expectedOldValue: ValueT,
            expectedReceivedUpdate: ReceivedUpdate<ValueT>?,
        ) {
            assertIs<WarmCellVertex<ValueT>>(
                value = subjectVertex,
                message = "Subject cell vertex is already frozen",
            )

            val preSampledValue = Transactions.execute { propagationContext ->
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

            val intraSampledValue = Transactions.execute(
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

            val postSampledValue = Transactions.execute { propagationContext ->
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
            val upstreamObserverHandle =
                this.upstreamObserverHandle ?: throw IllegalStateException("Verifier is already stopped")

            subjectVertex.unregisterObserver(
                handle = upstreamObserverHandle,
            )

            this.upstreamObserverHandle = null
        }

        override fun handleUpdate(
            propagationContext: Transactions.PropagationContext,
            update: Update<ValueT>?,
        ) {
            receivedUpdate = ReceivedUpdate(
                receivedUpdate = update,
            )
        }
    }

    fun <ValueT> observeForVerification(
        subjectCell: Cell<ValueT>,
    ): ObservingVerifier<ValueT> = Transactions.execute { propagationContext ->
        val subjectVertex = subjectCell.getVertex(
            propagationContext = propagationContext,
        )

        ObservingVerifier(
            subjectVertex = subjectVertex,
        )
    }

    /**
     * Verify that the [subjectCell] is still warm and samples to [expectedValue], both when it's not observed and when
     * it is. This utility adds a temporary no-op observer to perform the active sampling.
     */
    fun <ValueT> verifyAtRest(
        subjectCell: Cell<ValueT>,
        expectedValue: ValueT,
    ) {
        val subjectVertex = Transactions.execute { propagationContext ->
            subjectCell.getVertex(
                propagationContext = propagationContext,
            )
        }

        assertIs<WarmCellVertex<ValueT>>(
            value = subjectVertex,
            message = "Subject cell vertex is not warm as expected",
        )

        val passivelySampledValue = Transactions.execute { propagationContext ->
            subjectVertex.getOldValue(
                propagationContext = propagationContext,
            )
        }

        assertEquals(
            expected = expectedValue,
            actual = passivelySampledValue,
            message = "Passively sampled value of subject cell did not yield the expected value",
        )

        val activelySampledValue = Transactions.execute { propagationContext ->
            val observerHandle = subjectVertex.registerObserver(
                propagationContext = propagationContext,
                observer = NoopObserver,
            )

            val sampledValue = subjectVertex.getOldValue(
                propagationContext = propagationContext,
            )

            subjectVertex.unregisterObserver(
                handle = observerHandle,
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
     * Verify that the [subjectCell] is frozen.
     */
    fun <ValueT> verifyFrozen(
        subjectCell: Cell<ValueT>,
        expectedFrozenValue: ValueT,
    ) {
        val subjectVertex = Transactions.execute { propagationContext ->
            subjectCell.getVertex(
                propagationContext = propagationContext,
            )
        }

        assertIs<FrozenCellVertex<ValueT>>(
            value = subjectVertex,
            message = "Subject cell vertex is not frozen as expected",
        )

        val sampledValue = Transactions.execute { propagationContext ->
            subjectVertex.getOldValue(
                propagationContext = propagationContext,
            )
        }

        assertEquals(
            expected = expectedFrozenValue,
            actual = sampledValue,
            message = "Frozen subject cell's value did not match expected value",
        )
    }

    fun <ValueT> verifyFoo(
        subjectCell: Cell<ValueT>,
    ) {
        val observingVerifier = observeForVerification(
            subjectCell = subjectCell,
        )

        observingVerifier.stop()
    }


    /**
     * A helper wrapper for [ObservingVerifier.verifyUpdatesAsExpected], which observes the [subjectCell] for the
     * purpose of a single update verification.
     */
    fun <ValueT> verifyUpdatesAsExpected(
        subjectCell: Cell<ValueT>,
        inputStimulation: TestInputStimulation,
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
        inputStimulation: TestInputStimulation,
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
        inputStimulation: TestInputStimulation,
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
     * Register a no-op observer on the [subjectCell].
     */
    fun <ValueT> registerNoopObserver(
        subjectCell: Cell<ValueT>,
    ) {
        Transactions.execute { propagationContext ->
            val subjectVertex = subjectCell.getVertex(
                propagationContext = propagationContext,
            )

            subjectVertex.registerObserver(
                propagationContext = propagationContext,
                observer = NoopObserver,
            )
        }
    }
}
