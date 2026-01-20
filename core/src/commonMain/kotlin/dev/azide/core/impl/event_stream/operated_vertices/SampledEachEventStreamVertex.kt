package dev.azide.core.impl.event_stream.operated_vertices

import dev.azide.core.EventStream
import dev.azide.core.Moment
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.event_stream.EventStreamVertex
import dev.azide.core.impl.event_stream.EventStreamVertex.EmissionSubscriber
import dev.azide.core.impl.event_stream.abstract_vertices.AbstractSimpleStatelessEventStreamVertex
import dev.azide.core.impl.event_stream.registerEmissionSubscriber
import dev.azide.core.pullInternallyWrappedUp

class SampledEachEventStreamVertex<EventT>(
    private val sourceEventStream: EventStream<Moment<EventT>>,
) : AbstractSimpleStatelessEventStreamVertex<EventT>(), EmissionSubscriber<Moment<EventT>> {
    private val sourceVertex: EventStreamVertex<Moment<EventT>>
        get() = sourceEventStream.vertex

    private var upstreamSubscriberHandle: EventStreamVertex.SubscriberHandle? = null

    /**
     * Handle the emission of the source event stream.
     */
    override fun handleEmission(
        propagationContext: Transactions.PropagationContext,
        emission: EventStreamVertex.Emission<Moment<EventT>>?,
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
                    emission = emission.map { eventMoment: Moment<EventT> ->
                        eventMoment.pullInternallyWrappedUp(
                            propagationContext = propagationContext,
                        )
                    },
                )
            }
        }
    }

    override fun activate(
        propagationContext: Transactions.PropagationContext,
        mode: Vertex.ActivationMode,
    ): EventStreamVertex.Emission<EventT>? {
        if (upstreamSubscriberHandle != null) {
            throw IllegalStateException("Vertex seems to be already active")
        }

        upstreamSubscriberHandle = sourceVertex.registerEmissionSubscriber(
            propagationContext = propagationContext,
            subscriber = this,
            mode = mode,
        )

        return sourceVertex.ongoingEmission?.map { eventMoment: Moment<EventT> ->
            eventMoment.pullInternallyWrappedUp(
                propagationContext = propagationContext,
            )
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
