package dev.azide.core.impl.cell.operated_vertices

import dev.azide.core.EventStream
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.ListenableVertex.BoundListener
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.cell.abstract_vertices.AbstractStatefulCellVertex
import dev.azide.core.impl.event_stream.registerEmissionListenerWeakly

class HeldCellVertex<ValueT> private constructor(
    wrapUpContext: Transactions.WrapUpContext,
    private val sourceEventStream: EventStream<ValueT>,
    initialValue: ValueT,
) : AbstractStatefulCellVertex<ValueT>(
    wrapUpContext = wrapUpContext,
    initialValue = initialValue,
), BoundListener {
    companion object {
        fun <ValueT> start(
            wrapUpContext: Transactions.WrapUpContext,
            sourceEventStream: EventStream<ValueT>,
            initialValue: ValueT,
        ): HeldCellVertex<ValueT> = HeldCellVertex(
            wrapUpContext = wrapUpContext,
            sourceEventStream = sourceEventStream,
            initialValue = initialValue,
        )
    }

    /**
     * Handle the source event stream vertex emission.
     */
    override fun handle(
        propagationContext: Transactions.PropagationContext,
    ) {
        exposeUpdateNotifyingListeners(
            propagationContext = propagationContext,
            update = when (val emission = sourceEventStream.vertex.ongoingEmission) {
                null -> null
                else -> CellVertex.Update(
                    updatedValue = emission.emittedEvent,
                )
            },
        )
    }

    override fun initialize(
        propagationContext: Transactions.PropagationContext,
    ): CellVertex.Update<ValueT>? {
        val sourceVertex = sourceEventStream.vertex

        sourceVertex.registerEmissionListenerWeakly(
            processingContext = propagationContext,
            dependentVertex = this,
            listener = this,
        )

        return sourceVertex.ongoingEmission?.let { sourceOngoingEmission ->
            CellVertex.Update(
                updatedValue = sourceOngoingEmission.emittedEvent,
            )
        }
    }
}
