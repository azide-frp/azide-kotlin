package dev.azide.core.impl.event_stream.operated_vertices

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.ListenableVertex.BoundListener
import dev.azide.core.impl.ListenableVertex.ListenerHandle
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.event_stream.EventStreamVertex
import dev.azide.core.impl.event_stream.abstract_vertices.AbstractSimpleStatelessEventStreamVertex
import dev.azide.core.impl.registerBoundListener

class UpdatedValuesEventStreamVertex<ValueT>(
    private val sourceVertex: CellVertex<ValueT>,
) : AbstractSimpleStatelessEventStreamVertex<ValueT>(), BoundListener {
    private var upstreamListenerHandle: ListenerHandle? = null

    /**
     * Handle the emission of the source cell vertex.
     */
    override fun handle(
        propagationContext: Transactions.PropagationContext,
    ) {
        when (val update = sourceVertex.ongoingUpdate) {
            null -> { // Update revocation
                exposeEmissionNotifyingListeners(
                    propagationContext = propagationContext,
                    emission = null,
                )
            }

            else -> { // Initial update or correction
                exposeEmissionNotifyingListeners(
                    propagationContext = propagationContext,
                    emission = EventStreamVertex.Emission(
                        emittedEvent = update.updatedValue,
                    ),
                )
            }
        }
    }

    override fun activate(
        processingContext: Transactions.ProcessingContext,
    ): EventStreamVertex.Emission<ValueT>? {
        if (upstreamListenerHandle != null) {
            throw IllegalStateException("ListenableVertex seems to be already active")
        }

        upstreamListenerHandle = sourceVertex.registerBoundListener(
            processingContext = processingContext,
            listener = this,
        )

        val sourceOngoingUpdate = sourceVertex.ongoingUpdate

        return sourceOngoingUpdate?.let { update ->
            EventStreamVertex.Emission(
                emittedEvent = update.updatedValue,
            )
        }
    }

    override fun deactivate() {
        val upstreamListenerHandle =
            this.upstreamListenerHandle ?: throw IllegalStateException("ListenableVertex doesn't seem to be active")

        sourceVertex.unregisterListener(
            handle = upstreamListenerHandle,
        )

        this.upstreamListenerHandle = null
    }
}
