package dev.azide.core.impl.event_stream.operated_vertices

import dev.azide.core.EventStream
import dev.azide.core.Moment
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.Vertex.BoundListener
import dev.azide.core.impl.event_stream.EventStreamVertex
import dev.azide.core.impl.event_stream.abstract_vertices.AbstractSimpleStatelessEventStreamVertex
import dev.azide.core.impl.event_stream.registerBoundListener
import dev.azide.core.pullInternallyWrappedUp

class SampledEachEventStreamVertex<EventT>(
    private val sourceEventStream: EventStream<Moment<EventT>>,
) : AbstractSimpleStatelessEventStreamVertex<EventT>(), BoundListener {
    private val sourceVertex: EventStreamVertex<Moment<EventT>>
        get() = sourceEventStream.vertex

    private var upstreamListenerHandle: Vertex.ListenerHandle? = null

    /**
     * Handle the emission of the source event stream.
     */
    override fun handle(
        propagationContext: Transactions.PropagationContext,
    ) {
        when (val emission = sourceVertex.ongoingEmission) {
            null -> {
                exposeEmissionNotifyingListeners(
                    propagationContext = propagationContext,
                    emission = null,
                )
            }

            else -> {
                exposeEmissionNotifyingListeners(
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
        if (upstreamListenerHandle != null) {
            throw IllegalStateException("Vertex seems to be already active")
        }

        upstreamListenerHandle = sourceVertex.registerBoundListener(
            propagationContext = propagationContext,
            listener = this,
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
            this.upstreamListenerHandle ?: throw IllegalStateException("Vertex doesn't seem to be active")

        sourceVertex.unregisterListener(
            handle = subscriptionHandle,
        )

        this.upstreamListenerHandle = null
    }
}
