package dev.azide.core.internal.event_stream.abstract_vertices

import dev.azide.core.internal.Transactions
import dev.azide.core.internal.event_stream.EventStreamVertex

abstract class AbstractStatelessEventStreamVertex<EventT> : AbstractLiveEventStreamVertex<EventT>() {
    final override fun onFirstSubscriberRegistered(
        propagationContext: Transactions.PropagationContext,
    ) {
        val emissionOnActivation = activate(
            propagationContext = propagationContext,
        )

        exposeEmission(
            propagationContext = propagationContext,
            emission = emissionOnActivation,
        )
    }

    final override fun onLastSubscriberUnregistered() {
        deactivate()

        clearExposedEmission()
    }

    abstract fun activate(
        propagationContext: Transactions.PropagationContext,
    ): EventStreamVertex.Emission<EventT>?

    abstract fun deactivate()
}
