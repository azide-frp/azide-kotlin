package dev.azide.core.impl.cell.operated_vertices

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.cell.FrozenCellVertex
import dev.azide.core.impl.cell.abstract_vertices.AbstractDerivedFrozenCellVertex

class MappedFrozenCellVertex<ValueT, TransformedValueT>(
    private val sourceVertex: FrozenCellVertex<ValueT>,
    private val transform: (ValueT) -> TransformedValueT,
) : AbstractDerivedFrozenCellVertex<TransformedValueT>() {
    override fun computeFrozenValue(
        propagationContext: Transactions.PropagationContext,
    ): TransformedValueT = transform(
        sourceVertex.getOldValue(
            propagationContext = propagationContext,
        ),
    )
}
