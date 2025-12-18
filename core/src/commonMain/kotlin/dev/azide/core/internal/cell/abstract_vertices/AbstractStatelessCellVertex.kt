package dev.azide.core.internal.cell.abstract_vertices

import dev.azide.core.internal.Transactions
import dev.azide.core.internal.cell.CellVertex

abstract class AbstractStatelessCellVertex<ValueT> : AbstractAutonomousCellVertex<ValueT>() {
    final override fun onFirstObserverRegistered(
        propagationContext: Transactions.PropagationContext,
    ) {
        val updateOnActivation = activate(
            propagationContext = propagationContext,
        )

        exposeUpdate(
            propagationContext = propagationContext,
            update = updateOnActivation,
        )
    }

    final override fun onLastObserverUnregistered() {
        deactivate()

        clearExposedUpdate()
    }

    abstract fun activate(
        propagationContext: Transactions.PropagationContext,
    ): CellVertex.Update<ValueT>?

    abstract fun deactivate()
}
