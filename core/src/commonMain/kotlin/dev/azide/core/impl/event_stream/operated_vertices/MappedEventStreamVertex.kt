package dev.azide.core.impl.event_stream.operated_vertices

import dev.azide.core.EventStream
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.event_stream.EventStreamVertex
import dev.azide.core.impl.event_stream.EventStreamVertex.BoundEmissionSubscriber
import dev.azide.core.impl.event_stream.abstract_vertices.AbstractSimpleStatelessEventStreamVertex
import dev.azide.core.impl.event_stream.registerBoundEmissionSubscriber

class MappedEventStreamVertex<EventT, TransformedEventT>(
    private val sourceEventStream: EventStream<EventT>,
    private val transform: (EventT) -> TransformedEventT,
) : AbstractSimpleStatelessEventStreamVertex<TransformedEventT>(), BoundEmissionSubscriber {
    private val sourceVertex: EventStreamVertex<EventT>
        get() = sourceEventStream.vertex

    private var upstreamSubscriberHandle: EventStreamVertex.SubscriberHandle? = null

    /**
     * Handle the emission of the source event stream.
     */
    override fun handleEmission(
        propagationContext: Transactions.PropagationContext,
    ) {
        when (val emission = sourceVertex.ongoingEmission) {
            null -> {
                exposeAndPropagateEmission(
                    propagationContext = propagationContext,
                    emission = null,
                )
            }

            else -> {
                exposeAndPropagateEmission(
                    propagationContext = propagationContext,
                    emission = emission.map { event: EventT ->
                        transform(event)
                    },
                )
            }
        }
    }

    override fun activate(
        propagationContext: Transactions.PropagationContext,
        mode: Vertex.ActivationMode,
    ): EventStreamVertex.Emission<TransformedEventT>? {
        if (upstreamSubscriberHandle != null) {
            throw IllegalStateException("Vertex seems to be already active")
        }

        upstreamSubscriberHandle = sourceVertex.registerBoundEmissionSubscriber(
            propagationContext = propagationContext,
            subscriber = this,
            mode = mode,
        )

        return sourceVertex.ongoingEmission?.map(transform)
    }

    override fun deactivate() {
        val subscriptionHandle =
            this.upstreamSubscriberHandle ?: throw IllegalStateException("Vertex doesn't seem to be active")

        sourceVertex.unregisterSubscriber(
            handle = subscriptionHandle,
        )

        this.upstreamSubscriberHandle = null
    }
}
