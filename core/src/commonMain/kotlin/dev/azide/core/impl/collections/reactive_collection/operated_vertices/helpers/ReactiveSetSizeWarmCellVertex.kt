package dev.azide.core.impl.collections.reactive_collection.operated_vertices.helpers

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.collections.reactive_collection.ReactiveCollectionVertex
import dev.azide.core.impl.collections.reactive_collection.abstract_vertices.AbstractReactiveCollectionProxyCellVertex
import dev.azide.core.impl.collections.reactive_collection.sizeDelta

class ReactiveCollectionSizeWarmCellVertex<ElementT>(
    sourceVertex: ReactiveCollectionVertex<ElementT>,
) : AbstractReactiveCollectionProxyCellVertex<ElementT, Int>(
    sourceVertex = sourceVertex,
) {
    override fun buildUpdate(
        propagationContext: Transactions.PropagationContext,
        sourceChange: ReactiveCollectionVertex.CollectionChange<ElementT>,
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
        oldContentView: Collection<ElementT>,
    ): Int = oldContentView.size
}
