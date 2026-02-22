package dev.azide.core.impl.event_stream.abstract_vertices

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.event_stream.EventStreamVertex

abstract class AbstractStatelessEventStreamVertex<EventT> : AbstractLiveEventStreamVertex<EventT>() {
    final override fun onFirstListenerRegistered(
        processingContext: Transactions.ProcessingContext,
    ) {
        when (processingContext) {
            is Transactions.CommitmentContext -> {
                activateOffline(
                    commitmentContext = processingContext,
                )
            }

            is Transactions.PropagationContext -> {
                val emissionOnActivation = activateOnline(
                    propagationContext = processingContext,
                )

                exposeEmission(
                    propagationContext = processingContext,
                    emission = emissionOnActivation,
                )
            }
        }
    }

    final override fun onLastListenerUnregistered() {
        deactivate()

        clearExposedEmission()
    }

    abstract fun activateOnline(
        propagationContext: Transactions.PropagationContext,
    ): EventStreamVertex.Emission<EventT>?

    abstract fun activateOffline(
        commitmentContext: Transactions.CommitmentContext,
    )

    abstract fun deactivate()
}
