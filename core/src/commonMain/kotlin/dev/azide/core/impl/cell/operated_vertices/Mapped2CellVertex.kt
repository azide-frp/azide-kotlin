package dev.azide.core.impl.cell.operated_vertices

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.ListenableVertex
import dev.azide.core.impl.ListenableVertex.BoundListener
import dev.azide.core.impl.ListenableVertex.ListenerHandle
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.cell.abstract_vertices.AbstractCachingCellVertex
import dev.azide.core.impl.registerBoundListener

class Mapped2CellVertex<ValueT1, ValueT2, TransformedValueT>(
    private val sourceVertex1: CellVertex<ValueT1>,
    private val sourceVertex2: CellVertex<ValueT2>,
    private val transform: (ValueT1, ValueT2) -> TransformedValueT,
) : AbstractCachingCellVertex<TransformedValueT>(
    cacheType = CacheType.Momentary,
), BoundListener {
    private var upstreamListenerHandle1: ListenerHandle? = null
    private var upstreamListenerHandle2: ListenerHandle? = null

    override fun activate(
        propagationContext: Transactions.PropagationContext,
        mode: ListenableVertex.ActivationMode,
    ): CellVertex.Update<TransformedValueT>? {
        if (upstreamListenerHandle1 != null) {
            throw IllegalStateException("ListenableVertex seems to be already active")
        }

        if (upstreamListenerHandle2 != null) {
            throw IllegalStateException("ListenableVertex seems to be already active")
        }

        this.upstreamListenerHandle1 = sourceVertex1.registerBoundListener(
            propagationContext = propagationContext,
            listener = this,
            mode = mode,
        )

        this.upstreamListenerHandle2 = sourceVertex2.registerBoundListener(
            propagationContext = propagationContext,
            listener = this,
            mode = mode,
        )

        return buildTransformedUpdate(
            propagationContext = propagationContext,
        )
    }

    override fun deactivate() {
        // Unregister each listener if the respective source vertex is warm and actually gave us a handle

        this.upstreamListenerHandle1?.let { upstreamListenerHandle1 ->
            sourceVertex1.unregisterListener(
                handle = upstreamListenerHandle1,
            )
        }

        this.upstreamListenerHandle1 = null

        this.upstreamListenerHandle2?.let { upstreamListenerHandle2 ->
            sourceVertex2.unregisterListener(
                handle = upstreamListenerHandle2,
            )
        }

        this.upstreamListenerHandle2 = null
    }

    /**
     * Handle an update of one of the source vertices.
     */
    override fun handle(
        propagationContext: Transactions.PropagationContext,
    ) {
        exposeTransformedUpdateNotifyingListeners(
            propagationContext = propagationContext,
        )
    }

    private fun exposeTransformedUpdateNotifyingListeners(
        propagationContext: Transactions.PropagationContext,
    ) {
        val transformedUpdate = buildTransformedUpdate(
            propagationContext = propagationContext,
        )

        exposeUpdateNotifyingListeners(
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
