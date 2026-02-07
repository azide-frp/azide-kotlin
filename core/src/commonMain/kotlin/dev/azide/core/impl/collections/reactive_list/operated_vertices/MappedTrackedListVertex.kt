package dev.azide.core.impl.collections.reactive_list.operated_vertices

import dev.azide.core.impl.collections.reactive_collection.TrackedListVertex
import dev.azide.core.impl.collections.reactive_collection.abstract_vertices.AbstractTransformativeTrackedListVertex
import dev.azide.core.impl.collections.reactive_list.ListChange
import dev.azide.core.impl.collections.reactive_list.map
import dev.azide.core.impl.collections.reactive_list.utils.LazyMappedList

class MappedTrackedListVertex<ElementT, TransformedElementT>(
    override val sourceVertex: TrackedListVertex<ElementT>,
    private val transform: (ElementT) -> TransformedElementT,
) : AbstractTransformativeTrackedListVertex<ElementT, TransformedElementT>() {
    override fun transformOldContentView(
        oldContentView: List<ElementT>,
    ): List<TransformedElementT> = LazyMappedList(
        sourceList = oldContentView,
        transform = transform,
    )

    override fun transformChange(
        change: ListChange<ElementT>,
    ): ListChange<TransformedElementT> = change.map(transform)
}
