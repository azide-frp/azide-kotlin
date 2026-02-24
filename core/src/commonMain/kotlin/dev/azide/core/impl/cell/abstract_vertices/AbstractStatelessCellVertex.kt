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

        prepare(
            processingContext = processingContext,
        )

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
        deactivate()

        reset()

        clearExposedUpdate()
    }

    /**
     * Prepare the vertex for being sampled (initialize the internal cache).
     */
    open fun prepare(
        processingContext: Transactions.ProcessingContext,
    ) {
    }

    /**
     * Reset the vertex (drop the internal cache).
     */
    open fun reset() {
    }

    /**
     * Activate the vertex (register all required upstream listeners).
     */
    abstract fun activate(
        processingContext: Transactions.ProcessingContext,
    )

    /**
     * Deactivate the vertex (unregister all upstream listeners).
     */
    abstract fun deactivate()

    /**
     * Build the initial update.
     */
    abstract fun buildInitialUpdate(
        propagationContext: Transactions.PropagationContext,
    ): CellVertex.Update<ValueT>?
}
