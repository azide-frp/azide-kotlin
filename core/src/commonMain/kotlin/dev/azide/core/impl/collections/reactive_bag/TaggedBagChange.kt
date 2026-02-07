package dev.azide.core.impl.collections.reactive_bag

import dev.azide.core.collections.ReactiveBag.Tag
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex.CollectionChange

data class TaggedBagChange<out ElementT>(
    val changedElementByTag: Map<Tag, ElementT>,
    val removedTags: Set<Tag>,
) : CollectionChange<ElementT> {
    init {
        require(changedElementByTag.isNotEmpty() || removedTags.isNotEmpty()) {
            "A TaggedBagChange must have at least one changed or removed element."
        }
    }

    override val addedElements: Collection<ElementT>
        get() = TODO("Not yet implemented")

    override val removedElements: Collection<ElementT>
        get() = TODO("Not yet implemented")

    fun filter(
        predicate: (ElementT) -> Boolean,
    ): TaggedBagChange<ElementT>? = TODO()

    override val sizeDelta: Int
        get() = addedElements.size - removedElements.size
}

fun <ElementT> TaggedBagChange<ElementT>.applyTo(
    mutableTaggedBag: MutableTaggedBag<ElementT>,
) {
    for (removedTag: Tag in this.removedTags) {
        mutableTaggedBag.removeByTag(removedTag)
    }

    for ((tag: Tag, changedElement: ElementT) in this.changedElementByTag) {
        mutableTaggedBag.addByTag(
            tag = tag,
            element = changedElement,
        )
    }
}
