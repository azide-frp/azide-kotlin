package dev.azide.core.impl.cell

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.cell.CellVertex.UpdateNotificationObserver

interface FrozenCellVertex<out ValueT> : CellVertex<ValueT> {
    data object FrozenObserverHandle : CellVertex.ObserverHandle

    override val ongoingUpdate: Nothing?

    override fun registerUpdateNotificationObserver(
        propagationContext: Transactions.PropagationContext,
        observer: UpdateNotificationObserver,
        mode: Vertex.ActivationMode,
    ): FrozenObserverHandle
}
