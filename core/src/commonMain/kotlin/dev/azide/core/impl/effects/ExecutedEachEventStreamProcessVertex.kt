package dev.azide.core.impl.effects

import dev.azide.core.Action
import dev.azide.core.EventStream
import dev.azide.core.executeInternallyWrappedUpUnpacked
import dev.azide.core.impl.Revocable
import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.Vertex.BoundListener
import dev.azide.core.impl.Vertex.ListenerHandle
import dev.azide.core.impl.event_stream.EventStreamVertex.Emission
import dev.azide.core.impl.event_stream.abstract_vertices.AbstractLiveEventStreamVertex
import dev.azide.core.impl.event_stream.registerBoundListenerOnline

class ExecutedEachEventStreamProcessVertex<EventT>(
    private val sourceEventStream: EventStream<Action<EventT>>,
) : AbstractLiveEventStreamVertex<EventT>(), BoundListener, SymmetricProcessVertex {
    private var upstreamListenerHandle: ListenerHandle? = null

    private var executedActionRevocable: Revocable? = null

    /**
     * Handle the emission of the source action event stream vertex.
     */
    override fun handle(
        propagationContext: PropagationContext,
    ) {
        val sourceEventStream = this@ExecutedEachEventStreamProcessVertex.sourceEventStream

        when (val emission = sourceEventStream.vertex.ongoingEmission) {
            null -> {
                val executedActionRevocable =
                    this.executedActionRevocable ?: throw AssertionError("There's no record of the revoked action")

                executedActionRevocable.revoke()
                this.executedActionRevocable = null

                exposeEmissionNotifyingListeners(
                    propagationContext = propagationContext,
                    emission = null,
                )
            }

            else -> {
                this.executedActionRevocable?.revoke()

                val emittedAction: Action<EventT> = emission.emittedEvent

                val (emittedEvent: EventT, executedActionRevocable) = emittedAction.executeInternallyWrappedUpUnpacked(
                    propagationContext = propagationContext,
                )

                this.executedActionRevocable = executedActionRevocable

                exposeEmissionNotifyingListeners(
                    propagationContext = propagationContext,
                    emission = Emission(
                        emittedEvent = emittedEvent,
                    ),
                )
            }
        }
    }

    override fun resumeInternally(
        propagationContext: PropagationContext,
    ) {
        val sourceEventStream = this@ExecutedEachEventStreamProcessVertex.sourceEventStream

        val sourceVertex = sourceEventStream.vertex

        // Attach to the source event stream
        this@ExecutedEachEventStreamProcessVertex.upstreamListenerHandle =
            sourceVertex.registerBoundListenerOnline(
                propagationContext = propagationContext,
                listener = this@ExecutedEachEventStreamProcessVertex,
            )

        sourceVertex.ongoingEmission?.let { sourceOngoingActionEmission ->
            val emittedAction: Action<EventT> = sourceOngoingActionEmission.emittedEvent

            val (event: EventT, executedActionRevocable: Revocable) = emittedAction.executeInternallyWrappedUpUnpacked(
                propagationContext = propagationContext,
            )

            this.executedActionRevocable = executedActionRevocable

            exposeEmissionNotifyingListeners(
                propagationContext = propagationContext,
                emission = Emission(
                    emittedEvent = event,
                )
            )
        }
    }

    override fun pauseInternally(
        propagationContext: PropagationContext,
    ) {
        val sourceEventStream = this@ExecutedEachEventStreamProcessVertex.sourceEventStream

        val upstreamListenerHandle = this@ExecutedEachEventStreamProcessVertex.upstreamListenerHandle
            ?: throw IllegalStateException("It seems as if the vertex wasn't wrapped up properly or is already cancelled")

        this@ExecutedEachEventStreamProcessVertex.upstreamListenerHandle = null

        sourceEventStream.vertex.unregisterListener(
            handle = upstreamListenerHandle,
        )

        val executedActionRevocable = this@ExecutedEachEventStreamProcessVertex.executedActionRevocable

        if (executedActionRevocable != null) {
            executedActionRevocable.revoke()

            this@ExecutedEachEventStreamProcessVertex.executedActionRevocable = null

            // Revoke the event we emitted earlier
            exposeEmissionNotifyingListeners(
                propagationContext = propagationContext,
                emission = null,
            )
        }

    }

    override fun transit() {
        this.executedActionRevocable = null
    }
}
