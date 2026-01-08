package dev.azide.core.internal.event_stream.operated_vertices

import dev.azide.core.external.ExternalEventHandler
import dev.azide.core.external.ExternalStream
import dev.azide.core.internal.Transactions
import dev.azide.core.internal.Vertex.ActivationMode
import dev.azide.core.internal.event_stream.EventStreamVertex
import dev.azide.core.internal.event_stream.abstract_vertices.AbstractSimpleStatelessEventStreamVertex

class AdaptedExternalEventStreamVertex<EventT>(
    externalStream: ExternalStream<EventT>,
) : AbstractSimpleStatelessEventStreamVertex<EventT>(), ExternalEventHandler<EventT> {
    private val externalSubscriptionHandle = externalStream.bind(handler = this)

    override fun activate(
        propagationContext: Transactions.PropagationContext,
        mode: ActivationMode,
    ): EventStreamVertex.Emission<EventT>? {
        externalSubscriptionHandle.register()

        return null
    }

    override fun deactivate() {
        externalSubscriptionHandle.unregister()
    }

    override fun handle(event: EventT) {
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
