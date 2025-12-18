package dev.azide.core.internal.cell

import dev.azide.core.internal.Transactions
import dev.azide.core.internal.cell.abstract_vertices.AbstractFrozenCellVertex

class PureCellVertex<ValueT>(
    val value: ValueT,
) : AbstractFrozenCellVertex<ValueT>() {
    override fun getOldValue(
        propagationContext: Transactions.PropagationContext,
    ): ValueT = value
}
