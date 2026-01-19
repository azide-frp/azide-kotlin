package dev.azide.core.impl.collections.reactive_set.operated_vertices.helpers

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.collections.reactive_set.ReactiveSetVertex
import dev.azide.core.impl.collections.reactive_set.WarmReactiveSetVertex
import dev.azide.core.impl.collections.reactive_set.abstract_vertices.AbstractReactiveSetProxyCellVertex

class ReactiveSetContainsCellVertex<ElementT>(
    sourceVertex: WarmReactiveSetVertex<ElementT>,
    private val element: ElementT,
) : AbstractReactiveSetProxyCellVertex<ElementT, Boolean>(
    sourceVertex = sourceVertex,
) {
    override fun buildUpdate(
        propagationContext: Transactions.PropagationContext,
        sourceChange: ReactiveSetVertex.SetChange<ElementT>,
    ): CellVertex.Update<Boolean>? = when {
        sourceChange.addedElements.contains(element) -> CellVertex.Update(
            updatedValue = true,
        )

        sourceChange.removedElements.contains(element) -> CellVertex.Update(
            updatedValue = false,
        )

        else -> null
    }

    override fun computeOldValue(
        oldContentView: Set<ElementT>,
    ): Boolean = oldContentView.contains(element)
}
