package dev.azide.core.internal.cell

import dev.azide.core.internal.Transactions
import dev.azide.core.internal.cell.CellVertex.Observer
import dev.azide.core.internal.cell.CellVertex.ObserverHandle

interface WarmCellVertex<out ValueT> : CellVertex<ValueT> {
    override fun registerObserver(
        propagationContext: Transactions.PropagationContext,
        observer: Observer<ValueT>,
    ): ObserverHandle
}
