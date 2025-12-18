package dev.azide.internal.cell.abstract_vertices

import dev.azide.internal.Transactions
import dev.azide.internal.cell.CellVertex.Update

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
    ): Update<ValueT>?

    abstract fun deactivate()
}
