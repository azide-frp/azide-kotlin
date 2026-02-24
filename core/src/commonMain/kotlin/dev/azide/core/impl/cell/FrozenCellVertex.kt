package dev.azide.core.impl.cell

import dev.azide.core.impl.ListenableVertex.ListenerHandle

interface FrozenCellVertex<out ValueT> : CellVertex<ValueT> {
    data object FrozenListenerHandle : ListenerHandle

    override val ongoingUpdate: Nothing?
}
