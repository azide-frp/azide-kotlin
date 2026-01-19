package dev.azide.core.impl.event_stream.operated_vertices

import dev.azide.core.EventStream
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.event_stream.EventStreamVertex
import dev.azide.core.impl.event_stream.LiveEventStreamVertex
import dev.azide.core.impl.event_stream.abstract_vertices.AbstractSimpleStatelessEventStreamVertex

class MappedEventStreamVertex<EventT, TransformedEventT>(
    private val sourceEventStream: EventStream<EventT>,
    private val transform: (Transactions.PropagationContext, EventT) -> TransformedEventT,
) : AbstractSimpleStatelessEventStreamVertex<TransformedEventT>(), LiveEventStreamVertex.BasicSubscriber<EventT> {
    private val sourceVertex: EventStreamVertex<EventT>
        get() = sourceEventStream.vertex

    private var upstreamSubscriberHandle: EventStreamVertex.SubscriberHandle? = null

    /**
     * Handle the emission of the source event stream.
     */
    override fun handleEmission(
        propagationContext: Transactions.PropagationContext,
        emission: EventStreamVertex.Emission<EventT>?,
    ) {
        when (emission) {
            null -> {
                exposeAndPropagateEmission(
                    propagationContext = propagationContext,
                    emission = null,
                )
            }

            else -> {
                exposeAndPropagateEmission(
                    propagationContext = propagationContext,
                    emission = emission.map {
                        transform(propagationContext, it)
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

        upstreamSubscriberHandle = sourceVertex.registerSubscriber(
            propagationContext = propagationContext,
            subscriber = this,
            mode = mode,
        )

        return sourceVertex.ongoingEmission?.map {
            transform(propagationContext, it)
        }
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
