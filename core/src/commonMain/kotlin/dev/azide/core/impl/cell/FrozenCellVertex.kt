package dev.azide.core.impl.cell

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.cell.CellVertex.Observer

interface FrozenCellVertex<out ValueT> : CellVertex<ValueT> {
    data object FrozenObserverHandle : CellVertex.ObserverHandle

    override val ongoingUpdate: Nothing?

    override fun registerObserver(
        propagationContext: Transactions.PropagationContext,
        observer: Observer<ValueT>,
        mode: Vertex.ActivationMode,
    ): FrozenObserverHandle
}
