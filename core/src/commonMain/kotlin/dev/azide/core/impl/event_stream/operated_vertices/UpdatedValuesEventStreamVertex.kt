package dev.azide.core.impl.event_stream.operated_vertices

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.cell.CellVertex.UpdateObserver
import dev.azide.core.impl.cell.WarmCellVertex
import dev.azide.core.impl.cell.registerUpdateObserver
import dev.azide.core.impl.event_stream.EventStreamVertex
import dev.azide.core.impl.event_stream.abstract_vertices.AbstractSimpleStatelessEventStreamVertex

class UpdatedValuesEventStreamVertex<ValueT>(
    private val sourceVertex: CellVertex<ValueT>,
) : AbstractSimpleStatelessEventStreamVertex<ValueT>(), UpdateObserver<ValueT> {
    private var upstreamObserverHandle: CellVertex.ObserverHandle? = null

    /**
     * Handle the emission of the source cell vertex.
     */
    override fun handleUpdate(
        propagationContext: Transactions.PropagationContext,
        update: CellVertex.Update<ValueT>?,
    ) {
        when (update) {
            null -> { // Update revocation
                exposeAndPropagateEmission(
                    propagationContext = propagationContext,
                    emission = null,
                )
            }

            else -> { // Initial update or correction
                exposeAndPropagateEmission(
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
        if (upstreamObserverHandle != null) {
            throw IllegalStateException("Vertex seems to be already active")
        }

        upstreamObserverHandle = sourceVertex.registerUpdateObserver(
            propagationContext = propagationContext,
            observer = this,
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
        val upstreamObserverHandle =
            this.upstreamObserverHandle ?: throw IllegalStateException("Vertex doesn't seem to be active")

        sourceVertex.unregisterObserver(
            handle = upstreamObserverHandle,
        )

        this.upstreamObserverHandle = null
    }
}
