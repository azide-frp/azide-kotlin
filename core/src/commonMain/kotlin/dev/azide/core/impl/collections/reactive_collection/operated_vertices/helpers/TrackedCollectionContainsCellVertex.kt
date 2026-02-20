package dev.azide.core.impl.collections.reactive_collection.operated_vertices.helpers

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedCollectionVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex
import dev.azide.core.impl.collections.reactive_collection.abstract_vertices.AbstractTrackedCollectionProxyCellVertex

class TrackedCollectionContainsCellVertex<ElementT>(
    private val sourceVertex: TrackedCollectionVertex<ElementT>,
    private val element: ElementT,
) : AbstractTrackedCollectionProxyCellVertex<ElementT, Boolean>(
    sourceVertex = sourceVertex,
) {
    override fun buildUpdate(
        propagationContext: Transactions.PropagationContext,
        sourceVertex: TrackedCollectionVertex<ElementT>,
        sourceChange: TrackedGenericCollectionVertex.GenericCollectionChange<Collection<ElementT>>
    ): CellVertex.Update<Boolean>? {
        val oldContentView = sourceVertex.getOldContentView(
            propagationContext = propagationContext,
        )

        val abolishedContentView: Collection<ElementT> = sourceChange.getAbolishedContentView(
            oldContentView = oldContentView,
        )

        return when {
            sourceChange.introducedContentView.contains(element) -> CellVertex.Update(
                updatedValue = true,
            )

            abolishedContentView.contains(element) -> CellVertex.Update(
                updatedValue = false,
            )

            else -> null
        }
    }

    override fun computeOldValue(
        oldContentView: Collection<ElementT>,
    ): Boolean = oldContentView.contains(element)
}
