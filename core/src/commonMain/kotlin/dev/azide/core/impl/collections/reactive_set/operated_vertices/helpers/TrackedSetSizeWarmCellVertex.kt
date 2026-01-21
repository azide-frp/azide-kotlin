package dev.azide.core.impl.collections.reactive_set.operated_vertices.helpers

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.collections.reactive_set.TrackedSetVertex
import dev.azide.core.impl.collections.reactive_set.TrackedSetVertex.SetChange
import dev.azide.core.impl.collections.reactive_set.WarmTrackedSetVertex
import dev.azide.core.impl.collections.reactive_set.abstract_vertices.AbstractTrackedSetProxyCellVertex

class TrackedSetSizeWarmCellVertex<ElementT>(
    sourceVertex: WarmTrackedSetVertex<ElementT>,
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
