package dev.azide.core.impl.event_stream.abstract_vertices

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.event_stream.EventStreamVertex

abstract class AbstractSimpleStatelessEventStreamVertex<EventT> : AbstractStatelessEventStreamVertex<EventT>() {
    final override fun activateOnline(
        propagationContext: Transactions.PropagationContext,
    ): EventStreamVertex.Emission<EventT>? = activate(
        processingContext = propagationContext,
    )

    final override fun activateOffline(
        commitmentContext: Transactions.CommitmentContext,
    ) {
        val computedEmission = activate(
            processingContext = commitmentContext,
        )

        if (computedEmission != null) {
            throw AssertionError("The vertex unexpectedly computed the emission in the offline activation mode")
        }
    }

    abstract fun activate(
        processingContext: Transactions.ProcessingContext,
    ): EventStreamVertex.Emission<EventT>?
}
