package dev.azide.core.impl.cell

import dev.azide.core.impl.cell.CellVertex.ObserverHandle
import dev.azide.core.impl.cell.CellVertex.UpdateObserver
import dev.azide.core.impl.utils.weak_bag.MutableBag
import kotlin.jvm.JvmInline

interface WarmCellVertex<out ValueT> : CellVertex<ValueT> {
    @JvmInline
    value class WarmObserverHandle(
        internal val internalHandle: MutableBag.Handle<UpdateObserver>,
    ) : ObserverHandle
}
