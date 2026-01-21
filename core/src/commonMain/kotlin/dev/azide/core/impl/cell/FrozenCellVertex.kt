package dev.azide.core.impl.cell

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.cell.CellVertex.Listener

interface FrozenCellVertex<out ValueT> : CellVertex<ValueT> {
    data object FrozenListenerHandle : CellVertex.ListenerHandle

    override val ongoingUpdate: Nothing?

    override fun registerListener(
        propagationContext: Transactions.PropagationContext,
        listener: Listener,
        mode: Vertex.ActivationMode,
    ): FrozenListenerHandle
}
