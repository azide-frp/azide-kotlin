package dev.azide.core.impl.collections.reactive_set.operated_vertices

import dev.azide.core.impl.collections.reactive_collection.TrackedSetVertex
import dev.azide.core.impl.collections.reactive_collection.abstract_vertices.AbstractAlteringTrackedSetVertex
import dev.azide.core.impl.collections.reactive_set.SetChange
import dev.azide.core.impl.collections.reactive_set.utils.LazyFilteredSet

class FilteredTrackedSetVertex<ElementT>(
    override val sourceVertex: TrackedSetVertex<ElementT>,
    private val predicate: (ElementT) -> Boolean,
) : AbstractAlteringTrackedSetVertex<ElementT>() {
    override fun transformOldContentView(
        oldContentView: Set<ElementT>,
    ): Set<ElementT> = LazyFilteredSet(
        sourceSet = oldContentView,
        predicate = predicate,
    )

    override fun transformChange(
        change: SetChange<ElementT>,
    ): SetChange<ElementT>? = change.filter(predicate)
}
