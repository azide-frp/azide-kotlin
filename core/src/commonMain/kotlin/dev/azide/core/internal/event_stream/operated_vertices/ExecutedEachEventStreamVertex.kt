package dev.azide.core.internal.event_stream.operated_vertices

import dev.azide.core.Action
import dev.azide.core.internal.RevocationHandle
import dev.azide.core.executeInternallyWrappedUp
import dev.azide.core.internal.Transactions
import dev.azide.core.internal.effects.RestartableEffectVertex
import dev.azide.core.internal.event_stream.EventStreamVertex
import dev.azide.core.internal.event_stream.LiveEventStreamVertex
import dev.azide.core.internal.event_stream.abstract_vertices.AbstractStatefulEventStreamVertex
import dev.azide.core.internal.event_stream.registerSubscriberOnline

class ExecutedEachEventStreamVertex<EventT>(
    private val sourceVertex: EventStreamVertex<Action<EventT>>,
) : AbstractStatefulEventStreamVertex<EventT>(), LiveEventStreamVertex.BasicSubscriber<Action<EventT>>, RestartableEffectVertex {
    private var upstreamSubscriberHandle: EventStreamVertex.SubscriberHandle? = null

    private var executedActionRevocationHandle: RevocationHandle? = null

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
                    ?: throw AssertionError("There's no record of the revoked action")

                executedActionRevocationHandle.revoke()

                exposeAndPropagateEmission(
                    propagationContext = propagationContext,
                    emission = null,
                )
            }

            else -> {
                this.executedActionRevocationHandle?.revoke()

                val emittedAction: Action<EventT> = emission.emittedEvent

                val (emittedEvent: EventT, revocationHandle) = emittedAction.executeInternallyWrappedUp(
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

    override fun transit() {
        this.executedActionRevocationHandle = null
    }

    override fun start(
        propagationContext: Transactions.PropagationContext,
    ) {
        if (upstreamSubscriberHandle != null) {
            throw AssertionError("Vertex seems to be already active")
        }

        upstreamSubscriberHandle = sourceVertex.registerSubscriberOnline(
            propagationContext = propagationContext,
            subscriber = this,
        )

        sourceVertex.ongoingEmission?.let { sourceOngoingEmission ->
            val emittedAction: Action<EventT> = sourceOngoingEmission.emittedEvent

            val (emittedEvent: EventT, revocationHandle) = emittedAction.executeInternallyWrappedUp(
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

    override fun stop(
        propagationContext: Transactions.PropagationContext,
    ) {
        this.executedActionRevocationHandle?.revoke()
        this.executedActionRevocationHandle = null

        if (ongoingEmission != null) {
            exposeAndPropagateEmission(
                propagationContext = propagationContext,
                emission = null,
            )
        }

        val upstreamSubscriberHandle =
            this.upstreamSubscriberHandle ?: throw AssertionError("Vertex seems to be already stopped")

        this.upstreamSubscriberHandle = null

        sourceVertex.unregisterSubscriber(
            handle = upstreamSubscriberHandle,
        )
    }
}
