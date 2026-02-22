package dev.azide.core.impl.event_stream.abstract_vertices

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.event_stream.EventStreamVertex

abstract class AbstractStatefulEventStreamVertex<EventT>(
    wrapUpContext: Transactions.WrapUpContext,
) : AbstractLiveEventStreamVertex<EventT>() {
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
    ): EventStreamVertex.Emission<EventT>?

    private fun ensureInitialized(
        propagationContext: Transactions.PropagationContext,
    ) {
        val emissionOnInitialization = initialize(
            propagationContext = propagationContext,
        )

        exposeEmission(
            propagationContext = propagationContext,
            emission = emissionOnInitialization,
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
