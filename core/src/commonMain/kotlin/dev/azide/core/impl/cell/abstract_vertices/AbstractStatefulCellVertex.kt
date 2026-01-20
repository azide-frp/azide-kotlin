package dev.azide.core.impl.cell.abstract_vertices

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.cell.CellVertex

abstract class AbstractStatefulCellVertex<ValueT>(
    wrapUpContext: Transactions.WrapUpContext,
    initialValue: ValueT,
) : AbstractBaseStatefulCellVertex<ValueT>(
    initialValue = initialValue,
) {
    private var isInitialized = false

    final override fun onFirstObserverRegistered(
        propagationContext: Transactions.PropagationContext,
        mode: Vertex.ActivationMode,
    ) {
        if (isInitialized) return

        if (mode == Vertex.ActivationMode.Offline) {
            throw UnsupportedOperationException("Offline initialization is not supported")
        }

        ensureInitialized(
            propagationContext = propagationContext,
        )
    }

    final override fun onLastObserverUnregistered() {
    }

    protected abstract fun initialize(
        propagationContext: Transactions.PropagationContext,
    ): CellVertex.Update<ValueT>?

    private fun ensureInitialized(
        propagationContext: Transactions.PropagationContext,
    ) {
        val updateOnInitialization = initialize(
            propagationContext = propagationContext,
        )

        exposeUpdate(
            propagationContext = propagationContext,
            update = updateOnInitialization,
        )

        isInitialized = true
    }

    init {
        wrapUpContext.enqueueForWrapUp { propagationContext ->
            if (isInitialized) return@enqueueForWrapUp

            ensureInitialized(
                propagationContext = propagationContext,
            )
        }
    }
}
