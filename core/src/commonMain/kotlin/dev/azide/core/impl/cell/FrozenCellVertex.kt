package dev.azide.core.impl.cell

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.cell.CellVertex.UpdateObserver

interface FrozenCellVertex<out ValueT> : CellVertex<ValueT> {
    data object FrozenObserverHandle : CellVertex.ObserverHandle

    override val ongoingUpdate: Nothing?

    override fun registerUpdateObserver(
        propagationContext: Transactions.PropagationContext,
        observer: UpdateObserver,
        mode: Vertex.ActivationMode,
    ): FrozenObserverHandle
}
