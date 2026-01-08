package dev.azide.core.impl.event_stream.operated_vertices

import dev.azide.core.Action
import dev.azide.core.impl.Revocable
import dev.azide.core.executeInternallyWrappedUp
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.effects.RestartableEffectVertex
import dev.azide.core.impl.event_stream.EventStreamVertex
import dev.azide.core.impl.event_stream.LiveEventStreamVertex
import dev.azide.core.impl.event_stream.abstract_vertices.AbstractStatefulEventStreamVertex
import dev.azide.core.impl.event_stream.registerSubscriberOnline

class ExecutedEachEventStreamVertex<EventT>(
    private val sourceVertex: EventStreamVertex<Action<EventT>>,
) : AbstractStatefulEventStreamVertex<EventT>(), LiveEventStreamVertex.BasicSubscriber<Action<EventT>>, RestartableEffectVertex {
    private var upstreamSubscriberHandle: EventStreamVertex.SubscriberHandle? = null

    private var executedActionRevocable: Revocable? = null

    /**
     * Handle the emission of the source action event stream vertex.
     */
    override fun handleEmission(
        propagationContext: Transactions.PropagationContext,
        emission: EventStreamVertex.Emission<Action<EventT>>?,
    ) {
        when (emission) {
            null -> {
                val executedActionRevocable = this.executedActionRevocable
                    ?: throw AssertionError("There's no record of the revoked action")

                executedActionRevocable.revoke()

                exposeAndPropagateEmission(
                    propagationContext = propagationContext,
                    emission = null,
                )
            }

            else -> {
                this.executedActionRevocable?.revoke()

                val emittedAction: Action<EventT> = emission.emittedEvent

                val (emittedEvent: EventT, revocable) = emittedAction.executeInternallyWrappedUp(
                    propagationContext = propagationContext,
                )

                executedActionRevocable = revocable

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
        this.executedActionRevocable = null
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

            val (emittedEvent: EventT, revocable) = emittedAction.executeInternallyWrappedUp(
                propagationContext = propagationContext,
            )

            executedActionRevocable = revocable

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
        this.executedActionRevocable?.revoke()
        this.executedActionRevocable = null

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
