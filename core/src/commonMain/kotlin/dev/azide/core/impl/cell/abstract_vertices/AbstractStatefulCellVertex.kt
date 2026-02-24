package dev.azide.core.impl.cell.abstract_vertices

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.cell.CellVertex

abstract class AbstractStatefulCellVertex<ValueT>(
    wrapUpContext: Transactions.WrapUpContext,
    initialValue: ValueT,
) : AbstractBaseStatefulCellVertex<ValueT>(
    initialValue = initialValue,
) {
    private var isInitialized = false

    final override fun onFirstListenerRegistered(
        processingContext: Transactions.ProcessingContext,
    ) {
        if (isInitialized) return

        when (processingContext) {
            is Transactions.CommitmentContext -> {
                // TODO: Explain why this is unreachable
                throw UnsupportedOperationException("Offline initialization is not supported")
            }

            is Transactions.PropagationContext -> {
                ensureInitialized(
                    propagationContext = processingContext,
                )
            }
        }
    }

    final override fun onLastListenerUnregistered() {
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
