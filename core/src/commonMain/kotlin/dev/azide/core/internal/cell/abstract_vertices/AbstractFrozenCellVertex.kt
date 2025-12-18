package dev.azide.core.internal.cell.abstract_vertices

import dev.azide.core.internal.Transactions
import dev.azide.core.internal.cell.CellVertex
import dev.azide.core.internal.cell.FrozenCellVertex

abstract class AbstractFrozenCellVertex<ValueT> : FrozenCellVertex<ValueT> {
    private object NoopObserverHandle : CellVertex.ObserverHandle

    final override fun registerObserver(
        propagationContext: Transactions.PropagationContext,
        observer: CellVertex.Observer<ValueT>,
    ): CellVertex.ObserverHandle = NoopObserverHandle

    final override fun unregisterObserver(
        handle: CellVertex.ObserverHandle,
    ) {
    }

    final override val ongoingUpdate: Nothing?
        get() = null
}
