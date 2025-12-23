package dev.azide.core.internal.cell.operated_vertices

import dev.azide.core.internal.Transactions
import dev.azide.core.internal.cell.CellVertex
import dev.azide.core.internal.cell.WarmCellVertex
import dev.azide.core.internal.cell.abstract_vertices.AbstractCachingCellVertex

class Mapped2WarmCellVertex<ValueT1, ValueT2, TransformedValueT>(
    private val sourceVertex1: CellVertex<ValueT1>,
    private val sourceVertex2: CellVertex<ValueT2>,
    private val transform: (ValueT1, ValueT2) -> TransformedValueT,
) : AbstractCachingCellVertex<TransformedValueT>(
    cacheType = CacheType.Momentary,
), WarmCellVertex.BasicObserver<Any?> {
    private var upstreamObserverHandle1: CellVertex.ObserverHandle? = null
    private var upstreamObserverHandle2: CellVertex.ObserverHandle? = null

    override fun activate(
        propagationContext: Transactions.PropagationContext,
    ): CellVertex.Update<TransformedValueT>? {
        if (upstreamObserverHandle1 != null) {
            throw IllegalStateException("Vertex seems to be already active")
        }

        if (upstreamObserverHandle2 != null) {
            throw IllegalStateException("Vertex seems to be already active")
        }

        this.upstreamObserverHandle1 = sourceVertex1.registerObserver(
            propagationContext = propagationContext,
            observer = this,
        )

        this.upstreamObserverHandle2 = sourceVertex2.registerObserver(
            propagationContext = propagationContext,
            observer = this,
        )

        return buildTransformedUpdate(
            propagationContext = propagationContext,
        )
    }

    override fun deactivate() {
        // Unregister each observer if the respective source vertex is warm and actually gave us a handle

        this.upstreamObserverHandle1?.let { upstreamObserverHandle1 ->
            sourceVertex1.unregisterObserver(
                handle = upstreamObserverHandle1,
            )
        }

        this.upstreamObserverHandle1 = null

        this.upstreamObserverHandle2?.let { upstreamObserverHandle2 ->
            sourceVertex2.unregisterObserver(
                handle = upstreamObserverHandle2,
            )
        }

        this.upstreamObserverHandle2 = null
    }

    /**
     * Handle an update of one of the source vertices.
     */
    override fun handleUpdate(
        propagationContext: Transactions.PropagationContext,
        update: CellVertex.Update<Any?>?,
    ) {
        exposeAndPropagateTransformedUpdate(
            propagationContext = propagationContext,
        )
    }

    private fun exposeAndPropagateTransformedUpdate(
        propagationContext: Transactions.PropagationContext,
    ) {
        val transformedUpdate = buildTransformedUpdate(
            propagationContext = propagationContext,
        )

        exposeAndPropagateUpdate(
            propagationContext = propagationContext,
            update = transformedUpdate,
        )
    }

    private fun buildTransformedUpdate(
        propagationContext: Transactions.PropagationContext,
    ): CellVertex.Update<TransformedValueT>? {
        val sourceOngoingUpdate1 = sourceVertex1.ongoingUpdate
        val sourceOngoingUpdate2 = sourceVertex2.ongoingUpdate

        run {
            if (sourceOngoingUpdate1 != null) return@run
            if (sourceOngoingUpdate2 != null) return@run
            return null
        }

        val newSourceValue1 = when (sourceOngoingUpdate1) {
            null -> sourceVertex1.getOldValue(
                propagationContext = propagationContext,
            )

            else -> sourceOngoingUpdate1.updatedValue
        }

        val newSourceValue2 = when (sourceOngoingUpdate2) {
            null -> sourceVertex2.getOldValue(
                propagationContext = propagationContext,
            )

            else -> sourceOngoingUpdate2.updatedValue
        }

        return CellVertex.Update(
            updatedValue = transform(
                newSourceValue1,
                newSourceValue2,
            ),
        )
    }

    override fun computeOldValue(
        propagationContext: Transactions.PropagationContext,
    ): TransformedValueT = transform(
        sourceVertex1.getOldValue(propagationContext),
        sourceVertex2.getOldValue(propagationContext),
    )
}
