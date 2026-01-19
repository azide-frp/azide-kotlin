package dev.azide.core.impl.event_stream.abstract_vertices

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex.ActivationMode
import dev.azide.core.impl.event_stream.EventStreamVertex

abstract class AbstractStatelessEventStreamVertex<EventT> : AbstractLiveEventStreamVertex<EventT>() {
    final override fun onFirstSubscriberRegistered(
        propagationContext: Transactions.PropagationContext,
        mode: ActivationMode,
    ) {
        when (mode) {
            ActivationMode.Online -> {
                val emissionOnActivation = activateOnline(
                    propagationContext = propagationContext,
                )

                exposeEmission(
                    propagationContext = propagationContext,
                    emission = emissionOnActivation,
                )
            }

            ActivationMode.Offline -> {
                activateOffline(
                    propagationContext = propagationContext,
                )
            }
        }
    }

    final override fun onLastSubscriberUnregistered() {
        deactivate()

        clearExposedEmission()
    }

    abstract fun activateOnline(
        propagationContext: Transactions.PropagationContext,
    ): EventStreamVertex.Emission<EventT>?

    abstract fun activateOffline(
        propagationContext: Transactions.PropagationContext,
    )

    abstract fun deactivate()
}
