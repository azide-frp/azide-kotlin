package dev.azide.core.impl.event_stream.operated_vertices

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex.ActivationMode
import dev.azide.core.impl.event_stream.EventStreamVertex
import dev.azide.core.impl.event_stream.EventStreamVertex.EmissionSubscriber
import dev.azide.core.impl.event_stream.abstract_vertices.AbstractSimpleStatelessEventStreamVertex
import dev.azide.core.impl.event_stream.registerEmissionSubscriber

class FilteredEventStreamVertex<EventT>(
    private val sourceVertex: EventStreamVertex<EventT>,
    private val predicate: (EventT) -> Boolean,
) : AbstractSimpleStatelessEventStreamVertex<EventT>(), EmissionSubscriber {
    private var upstreamSubscriberHandle: EventStreamVertex.SubscriberHandle? = null

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
        mode: ActivationMode,
    ): EventStreamVertex.Emission<EventT>? {
        if (upstreamSubscriberHandle != null) {
            throw IllegalStateException("Vertex seems to be already active")
        }

        upstreamSubscriberHandle = sourceVertex.registerEmissionSubscriber(
            propagationContext = propagationContext,
            subscriber = this,
            mode = mode,
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
