package dev.azide.core.impl.collections.reactive_set.operated_vertices

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.collections.reactive_set.FrozenReactiveSetVertex
import dev.azide.core.impl.collections.reactive_set.abstract_vertices.AbstractFrozenReactiveSetVertex
import dev.azide.core.impl.collections.reactive_set.utils.LazyFilteredSet

class FilteredFrozenReactiveSetVertex<ElementT>(
    private val sourceVertex: FrozenReactiveSetVertex<ElementT>,
    private val predicate: (ElementT) -> Boolean,
) : AbstractFrozenReactiveSetVertex<ElementT>() {
    override fun getOldContentView(
        propagationContext: Transactions.PropagationContext,
    ): Set<ElementT> {
        val oldContentView = sourceVertex.getOldContentView(
            propagationContext = propagationContext,
        )

        return LazyFilteredSet(
            sourceSet = oldContentView,
            predicate = predicate,
        )
    }
}
