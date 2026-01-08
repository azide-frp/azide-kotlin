package dev.azide.core.impl.cell.operated_vertices

import dev.azide.core.EventStream
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex.ActivationMode
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.cell.abstract_vertices.AbstractStatefulCellVertex
import dev.azide.core.impl.event_stream.EventStreamVertex
import dev.azide.core.impl.event_stream.LiveEventStreamVertex
import dev.azide.core.impl.event_stream.registerSubscriberWeakly

class HeldCellVertex<ValueT> private constructor(
    wrapUpContext: Transactions.WrapUpContext,
    sourceEventStream: EventStream<ValueT>,
    initialValue: ValueT,
) : AbstractStatefulCellVertex<ValueT>(
    initialValue = initialValue,
), LiveEventStreamVertex.BasicSubscriber<ValueT> {
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
    override fun handleEmission(
        propagationContext: Transactions.PropagationContext,
        emission: EventStreamVertex.Emission<ValueT>?,
    ) {
        exposeAndPropagateUpdate(
            propagationContext = propagationContext,
            update = when (emission) {
                null -> null
                else -> CellVertex.Update(
                    updatedValue = emission.emittedEvent,
                )
            },
        )
    }

    init {
        wrapUpContext.enqueueForWrapUp { propagationContext ->
            if (hasObservers) {
                throw IllegalStateException("Cell vertex should not have observers during wrap-up")
            }

            val sourceVertex = sourceEventStream.vertex

            sourceVertex.registerSubscriberWeakly(
                propagationContext = propagationContext,
                dependentVertex = this,
                subscriber = this,
                mode = ActivationMode.Online,
            )

            sourceVertex.ongoingEmission?.let { sourceOngoingEmission ->
                exposeUpdate(
                    propagationContext = propagationContext,
                    update = CellVertex.Update(
                        updatedValue = sourceOngoingEmission.emittedEvent,
                    ),
                )
            }
        }
    }
}
