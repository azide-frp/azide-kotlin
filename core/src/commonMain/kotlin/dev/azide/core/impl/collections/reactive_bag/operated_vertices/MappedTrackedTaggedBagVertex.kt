package dev.azide.core.impl.collections.reactive_bag.operated_vertices

import dev.azide.core.impl.collections.reactive_bag.TaggedBag
import dev.azide.core.impl.collections.reactive_bag.TaggedBagChange
import dev.azide.core.impl.collections.reactive_bag.map
import dev.azide.core.impl.collections.reactive_bag.utils.LazyMappedTaggedBag
import dev.azide.core.impl.collections.reactive_collection.TrackedTaggedBagVertex
import dev.azide.core.impl.collections.reactive_collection.abstract_vertices.AbstractTransformativeTrackedTaggedBagVertex

class MappedTrackedTaggedBagVertex<ElementT, TransformedElementT>(
    override val sourceVertex: TrackedTaggedBagVertex<ElementT>,
    private val transform: (ElementT) -> TransformedElementT,
) : AbstractTransformativeTrackedTaggedBagVertex<ElementT, TransformedElementT>() {
    override fun transformOldContentView(
        oldContentView: TaggedBag<ElementT>,
    ): TaggedBag<TransformedElementT> = LazyMappedTaggedBag(
        sourceTaggedBag = oldContentView,
        transform = transform,
    )

    override fun transformChange(
        change: TaggedBagChange<ElementT>,
    ): TaggedBagChange<TransformedElementT> = change.map(transform)
}
