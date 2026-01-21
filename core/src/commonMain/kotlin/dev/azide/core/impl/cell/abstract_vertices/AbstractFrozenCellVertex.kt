package dev.azide.core.impl.cell.abstract_vertices

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.cell.FrozenCellVertex

abstract class AbstractFrozenCellVertex<ValueT> : FrozenCellVertex<ValueT> {
    override fun registerListener(
        propagationContext: Transactions.PropagationContext,
        listener: CellVertex.Listener,
        mode: Vertex.ActivationMode,
    ): FrozenCellVertex.FrozenListenerHandle = FrozenCellVertex.FrozenListenerHandle

    final override fun unregisterListener(
        handle: CellVertex.ListenerHandle,
    ) {
        if (handle != FrozenCellVertex.FrozenListenerHandle) {
            throw IllegalArgumentException("Invalid handle")
        }
    }

    final override val ongoingUpdate: Nothing?
        get() = null
}
