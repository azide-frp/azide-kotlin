package dev.azide.core.impl.effects

import dev.azide.core.Action
import dev.azide.core.EventStream
import dev.azide.core.executeInternallyWrappedUp
import dev.azide.core.impl.Revocable
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.event_stream.EventStreamVertex
import dev.azide.core.impl.event_stream.EventStreamVertex.Emission
import dev.azide.core.impl.event_stream.LiveEventStreamVertex.BasicSubscriber
import dev.azide.core.impl.event_stream.abstract_vertices.AbstractStatefulEventStreamVertex
import dev.azide.core.impl.event_stream.registerSubscriberOnline

class ExecutedEachEventStreamEffectVertex<EventT> private constructor(
    wrapUpContext: Transactions.WrapUpContext,
    initialSourceEventStream: EventStream<Action<EventT>>,
) : AbstractStatefulEventStreamVertex<EventT>(), BasicSubscriber<Action<EventT>>, EffectVertex, Revocable {
    companion object {
        fun <EventT> startInternally(
            wrapUpContext: Transactions.WrapUpContext,
            sourceEventStream: EventStream<Action<EventT>>,
        ): ExecutedEachEventStreamEffectVertex<EventT> = ExecutedEachEventStreamEffectVertex(
            wrapUpContext = wrapUpContext,
            initialSourceEventStream = sourceEventStream,
        )
    }

    private var sourceEventStream: EventStream<Action<EventT>>? = initialSourceEventStream

    private var upstreamSubscriberHandle: EventStreamVertex.SubscriberHandle? = null

    private var executedActionRevocable: Revocable? = null

    /**
     * Handle the emission of the source action event stream vertex.
     */
    override fun handleEmission(
        propagationContext: PropagationContext,
        emission: Emission<Action<EventT>>?,
    ) {
        when (emission) {
            null -> {
                val executedActionRevocable =
                    this.executedActionRevocable ?: throw AssertionError("There's no record of the revoked action")

                executedActionRevocable.revoke()
                this.executedActionRevocable = null

                exposeAndPropagateEmission(
                    propagationContext = propagationContext,
                    emission = null,
                )
            }

            else -> {
                this.executedActionRevocable?.revoke()

                val emittedAction: Action<EventT> = emission.emittedEvent

                val (emittedEvent: EventT, executedActionRevocable) = emittedAction.executeInternallyWrappedUp(
                    propagationContext = propagationContext,
                )

                this.executedActionRevocable = executedActionRevocable

                exposeAndPropagateEmission(
                    propagationContext = propagationContext,
                    emission = Emission(
                        emittedEvent = emittedEvent,
                    ),
                )
            }
        }
    }

    override fun cancelInternally(
        propagationContext: PropagationContext,
    ): Revocable {
        val sourceEventStream = this@ExecutedEachEventStreamEffectVertex.sourceEventStream
            ?: throw IllegalStateException("Cannot cancel a revoked ExecutedEachEventStreamEffectVertex")

        val upstreamSubscriberHandle = this@ExecutedEachEventStreamEffectVertex.upstreamSubscriberHandle
            ?: throw IllegalStateException("It seems as if the vertex wasn't wrapped up properly or is already cancelled")

        this@ExecutedEachEventStreamEffectVertex.upstreamSubscriberHandle = null

        sourceEventStream.vertex.unregisterSubscriber(
            handle = upstreamSubscriberHandle,
        )

        val executedActionRevocable = this@ExecutedEachEventStreamEffectVertex.executedActionRevocable

        if (executedActionRevocable != null) {
            executedActionRevocable.revoke()

            this@ExecutedEachEventStreamEffectVertex.executedActionRevocable = null

            // Revoke the event we emitted earlier
            exposeAndPropagateEmission(
                propagationContext = propagationContext,
                emission = null,
            )
        }

        return object : Revocable {
            override fun revoke() {
                // We have to fetch the source event stream again, to ensure that effect's start wasn't revoked
                // in the meantime
                val sourceEventStream = this@ExecutedEachEventStreamEffectVertex.sourceEventStream ?: return
                val sourceVertex = sourceEventStream.vertex

                // Re-attach to the source event stream
                attach(
                    propagationContext = propagationContext,
                    sourceVertex = sourceVertex,
                    shouldPropagate = true,
                )
            }
        }
    }

    override fun revoke() {
        this.executedActionRevocable?.revoke()
        this.executedActionRevocable = null

        val sourceEventStream = this@ExecutedEachEventStreamEffectVertex.sourceEventStream
            ?: throw IllegalStateException("Cannot cancel an already revoked ExecutedEachEventStreamEffectVertex")

        this@ExecutedEachEventStreamEffectVertex.sourceEventStream = null

        val upstreamSubscriberHandle = this@ExecutedEachEventStreamEffectVertex.upstreamSubscriberHandle

        when {
            upstreamSubscriberHandle != null -> {
                sourceEventStream.vertex.unregisterSubscriber(
                    handle = upstreamSubscriberHandle,
                )
            }

            else -> {
                // The effect's start was revoked after it was quick-cancelled. There's nothing to do.
            }
        }
    }

    override fun transit() {
        this.executedActionRevocable = null
    }

    private fun attach(
        sourceVertex: EventStreamVertex<Action<EventT>>,
        propagationContext: PropagationContext,
        shouldPropagate: Boolean,
    ) {
        upstreamSubscriberHandle = sourceVertex.registerSubscriberOnline(
            propagationContext = propagationContext,
            subscriber = this,
        )

        sourceVertex.ongoingEmission?.let { sourceOngoingEmission ->
            val emittedAction: Action<EventT> = sourceOngoingEmission.emittedEvent

            val (event: EventT, executedActionRevocable: Revocable) = emittedAction.executeInternallyWrappedUp(
                propagationContext = propagationContext,
            )

            this.executedActionRevocable = executedActionRevocable

            when {
                shouldPropagate -> {
                    exposeAndPropagateEmission(
                        propagationContext = propagationContext,
                        emission = Emission(
                            emittedEvent = event,
                        ),
                    )
                }

                else -> {
                    exposeEmission(
                        propagationContext = propagationContext,
                        emission = Emission(
                            emittedEvent = event,
                        ),
                    )
                }
            }
        }
    }

    init {
        wrapUpContext.enqueueForWrapUp { propagationContext ->
            val sourceEventStream = sourceEventStream
                ?: throw IllegalStateException("ExecutedEachEventStreamEffectVertex was revoked before being wrapped-up")

            if (hasSubscribers) {
                // TODO: Figure out the actual contract
//                throw IllegalStateException("Event stream vertex should not have observers during wrap-up")
            }

            val sourceVertex = sourceEventStream.vertex

            attach(
                propagationContext = propagationContext,
                sourceVertex = sourceVertex,
                shouldPropagate = false, // TODO: Should we?...
            )
        }
    }
}
