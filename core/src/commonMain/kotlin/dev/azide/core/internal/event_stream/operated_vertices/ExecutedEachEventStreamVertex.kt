package dev.azide.core.internal.event_stream.operated_vertices

import dev.azide.core.Action
import dev.azide.core.internal.Transactions
import dev.azide.core.internal.event_stream.EventStreamVertex
import dev.azide.core.internal.event_stream.LiveEventStreamVertex
import dev.azide.core.internal.event_stream.abstract_vertices.AbstractStatefulEventStreamVertex

class ExecutedEachEventStreamVertex<EventT> private constructor(
    propagationContext: Transactions.PropagationContext,
    private val sourceVertex: LiveEventStreamVertex<Action<EventT>>,
) : AbstractStatefulEventStreamVertex<EventT>(), LiveEventStreamVertex.BasicSubscriber<Action<EventT>> {
    companion object {
        fun <EventT> start(
            propagationContext: Transactions.PropagationContext,
            sourceVertex: LiveEventStreamVertex<Action<EventT>>,
        ): ExecutedEachEventStreamVertex<EventT> = ExecutedEachEventStreamVertex(
            propagationContext = propagationContext,
            sourceVertex = sourceVertex,
        )
    }

    private var upstreamSubscriberHandle: EventStreamVertex.SubscriberHandle? = null

    private var executedActionRevocationHandle: Action.RevocationHandle? = null

    /**
     * Handle the emission of the source action event stream vertex.
     */
    override fun handleEmission(
        propagationContext: Transactions.PropagationContext,
        emission: EventStreamVertex.Emission<Action<EventT>>?,
    ) {
        when (emission) {
            null -> {
                val executedActionRevocationHandle = this.executedActionRevocationHandle
                    ?: throw AssertionError("Expected executed action revocation handle to be non-null when handling revoked emission")

                executedActionRevocationHandle.revoke()

                exposeAndPropagateEmission(
                    propagationContext = propagationContext,
                    emission = null,
                )
            }

            else -> {
                this.executedActionRevocationHandle?.revoke()

                val emittedAction: Action<EventT> = emission.emittedEvent

                val (emittedEvent: EventT, revocationHandle) = emittedAction.executeInternally(
                    propagationContext = propagationContext,
                )

                executedActionRevocationHandle = revocationHandle

                exposeAndPropagateEmission(
                    propagationContext = propagationContext,
                    emission = EventStreamVertex.Emission(
                        emittedEvent = emittedEvent,
                    ),
                )
            }
        }
    }

    fun abort() {
        this.executedActionRevocationHandle?.revoke()
        this.executedActionRevocationHandle = null

        val upstreamSubscriberHandle =
            this.upstreamSubscriberHandle ?: throw IllegalStateException("Vertex is already aborted")

        sourceVertex.unregisterSubscriber(
            handle = upstreamSubscriberHandle,
        )
    }

    fun restart(
        propagationContext: Transactions.PropagationContext,
    ) {
        if (upstreamSubscriberHandle != null) {
            throw IllegalStateException("Vertex seems to be already active")
        }

        upstreamSubscriberHandle = sourceVertex.registerSubscriber(
            propagationContext = propagationContext,
            subscriber = this,
        )

        sourceVertex.ongoingEmission?.let { sourceOngoingEmission ->
            val emittedAction: Action<EventT> = sourceOngoingEmission.emittedEvent

            val (emittedEvent: EventT, revocationHandle) = emittedAction.executeInternally(
                propagationContext = propagationContext,
            )

            executedActionRevocationHandle = revocationHandle

            exposeEmission(
                propagationContext = propagationContext,
                emission = EventStreamVertex.Emission(
                    emittedEvent = emittedEvent,
                ),
            )
        }
    }

    init {
        upstreamSubscriberHandle = sourceVertex.registerSubscriber(
            propagationContext = propagationContext,
            subscriber = this,
        )

        sourceVertex.ongoingEmission?.let { sourceOngoingEmission ->
            val emittedAction: Action<EventT> = sourceOngoingEmission.emittedEvent

            val (emittedEvent: EventT, revocationHandle) = emittedAction.executeInternally(
                propagationContext = propagationContext,
            )

            executedActionRevocationHandle = revocationHandle

            exposeEmission(
                propagationContext = propagationContext,
                emission = EventStreamVertex.Emission(
                    emittedEvent = emittedEvent,
                ),
            )
        }
    }
}
