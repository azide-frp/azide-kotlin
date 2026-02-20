package dev.azide.core.impl.collections.reactive_bag

import dev.azide.core.collections.ReactiveBag.Tag
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex.CollectionChange

data class TaggedBagChange<out ElementT>(
    val changedElementByTag: Map<Tag, ElementT>,
    val removedTags: Set<Tag>,
) : CollectionChange<ElementT> {
    companion object {
        fun <ElementT> of(
            changedElementByTag: Map<Tag, ElementT>,
            removedTags: Set<Tag>,
        ): TaggedBagChange<ElementT>? = when {
            changedElementByTag.isEmpty() && removedTags.isEmpty() -> null
            else -> TaggedBagChange(
                changedElementByTag = changedElementByTag,
                removedTags = removedTags,
            )
        }
    }

    init {
        require(changedElementByTag.isNotEmpty() || removedTags.isNotEmpty()) {
            "A TaggedBagChange must have at least one changed or removed element."
        }
    }

    override val addedElements: Collection<ElementT>
        get() = TODO("addedElements")

    override val removedElements: Collection<ElementT>
        get() = TODO("removedElements")

    fun filter(
        predicate: (ElementT) -> Boolean,
    ): TaggedBagChange<ElementT>? = TODO("TaggedBagChange.filter")

    override val sizeDelta: Int
        get() = addedElements.size - removedElements.size
}

fun <ElementT, TransformedElementT> TaggedBagChange<ElementT>.map(
    transform: (ElementT) -> TransformedElementT,
): TaggedBagChange<TransformedElementT> = TaggedBagChange(
    changedElementByTag = changedElementByTag.mapValues { (_, element) -> transform(element) },
    removedTags = removedTags,
)

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
