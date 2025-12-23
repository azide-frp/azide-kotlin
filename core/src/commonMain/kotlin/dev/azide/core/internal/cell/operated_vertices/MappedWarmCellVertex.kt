package dev.azide.core.internal.cell.operated_vertices

import dev.azide.core.internal.Transactions
import dev.azide.core.internal.cell.CellVertex
import dev.azide.core.internal.cell.WarmCellVertex
import dev.azide.core.internal.cell.abstract_vertices.AbstractCachingCellVertex

class MappedWarmCellVertex<ValueT, TransformedValueT>(
    private val sourceVertex: WarmCellVertex<ValueT>,
    private val transform: (ValueT) -> TransformedValueT,
) : AbstractCachingCellVertex<TransformedValueT>(
    cacheType = CacheType.Momentary,
), WarmCellVertex.BasicObserver<ValueT> {
    private var upstreamObserverHandle: CellVertex.ObserverHandle? = null

    /**
     * Handle the update of the source cell.
     */
    override fun handleUpdate(
        propagationContext: Transactions.PropagationContext,
        update: CellVertex.Update<ValueT>?,
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
    ): CellVertex.Update<TransformedValueT>? {
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
