package dev.azide.core.impl.cell.operated_vertices

import dev.azide.core.Cell
import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.ListenableVertex.ActivationMode
import dev.azide.core.impl.ListenableVertex.BoundListener
import dev.azide.core.impl.ListenableVertex.ListenerHandle
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.cell.abstract_vertices.AbstractSimpleStatelessCellVertex
import dev.azide.core.impl.cell.getNewValue
import dev.azide.core.impl.registerBoundListener

class SwitchedCellVertex<ValueT>(
    private val outerSourceVertex: CellVertex<Cell<ValueT>>,
) : AbstractSimpleStatelessCellVertex<ValueT>(), BoundListener {
    /**
     * The outer vertex listener handle.
     *
     * If the vertex is inactive: `null`
     *
     * If the vertex is active: A handle to the listener registered in [outerSourceVertex].
     */
    private var upstreamOuterListenerHandle: ListenerHandle? = null

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
     * The handle to the new (updated or stable otherwise) inner cell vertex listener.
     *
     * If the vertex is inactive: `null`
     *
     * If the vertex is active:
     * - If [updatedInnerSourceVertex] is non-null: a handle registered in [updatedInnerSourceVertex]
     * - Else: a handle registered in [stableInnerSourceVertex]
     */
    private var upstreamNewInnerListenerHandle: ListenerHandle? = null

    private val innerSourceListener = object : BoundListener {
        /**
         * Handle the update of the inner source cell.
         */
        override fun handle(
            propagationContext: PropagationContext,
        ) {
            val stableInnerSourceVertex = this@SwitchedCellVertex.stableInnerSourceVertex
                ?: throw IllegalStateException("ListenableVertex doesn't seem to be active")

            val updatedInnerSourceVertex = this@SwitchedCellVertex.updatedInnerSourceVertex

            val newInnerSourceVertex = updatedInnerSourceVertex ?: stableInnerSourceVertex

            when (val update = newInnerSourceVertex.ongoingUpdate) {
                null -> { // The inner source cell vertex update is revoked
                    when (updatedInnerSourceVertex) {
                        null -> { // The inner source cell vertex revoking the update is the _stable_ inner cell vertex
                            // Revoke the update
                            exposeUpdateNotifyingListeners(
                                propagationContext = propagationContext,
                                update = null,
                            )
                        }

                        else -> { // The inner source cell vertex revoking the update is the _updated_ inner cell vertex
                            // Correct the update, falling back to the old value of the updated inner cell
                            exposeUpdateNotifyingListeners(
                                propagationContext = propagationContext,
                                update = CellVertex.Update(
                                    updatedValue = updatedInnerSourceVertex.getOldValue(propagationContext),
                                ),
                            )
                        }
                    }
                }

                else -> { // The inner source cell vertex has a proper update (potentially a correction)
                    // Propagate the update (potentially correcting the previous one)
                    exposeUpdateNotifyingListeners(
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
    override fun handle(
        propagationContext: PropagationContext,
    ) {
        when (val update = outerSourceVertex.ongoingUpdate) {
            null -> { // The outer source vertex update is revoked
                // Unregister from the previous updated inner vertex (now revoked)

                val updatedInnerSourceVertex = this.updatedInnerSourceVertex
                    ?: throw IllegalStateException("The outer source vertex doesn't seem to have updated")

                val upstreamNewInnerListenerHandle = this.upstreamNewInnerListenerHandle ?: throw IllegalStateException(
                    "ListenableVertex doesn't seem to be active"
                )

                updatedInnerSourceVertex.unregisterListener(
                    handle = upstreamNewInnerListenerHandle,
                )

                // Forget the previous updated inner vertex

                this.updatedInnerSourceVertex = null

                // Re-subscribe to the stable inner vertex

                val stableInnerSourceVertex =
                    this.stableInnerSourceVertex
                        ?: throw IllegalStateException("ListenableVertex doesn't seem to be active")

                this.upstreamNewInnerListenerHandle = stableInnerSourceVertex.registerBoundListener(
                    propagationContext = propagationContext,
                    listener = innerSourceListener,
                    mode = ActivationMode.Online,
                )

                when (val ongoingStableInnerUpdate = stableInnerSourceVertex.ongoingUpdate) {
                    null -> { // The inner cell doesn't have an ongoing update, so revoke the update of this vertex
                        exposeUpdateNotifyingListeners(
                            propagationContext = propagationContext,
                            update = null,
                        )
                    }

                    else -> { // The stable inner cell has an ongoing update, so let's use it for the correction
                        exposeUpdateNotifyingListeners(
                            propagationContext = propagationContext,
                            update = ongoingStableInnerUpdate,
                        )
                    }
                }
            }

            else -> { // The outer source vertex has a proper update (potentially a correction)
                val stableInnerSourceVertex = this@SwitchedCellVertex.stableInnerSourceVertex
                    ?: throw IllegalStateException("ListenableVertex doesn't seem to be active")

                val previousNewInnerSourceVertex = updatedInnerSourceVertex ?: stableInnerSourceVertex

                val handledUpdatedInnerSourceCell: Cell<ValueT> = update.updatedValue

                val handledUpdatedInnerSourceVertex = handledUpdatedInnerSourceCell.vertex

                if (handledUpdatedInnerSourceVertex == previousNewInnerSourceVertex) {
                    // If the source inner vertex doesn't effectively change, we can just ignore the update.
                    // TODO: Clarify the CellVertex-level contract
                    return
                }

                // Store link to the updated inner source vertex

                this.updatedInnerSourceVertex = handledUpdatedInnerSourceVertex

                // Unsubscribe from the previous updated inner source vertex / stable source vertex

                val previousUpstreamNewInnerListenerHandle = this.upstreamNewInnerListenerHandle
                    ?: throw IllegalStateException("ListenableVertex doesn't seem to be active")

                previousNewInnerSourceVertex.unregisterListener(
                    handle = previousUpstreamNewInnerListenerHandle,
                )

                // Subscribe to the handled updated inner source vertex

                this.upstreamNewInnerListenerHandle = handledUpdatedInnerSourceVertex.registerBoundListener(
                    propagationContext = propagationContext,
                    listener = innerSourceListener,
                    mode = ActivationMode.Online,
                )

                // Propagate the update

                exposeUpdateNotifyingListeners(
                    propagationContext = propagationContext,
                    update = CellVertex.Update(
                        updatedValue = handledUpdatedInnerSourceVertex.getNewValue(
                            propagationContext = propagationContext,
                        ),
                    ),
                )
            }
        }
    }

    override fun activate(
        propagationContext: PropagationContext,
        mode: ActivationMode,
    ): CellVertex.Update<ValueT>? {
        if (upstreamOuterListenerHandle != null || stableInnerSourceVertex != null || updatedInnerSourceVertex != null || upstreamNewInnerListenerHandle != null) {
            throw IllegalStateException("ListenableVertex seems to be already active")
        }

        // Register the outer listener

        this.upstreamOuterListenerHandle = outerSourceVertex.registerBoundListener(
            propagationContext = propagationContext,
            listener = this,
            mode = mode,
        )

        // Resolve the stable / updated inner source cells

        val stableInnerSourceCell: Cell<ValueT> = outerSourceVertex.getOldValue(
            propagationContext = propagationContext,
        )

        val updatedInnerSourceCell: Cell<ValueT>? = outerSourceVertex.ongoingUpdate?.updatedValue

        val newInnerSourceCell: Cell<ValueT> = updatedInnerSourceCell ?: outerSourceVertex.getOldValue(
            propagationContext = propagationContext,
        )

        val stableInnerSourceVertex = stableInnerSourceCell.vertex

        val updatedInnerSourceVertex = updatedInnerSourceCell?.vertex

        val newInnerSourceVertex = newInnerSourceCell.vertex

        // Store the links to the stable / updated source inner vertices

        this.stableInnerSourceVertex = stableInnerSourceVertex
        this.updatedInnerSourceVertex = updatedInnerSourceVertex

        // Register the inner source vertex listener (to the new inner source vertex)

        this.upstreamNewInnerListenerHandle = newInnerSourceVertex.registerBoundListener(
            propagationContext = propagationContext,
            listener = innerSourceListener,
            mode = mode,
        )

        return newInnerSourceVertex.ongoingUpdate
    }

    override fun deactivate() {
        val upstreamOuterListenerHandle = this.upstreamOuterListenerHandle
        val stableInnerSourceVertex = this.stableInnerSourceVertex
        val upstreamNewInnerListenerHandle = this.upstreamNewInnerListenerHandle

        if (upstreamOuterListenerHandle == null || stableInnerSourceVertex == null) {
            throw IllegalStateException("ListenableVertex doesn't seem to be active")
        }

        // Unregister the outer source vertex listener

        outerSourceVertex.unregisterListener(
            handle = upstreamOuterListenerHandle,
        )

        this.upstreamOuterListenerHandle = null

        // Unregister the inner source vertex listener

        val updatedInnerSourceVertex = this.updatedInnerSourceVertex
        val newInnerSourceVertex = updatedInnerSourceVertex ?: stableInnerSourceVertex

        if (upstreamNewInnerListenerHandle != null) {
            newInnerSourceVertex.unregisterListener(
                handle = upstreamNewInnerListenerHandle,
            )
        }

        this.stableInnerSourceVertex = null
        this.updatedInnerSourceVertex = null
        this.upstreamNewInnerListenerHandle = null
    }

    override fun getOldValue(
        propagationContext: PropagationContext,
    ): ValueT {
        val oldInnerSourceVertex = when (val oldInnerSourceVertex = this.stableInnerSourceVertex) {
            null -> {
                // When the vertex is inactive, (potentially) recompute the old cell. This might trigger user-provided
                // transformations.
                val oldCell = outerSourceVertex.getOldValue(propagationContext)

                oldCell.vertex
            }

            // When the vertex is active, use the stored link
            else -> oldInnerSourceVertex
        }

        return oldInnerSourceVertex.getOldValue(
            propagationContext = propagationContext,
        )
    }

    override fun transit() {
        val updatedInnerSourceVertex = this.updatedInnerSourceVertex ?: return

        this.stableInnerSourceVertex = updatedInnerSourceVertex
        this.updatedInnerSourceVertex = null
    }
}
