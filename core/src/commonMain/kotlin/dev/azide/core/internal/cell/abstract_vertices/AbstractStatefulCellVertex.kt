package dev.azide.core.internal.cell.abstract_vertices

import dev.azide.core.internal.Transactions
import dev.azide.core.internal.cell.CellVertex

abstract class AbstractStatefulCellVertex<ValueT>() : AbstractAutonomousCellVertex<ValueT>() {
    constructor(
        initialValue: ValueT,
    ) : this() {
        _stableValue = initialValue
    }

    private var _stableValue: ValueT? = null

    final override fun persist(
        ongoingUpdate: CellVertex.Update<ValueT>?,
    ) {
        if (ongoingUpdate != null) {
            _stableValue = ongoingUpdate.updatedValue
        }
    }

    final override fun getOldValue(
        propagationContext: Transactions.PropagationContext,
    ): ValueT = _stableValue ?: throw IllegalStateException("Initial value was not set")

    /**
     * Sets the initial value of the cell vertex. This method is supposed to be called only in subclass constructors.
     * If the subclass doesn't invoke [AbstractStatefulCellVertex] constructor variant that accepts an initial value,
     * it **must** call this method to set the initial value.
     */
    protected fun setInitialValue(
        initialValue: ValueT,
    ) {
        _stableValue = initialValue
    }
}
