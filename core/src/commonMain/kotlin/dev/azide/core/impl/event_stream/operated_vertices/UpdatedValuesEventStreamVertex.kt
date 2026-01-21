package dev.azide.core.impl.event_stream.operated_vertices

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.cell.CellVertex.BoundListener
import dev.azide.core.impl.cell.registerBoundListener
import dev.azide.core.impl.event_stream.EventStreamVertex
import dev.azide.core.impl.event_stream.abstract_vertices.AbstractSimpleStatelessEventStreamVertex

class UpdatedValuesEventStreamVertex<ValueT>(
    private val sourceVertex: CellVertex<ValueT>,
) : AbstractSimpleStatelessEventStreamVertex<ValueT>(), BoundListener {
    private var upstreamListenerHandle: CellVertex.ListenerHandle? = null

    /**
     * Handle the emission of the source cell vertex.
     */
    override fun handleUpdate(
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
        propagationContext: Transactions.PropagationContext,
        mode: Vertex.ActivationMode,
    ): EventStreamVertex.Emission<ValueT>? {
        if (upstreamListenerHandle != null) {
            throw IllegalStateException("Vertex seems to be already active")
        }

        upstreamListenerHandle = sourceVertex.registerBoundListener(
            propagationContext = propagationContext,
            listener = this,
            mode = mode,
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
            this.upstreamListenerHandle ?: throw IllegalStateException("Vertex doesn't seem to be active")

        sourceVertex.unregisterListener(
            handle = upstreamListenerHandle,
        )

        this.upstreamListenerHandle = null
    }
}
