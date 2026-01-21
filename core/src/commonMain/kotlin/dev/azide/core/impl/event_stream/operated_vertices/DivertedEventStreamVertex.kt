package dev.azide.core.impl.event_stream.operated_vertices

import dev.azide.core.EventStream
import dev.azide.core.impl.PostProcessableVertex
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex.ActivationMode
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.cell.CellVertex.UpdateObserver
import dev.azide.core.impl.cell.getNewValue
import dev.azide.core.impl.cell.registerUpdateObserver
import dev.azide.core.impl.cell.registerUpdateObserverOffline
import dev.azide.core.impl.event_stream.EventStreamVertex
import dev.azide.core.impl.event_stream.EventStreamVertex.EmissionSubscriber
import dev.azide.core.impl.event_stream.abstract_vertices.AbstractStatelessEventStreamVertex
import dev.azide.core.impl.event_stream.registerEmissionSubscriber
import dev.azide.core.impl.event_stream.registerEmissionSubscriberOffline

class DivertedEventStreamVertex<EventT>(
    private val outerSourceVertex: CellVertex<EventStream<EventT>>,
) : AbstractStatelessEventStreamVertex<EventT>(), PostProcessableVertex, UpdateObserver {
    /**
     * The outer vertex observer handle.
     *
     * If the vertex is inactive: `null`
     *
     * If the vertex is active: A handle to the observer registered in [outerSourceVertex].
     */
    private var upstreamOuterObserverHandle: CellVertex.ObserverHandle? = null

    /**
     * The stable event stream vertex.
     *
     * If the vertex is inactive: `null`
     *
     * If the vertex is active: The current stable event stream vertex.
     */
    private var stableInnerSourceVertex: EventStreamVertex<EventT>? = null

    /**
     * The updated inner event stream vertex.
     *
     * If the vertex is inactive: `null`
     *
     * If the vertex is active:
     * - If the [outerSourceVertex] has an ongoing update: The updated value of [outerSourceVertex].
     * - Otherwise: `null`
     */
    private var updatedInnerSourceVertex: EventStreamVertex<EventT>? = null

    /**
     * The handle to the subscriber registered in the stable inner event stream vertex.
     *
     * If the vertex is inactive: `null`
     *
     * If the vertex is active: a handle registered in [stableInnerSourceVertex]
     */
    private var upstreamStableInnerSubscriberHandle: EventStreamVertex.SubscriberHandle? = null

    private val innerSourceSubscriber = object : EmissionSubscriber {
        /**
         * Handle the emission of the inner event stream.
         */
        override fun handleEmission(
            propagationContext: Transactions.PropagationContext,
        ) {
            val stableInnerSourceVertex = this@DivertedEventStreamVertex.stableInnerSourceVertex
                ?: throw IllegalStateException("Vertex doesn't seem to be active")

            exposeAndPropagateEmission(
                propagationContext = propagationContext,
                emission = stableInnerSourceVertex.ongoingEmission,
            )
        }
    }

    private var isEnqueuedForPostProcessing = false

    /**
     * Handle the update of the outer source vertex.
     */
    override fun handleUpdate(
        propagationContext: Transactions.PropagationContext,
    ) {
        when (val update = outerSourceVertex.ongoingUpdate) {
            null -> { // The outer source vertex update is revoked
                // Forget the previous updated inner vertex

                this.updatedInnerSourceVertex = null
            }

            else -> { // The outer source vertex has a proper update (potentially a correction)
                ensureEnqueuedForPostProcessing(
                    propagationContext = propagationContext,
                )

                val handledUpdatedInnerSourceEventStream: EventStream<EventT> = update.updatedValue

                val handledUpdatedInnerSourceVertex = handledUpdatedInnerSourceEventStream.vertex

                // Store link to the updated inner source vertex

                this.updatedInnerSourceVertex = handledUpdatedInnerSourceVertex
            }
        }
    }

    override fun activateOnline(
        propagationContext: Transactions.PropagationContext,
    ): EventStreamVertex.Emission<EventT>? {
        if (upstreamOuterObserverHandle != null || stableInnerSourceVertex != null || updatedInnerSourceVertex != null || upstreamStableInnerSubscriberHandle != null) {
            throw IllegalStateException("Vertex seems to be already active")
        }

        // Register the outer observer

        this.upstreamOuterObserverHandle = outerSourceVertex.registerUpdateObserver(
            propagationContext = propagationContext,
            observer = this,
            mode = ActivationMode.Online,
        )

        // Resolve the stable / updated inner source event streams

        val stableInnerSourceEventStream: EventStream<EventT> = outerSourceVertex.getOldValue(
            propagationContext = propagationContext,
        )

        val stableInnerSourceVertex = stableInnerSourceEventStream.vertex

        val updatedInnerSourceEventStream: EventStream<EventT>? = outerSourceVertex.ongoingUpdate?.updatedValue

        if (updatedInnerSourceEventStream != null) {
            // As the vertex is activated online (when the propagation is still ongoing), we can't subscribe to the
            // updated inner source event stream for both correctness and performance reasons. Instead, we enqueue
            // for post-processing to update our inner subscription.
            //
            // We can't use the commitment phase for that, as activation is impossible in the commitment phase (in the
            // commitment phase we can't access the old / new state of other vertices, which is required for activating
            // higher order vertices).
            ensureEnqueuedForPostProcessing(
                propagationContext = propagationContext,
            )
        }

        val updatedInnerSourceVertex = updatedInnerSourceEventStream?.vertex

        // Store the links to the stable / updated source inner vertices

        this.stableInnerSourceVertex = stableInnerSourceVertex
        this.updatedInnerSourceVertex = updatedInnerSourceVertex

        // Register the inner source vertex subscriber (to the stable inner source vertex)

        this.upstreamStableInnerSubscriberHandle = stableInnerSourceVertex.registerEmissionSubscriber(
            propagationContext = propagationContext,
            subscriber = innerSourceSubscriber,
            mode = ActivationMode.Online,
        )

        return stableInnerSourceVertex.ongoingEmission
    }

    override fun activateOffline(
        propagationContext: Transactions.PropagationContext,
    ) {
        if (upstreamOuterObserverHandle != null || stableInnerSourceVertex != null || updatedInnerSourceVertex != null || upstreamStableInnerSubscriberHandle != null) {
            throw IllegalStateException("Vertex seems to be already active")
        }

        // Register the outer observer

        this.upstreamOuterObserverHandle = outerSourceVertex.registerUpdateObserverOffline(
            propagationContext = propagationContext,
            observer = this,
        )

        // Resolve the new inner source event streams

        val newInnerSourceEventStream: EventStream<EventT> = outerSourceVertex.getNewValue(
            propagationContext = propagationContext,
        )

        val newInnerSourceVertex = newInnerSourceEventStream.vertex

        // Store the link to the new source inner vertex
        // During the offline activation, we can save the _new_ source vertex as the stable vertex, as no more
        // propagation is going to take place. We don't need to enqueue for further commitment.

        this.stableInnerSourceVertex = newInnerSourceVertex

        // Register the inner source vertex subscriber (to the new inner source vertex)

        this.upstreamStableInnerSubscriberHandle = newInnerSourceVertex.registerEmissionSubscriberOffline(
            propagationContext = propagationContext,
            subscriber = innerSourceSubscriber,
        )
    }

    override fun deactivate() {
        val upstreamOuterObserverHandle = this.upstreamOuterObserverHandle
        val stableInnerSourceVertex = this.stableInnerSourceVertex
        val upstreamStableInnerSubscriberHandle = this.upstreamStableInnerSubscriberHandle

        if (upstreamOuterObserverHandle == null || stableInnerSourceVertex == null) {
            throw IllegalStateException("Vertex doesn't seem to be active")
        }

        // Unregister the outer source vertex observer

        outerSourceVertex.unregisterObserver(
            handle = upstreamOuterObserverHandle,
        )

        this.upstreamOuterObserverHandle = null

        // Unregister the inner source vertex subscriber

        if (upstreamStableInnerSubscriberHandle != null) {
            stableInnerSourceVertex.unregisterSubscriber(
                handle = upstreamStableInnerSubscriberHandle,
            )
        }

        this.stableInnerSourceVertex = null
        this.updatedInnerSourceVertex = null
        this.upstreamStableInnerSubscriberHandle = null
    }

    override fun postProcess(
        propagationContext: Transactions.PropagationContext,
    ) {
        val stableInnerSourceVertex = this.stableInnerSourceVertex ?: run {
            // The vertex doesn't seem to be active. It might have been deactivated before the post-processing phase.
            // Assume the vertex is inactive and abort.
            return
        }

        // From now on, assume the vertex is active.

        val updatedInnerSourceVertex = this.updatedInnerSourceVertex ?: run {
            // There's no stored updated inner source vertex. The outer source cell update might've been revoked.
            return
        }

        val upstreamStableInnerSubscriberHandle =
            upstreamStableInnerSubscriberHandle ?: throw AssertionError("Vertex doesn't seem to be active")

        stableInnerSourceVertex.unregisterSubscriber(
            handle = upstreamStableInnerSubscriberHandle,
        )

        // In the post-processing phase, the offline activation mode has to be utilized
        val newInnerSubscriberHandle = updatedInnerSourceVertex.registerEmissionSubscriberOffline(
            propagationContext = propagationContext,
            subscriber = innerSourceSubscriber,
        )

        this.stableInnerSourceVertex = updatedInnerSourceVertex
        this.upstreamStableInnerSubscriberHandle = newInnerSubscriberHandle
        this.updatedInnerSourceVertex = null
    }

    private fun ensureEnqueuedForPostProcessing(
        propagationContext: Transactions.PropagationContext,
    ) {
        if (!isEnqueuedForPostProcessing) {
            propagationContext.enqueueForPostProcessing(this)

            isEnqueuedForPostProcessing = true
        }
    }
}
