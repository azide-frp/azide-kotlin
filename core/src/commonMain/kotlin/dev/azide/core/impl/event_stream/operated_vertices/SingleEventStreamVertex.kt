package dev.azide.core.impl.event_stream.operated_vertices

import dev.azide.core.EventStream
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.ListenableVertex.BoundListener
import dev.azide.core.impl.event_stream.EventStreamVertex
import dev.azide.core.impl.event_stream.LiveEventStreamVertex
import dev.azide.core.impl.event_stream.abstract_vertices.AbstractStatefulEventStreamVertex
import dev.azide.core.impl.event_stream.registerEmissionListenerWeakly

class SingleEventStreamVertex<EventT>(
    wrapUpContext: Transactions.WrapUpContext,
    private val sourceEventStream: EventStream<EventT>,
) : AbstractStatefulEventStreamVertex<EventT>(
    wrapUpContext = wrapUpContext,
), BoundListener {
    private val sourceVertex: EventStreamVertex<EventT>
        get() = sourceEventStream.vertex

    private var upstreamWeakListenerHandle: LiveEventStreamVertex.WeakListenerHandle? = null

    /**
     * Handle the emission of the source event stream.
     */
    override fun handle(
        propagationContext: Transactions.PropagationContext,
    ) {
        when (val emission = sourceVertex.ongoingEmission) {
            null -> {
                exposeEmissionNotifyingListeners(
                    propagationContext = propagationContext,
                    emission = null,
                )
            }

            else -> {
                exposeEmissionNotifyingListeners(
                    propagationContext = propagationContext,
                    emission = emission,
                )
            }
        }
    }

    override fun initialize(
        propagationContext: Transactions.PropagationContext,
    ): EventStreamVertex.Emission<EventT>? {

        upstreamWeakListenerHandle = sourceVertex.registerEmissionListenerWeakly(
            processingContext = propagationContext,
            dependentVertex = this,
            listener = this,
        )

        return sourceVertex.ongoingEmission
    }

    override fun transit(
        commitmentContext: Transactions.CommitmentContext,
        ongoingEmission: EventStreamVertex.Emission<EventT>?,
    ) {
        val upstreamWeakListenerHandle = this.upstreamWeakListenerHandle
            ?: throw IllegalStateException("It looks as if the single emission already had place or the vertex wasn't initialized")

        if (ongoingEmission == null) {
            return
        }

        upstreamWeakListenerHandle.cancel()

        this.upstreamWeakListenerHandle = null
    }
}
