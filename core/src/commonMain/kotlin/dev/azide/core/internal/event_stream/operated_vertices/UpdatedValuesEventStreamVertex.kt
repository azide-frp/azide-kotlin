package dev.azide.core.internal.event_stream.operated_vertices

import dev.azide.core.internal.Transactions
import dev.azide.core.internal.cell.CellVertex
import dev.azide.core.internal.cell.WarmCellVertex
import dev.azide.core.internal.event_stream.EventStreamVertex
import dev.azide.core.internal.event_stream.abstract_vertices.AbstractStatelessEventStreamVertex

class UpdatedValuesEventStreamVertex<ValueT>(
    private val sourceVertex: CellVertex<ValueT>,
) : AbstractStatelessEventStreamVertex<ValueT>(), WarmCellVertex.BasicObserver<ValueT> {
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
    ): EventStreamVertex.Emission<ValueT>? {
        if (upstreamObserverHandle != null) {
            throw IllegalStateException("Vertex seems to be already active")
        }

        upstreamObserverHandle = sourceVertex.registerObserver(
            propagationContext = propagationContext,
            observer = this,
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
