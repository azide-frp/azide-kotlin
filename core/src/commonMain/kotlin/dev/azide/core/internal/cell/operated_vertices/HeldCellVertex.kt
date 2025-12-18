package dev.azide.core.internal.cell.operated_vertices

import dev.azide.core.internal.Transactions
import dev.azide.core.internal.cell.CellVertex
import dev.azide.core.internal.cell.abstract_vertices.AbstractStatefulCellVertex
import dev.azide.core.internal.event_stream.EventStreamVertex
import dev.azide.core.internal.event_stream.LiveEventStreamVertex
import dev.azide.core.internal.event_stream.registerLooseSubscriber

class HeldCellVertex<ValueT>(
    propagationContext: Transactions.PropagationContext,
    sourceVertex: LiveEventStreamVertex<ValueT>,
    initialValue: ValueT,
) : AbstractStatefulCellVertex<ValueT>(
    initialValue = initialValue,
), LiveEventStreamVertex.BasicSubscriber<ValueT> {
    companion object {
        fun <ValueT> buildUpdate(
            sourceEmission: EventStreamVertex.Emission<ValueT>,
        ): CellVertex.Update<ValueT> = CellVertex.Update(
            updatedValue = sourceEmission.emittedEvent,
        )
    }

    /**
     * Handle the source event stream vertex emission.
     */
    override fun handleEmission(
        propagationContext: Transactions.PropagationContext,
        emission: EventStreamVertex.Emission<ValueT>?,
    ) {
        exposeAndPropagateUpdate(
            propagationContext = propagationContext,
            update = when (emission) {
                null -> null
                else -> buildUpdate(sourceEmission = emission)
            },
        )
    }

    init {
        sourceVertex.registerLooseSubscriber(
            propagationContext = propagationContext,
            dependentVertex = this,
            subscriber = this,
        )

        sourceVertex.ongoingEmission?.let { sourceOngoingEmission ->
            exposeUpdate(
                propagationContext = propagationContext,
                update = buildUpdate(sourceEmission = sourceOngoingEmission),
            )
        }
    }
}
