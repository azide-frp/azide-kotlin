package dev.azide.core.internal.event_stream.operated_vertices

import dev.azide.core.EventStream
import dev.azide.core.internal.Transactions
import dev.azide.core.internal.Vertex
import dev.azide.core.internal.event_stream.EventStreamVertex
import dev.azide.core.internal.event_stream.LiveEventStreamVertex
import dev.azide.core.internal.event_stream.abstract_vertices.AbstractSimpleStatelessEventStreamVertex

class Merged2EventStreamVertex<EventT>(
    private val sourceEventStream1: EventStream<EventT>,
    private val sourceEventStream2: EventStream<EventT>,
) : AbstractSimpleStatelessEventStreamVertex<EventT>() {
    private val sourceVertex1: EventStreamVertex<EventT>
        get() = sourceEventStream1.vertex

    private val sourceVertex2: EventStreamVertex<EventT>
        get() = sourceEventStream2.vertex

    private var upstreamSubscriberHandle1: EventStreamVertex.SubscriberHandle? = null
    private var upstreamSubscriberHandle2: EventStreamVertex.SubscriberHandle? = null

    val innerSubscriber1 = object : LiveEventStreamVertex.BasicSubscriber<EventT> {
        /**
         * Handle the emission of the first event stream.
         */
        override fun handleEmission(
            propagationContext: Transactions.PropagationContext,
            emission: EventStreamVertex.Emission<EventT>?,
        ) {
            when (emission) {
                null -> { // Emission revocation
                    val emission2 = sourceVertex2.ongoingEmission

                    exposeAndPropagateEmission(
                        propagationContext = propagationContext,
                        emission = emission2,
                    )
                }

                else -> { // Initial emission / correction
                    exposeAndPropagateEmission(
                        propagationContext = propagationContext,
                        emission = emission,
                    )
                }
            }
        }
    }

    val innerSubscriber2 = object : LiveEventStreamVertex.BasicSubscriber<EventT> {
        /**
         * Handle the emission of the second event stream.
         */
        override fun handleEmission(
            propagationContext: Transactions.PropagationContext,
            emission: EventStreamVertex.Emission<EventT>?,
        ) {
            val emission1 = sourceVertex1.ongoingEmission

            if (emission1 != null) {
                return
            }

            exposeAndPropagateEmission(
                propagationContext = propagationContext,
                emission = emission,
            )
        }
    }

    override fun activate(
        propagationContext: Transactions.PropagationContext,
        mode: Vertex.ActivationMode,
    ): EventStreamVertex.Emission<EventT>? {
        if (upstreamSubscriberHandle1 != null || upstreamSubscriberHandle2 != null) {
            throw IllegalStateException("Vertex seems to be already active")
        }

        upstreamSubscriberHandle1 = sourceVertex1.registerSubscriber(
            propagationContext = propagationContext,
            subscriber = innerSubscriber1,
            mode = mode,
        )

        upstreamSubscriberHandle2 = sourceVertex2.registerSubscriber(
            propagationContext = propagationContext,
            subscriber = innerSubscriber2,
            mode = mode,
        )

        return sourceVertex1.ongoingEmission ?: sourceVertex2.ongoingEmission
    }

    override fun deactivate() {
        val subscriptionHandle1 =
            this.upstreamSubscriberHandle1 ?: throw IllegalStateException("Vertex doesn't seem to be active")

        val subscriptionHandle2 =
            this.upstreamSubscriberHandle2 ?: throw IllegalStateException("Vertex doesn't seem to be active")

        sourceVertex1.unregisterSubscriber(
            handle = subscriptionHandle1,
        )

        this.upstreamSubscriberHandle1 = null

        sourceVertex2.unregisterSubscriber(
            handle = subscriptionHandle2,
        )

        this.upstreamSubscriberHandle2 = null
    }
}
