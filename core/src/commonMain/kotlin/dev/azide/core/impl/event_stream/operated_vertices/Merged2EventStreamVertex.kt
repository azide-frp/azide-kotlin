package dev.azide.core.impl.event_stream.operated_vertices

import dev.azide.core.EventStream
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.ListenableVertex
import dev.azide.core.impl.ListenableVertex.BoundListener
import dev.azide.core.impl.event_stream.EventStreamVertex
import dev.azide.core.impl.event_stream.abstract_vertices.AbstractSimpleStatelessEventStreamVertex
import dev.azide.core.impl.event_stream.registerBoundListener

class Merged2EventStreamVertex<EventT>(
    private val sourceEventStream1: EventStream<EventT>,
    private val sourceEventStream2: EventStream<EventT>,
) : AbstractSimpleStatelessEventStreamVertex<EventT>() {
    private val sourceVertex1: EventStreamVertex<EventT>
        get() = sourceEventStream1.vertex

    private val sourceVertex2: EventStreamVertex<EventT>
        get() = sourceEventStream2.vertex

    private var upstreamListenerHandle1: ListenableVertex.ListenerHandle? = null
    private var upstreamListenerHandle2: ListenableVertex.ListenerHandle? = null

    val innerListener1 = object : BoundListener {
        /**
         * Handle the emission of the first event stream.
         */
        override fun handle(
            propagationContext: Transactions.PropagationContext,
        ) {
            when (val emission1 = sourceVertex1.ongoingEmission) {
                null -> { // Emission revocation
                    val emission2 = sourceVertex2.ongoingEmission

                    exposeEmissionNotifyingListeners(
                        propagationContext = propagationContext,
                        emission = emission2,
                    )
                }

                else -> { // Initial emission / correction
                    exposeEmissionNotifyingListeners(
                        propagationContext = propagationContext,
                        emission = emission1,
                    )
                }
            }
        }
    }

    val innerListener2 = object : BoundListener {
        /**
         * Handle the emission of the second event stream.
         */
        override fun handle(
            propagationContext: Transactions.PropagationContext,
        ) {
            val emission1 = sourceVertex1.ongoingEmission

            if (emission1 != null) {
                return
            }

            val emission2 = sourceVertex2.ongoingEmission

            exposeEmissionNotifyingListeners(
                propagationContext = propagationContext,
                emission = emission2,
            )
        }
    }

    override fun activate(
        processingContext: Transactions.ProcessingContext,
    ): EventStreamVertex.Emission<EventT>? {
        if (upstreamListenerHandle1 != null || upstreamListenerHandle2 != null) {
            throw IllegalStateException("ListenableVertex seems to be already active")
        }

        upstreamListenerHandle1 = sourceVertex1.registerBoundListener(
            propagationContext = processingContext,
            listener = innerListener1,
        )

        upstreamListenerHandle2 = sourceVertex2.registerBoundListener(
            propagationContext = processingContext,
            listener = innerListener2,
        )

        return sourceVertex1.ongoingEmission ?: sourceVertex2.ongoingEmission
    }

    override fun deactivate() {
        val subscriptionHandle1 =
            this.upstreamListenerHandle1 ?: throw IllegalStateException("ListenableVertex doesn't seem to be active")

        val subscriptionHandle2 =
            this.upstreamListenerHandle2 ?: throw IllegalStateException("ListenableVertex doesn't seem to be active")

        sourceVertex1.unregisterListener(
            handle = subscriptionHandle1,
        )

        this.upstreamListenerHandle1 = null

        sourceVertex2.unregisterListener(
            handle = subscriptionHandle2,
        )

        this.upstreamListenerHandle2 = null
    }
}
