package dev.azide.core.impl.cell.effects

import dev.azide.core.Cell
import dev.azide.core.Trigger
import dev.azide.core.executeInternallyWrappedUp
import dev.azide.core.impl.Committable
import dev.azide.core.impl.Revocable
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.ListenableVertex
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.cell.CellVertex.Update
import dev.azide.core.impl.effects.InternalEffect
import dev.azide.core.impl.effects.InternalSchedule
import dev.azide.core.impl.registerBoundListenerOnline

class CellTriggerEverySchedule(
    private val sourceActionCell: Cell<Trigger>,
) : InternalSchedule {
    private enum class InternalState {
        ShutDown, StartedUp, Disposed,
    }

    override fun startInternally(
        propagationContext: Transactions.PropagationContext,
        wrapUpContext: Transactions.WrapUpContext,
    ): InternalEffect.RevocableOutcome<Unit> {
        val initialInnerTrigger: Trigger = sourceActionCell.vertex.getOldValue(
            propagationContext = propagationContext,
        )

        val initialInnerTriggerRevocable = initialInnerTrigger.executeInternally(
            propagationContext = propagationContext,
            wrapUpContext = wrapUpContext,
        ).revocable

        class TriggerEveryRevocableOutcome : InternalEffect.RevocableOutcome<Unit>, ListenableVertex.BoundListener,
            Committable {
            private val sourceVertex: CellVertex<Trigger>
                get() = sourceActionCell.vertex

            override val result = Unit

            private var upstreamListenerHandle: ListenableVertex.ListenerHandle? = null

            private var unstableNewInnerTriggerRevocable: Revocable? = null

            private var internalState = InternalState.ShutDown

            private var _isEnqueuedForCommitment = false

            /**
             * Handle the source schedule cell update
             */
            override fun handle(
                propagationContext: Transactions.PropagationContext,
            ) {
                val sourceOngoingUpdate: Update<Trigger>? = sourceVertex.ongoingUpdate

                unstableNewInnerTriggerRevocable?.revoke()

                when (sourceOngoingUpdate) {
                    null -> { // Source update revocation
                        // Revoke the ongoing new inner schedule start
                        unstableNewInnerTriggerRevocable = null
                    }

                    else -> { // Initial source update / source update correction
                        // Revoke the ongoing new inner schedule start
                        val newInnerTrigger: Trigger = sourceOngoingUpdate.updatedValue

                        val newInnerTriggerRevocable = newInnerTrigger.executeInternallyWrappedUp(
                            propagationContext = propagationContext,
                        ).revocable

                        unstableNewInnerTriggerRevocable = newInnerTriggerRevocable
                    }
                }
            }

            /**
             * Cancel the cell execute-every schedule.
             */
            override fun cancelInternally(
                propagationContext: Transactions.PropagationContext,
                wrapUpContext: Transactions.WrapUpContext,
            ): Revocable {
                shutDown()

                return object : Revocable {
                    /**
                     * Revoke the cancellation of the cell actuation effect.
                     */
                    override fun revoke() {
                        if (internalState == InternalState.Disposed) {
                            return
                        }

                        // Re-initialize the schedule
                        startUp(
                            propagationContext = propagationContext,
                        )
                    }
                }
            }

            override fun revoke() {
                initialInnerTriggerRevocable.revoke()

                if (internalState == InternalState.StartedUp) {
                    shutDown()
                }

                internalState = InternalState.Disposed
            }

            private fun startUp(
                propagationContext: Transactions.PropagationContext,
            ) {
                if (internalState != InternalState.ShutDown) {
                    throw IllegalStateException("Schedule is already started up or has been revoked: $internalState")
                }

                if (this@TriggerEveryRevocableOutcome.upstreamListenerHandle != null || this@TriggerEveryRevocableOutcome.unstableNewInnerTriggerRevocable != null) {
                    throw IllegalStateException("ListenableVertex seems to already be started up")
                }

                // Re-register the listener
                this@TriggerEveryRevocableOutcome.upstreamListenerHandle = sourceVertex.registerBoundListenerOnline(
                    propagationContext = propagationContext,
                    listener = this@TriggerEveryRevocableOutcome,
                )

                val sourceOngoingUpdate: Update<Trigger>? = sourceVertex.ongoingUpdate

                if (sourceOngoingUpdate != null) { // There's an ongoing source effect update
                    // Execute the new inner action
                    val newInnerAction: Trigger = sourceOngoingUpdate.updatedValue

                    val initialNewInnerActionExecutionOutcome = newInnerAction.executeInternallyWrappedUp(
                        propagationContext = propagationContext,
                    )

                    this@TriggerEveryRevocableOutcome.unstableNewInnerTriggerRevocable =
                        initialNewInnerActionExecutionOutcome.revocable
                }

                ensureEnqueuedForCommitment(
                    propagationContext = propagationContext,
                )

                internalState = InternalState.StartedUp
            }

            private fun shutDown() {
                if (internalState != InternalState.StartedUp) {
                    throw IllegalStateException("Action is not started up: $internalState")
                }

                run {
                    val upstreamListenerHandle = this.upstreamListenerHandle
                        ?: throw IllegalStateException("Action doesn't seem to be started up")

                    this.upstreamListenerHandle = null

                    // Unregister the listener
                    sourceVertex.unregisterListener(
                        handle = upstreamListenerHandle,
                    )
                }

                unstableNewInnerTriggerRevocable?.revoke()
                unstableNewInnerTriggerRevocable = null

                internalState = InternalState.ShutDown
            }

            private fun ensureEnqueuedForCommitment(
                propagationContext: Transactions.PropagationContext,
            ) {
                if (!_isEnqueuedForCommitment) {
                    propagationContext.enqueueForCommitment(this)

                    _isEnqueuedForCommitment = true
                }
            }

            override fun commit(
                commitmentContext: Transactions.CommitmentContext,
            ) {
                if (internalState != InternalState.StartedUp) {
                    return
                }

                // FIXME: This line is necessary, but removing it doesn't cause any of the tests to fail
                this.unstableNewInnerTriggerRevocable = null
            }

            init {
                wrapUpContext.enqueueForWrapUp { propagationContext ->
                    startUp(
                        propagationContext = propagationContext,
                    )
                }
            }
        }

        return TriggerEveryRevocableOutcome()
    }
}
