package dev.azide.core.impl.collections.reactive_list.abstract_vertices

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.collections.reactive_bag.MutableTaggedBag
import dev.azide.core.impl.collections.reactive_bag.TaggedBag
import dev.azide.core.impl.collections.reactive_bag.TaggedBagChange
import dev.azide.core.impl.collections.reactive_collection.abstract_vertices.AbstractTrackedListVertex
import dev.azide.core.impl.collections.reactive_collection.abstract_vertices.AbstractTrackedTaggedBagVertex
import dev.azide.core.impl.collections.reactive_list.ListChange
import dev.azide.core.impl.collections.reactive_list.applyTo

abstract class AbstractStatefulTrackedListVertex<ElementT>(
    initialElements: MutableList<ElementT>,
) : AbstractTrackedListVertex<ElementT>() {
    private var _stableElements: MutableList<ElementT> = initialElements

    final override fun commit(
        ongoingChange: ListChange<ElementT>?,
    ) {
        ongoingChange?.applyTo(
            mutableList = _stableElements,
        )
    }

    final override fun getOldContentView(
        propagationContext: Transactions.PropagationContext,
    ): List<ElementT> = _stableElements
}
