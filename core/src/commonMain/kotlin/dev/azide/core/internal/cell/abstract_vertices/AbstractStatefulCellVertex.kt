package dev.azide.core.internal.cell.abstract_vertices

import dev.azide.core.internal.Transactions
import dev.azide.core.internal.cell.CellVertex

abstract class AbstractStatefulCellVertex<ValueT>(
    initialValue: ValueT,
) : AbstractAutonomousCellVertex<ValueT>() {
    private var _stableValue: ValueT = initialValue

    final override fun commit(
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
