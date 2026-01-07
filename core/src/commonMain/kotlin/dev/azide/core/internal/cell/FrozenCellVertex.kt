package dev.azide.core.internal.cell

import dev.azide.core.internal.Transactions
import dev.azide.core.internal.Vertex
import dev.azide.core.internal.cell.CellVertex.Observer

interface FrozenCellVertex<out ValueT> : CellVertex<ValueT> {
    data object FrozenObserverHandle : CellVertex.ObserverHandle

    override val ongoingUpdate: Nothing?

    override fun registerObserver(
        propagationContext: Transactions.PropagationContext,
        observer: Observer<ValueT>,
        mode: Vertex.ActivationMode,
    ): FrozenObserverHandle
}
