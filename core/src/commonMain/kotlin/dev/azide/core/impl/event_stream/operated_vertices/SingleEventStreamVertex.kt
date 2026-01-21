package dev.azide.core.impl.event_stream.operated_vertices

import dev.azide.core.EventStream
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex.ActivationMode
import dev.azide.core.impl.event_stream.EventStreamVertex
import dev.azide.core.impl.event_stream.EventStreamVertex.BoundEmissionSubscriber
import dev.azide.core.impl.event_stream.LiveEventStreamVertex
import dev.azide.core.impl.event_stream.abstract_vertices.AbstractStatefulEventStreamVertex
import dev.azide.core.impl.event_stream.registerEmissionSubscriberWeakly

class SingleEventStreamVertex<EventT>(
    wrapUpContext: Transactions.WrapUpContext,
    private val sourceEventStream: EventStream<EventT>,
) : AbstractStatefulEventStreamVertex<EventT>(
    wrapUpContext = wrapUpContext,
), BoundEmissionSubscriber {
    private val sourceVertex: EventStreamVertex<EventT>
        get() = sourceEventStream.vertex

    private var upstreamWeakSubscriberHandle: LiveEventStreamVertex.WeakSubscriberHandle? = null

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
                    emission = emission,
                )
            }
        }
    }

    override fun initialize(
        propagationContext: Transactions.PropagationContext,
    ): EventStreamVertex.Emission<EventT>? {

        upstreamWeakSubscriberHandle = sourceVertex.registerEmissionSubscriberWeakly(
            propagationContext = propagationContext,
            dependentVertex = this,
            subscriber = this,
            mode = ActivationMode.Online,
        )

        return sourceVertex.ongoingEmission
    }

    override fun transit() {
        val upstreamWeakSubscriberHandle = this.upstreamWeakSubscriberHandle
            ?: throw IllegalStateException("It looks as if the single emission already had place or the vertex wasn't initialized")

        upstreamWeakSubscriberHandle.cancel()

        this.upstreamWeakSubscriberHandle = null
    }
}
