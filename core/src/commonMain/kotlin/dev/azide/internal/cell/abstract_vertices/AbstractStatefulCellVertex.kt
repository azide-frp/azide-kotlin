package dev.azide.internal.cell.abstract_vertices

import dev.azide.internal.Transactions
import dev.azide.internal.cell.CellVertex.Update

abstract class AbstractStatefulCellVertex<ValueT>(
    initialValue: ValueT,
) : AbstractAutonomousCellVertex<ValueT>() {
    private var _stableValue: ValueT = initialValue

    final override fun commit(
        ongoingUpdate: Update<ValueT>?,
    ) {
        if (ongoingUpdate != null) {
            _stableValue = ongoingUpdate.updatedValue
        }
    }

    final override fun getOldValue(
        propagationContext: Transactions.PropagationContext,
    ): ValueT = _stableValue
}
