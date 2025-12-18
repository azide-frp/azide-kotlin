package dev.azide.internal.cell.abstract_vertices

import dev.azide.internal.Transactions
import dev.azide.internal.cell.CellVertex
import dev.azide.internal.cell.CellVertex.ObserverHandle
import dev.azide.internal.cell.FrozenCellVertex

abstract class AbstractFrozenCellVertex<ValueT> : FrozenCellVertex<ValueT> {
    private object NoopObserverHandle : ObserverHandle

    final override fun registerObserver(
        propagationContext: Transactions.PropagationContext,
        observer: CellVertex.Observer<ValueT>,
    ): ObserverHandle = NoopObserverHandle

    final override fun unregisterObserver(
        handle: ObserverHandle,
    ) {
    }

    final override val ongoingUpdate: Nothing?
        get() = null
}
