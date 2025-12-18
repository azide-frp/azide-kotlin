package dev.azide.internal.event_stream.operated_vertices

import dev.azide.internal.Transactions
import dev.azide.internal.event_stream.EventStreamVertex
import dev.azide.internal.event_stream.LiveEventStreamVertex
import dev.azide.internal.event_stream.LiveEventStreamVertex.BasicSubscriber
import dev.azide.internal.event_stream.LiveEventStreamVertex.LooseSubscription
import dev.azide.internal.event_stream.abstract_vertices.AbstractStatefulEventStreamVertex
import dev.azide.internal.event_stream.registerLooseSubscriber

class SingleEventStreamVertex<EventT>(
    propagationContext: Transactions.PropagationContext,
    sourceVertex: LiveEventStreamVertex<EventT>,
) : AbstractStatefulEventStreamVertex<EventT>(), BasicSubscriber<EventT> {
    private var upstreamLooseSubscription: LooseSubscription? = sourceVertex.registerLooseSubscriber(
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

    override fun commit(
        ongoingEmission: EventStreamVertex.Emission<EventT>?,
    ) {
        val upstreamLooseSubscription = this.upstreamLooseSubscription
            ?: throw IllegalStateException("It looks as if the single emission already had place")

        if (ongoingEmission != null) {
            upstreamLooseSubscription.cancel()

            this.upstreamLooseSubscription = null
        }
    }
}
