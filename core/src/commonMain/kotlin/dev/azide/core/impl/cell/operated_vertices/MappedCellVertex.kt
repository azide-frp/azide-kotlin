package dev.azide.core.impl.cell.operated_vertices

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.ListenableVertex.ActivationMode
import dev.azide.core.impl.ListenableVertex.BoundListener
import dev.azide.core.impl.ListenableVertex.ListenerHandle
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.cell.abstract_vertices.AbstractCachingCellVertex
import dev.azide.core.impl.registerBoundListener

class MappedCellVertex<ValueT, TransformedValueT>(
    private val sourceVertex: CellVertex<ValueT>,
    private val transform: (ValueT) -> TransformedValueT,
) : AbstractCachingCellVertex<TransformedValueT>(
), BoundListener {
    private var upstreamListenerHandle: ListenerHandle? = null

    /**
     * Handle the update of the source cell.
     */
    override fun handle(
        propagationContext: Transactions.PropagationContext,
    ) {
        when (val update = sourceVertex.ongoingUpdate) {
            null -> {
                exposeUpdateNotifyingListeners(
                    propagationContext = propagationContext,
                    update = null,
                )
            }

            else -> {
                exposeUpdateNotifyingListeners(
                    propagationContext = propagationContext,
                    update = update.map(transform),
                )
            }
        }
    }

    override fun activate(
        propagationContext: Transactions.PropagationContext,
        mode: ActivationMode,
    ): CellVertex.Update<TransformedValueT>? {
        if (upstreamListenerHandle != null) {
            throw IllegalStateException("ListenableVertex seems to be already active")
        }

        upstreamListenerHandle = sourceVertex.registerBoundListener(
            propagationContext = propagationContext,
            listener = this,
            mode = mode,
        )

        return sourceVertex.ongoingUpdate?.map(transform)
    }

    override fun deactivate() {
        val subscriptionHandle =
            this.upstreamListenerHandle ?: throw IllegalStateException("ListenableVertex doesn't seem to be active")

        sourceVertex.unregisterListener(
            handle = subscriptionHandle,
        )

        this.upstreamListenerHandle = null
    }

    override fun computeOldValue(
        propagationContext: Transactions.PropagationContext,
    ): TransformedValueT = transform(
        sourceVertex.getOldValue(
            propagationContext = propagationContext,
        ),
    )
}
