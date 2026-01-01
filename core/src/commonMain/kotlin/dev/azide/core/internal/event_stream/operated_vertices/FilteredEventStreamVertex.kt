package dev.azide.core.internal.event_stream.operated_vertices

import dev.azide.core.internal.Transactions
import dev.azide.core.internal.event_stream.EventStreamVertex
import dev.azide.core.internal.event_stream.LiveEventStreamVertex
import dev.azide.core.internal.event_stream.abstract_vertices.AbstractStatelessEventStreamVertex

class FilteredEventStreamVertex<EventT>(
    private val sourceVertex: EventStreamVertex<EventT>,
    private val predicate: (EventT) -> Boolean,
) : AbstractStatelessEventStreamVertex<EventT>(), LiveEventStreamVertex.BasicSubscriber<EventT> {
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
                when {
                    predicate(emission.emittedEvent) -> { // The predicate accepted the event
                        // We have to propagate the emission (it might be a correction)
                        exposeAndPropagateEmission(
                            propagationContext = propagationContext,
                            emission = emission,
                        )
                    }

                    else -> { // The predicate rejected the event, it's filtered out
                        if (ongoingEmission != null) {
                            // If we previously propagated an emission (when the predicate accepted the event), we have
                            // to revoke it
                            exposeAndPropagateEmission(
                                propagationContext = propagationContext,
                                emission = null,
                            )
                        }
                    }
                }
            }
        }
    }

    override fun activate(
        propagationContext: Transactions.PropagationContext,
    ): EventStreamVertex.Emission<EventT>? {
        if (upstreamSubscriberHandle != null) {
            throw IllegalStateException("Vertex seems to be already active")
        }

        upstreamSubscriberHandle = sourceVertex.registerSubscriber(
            propagationContext = propagationContext,
            subscriber = this,
        )

        return sourceVertex.ongoingEmission?.takeIf {
            predicate(it.emittedEvent)
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
