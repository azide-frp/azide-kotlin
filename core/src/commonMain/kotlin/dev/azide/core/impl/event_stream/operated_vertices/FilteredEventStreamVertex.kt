package dev.azide.core.impl.event_stream.operated_vertices

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.ListenableVertex.ActivationMode
import dev.azide.core.impl.ListenableVertex.BoundListener
import dev.azide.core.impl.ListenableVertex.ListenerHandle
import dev.azide.core.impl.event_stream.EventStreamVertex
import dev.azide.core.impl.event_stream.abstract_vertices.AbstractSimpleStatelessEventStreamVertex
import dev.azide.core.impl.event_stream.registerBoundListener

class FilteredEventStreamVertex<EventT>(
    private val sourceVertex: EventStreamVertex<EventT>,
    private val predicate: (EventT) -> Boolean,
) : AbstractSimpleStatelessEventStreamVertex<EventT>(), BoundListener {
    private var upstreamListenerHandle: ListenerHandle? = null

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
                when {
                    predicate(emission.emittedEvent) -> { // The predicate accepted the event
                        // We have to propagate the emission (it might be a correction)
                        exposeEmissionNotifyingListeners(
                            propagationContext = propagationContext,
                            emission = emission,
                        )
                    }

                    else -> { // The predicate rejected the event, it's filtered out
                        if (ongoingEmission != null) {
                            // If we previously propagated an emission (when the predicate accepted the event), we have
                            // to revoke it
                            exposeEmissionNotifyingListeners(
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
        if (upstreamListenerHandle != null) {
            throw IllegalStateException("ListenableVertex seems to be already active")
        }

        upstreamListenerHandle = sourceVertex.registerBoundListener(
            propagationContext = propagationContext,
            listener = this,
            mode = mode,
        )

        return sourceVertex.ongoingEmission?.takeIf {
            predicate(it.emittedEvent)
        }
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
