package dev.azide.core.impl.cell.operated_vertices

import dev.azide.core.Action
import dev.azide.core.Cell
import dev.azide.core.executeInternallyWrappedUp
import dev.azide.core.impl.CommittableVertex
import dev.azide.core.impl.Revocable
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.cell.CellVertex.Update
import dev.azide.core.impl.cell.abstract_vertices.AbstractStatefulCellVertex
import dev.azide.core.impl.effects.InternalEffect
import dev.azide.core.impl.registerBoundListenerOnline

class ExecutedEveryCellVertex<InnerResultT> private constructor(
    wrapUpContext: Transactions.WrapUpContext,
    private val sourceActionCell: Cell<Action<InnerResultT>>,
    initialInnerActionResult: InnerResultT,
) : AbstractStatefulCellVertex<InnerResultT>(
    wrapUpContext = wrapUpContext,
    initialValue = initialInnerActionResult,
), Vertex.BoundListener, CommittableVertex {
    class ExecutionEffect<InnerResultT>(
        private val sourceActionCell: Cell<Action<InnerResultT>>,
    ) : InternalEffect<Cell<InnerResultT>> {
        override fun startInternally(
            propagationContext: Transactions.PropagationContext,
            wrapUpContext: Transactions.WrapUpContext,
        ): InternalEffect.RevocableOutcome<Cell<InnerResultT>> {
            val initialInnerAction: Action<InnerResultT> = sourceActionCell.vertex.getOldValue(
                propagationContext = propagationContext,
            )

            val initialInnerActionExecutionOutcome: Action.Outcome<InnerResultT> = initialInnerAction.executeInternally(
                propagationContext = propagationContext,
                wrapUpContext = wrapUpContext,
            )


            val subjectVertex = ExecutedEveryCellVertex(
                wrapUpContext = wrapUpContext,
                sourceActionCell = sourceActionCell,
                initialInnerActionResult = initialInnerActionExecutionOutcome.result,
            )

            return object : InternalEffect.RevocableOutcome<Cell<InnerResultT>> {
                override val result = Cell.Ordinary(
                    vertex = subjectVertex,
                )

                /**
                 * Cancel the cell actuation effect.
                 */
                override fun cancelInternally(
                    propagationContext: Transactions.PropagationContext,
                    wrapUpContext: Transactions.WrapUpContext,
                ): Revocable = with(subjectVertex) {
                    shutDown()

                    // Revoke the ongoing update (if any)
                    if (ongoingUpdate != null) {
                        exposeUpdateNotifyingListeners(
                            propagationContext = propagationContext,
                            update = null,
                        )
                    }

                    return object : Revocable {
                        /**
                         * Revoke the cancellation of the cell actuation effect.
                         */
                        override fun revoke() {
                            if (internalState == InternalState.Disposed) {
                                return
                            }

                            // Re-initialize the effect
                            val startUpUpdate = startUp(
                                propagationContext = propagationContext,
                            )

                            exposeUpdateNotifyingListeners(
                                propagationContext = propagationContext,
                                update = startUpUpdate,
                            )
                        }
                    }
                }

                override fun revoke() {
                    initialInnerActionExecutionOutcome.revocable.revoke()

                    subjectVertex.dispose()
                }
            }
        }
    }

    private enum class InternalState {
        ShutDown, StartedUp, Disposed,
    }

    private val sourceVertex: CellVertex<Action<InnerResultT>>
        get() = sourceActionCell.vertex

    private var internalState = InternalState.ShutDown

    private var upstreamListenerHandle: Vertex.ListenerHandle? = null

    private var unstableNewInnerActionExecutionOutcome: Action.Outcome<InnerResultT>? = null

    /**
     * Handle the source effect cell update
     */
    override fun handle(
        propagationContext: Transactions.PropagationContext,
    ) {
        val sourceOngoingUpdate: Update<Action<InnerResultT>>? = sourceVertex.ongoingUpdate

        when (sourceOngoingUpdate) {
            null -> { // Source update revocation
                // Revoke the ongoing new inner effect start
                unstableNewInnerActionExecutionOutcome?.revocable?.revoke()
                unstableNewInnerActionExecutionOutcome = null

                exposeUpdateNotifyingListeners(
                    propagationContext = propagationContext,
                    update = null,
                )
            }

            else -> { // Initial source update / source update correction
                // Revoke the ongoing new inner effect start
                unstableNewInnerActionExecutionOutcome?.revocable?.revoke()

                val newInnerAction: Action<InnerResultT> = sourceOngoingUpdate.updatedValue

                val newInnerActionExecutionOutcome: Action.Outcome<InnerResultT> =
                    newInnerAction.executeInternallyWrappedUp(
                        propagationContext = propagationContext,
                    )

                unstableNewInnerActionExecutionOutcome = newInnerActionExecutionOutcome

                exposeUpdateNotifyingListeners(
                    propagationContext = propagationContext,
                    update = Update(
                        updatedValue = newInnerActionExecutionOutcome.result,
                    ),
                )
            }
        }
    }

    override fun transit() {
        if (internalState != InternalState.StartedUp) {
            return
        }

        // FIXME: This line is necessary, but removing it doesn't cause any of the tests to fail
        this.unstableNewInnerActionExecutionOutcome = null
    }

    override fun initialize(
        propagationContext: Transactions.PropagationContext,
    ): Update<InnerResultT>? = startUp(
        propagationContext = propagationContext,
    )

    private fun dispose() {
        if (internalState == InternalState.StartedUp) {
            shutDown()
        }

        internalState = InternalState.Disposed
    }

    private fun startUp(
        propagationContext: Transactions.PropagationContext,
    ): Update<InnerResultT>? {
        if (internalState != InternalState.ShutDown) {
            throw IllegalStateException("Effect is already started up or has been revoked: $internalState")
        }

        if (this@ExecutedEveryCellVertex.upstreamListenerHandle != null || this@ExecutedEveryCellVertex.unstableNewInnerActionExecutionOutcome != null) {
            throw IllegalStateException("Vertex seems to already be started up")
        }

        // Re-register the listener
        this@ExecutedEveryCellVertex.upstreamListenerHandle = sourceVertex.registerBoundListenerOnline(
            propagationContext = propagationContext,
            listener = this@ExecutedEveryCellVertex,
        )

        val sourceOngoingUpdate: Update<Action<InnerResultT>>? = sourceVertex.ongoingUpdate

        val startUpUpdate = when (sourceOngoingUpdate) {
            null -> { // There's no ongoing source effect update
                null
            }

            else -> { // There's an ongoing source effect update
                // Execute the new inner action
                val newInnerAction: Action<InnerResultT> = sourceOngoingUpdate.updatedValue

                val initialNewInnerActionExecutionOutcome = newInnerAction.executeInternallyWrappedUp(
                    propagationContext = propagationContext,
                )

                this@ExecutedEveryCellVertex.unstableNewInnerActionExecutionOutcome =
                    initialNewInnerActionExecutionOutcome

                Update(
                    updatedValue = initialNewInnerActionExecutionOutcome.result,
                )
            }
        }

        ensureEnqueuedForCommitment(
            propagationContext = propagationContext,
        )

        internalState = InternalState.StartedUp

        return startUpUpdate
    }

    private fun shutDown() {
        if (internalState != InternalState.StartedUp) {
            throw IllegalStateException("Action is not started up: $internalState")
        }

        run {
            val upstreamListenerHandle =
                this.upstreamListenerHandle ?: throw IllegalStateException("Action doesn't seem to be started up")

            this.upstreamListenerHandle = null

            // Unregister the listener
            sourceVertex.unregisterListener(
                handle = upstreamListenerHandle,
            )
        }

        unstableNewInnerActionExecutionOutcome?.revocable?.revoke()
        unstableNewInnerActionExecutionOutcome = null

        internalState = InternalState.ShutDown
    }
}
