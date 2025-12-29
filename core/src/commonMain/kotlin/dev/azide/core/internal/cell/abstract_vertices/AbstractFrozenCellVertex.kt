package dev.azide.core.internal.cell.abstract_vertices

import dev.azide.core.internal.Transactions
import dev.azide.core.internal.cell.CellVertex
import dev.azide.core.internal.cell.FrozenCellVertex

abstract class AbstractFrozenCellVertex<ValueT> : FrozenCellVertex<ValueT> {
    final override fun registerObserver(
        propagationContext: Transactions.PropagationContext,
        observer: CellVertex.Observer<ValueT>,
    ): Nothing? = null

    final override fun unregisterObserver(
        handle: CellVertex.ObserverHandle,
    ): Nothing {
        throw UnsupportedOperationException("Frozen cell vertices do not support unregistering observers")
    }

    final override val ongoingUpdate: Nothing?
        get() = null
}
