package dev.azide.core.impl.cell.abstract_vertices

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.cell.CellVertex

abstract class AbstractStatelessCellVertex<ValueT> : AbstractCellVertex<ValueT>() {
    final override fun onFirstListenerRegistered(
        processingContext: Transactions.ProcessingContext,
    ) {
        activate(
            processingContext = processingContext,
        )

        prepare() // FIXME: Here?

        if (processingContext is Transactions.PropagationContext) {
            exposeUpdate(
                propagationContext = processingContext,
                update = buildInitialUpdate(
                    propagationContext = processingContext,
                ),
            )
        }
    }

    final override fun onLastListenerUnregistered() {
        reset()

        deactivate()

        clearExposedUpdate()
    }

    /**
     *
     */
    open fun prepare() {
    }

    abstract fun activate(
        processingContext: Transactions.ProcessingContext,
    )

    abstract fun buildInitialUpdate(
        propagationContext: Transactions.PropagationContext,
    ): CellVertex.Update<ValueT>?

    abstract fun deactivate()

    /**
     *
     */
    open fun reset() {
    }
}
