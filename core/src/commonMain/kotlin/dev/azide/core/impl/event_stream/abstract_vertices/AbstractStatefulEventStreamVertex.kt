package dev.azide.core.impl.event_stream.abstract_vertices

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.event_stream.EventStreamVertex

abstract class AbstractStatefulEventStreamVertex<EventT>(
    wrapUpContext: Transactions.WrapUpContext,
) : AbstractLiveEventStreamVertex<EventT>() {
    private var isInitialized = false

    final override fun onFirstListenerRegistered(
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
