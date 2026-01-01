package dev.azide.core.internal.event_stream.operated_vertices

import dev.azide.core.internal.Transactions
import dev.azide.core.internal.event_stream.EventStreamVertex
import dev.azide.core.internal.event_stream.LiveEventStreamVertex
import dev.azide.core.internal.event_stream.abstract_vertices.AbstractStatefulEventStreamVertex
import dev.azide.core.internal.event_stream.registerSubscriberWeakly

class SingleEventStreamVertex<EventT>(
    propagationContext: Transactions.PropagationContext,
    sourceVertex: EventStreamVertex<EventT>,
) : AbstractStatefulEventStreamVertex<EventT>(), LiveEventStreamVertex.BasicSubscriber<EventT> {
    private var upstreamWeakSubscriberHandle: LiveEventStreamVertex.WeakSubscriberHandle? =
        sourceVertex.registerSubscriberWeakly(
            propagationContext = propagationContext,
            dependentVertex = this,
            subscriber = this,
        )

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
                    emission = emission,
                )
            }
        }
    }

    init {
        sourceVertex.ongoingEmission?.let { sourceOngoingEmission ->
            exposeEmission(
                propagationContext = propagationContext,
                emission = sourceOngoingEmission,
            )
        }
    }

    override fun transit() {
        val upstreamWeakSubscriberHandle = this.upstreamWeakSubscriberHandle
            ?: throw IllegalStateException("It looks as if the single emission already had place")

        upstreamWeakSubscriberHandle.cancel()

        this.upstreamWeakSubscriberHandle = null
    }
}
