package dev.azide.core.impl.cell.operated_vertices

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex.ActivationMode
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.cell.CellVertex.UpdateObserver
import dev.azide.core.impl.cell.abstract_vertices.AbstractCachingCellVertex
import dev.azide.core.impl.cell.registerUpdateObserver

class MappedWarmCellVertex<ValueT, TransformedValueT>(
    private val sourceVertex: CellVertex<ValueT>,
    private val transform: (ValueT) -> TransformedValueT,
) : AbstractCachingCellVertex<TransformedValueT>(
    cacheType = CacheType.Momentary,
), UpdateObserver {
    private var upstreamObserverHandle: CellVertex.ObserverHandle? = null

    /**
     * Handle the update of the source cell.
     */
    override fun handleUpdate(
        propagationContext: Transactions.PropagationContext,
    ) {
        when (val update = sourceVertex.ongoingUpdate) {
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
        mode: ActivationMode,
    ): CellVertex.Update<TransformedValueT>? {
        if (upstreamObserverHandle != null) {
            throw IllegalStateException("Vertex seems to be already active")
        }

        upstreamObserverHandle = sourceVertex.registerUpdateObserver(
            propagationContext = propagationContext,
            observer = this,
            mode = mode,
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
