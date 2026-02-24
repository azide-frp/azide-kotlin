package dev.azide.core.impl.collections.reactive_bag.operated_vertices

import dev.azide.core.impl.ListenableVertex.BoundListener
import dev.azide.core.impl.collections.reactive_bag.TaggedBag
import dev.azide.core.impl.collections.reactive_bag.TaggedBagChange
import dev.azide.core.impl.collections.reactive_collection.TrackedSetVertex
import dev.azide.core.impl.collections.reactive_collection.abstract_vertices.AbstractAlteringTrackedSetToTaggedBagVertex
import dev.azide.core.impl.collections.reactive_set.SetChange

class FromSetTaggedBagVertex<ElementT>(
    override val sourceVertex: TrackedSetVertex<ElementT>,
) : AbstractAlteringTrackedSetToTaggedBagVertex<ElementT>(), BoundListener {
    override fun transformOldContentView(
        oldContentView: Set<ElementT>,
    ): TaggedBag<ElementT> = TaggedBag.ofTaggedContent(
        elementByTag = oldContentView.associateBy { it },
    )

    override fun transformChange(
        change: SetChange<ElementT>,
    ): TaggedBagChange<ElementT> = TaggedBagChange(
        addedElementByTag = change.addedElements.associateBy { it },
        replacedElementByTag = emptyMap(),
        removedTags = change.removedElements,
    )
}
