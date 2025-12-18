package dev.azide.core.internal.cell

interface FrozenCellVertex<out ValueT> : CellVertex<ValueT> {
    override val ongoingUpdate: Nothing?
}
