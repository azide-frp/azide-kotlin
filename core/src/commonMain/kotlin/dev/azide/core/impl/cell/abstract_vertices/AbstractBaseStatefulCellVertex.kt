package dev.azide.core.impl.cell.abstract_vertices

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.cell.CellVertex

abstract class AbstractBaseStatefulCellVertex<ValueT>(
    initialValue: ValueT,
) : AbstractCellVertex<ValueT>() {
    private var _stableValue: ValueT = initialValue


    final override fun persist(
        ongoingUpdate: CellVertex.Update<ValueT>?,
    ) {
        if (ongoingUpdate != null) {
            _stableValue = ongoingUpdate.updatedValue
        }
    }

    final override fun getOldValue(
        propagationContext: Transactions.PropagationContext,
    ): ValueT = _stableValue
}
