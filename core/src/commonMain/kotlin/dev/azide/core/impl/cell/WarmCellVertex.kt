package dev.azide.core.impl.cell

import dev.azide.core.impl.cell.CellVertex.ListenerHandle
import dev.azide.core.impl.cell.CellVertex.Listener
import dev.azide.core.impl.utils.weak_bag.MutableBag
import kotlin.jvm.JvmInline

interface WarmCellVertex<out ValueT> : CellVertex<ValueT> {
    @JvmInline
    value class WarmListenerHandle(
        internal val internalHandle: MutableBag.Handle<Listener>,
    ) : ListenerHandle
}
