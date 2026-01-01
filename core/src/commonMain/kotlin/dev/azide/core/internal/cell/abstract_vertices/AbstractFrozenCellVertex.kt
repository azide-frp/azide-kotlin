package dev.azide.core.internal.cell.abstract_vertices

import dev.azide.core.internal.Transactions
import dev.azide.core.internal.cell.CellVertex
import dev.azide.core.internal.cell.FrozenCellVertex
import kotlin.jvm.JvmInline

abstract class AbstractFrozenCellVertex<ValueT> : FrozenCellVertex<ValueT> {
    final override fun registerObserver(
        propagationContext: Transactions.PropagationContext,
        observer: CellVertex.Observer<ValueT>,
    ): FrozenCellVertex.FrozenObserverHandle = FrozenCellVertex.FrozenObserverHandle

    final override fun unregisterObserver(
        handle: CellVertex.ObserverHandle,
    ) {
        if (handle != FrozenCellVertex.FrozenObserverHandle) {
            throw IllegalArgumentException("Invalid handle")
        }
    }

    final override val ongoingUpdate: Nothing?
        get() = null
}
