package dev.azide.core.internal.event_stream.operated_vertices

import dev.azide.core.ExternalSourceAdapter
import dev.azide.core.internal.Transactions
import dev.azide.core.internal.event_stream.EventStreamVertex
import dev.azide.core.internal.event_stream.abstract_vertices.AbstractStatelessEventStreamVertex

class WrappedExternalEventStreamVertex<EventT>(
    externalSourceAdapter: ExternalSourceAdapter<EventT>,
) : AbstractStatelessEventStreamVertex<EventT>(), ExternalSourceAdapter.EventDistributor<EventT> {
    private val externalSubscriptionHandle = externalSourceAdapter.bind(eventDistributor = this)

    override fun activate(
        propagationContext: Transactions.PropagationContext,
    ): EventStreamVertex.Emission<EventT>? {
        externalSubscriptionHandle.register()

        return null
    }

    override fun deactivate() {
        externalSubscriptionHandle.unregister()
    }

    override fun distribute(event: EventT) {
        Transactions.execute { propagationContext ->
            exposeAndPropagateEmission(
                propagationContext = propagationContext,
                emission = EventStreamVertex.Emission(
                    emittedEvent = event,
                ),
            )
        }
    }
}
