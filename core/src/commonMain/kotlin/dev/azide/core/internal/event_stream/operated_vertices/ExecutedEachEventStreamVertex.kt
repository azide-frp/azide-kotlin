package dev.azide.core.internal.event_stream.operated_vertices

import dev.azide.core.Action
import dev.azide.core.executeInternallyWrappedUp
import dev.azide.core.internal.Transactions
import dev.azide.core.internal.event_stream.EventStreamVertex
import dev.azide.core.internal.event_stream.LiveEventStreamVertex
import dev.azide.core.internal.event_stream.abstract_vertices.AbstractStatefulEventStreamVertex

class ExecutedEachEventStreamVertex<EventT> private constructor(
    propagationContext: Transactions.PropagationContext,
    private val sourceVertex: EventStreamVertex<Action<EventT>>,
) : AbstractStatefulEventStreamVertex<EventT>(), LiveEventStreamVertex.BasicSubscriber<Action<EventT>> {
    private enum class LifecycleState {
        /**
         * The initial state, when the vertex is subscribed to its upstream and executes each action.
         */
        Started,

        /**
         * The state when the vertex is not attached to its upstream and is not actively executing actions, but can
         * still be _restarted_ (enter the "started" state again).
         */
        Stopped,

        /**
         * The final state, the vertex is detached from the upstream and not capable of restarting.
         */
        Shutdown,
    }

    companion object {
        fun <EventT> start(
            propagationContext: Transactions.PropagationContext,
            sourceVertex: EventStreamVertex<Action<EventT>>,
        ): ExecutedEachEventStreamVertex<EventT> = ExecutedEachEventStreamVertex(
            propagationContext = propagationContext,
            sourceVertex = sourceVertex,
        )
    }

    private var lifecycleState = LifecycleState.Started

    val isShutdown: Boolean
        get() = lifecycleState == LifecycleState.Shutdown

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

        if (lifecycleState == LifecycleState.Stopped) {
            // If the vertex is stopped during the commitment phase, it implicitly transitions to the shutdown state

            lifecycleState = LifecycleState.Shutdown
        }
    }

    fun stop() {
        if (lifecycleState != LifecycleState.Started) {
            throw IllegalStateException("Vertex is not in the started state (actual state: $lifecycleState)")
        }

        lifecycleState = LifecycleState.Stopped

        detach()
    }

    fun restart(
        propagationContext: Transactions.PropagationContext,
    ) {
        if (lifecycleState != LifecycleState.Stopped) {
            throw IllegalStateException("Vertex is not in the stopped state (actual state: $lifecycleState)")
        }

        lifecycleState = LifecycleState.Started

        attach(
            propagationContext = propagationContext,
        )
    }

    fun shutDown() {
        when (lifecycleState) {
            LifecycleState.Started -> { // The typical case, the vertex is shut down when started
                detach()

                lifecycleState = LifecycleState.Shutdown
            }

            LifecycleState.Stopped -> { // A possible case when the vertex is explicitly shut down after being stopped
                lifecycleState = LifecycleState.Shutdown
            }

            LifecycleState.Shutdown -> {
                throw IllegalStateException("Vertex is already disposed")
            }
        }
    }

    private fun attach(
        propagationContext: Transactions.PropagationContext,
    ) {
        if (upstreamSubscriberHandle != null) {
            throw AssertionError("Vertex seems to be already active")
        }

        upstreamSubscriberHandle = sourceVertex.registerSubscriber(
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

    private fun detach() {
        this.executedActionRevocationHandle?.revoke()
        this.executedActionRevocationHandle = null

        val upstreamSubscriberHandle =
            this.upstreamSubscriberHandle ?: throw AssertionError("Vertex seems to be already stopped")

        this.upstreamSubscriberHandle = null

        sourceVertex.unregisterSubscriber(
            handle = upstreamSubscriberHandle,
        )
    }

    init {
        attach(
            propagationContext = propagationContext,
        )
    }
}
