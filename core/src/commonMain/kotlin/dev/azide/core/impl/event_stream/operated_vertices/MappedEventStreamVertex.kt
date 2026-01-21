package dev.azide.core.impl.event_stream.operated_vertices

import dev.azide.core.EventStream
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.event_stream.EventStreamVertex
import dev.azide.core.impl.event_stream.EventStreamVertex.BoundListener
import dev.azide.core.impl.event_stream.abstract_vertices.AbstractSimpleStatelessEventStreamVertex
import dev.azide.core.impl.event_stream.registerBoundListener

class MappedEventStreamVertex<EventT, TransformedEventT>(
    private val sourceEventStream: EventStream<EventT>,
    private val transform: (EventT) -> TransformedEventT,
) : AbstractSimpleStatelessEventStreamVertex<TransformedEventT>(), BoundListener {
    private val sourceVertex: EventStreamVertex<EventT>
        get() = sourceEventStream.vertex

    private var upstreamListenerHandle: EventStreamVertex.ListenerHandle? = null

    /**
     * Handle the emission of the source event stream.
     */
    override fun handleEmission(
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
        if (upstreamListenerHandle != null) {
            throw IllegalStateException("Vertex seems to be already active")
        }

        upstreamListenerHandle = sourceVertex.registerBoundListener(
            propagationContext = propagationContext,
            listener = this,
            mode = mode,
        )

        return sourceVertex.ongoingEmission?.map(transform)
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
