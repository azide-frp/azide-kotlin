package dev.azide.core.impl.collections.reactive_collection.operated_vertices.helpers

import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedCollectionVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex.CollectionChange
import dev.azide.core.impl.collections.reactive_collection.abstract_vertices.AbstractTrackedCollectionProxyCellVertex

class TrackedCollectionSizeCellVertex(
    sourceVertex: TrackedCollectionVertex<*>,
) : AbstractTrackedCollectionProxyCellVertex<Any?, Int>(
    sourceVertex = sourceVertex,
) {
    override fun buildUpdate(
        propagationContext: PropagationContext,
        sourceChange: CollectionChange<*>,
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
        oldContentView: Collection<*>,
    ): Int = oldContentView.size
}
