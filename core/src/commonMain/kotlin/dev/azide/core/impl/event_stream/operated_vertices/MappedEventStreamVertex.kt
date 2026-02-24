package dev.azide.core.impl.event_stream.operated_vertices

import dev.azide.core.EventStream
import dev.azide.core.impl.ListenableVertex
import dev.azide.core.impl.ListenableVertex.BoundListener
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.event_stream.EventStreamVertex
import dev.azide.core.impl.event_stream.abstract_vertices.AbstractSimpleStatelessEventStreamVertex
import dev.azide.core.impl.event_stream.registerBoundListener

class MappedEventStreamVertex<EventT, TransformedEventT>(
    private val sourceEventStream: EventStream<EventT>,
    private val transform: (EventT) -> TransformedEventT,
) : AbstractSimpleStatelessEventStreamVertex<TransformedEventT>(), BoundListener {
    private val sourceVertex: EventStreamVertex<EventT>
        get() = sourceEventStream.vertex

    private var upstreamListenerHandle: ListenableVertex.ListenerHandle? = null

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
                    emission = emission.map { event: EventT ->
                        transform(event)
                    },
                )
            }
        }
    }

    override fun activate(
        processingContext: Transactions.ProcessingContext,
    ): EventStreamVertex.Emission<TransformedEventT>? {
        if (upstreamListenerHandle != null) {
            throw IllegalStateException("ListenableVertex seems to be already active")
        }

        upstreamListenerHandle = sourceVertex.registerBoundListener(
            propagationContext = processingContext,
            listener = this,
        )

        return sourceVertex.ongoingEmission?.map(transform)
    }

    override fun deactivate() {
        val subscriptionHandle =
            this.upstreamListenerHandle ?: throw IllegalStateException("ListenableVertex doesn't seem to be active")

        sourceVertex.unregisterListener(
            handle = subscriptionHandle,
        )

        this.upstreamListenerHandle = null
    }
}
