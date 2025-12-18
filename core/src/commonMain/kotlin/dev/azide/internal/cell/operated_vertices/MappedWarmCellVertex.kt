package dev.azide.internal.cell.operated_vertices

import dev.azide.internal.Transactions
import dev.azide.internal.cell.CellVertex.Update
import dev.azide.internal.cell.WarmCellVertex
import dev.azide.internal.cell.CellVertex.Observer
import dev.azide.internal.cell.CellVertex.ObserverHandle
import dev.azide.internal.cell.abstract_vertices.AbstractCachingCellVertex

class MappedWarmCellVertex<ValueT, TransformedValueT>(
    private val sourceVertex: WarmCellVertex<ValueT>,
    private val transform: (ValueT) -> TransformedValueT,
) : AbstractCachingCellVertex<TransformedValueT>(
    cacheType = CacheType.Momentary,
), Observer<ValueT> {
    private var upstreamObserverHandle: ObserverHandle? = null

    /**
     * Handle the update of the source cell.
     */
    override fun handleUpdate(
        propagationContext: Transactions.PropagationContext,
        update: Update<ValueT>?,
    ) {
        when (update) {
            null -> {
                exposeAndPropagateUpdate(
                    propagationContext = propagationContext,
                    update = null,
                )
            }

            else -> {
                exposeAndPropagateUpdate(
                    propagationContext = propagationContext,
                    update = update.map(transform),
                )
            }
        }
    }

    override fun activate(
        propagationContext: Transactions.PropagationContext,
    ): Update<TransformedValueT>? {
        if (upstreamObserverHandle != null) {
            throw IllegalStateException("Vertex seems to be already active")
        }

        upstreamObserverHandle = sourceVertex.registerObserver(
            propagationContext = propagationContext,
            observer = this,
        )

        return sourceVertex.ongoingUpdate?.map(transform)
    }

    override fun deactivate() {
        val subscriptionHandle =
            this.upstreamObserverHandle ?: throw IllegalStateException("Vertex doesn't seem to be active")

        sourceVertex.unregisterObserver(
            handle = subscriptionHandle,
        )

        this.upstreamObserverHandle = null
    }

    override fun computeOldValue(
        propagationContext: Transactions.PropagationContext,
    ): TransformedValueT = transform(
        sourceVertex.getOldValue(
            propagationContext = propagationContext,
        ),
    )
}
