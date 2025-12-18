package dev.azide.internal.cell.operated_vertices

import dev.azide.Cell
import dev.azide.internal.Transactions
import dev.azide.internal.cell.CellVertex
import dev.azide.internal.cell.CellVertex.Observer
import dev.azide.internal.cell.CellVertex.ObserverHandle
import dev.azide.internal.cell.CellVertex.Update
import dev.azide.internal.cell.WarmCellVertex
import dev.azide.internal.cell.abstract_vertices.AbstractStatelessCellVertex
import dev.azide.internal.cell.getNewValue

class SwitchedCellVertex<ValueT>(
    private val outerSourceVertex: WarmCellVertex<Cell<ValueT>>,
) : AbstractStatelessCellVertex<ValueT>(), Observer<Cell<ValueT>> {
    /**
     * The outer vertex observer handle.
     *
     * If the vertex is inactive: `null`
     *
     * If the vertex is active: A handle to the observer registered in [outerSourceVertex].
     */
    private var upstreamOuterObserverHandle: ObserverHandle? = null

    /**
     * The stable cell vertex.
     *
     * If the vertex is inactive: `null`
     *
     * If the vertex is active: A current stable cell vertex.
     */
    private var stableInnerSourceVertex: CellVertex<ValueT>? = null

    /**
     * The updated inner cell vertex.
     *
     * If the vertex is inactive: `null`
     *
     * If the vertex is active:
     * - If the [outerSourceVertex] has an ongoing update: The updated value of [outerSourceVertex].
     * - Otherwise: `null`
     */
    private var updatedInnerSourceVertex: CellVertex<ValueT>? = null

    /**
     * The handle to the new (updated or stable otherwise) inner cell vertex.
     *
     * If the vertex is inactive: `null`
     *
     * If the vertex is active:
     * - If [updatedInnerSourceVertex] is non-null: a handle registered in [updatedInnerSourceVertex]
     * - Else: a handle registered in [stableInnerSourceVertex]
     */
    private var upstreamNewInnerObserverHandle: ObserverHandle? = null

    private val innerSourceObserver = object : Observer<ValueT> {
        /**
         * Handle the update of the inner source cell.
         */
        override fun handleUpdate(
            propagationContext: Transactions.PropagationContext,
            update: Update<ValueT>?,
        ) {
            when (update) {
                null -> { // The inner source cell vertex update is revoked
                    when (val updatedInnerSourceVertex = this@SwitchedCellVertex.updatedInnerSourceVertex) {
                        null -> { // The inner source cell vertex revoking the update is the _stable_ inner cell vertex
                            // Revoke the update
                            exposeAndPropagateUpdate(
                                propagationContext = propagationContext,
                                update = null,
                            )
                        }

                        else -> { // The inner source cell vertex revoking the update is the _updated_ inner cell vertex
                            // Correct the update, falling back to the old value of the updated inner cell
                            exposeAndPropagateUpdate(
                                propagationContext = propagationContext,
                                update = Update(
                                    updatedValue = updatedInnerSourceVertex.getOldValue(propagationContext),
                                ),
                            )
                        }
                    }
                }

                else -> { // The inner source cell vertex has a proper update (potentially a correction)
                    // Propagate the update (potentially correcting the previous one)
                    exposeAndPropagateUpdate(
                        propagationContext = propagationContext,
                        update = update,
                    )
                }
            }
        }
    }

    /**
     * Handle the update of the outer source vertex.
     */
    override fun handleUpdate(
        propagationContext: Transactions.PropagationContext,
        update: Update<Cell<ValueT>>?,
    ) {
        when (update) {
            null -> { // The outer source vertex update is revoked
                // Unregister from the previous updated inner vertex (now revoked)

                val updatedInnerSourceVertex = this.updatedInnerSourceVertex
                    ?: throw IllegalStateException("The outer source vertex doesn't seem to have updated")

                val upstreamNewInnerObserverHandle = this.upstreamNewInnerObserverHandle ?: throw IllegalStateException(
                    "Vertex doesn't seem to be active"
                )

                updatedInnerSourceVertex.unregisterObserver(
                    handle = upstreamNewInnerObserverHandle,
                )

                // Forget the previous updated inner vertex

                this.updatedInnerSourceVertex = null

                // Re-subscribe to the stable inner vertex

                val stableInnerSourceVertex =
                    this.stableInnerSourceVertex ?: throw IllegalStateException("Vertex doesn't seem to be active")

                this.upstreamNewInnerObserverHandle = stableInnerSourceVertex.registerObserver(
                    propagationContext = propagationContext,
                    observer = innerSourceObserver,
                )

                when (val ongoingStableInnerUpdate = stableInnerSourceVertex.ongoingUpdate) {
                    null -> { // The inner cell doesn't have an ongoing update, so revoke the update of this vertex
                        exposeAndPropagateUpdate(
                            propagationContext = propagationContext,
                            update = null,
                        )
                    }

                    else -> { // The stable inner cell has an ongoing update, so let's use it for the correction
                        exposeAndPropagateUpdate(
                            propagationContext = propagationContext,
                            update = ongoingStableInnerUpdate,
                        )
                    }
                }
            }

            else -> { // The inner source vertex has a proper update (potentially a correction)
                val stableInnerSourceVertex = this@SwitchedCellVertex.stableInnerSourceVertex
                    ?: throw IllegalStateException("Vertex doesn't seem to be active")

                val previousNewInnerSourceVertex = updatedInnerSourceVertex ?: stableInnerSourceVertex

                val handledUpdatedInnerSourceCell: Cell<ValueT> = update.updatedValue

                val handledUpdatedInnerSourceVertex = handledUpdatedInnerSourceCell.getVertex(
                    propagationContext = propagationContext,
                )

                if (handledUpdatedInnerSourceVertex == previousNewInnerSourceVertex) {
                    // If the source inner vertex doesn't effectively change, we can just ignore the update.
                    // TODO: Clarify the CellVertex-level contract
                    return
                }

                // Store link to the updated inner source vertex

                this.updatedInnerSourceVertex = handledUpdatedInnerSourceVertex

                // Unsubscribe from the previous updated inner source vertex / stable source verte

                val previousUpstreamNewInnerObserverHandle = this.upstreamNewInnerObserverHandle
                    ?: throw IllegalStateException("Vertex doesn't seem to be active")

                previousNewInnerSourceVertex.unregisterObserver(
                    handle = previousUpstreamNewInnerObserverHandle,
                )

                // Subscribe to the handled updated inner source vertex

                this.upstreamNewInnerObserverHandle = handledUpdatedInnerSourceVertex.registerObserver(
                    propagationContext = propagationContext,
                    observer = innerSourceObserver,
                )

                // Propagate the update

                exposeAndPropagateUpdate(
                    propagationContext = propagationContext,
                    update = Update(
                        updatedValue = handledUpdatedInnerSourceVertex.getNewValue(
                            propagationContext = propagationContext,
                        ),
                    ),
                )
            }
        }
    }

    override fun activate(
        propagationContext: Transactions.PropagationContext,
    ): Update<ValueT>? {
        if (upstreamOuterObserverHandle != null || stableInnerSourceVertex != null || upstreamNewInnerObserverHandle != null || upstreamNewInnerObserverHandle != null) {
            throw IllegalStateException("Vertex seems to be already active")
        }

        // Register the outer observer

        this.upstreamOuterObserverHandle = outerSourceVertex.registerObserver(
            propagationContext = propagationContext,
            observer = this,
        )

        // Resolve the stable / updated inner source cells

        val stableInnerSourceCell: Cell<ValueT> = outerSourceVertex.getOldValue(
            propagationContext = propagationContext,
        )

        val updatedInnerSourceCell: Cell<ValueT>? = outerSourceVertex.ongoingUpdate?.updatedValue

        val newInnerSourceCell: Cell<ValueT> = updatedInnerSourceCell ?: outerSourceVertex.getOldValue(
            propagationContext = propagationContext,
        )

        val stableInnerSourceVertex = stableInnerSourceCell.getVertex(
            propagationContext = propagationContext,
        )

        val updatedInnerSourceVertex = updatedInnerSourceCell?.getVertex(
            propagationContext = propagationContext,
        )

        val newInnerSourceVertex = newInnerSourceCell.getVertex(
            propagationContext = propagationContext,
        )

        // Store the links to the stable / updated source inner vertices

        this.stableInnerSourceVertex = stableInnerSourceVertex
        this.updatedInnerSourceVertex = updatedInnerSourceVertex

        // Register the inner source vertex observer (to the new inner source vertex)

        this.upstreamNewInnerObserverHandle = newInnerSourceVertex.registerObserver(
            propagationContext = propagationContext,
            observer = innerSourceObserver,
        )

        return newInnerSourceVertex.ongoingUpdate
    }

    override fun deactivate() {
        val upstreamOuterObserverHandle = this.upstreamOuterObserverHandle
        val stableInnerSourceVertex = this.stableInnerSourceVertex
        val upstreamNewInnerObserverHandle = this.upstreamNewInnerObserverHandle

        if (upstreamOuterObserverHandle == null || stableInnerSourceVertex == null || upstreamNewInnerObserverHandle == null) {
            throw IllegalStateException("Vertex doesn't seem to be active")
        }

        // Unregister the outer source vertex observer

        outerSourceVertex.unregisterObserver(
            handle = upstreamOuterObserverHandle,
        )

        this.upstreamOuterObserverHandle = null

        // Unregister the inner source vertex observer

        val updatedInnerSourceVertex = this.updatedInnerSourceVertex
        val newInnerSourceVertex = updatedInnerSourceVertex ?: stableInnerSourceVertex

        newInnerSourceVertex.unregisterObserver(
            handle = upstreamNewInnerObserverHandle,
        )

        this.stableInnerSourceVertex = null
        this.updatedInnerSourceVertex = null
        this.upstreamNewInnerObserverHandle = null
    }

    override fun getOldValue(
        propagationContext: Transactions.PropagationContext,
    ): ValueT {
        val oldInnerSourceVertex = when (val oldInnerSourceVertex = this.stableInnerSourceVertex) {
            null -> {
                // When the vertex is inactive, (potentially) recompute the old cell. This might trigger user-provided
                // transformations.
                val oldCell = outerSourceVertex.getOldValue(propagationContext)

                oldCell.getVertex(
                    propagationContext = propagationContext,
                )
            }

            // When the vertex is active, use the stored link
            else -> oldInnerSourceVertex
        }

        return oldInnerSourceVertex.getOldValue(
            propagationContext = propagationContext,
        )
    }

    override fun commit(
        ongoingUpdate: Update<ValueT>?,
    ) {
        val updatedInnerSourceVertex = this.updatedInnerSourceVertex ?: return

        this.stableInnerSourceVertex = updatedInnerSourceVertex
        this.updatedInnerSourceVertex = null
    }
}
