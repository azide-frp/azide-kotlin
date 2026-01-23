package dev.azide.core.impl.collections.reactive_set.operated_vertices.helpers

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedSetVertex
import dev.azide.core.impl.collections.reactive_collection.abstract_vertices.AbstractTrackedSetProxyCellVertex
import dev.azide.core.impl.collections.reactive_set.SetChange

class TrackedSetSizeWarmCellVertex<ElementT>(
    sourceVertex: TrackedSetVertex<ElementT>,
) : AbstractTrackedSetProxyCellVertex<ElementT, Int>(
    sourceVertex = sourceVertex,
) {
    override fun buildUpdate(
        propagationContext: Transactions.PropagationContext,
        sourceChange: SetChange<ElementT>,
    ): CellVertex.Update<Int>? {
        val sizeDelta = sourceChange.sizeDelta

        return when {
            sizeDelta == 0 -> null

            else -> {
                val oldSize = getOldValue(
                    propagationContext = propagationContext,
                )

                val newSize = oldSize + sizeDelta

                CellVertex.Update(
                    updatedValue = newSize,
                )
            }
        }
    }

    override fun computeOldValue(
        oldContentView: Set<ElementT>,
    ): Int = oldContentView.size
}
