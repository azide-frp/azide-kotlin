package dev.azide.core.impl.cell.operated_vertices

import dev.azide.core.Action
import dev.azide.core.Cell
import dev.azide.core.Effect
import dev.azide.core.executeInternallyWrappedUp
import dev.azide.core.impl.CommittableVertex
import dev.azide.core.impl.Revocable
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.ListenableVertex
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.cell.CellVertex.Update
import dev.azide.core.impl.cell.abstract_vertices.AbstractStatefulCellVertex
import dev.azide.core.impl.effects.InternalEffect
import dev.azide.core.impl.registerBoundListenerOnline

class ActuatedCellVertex<InnerResultT> private constructor(
    wrapUpContext: Transactions.WrapUpContext,
    private val sourceEffectCell: Cell<Effect<InnerResultT>>,
    initialInnerEffectOutcome: Effect.Outcome<InnerResultT>,
) : AbstractStatefulCellVertex<InnerResultT>(
    wrapUpContext = wrapUpContext, initialValue = initialInnerEffectOutcome.result
), ListenableVertex.BoundListener, CommittableVertex {
    class ActuationEffect<InnerResultT>(
        private val sourceEffectCell: Cell<Effect<InnerResultT>>,
    ) : InternalEffect<Cell<InnerResultT>> {
        override fun startInternally(
            propagationContext: Transactions.PropagationContext,
            wrapUpContext: Transactions.WrapUpContext,
        ): InternalEffect.RevocableOutcome<Cell<InnerResultT>> {
            val initialInnerEffect: Effect<InnerResultT> = sourceEffectCell.vertex.getOldValue(
                propagationContext = propagationContext,
            )

            val initialInnerEffectStartOutcome: Action.Outcome<Effect.Outcome<InnerResultT>> =
                initialInnerEffect.start.executeInternally(
                    propagationContext = propagationContext,
                    wrapUpContext = wrapUpContext,
                )


            val initialInnerEffectOutcome: Effect.Outcome<InnerResultT> = initialInnerEffectStartOutcome.result

            val subjectVertex = ActuatedCellVertex(
                wrapUpContext = wrapUpContext,
                sourceEffectCell = sourceEffectCell,
                initialInnerEffectOutcome = initialInnerEffectOutcome,
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

                    // Cancel the stable inner effect
                    val stableInnerEffectCancellationRevocable = stableInnerEffectHandle.cancel.executeInternally(
                        propagationContext = propagationContext,
                        wrapUpContext = wrapUpContext,
                    ).revocable

                    return object : Revocable {
                        /**
                         * Revoke the cancellation of the cell actuation effect.
                         */
                        override fun revoke() {
                            if (internalState == InternalState.Disposed) {
                                return
                            }

                            // Revoke the cancellation of the stable inner effect
                            stableInnerEffectCancellationRevocable.revoke()

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
                    initialInnerEffectStartOutcome.revocable.revoke()

                    subjectVertex.dispose()
                }
            }
        }
    }

    private enum class InternalState {
        ShutDown, StartedUp, Disposed,
    }

    private val sourceVertex: CellVertex<Effect<InnerResultT>>
        get() = sourceEffectCell.vertex

    private var stableInnerEffectHandle: Effect.Handle = initialInnerEffectOutcome.handle

    private var internalState = InternalState.ShutDown

    private var upstreamListenerHandle: ListenableVertex.ListenerHandle? = null

    private var unstableInnerEffectCancellationRevocable: Revocable? = null

    private var unstableNewInnerEffectStartOutcome: Action.Outcome<Effect.Outcome<InnerResultT>>? = null

    /**
     * Handle the source effect cell update
     */
    override fun handle(
        propagationContext: Transactions.PropagationContext,
    ) {
        val sourceOngoingUpdate: Update<Effect<InnerResultT>>? = sourceVertex.ongoingUpdate

        when (sourceOngoingUpdate) {
            null -> { // Source update revocation
                // Revoke the ongoing inner effect cancellation
                unstableInnerEffectCancellationRevocable?.revoke()
                unstableInnerEffectCancellationRevocable = null

                // Revoke the ongoing new inner effect start
                unstableNewInnerEffectStartOutcome?.revocable?.revoke()
                unstableNewInnerEffectStartOutcome = null

                exposeUpdateNotifyingListeners(
                    propagationContext = propagationContext,
                    update = null,
                )
            }

            else -> { // Initial source update / source update correction
                if (unstableInnerEffectCancellationRevocable == null) {
                    unstableInnerEffectCancellationRevocable =
                        stableInnerEffectHandle.cancel.executeInternallyWrappedUp(
                            propagationContext = propagationContext,
                        ).revocable
                }

                // Revoke the ongoing new inner effect start
                unstableNewInnerEffectStartOutcome?.revocable?.revoke()

                val newInnerEffect: Effect<InnerResultT> = sourceOngoingUpdate.updatedValue

                val newInnerEffectStartOutcome: Action.Outcome<Effect.Outcome<InnerResultT>> =
                    newInnerEffect.start.executeInternallyWrappedUp(
                        propagationContext = propagationContext,
                    )

                unstableNewInnerEffectStartOutcome = newInnerEffectStartOutcome

                exposeUpdateNotifyingListeners(
                    propagationContext = propagationContext,
                    update = Update(
                        updatedValue = newInnerEffectStartOutcome.result.result,
                    ),
                )
            }
        }
    }

    override fun transit() {
        if (internalState != InternalState.StartedUp) {
            return
        }

        this.unstableInnerEffectCancellationRevocable = null

        this.unstableNewInnerEffectStartOutcome?.let { unstableNewInnerEffectStartOutcome ->
            stableInnerEffectHandle = unstableNewInnerEffectStartOutcome.result.handle

            // FIXME: This line is necessary, but removing it doesn't cause any of the tests to fail
            this.unstableNewInnerEffectStartOutcome = null
        }
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

        if (this@ActuatedCellVertex.upstreamListenerHandle != null || this@ActuatedCellVertex.unstableInnerEffectCancellationRevocable != null || this@ActuatedCellVertex.unstableNewInnerEffectStartOutcome != null) {
            throw IllegalStateException("ListenableVertex seems to already be started up")
        }

        // Re-register the listener
        this@ActuatedCellVertex.upstreamListenerHandle = sourceVertex.registerBoundListenerOnline(
            propagationContext = propagationContext,
            listener = this@ActuatedCellVertex,
        )

        val sourceOngoingUpdate: Update<Effect<InnerResultT>>? = sourceVertex.ongoingUpdate

        val startUpUpdate = when (sourceOngoingUpdate) {
            null -> { // There's no ongoing source effect update
                null
            }

            else -> { // There's an ongoing source effect update
                // Cancel the stable effect
                val initialInnerEffectCancellationRevocable = stableInnerEffectHandle.cancel.executeInternallyWrappedUp(
                    propagationContext = propagationContext,
                ).revocable

                // Start the new inner effect
                val newInnerEffect: Effect<InnerResultT> = sourceOngoingUpdate.updatedValue

                val initialNewInnerEffectStartOutcome = newInnerEffect.start.executeInternallyWrappedUp(
                    propagationContext = propagationContext,
                )

                this@ActuatedCellVertex.unstableInnerEffectCancellationRevocable =
                    initialInnerEffectCancellationRevocable

                this@ActuatedCellVertex.unstableNewInnerEffectStartOutcome = initialNewInnerEffectStartOutcome

                Update(
                    updatedValue = initialNewInnerEffectStartOutcome.result.result,
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
            throw IllegalStateException("Effect is not started up: $internalState")
        }

        run {
            val upstreamListenerHandle =
                this.upstreamListenerHandle ?: throw IllegalStateException("Effect doesn't seem to be started up")

            this.upstreamListenerHandle = null

            // Unregister the listener
            sourceVertex.unregisterListener(
                handle = upstreamListenerHandle,
            )
        }

        unstableInnerEffectCancellationRevocable?.revoke()
        unstableInnerEffectCancellationRevocable = null

        unstableNewInnerEffectStartOutcome?.revocable?.revoke()
        unstableNewInnerEffectStartOutcome = null

        internalState = InternalState.ShutDown
    }
}
