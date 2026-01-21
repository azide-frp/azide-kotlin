package dev.azide.core.impl.collections.reactive_set.operated_vertices.helpers

import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.collections.reactive_set.TrackedSetVertex.SetChange
import dev.azide.core.impl.collections.reactive_set.WarmTrackedSetVertex
import dev.azide.core.impl.collections.reactive_set.abstract_vertices.AbstractTrackedSetProxyCellVertex

class TrackedSetContainsCellVertex<ElementT>(
    sourceVertex: WarmTrackedSetVertex<ElementT>,
    private val element: ElementT,
) : AbstractTrackedSetProxyCellVertex<ElementT, Boolean>(
    sourceVertex = sourceVertex,
) {
    override fun buildUpdate(
        propagationContext: PropagationContext,
        sourceChange: SetChange<ElementT>,
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
