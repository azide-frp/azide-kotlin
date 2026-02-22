package dev.azide.core.impl.cell.abstract_vertices

import dev.azide.core.impl.ListenableVertex.Listener
import dev.azide.core.impl.ListenableVertex.ListenerHandle
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.cell.FrozenCellVertex

abstract class AbstractFrozenCellVertex<ValueT> : FrozenCellVertex<ValueT> {
    final override val listenerCount: Int
        get() = 0

    final override fun registerListener(
        processingContext: Transactions.ProcessingContext,
        listener: Listener,
    ): FrozenCellVertex.FrozenListenerHandle = FrozenCellVertex.FrozenListenerHandle

    final override fun unregisterListener(
        handle: ListenerHandle,
    ) {
        if (handle != FrozenCellVertex.FrozenListenerHandle) {
            throw IllegalArgumentException("Invalid handle")
        }
    }

    final override val ongoingUpdate: Nothing?
        get() = null
}
