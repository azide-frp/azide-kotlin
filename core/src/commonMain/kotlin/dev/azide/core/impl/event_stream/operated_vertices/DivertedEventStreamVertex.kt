package dev.azide.core.impl.event_stream.operated_vertices

import dev.azide.core.EventStream
import dev.azide.core.impl.PostProcessableVertex
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.ListenableVertex.ActivationMode
import dev.azide.core.impl.ListenableVertex.ListenerHandle
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.cell.getNewValue
import dev.azide.core.impl.enqueueForPostProcessing
import dev.azide.core.impl.event_stream.EventStreamVertex
import dev.azide.core.impl.event_stream.abstract_vertices.AbstractStatelessEventStreamVertex
import dev.azide.core.impl.event_stream.registerBoundListener
import dev.azide.core.impl.event_stream.registerBoundListenerOffline
import dev.azide.core.impl.registerBoundListener
import dev.azide.core.impl.registerBoundListenerOffline
import dev.azide.core.impl.ListenableVertex.BoundListener as BoundCellListener
import dev.azide.core.impl.ListenableVertex.BoundListener as BoundEventStreamListener

class DivertedEventStreamVertex<EventT>(
    private val outerSourceVertex: CellVertex<EventStream<EventT>>,
) : AbstractStatelessEventStreamVertex<EventT>(), PostProcessableVertex, BoundCellListener {
    /**
     * The outer vertex listener handle.
     *
     * If the vertex is inactive: `null`
     *
     * If the vertex is active: A handle to the listener registered in [outerSourceVertex].
     */
    private var upstreamOuterListenerHandle: ListenerHandle? = null

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
     * The handle to the listener registered in the stable inner event stream vertex.
     *
     * If the vertex is inactive: `null`
     *
     * If the vertex is active: a handle registered in [stableInnerSourceVertex]
     */
    private var upstreamStableInnerListenerHandle: ListenerHandle? = null

    private val innerSourceListener = object : BoundEventStreamListener {
        /**
         * Handle the emission of the inner event stream.
         */
        override fun handle(
            propagationContext: Transactions.PropagationContext,
        ) {
            val stableInnerSourceVertex = this@DivertedEventStreamVertex.stableInnerSourceVertex
                ?: throw IllegalStateException("ListenableVertex doesn't seem to be active")

            exposeEmissionNotifyingListeners(
                propagationContext = propagationContext,
                emission = stableInnerSourceVertex.ongoingEmission,
            )
        }
    }

    private var isEnqueuedForPostProcessing = false

    /**
     * Handle the update of the outer source vertex.
     */
    override fun handle(
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
        if (upstreamOuterListenerHandle != null || stableInnerSourceVertex != null || updatedInnerSourceVertex != null || upstreamStableInnerListenerHandle != null) {
            throw IllegalStateException("ListenableVertex seems to be already active")
        }

        // Register the outer listener

        this.upstreamOuterListenerHandle = outerSourceVertex.registerBoundListener(
            propagationContext = propagationContext,
            listener = this,
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

        // Register the inner source vertex listener (to the stable inner source vertex)

        this.upstreamStableInnerListenerHandle = stableInnerSourceVertex.registerBoundListener(
            propagationContext = propagationContext,
            listener = innerSourceListener,
            mode = ActivationMode.Online,
        )

        return stableInnerSourceVertex.ongoingEmission
    }

    override fun activateOffline(
        propagationContext: Transactions.PropagationContext,
    ) {
        if (upstreamOuterListenerHandle != null || stableInnerSourceVertex != null || updatedInnerSourceVertex != null || upstreamStableInnerListenerHandle != null) {
            throw IllegalStateException("ListenableVertex seems to be already active")
        }

        // Register the outer listener

        this.upstreamOuterListenerHandle = outerSourceVertex.registerBoundListenerOffline(
            propagationContext = propagationContext,
            listener = this,
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

        // Register the inner source vertex listener (to the new inner source vertex)

        this.upstreamStableInnerListenerHandle = newInnerSourceVertex.registerBoundListenerOffline(
            propagationContext = propagationContext,
            listener = innerSourceListener,
        )
    }

    override fun deactivate() {
        val upstreamOuterListenerHandle = this.upstreamOuterListenerHandle
        val stableInnerSourceVertex = this.stableInnerSourceVertex
        val upstreamStableInnerListenerHandle = this.upstreamStableInnerListenerHandle

        if (upstreamOuterListenerHandle == null || stableInnerSourceVertex == null) {
            throw IllegalStateException("ListenableVertex doesn't seem to be active")
        }

        // Unregister the outer source vertex listener

        outerSourceVertex.unregisterListener(
            handle = upstreamOuterListenerHandle,
        )

        this.upstreamOuterListenerHandle = null

        // Unregister the inner source vertex listener

        if (upstreamStableInnerListenerHandle != null) {
            stableInnerSourceVertex.unregisterListener(
                handle = upstreamStableInnerListenerHandle,
            )
        }

        this.stableInnerSourceVertex = null
        this.updatedInnerSourceVertex = null
        this.upstreamStableInnerListenerHandle = null
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

        val upstreamStableInnerListenerHandle =
            upstreamStableInnerListenerHandle ?: throw AssertionError("ListenableVertex doesn't seem to be active")

        stableInnerSourceVertex.unregisterListener(
            handle = upstreamStableInnerListenerHandle,
        )

        // In the post-processing phase, the offline activation mode has to be utilized
        val newInnerListenerHandle = updatedInnerSourceVertex.registerBoundListenerOffline(
            propagationContext = propagationContext,
            listener = innerSourceListener,
        )

        this.stableInnerSourceVertex = updatedInnerSourceVertex
        this.upstreamStableInnerListenerHandle = newInnerListenerHandle
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
