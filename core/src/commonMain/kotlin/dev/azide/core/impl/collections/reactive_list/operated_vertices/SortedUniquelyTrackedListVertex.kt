package dev.azide.core.impl.collections.reactive_list.operated_vertices

import dev.azide.core.collections.helpers.SortableValue
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.collections.reactive_collection.TrackedCollectionVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedTaggedBagVertex
import dev.azide.core.impl.collections.reactive_collection.abstract_vertices.AbstractStatelessTrackedListVertex
import dev.azide.core.impl.collections.reactive_list.ListChange
import dev.azide.core.impl.collections.reactive_list.map
import dev.azide.core.impl.collections.reactive_list.utils.LazyMappedList

class SortedUniquelyTrackedListVertex<ElementT, SortKeyT : Comparable<SortKeyT>>(
    private val sourceVertex: TrackedCollectionVertex<SortableValue<ElementT, SortKeyT>>,
) : AbstractStatelessTrackedListVertex<ElementT>() {
    override fun activate(
        propagationContext: Transactions.PropagationContext,
        mode: Vertex.ActivationMode,
    ): ListChange<ElementT>? {
        TODO("Not yet implemented")
    }

    override fun deactivate() {
        TODO("Not yet implemented")
    }

    override fun getOldContentView(
        propagationContext: Transactions.PropagationContext,
    ): List<ElementT> {
        val sourceContentView = sourceVertex.getOldContentView(
            propagationContext = propagationContext,
        )

        return sourceContentView.sortedBy { it.sortKey }.map { it.value }
    }
}
