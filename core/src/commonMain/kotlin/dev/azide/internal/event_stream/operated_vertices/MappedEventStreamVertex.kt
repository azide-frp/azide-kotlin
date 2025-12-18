package dev.azide.internal.event_stream.operated_vertices

import dev.azide.internal.Transactions
import dev.azide.internal.event_stream.EventStreamVertex
import dev.azide.internal.event_stream.LiveEventStreamVertex
import dev.azide.internal.event_stream.LiveEventStreamVertex.BasicSubscriber
import dev.azide.internal.event_stream.LiveEventStreamVertex.SubscriberHandle
import dev.azide.internal.event_stream.abstract_vertices.AbstractStatelessEventStreamVertex

class MappedEventStreamVertex<EventT, TransformedEventT>(
    private val sourceVertex: LiveEventStreamVertex<EventT>,
    private val transform: (EventT) -> TransformedEventT,
) : AbstractStatelessEventStreamVertex<TransformedEventT>(), BasicSubscriber<EventT> {
    private var upstreamSubscriberHandle: SubscriberHandle? = null

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
                    emission = emission.map(transform),
                )
            }
        }
    }

    override fun activate(
        propagationContext: Transactions.PropagationContext,
    ): EventStreamVertex.Emission<TransformedEventT>? {
        if (upstreamSubscriberHandle != null) {
            throw IllegalStateException("Vertex seems to be already active")
        }

        upstreamSubscriberHandle = sourceVertex.registerSubscriber(
            propagationContext = propagationContext,
            subscriber = this,
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
