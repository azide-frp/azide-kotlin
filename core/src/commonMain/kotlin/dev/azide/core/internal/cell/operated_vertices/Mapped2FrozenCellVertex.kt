package dev.azide.core.internal.cell.operated_vertices

import dev.azide.core.internal.Transactions
import dev.azide.core.internal.cell.FrozenCellVertex
import dev.azide.core.internal.cell.abstract_vertices.AbstractDerivedFrozenCellVertex

class Mapped2FrozenCellVertex<ValueT1, ValueT2, TransformedValueT>(
    private val sourceVertex1: FrozenCellVertex<ValueT1>,
    private val sourceVertex2: FrozenCellVertex<ValueT2>,
    private val transform: (ValueT1, ValueT2) -> TransformedValueT,
) : AbstractDerivedFrozenCellVertex<TransformedValueT>() {
    override fun computeFrozenValue(
        propagationContext: Transactions.PropagationContext,
    ): TransformedValueT = transform(
        sourceVertex1.getOldValue(
            propagationContext = propagationContext,
        ),
        sourceVertex2.getOldValue(
            propagationContext = propagationContext,
        ),
    )
}
