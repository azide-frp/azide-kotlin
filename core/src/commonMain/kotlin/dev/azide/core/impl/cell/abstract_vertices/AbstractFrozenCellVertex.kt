package dev.azide.core.impl.cell.abstract_vertices

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.cell.FrozenCellVertex

abstract class AbstractFrozenCellVertex<ValueT> : FrozenCellVertex<ValueT> {
    override fun registerUpdateNotificationObserver(
        propagationContext: Transactions.PropagationContext,
        observer: CellVertex.UpdateNotificationObserver<ValueT>,
        mode: Vertex.ActivationMode,
    ): FrozenCellVertex.FrozenObserverHandle = FrozenCellVertex.FrozenObserverHandle

    final override fun unregisterObserver(
        handle: CellVertex.ObserverHandle,
    ) {
        if (handle != FrozenCellVertex.FrozenObserverHandle) {
            throw IllegalArgumentException("Invalid handle")
        }
    }

    final override val ongoingUpdate: Nothing?
        get() = null
}
