package dev.azide.core.impl.collections.reactive_bag.abstract_vertices

import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.collections.reactive_bag.MutableTaggedBag
import dev.azide.core.impl.collections.reactive_bag.TaggedBag
import dev.azide.core.impl.collections.reactive_bag.TaggedBagChange
import dev.azide.core.impl.collections.reactive_bag.applyTo
import dev.azide.core.impl.collections.reactive_collection.abstract_vertices.AbstractTrackedTaggedBagVertex

abstract class AbstractBaseStatefulTrackedTaggedBagVertex<ElementT>(
    initialTaggedElements: MutableTaggedBag<ElementT>,
) : AbstractTrackedTaggedBagVertex<ElementT>() {
    private var _stableTaggedElements: MutableTaggedBag<ElementT> = initialTaggedElements

    final override fun commit(
        ongoingChange: TaggedBagChange<ElementT>?,
    ) {
        ongoingChange?.applyTo(_stableTaggedElements)
    }

    final override fun getOldContentView(
        propagationContext: PropagationContext,
    ): TaggedBag<ElementT> = _stableTaggedElements
}
