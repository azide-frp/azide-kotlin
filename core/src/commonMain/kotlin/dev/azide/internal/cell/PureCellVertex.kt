package dev.azide.internal.cell

import dev.azide.internal.Transactions
import dev.azide.internal.cell.abstract_vertices.AbstractFrozenCellVertex

class PureCellVertex<ValueT>(
    val value: ValueT,
) : AbstractFrozenCellVertex<ValueT>() {
    override fun getOldValue(
        propagationContext: Transactions.PropagationContext,
    ): ValueT = value
}
