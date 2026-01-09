package dev.azide.core.impl.collections.reactive_set.operated_vertices.helpers

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.collections.reactive_set.ReactiveSetVertex
import dev.azide.core.impl.collections.reactive_set.WarmReactiveSetVertex
import dev.azide.core.impl.collections.reactive_set.abstract_vertices.AbstractReactiveSetProxyCellVertex

class ReactiveSetSizeWarmCellVertex<ElementT>(
    sourceVertex: WarmReactiveSetVertex<ElementT>,
) : AbstractReactiveSetProxyCellVertex<ElementT, Int>(
    sourceVertex = sourceVertex,
) {
    override fun buildUpdate(
        propagationContext: Transactions.PropagationContext,
        sourceChange: ReactiveSetVertex.SetChange<ElementT>,
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
