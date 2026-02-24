package dev.azide.core.impl.collections.reactive_collection.operated_vertices

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedCollectionVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex
import dev.azide.core.impl.collections.reactive_collection.abstract_vertices.AbstractTrackedCollectionProxyCellVertex

class TrackedCollectionSumCellVertex(
    sourceVertex: TrackedCollectionVertex<Int>,
) : AbstractTrackedCollectionProxyCellVertex<Int, Int>(
    sourceVertex = sourceVertex,
) {
    override fun buildUpdate(
        propagationContext: Transactions.PropagationContext,
        sourceVertex: TrackedCollectionVertex<Int>,
        sourceChange: TrackedGenericCollectionVertex.GenericCollectionChange<Collection<Int>>
    ): CellVertex.Update<Int>? {
        val oldContentView = sourceVertex.getOldContentView(propagationContext)

        val abolishedContentView = sourceChange.getAbolishedContentView(
            oldContentView = oldContentView,
        )

        val positiveDelta = sourceChange.introducedContentView.sum()
        val negativeDelta = abolishedContentView.sum()

        val totalDelta = positiveDelta - negativeDelta

        return when {
            totalDelta == 0 -> null

            else -> {
                val oldSum = getOldValue(
                    processingContext = propagationContext,
                )

                val newSum = oldSum + totalDelta

                CellVertex.Update(
                    updatedValue = newSum,
                )
            }
        }
    }

    override fun computeOldValue(
        oldContentView: Collection<Int>,
    ): Int = oldContentView.sum()
}
