package dev.azide.core.impl.cell

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.cell.abstract_vertices.AbstractFrozenCellVertex

class PureCellVertex<ValueT>(
    val value: ValueT,
) : AbstractFrozenCellVertex<ValueT>() {
    override fun getOldValue(
        propagationContext: Transactions.PropagationContext,
    ): ValueT = value
}
