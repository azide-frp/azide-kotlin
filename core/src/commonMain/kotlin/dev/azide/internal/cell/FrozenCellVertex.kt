package dev.azide.internal.cell

interface FrozenCellVertex<out ValueT> : CellVertex<ValueT> {
    override val ongoingUpdate: Nothing?
}
