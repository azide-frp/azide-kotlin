package dev.azide.core.impl.event_stream.abstract_vertices

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.event_stream.EventStreamVertex

abstract class AbstractSimpleStatelessEventStreamVertex<EventT> : AbstractStatelessEventStreamVertex<EventT>() {
    final override fun activateOnline(
        propagationContext: Transactions.PropagationContext,
    ): EventStreamVertex.Emission<EventT>? = activate(
        propagationContext = propagationContext,
        mode = Vertex.ActivationMode.Online,
    )

    final override fun activateOffline(
        propagationContext: Transactions.PropagationContext,
    ) {
        val computedEmission = activate(
            propagationContext = propagationContext,
            mode = Vertex.ActivationMode.Offline,
        )

        if (computedEmission != null) {
            throw AssertionError("The vertex unexpectedly computed the emission in the offline activation mode")
        }
    }

    abstract fun activate(
        propagationContext: Transactions.PropagationContext,
        mode: Vertex.ActivationMode,
    ): EventStreamVertex.Emission<EventT>?
}
