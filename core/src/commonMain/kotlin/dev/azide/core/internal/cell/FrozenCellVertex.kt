package dev.azide.core.internal.cell

import dev.azide.core.internal.Transactions
import dev.azide.core.internal.cell.CellVertex.Observer
import dev.azide.core.internal.cell.CellVertex.ObserverHandle

interface FrozenCellVertex<out ValueT> : CellVertex<ValueT> {
    override val ongoingUpdate: Nothing?

    override fun registerObserver(
        propagationContext: Transactions.PropagationContext,
        observer: Observer<ValueT>,
    ): Nothing?

    override fun unregisterObserver(
        handle: ObserverHandle,
    ): Nothing
}
