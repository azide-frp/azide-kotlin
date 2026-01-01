package dev.azide.core.internal.cell.operated_vertices

import dev.azide.core.internal.Transactions
import dev.azide.core.internal.cell.CellVertex
import dev.azide.core.internal.cell.WarmCellVertex
import dev.azide.core.internal.cell.abstract_vertices.AbstractStatefulCellVertex
import dev.azide.core.internal.cell.registerObserverWeakly

class MappedAtCellVertex<ValueT, TransformedValueT> private constructor(
    propagationContext: Transactions.PropagationContext,
    wrapUpContext: Transactions.WrapUpContext,
    sourceVertex: WarmCellVertex<ValueT>,
    private val transform: (Transactions.PropagationContext, ValueT) -> TransformedValueT,
) : AbstractStatefulCellVertex<TransformedValueT>(
    initialValue = transform(
        propagationContext,
        sourceVertex.getOldValue(propagationContext = propagationContext),
    ),
), WarmCellVertex.BasicObserver<ValueT> {
    companion object {
        fun <ValueT, TransformedValueT> start(
            propagationContext: Transactions.PropagationContext,
            wrapUpContext: Transactions.WrapUpContext,
            sourceVertex: WarmCellVertex<ValueT>,
            transform: (Transactions.PropagationContext, ValueT) -> TransformedValueT,
        ): MappedAtCellVertex<ValueT, TransformedValueT> = MappedAtCellVertex(
            propagationContext = propagationContext,
            wrapUpContext = wrapUpContext,
            sourceVertex = sourceVertex,
            transform = transform,
        )
    }

    /**
     * Handle the source cell vertex update.
     */
    override fun handleUpdate(
        propagationContext: Transactions.PropagationContext,
        update: CellVertex.Update<ValueT>?,
    ) {
        exposeAndPropagateUpdate(
            propagationContext = propagationContext,
            update = when (update) {
                null -> null
                else -> CellVertex.Update(
                    updatedValue = transform(
                        propagationContext,
                        update.updatedValue,
                    ),
                )
            },
        )
    }

    init {
        sourceVertex.registerObserverWeakly(
            propagationContext = propagationContext,
            dependentVertex = this,
            observer = this,
        )

        sourceVertex.ongoingUpdate?.let { sourceOngoingUpdate ->
            exposeUpdate(
                propagationContext = propagationContext,
                update = CellVertex.Update(
                    updatedValue = transform(
                        propagationContext,
                        sourceOngoingUpdate.updatedValue,
                    ),
                ),
            )
        }
    }
}
