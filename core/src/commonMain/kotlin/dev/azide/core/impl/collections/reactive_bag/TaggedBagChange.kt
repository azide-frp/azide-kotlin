package dev.azide.core.impl.collections.reactive_bag

import dev.azide.core.collections.ReactiveBag.Tag
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex.GenericCollectionChange

data class TaggedBagChange<out ElementT>(
    val changedElementByTag: Map<Tag, ElementT>,
    val removedTags: Set<Tag>,
) : GenericCollectionChange<TaggedBag<ElementT>> {
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

    fun filter(
        predicate: (ElementT) -> Boolean,
    ): TaggedBagChange<ElementT>? = TODO("TaggedBagChange.filter")

    override val sizeDelta: Int
        get() = TODO()

    override val addedContent: TaggedBag<ElementT>
        get() = TODO("Not yet implemented")

    override fun getRemovedContentView(
        oldContentView: TaggedBag<@UnsafeVariance ElementT>,
    ): TaggedBag<ElementT> = TaggedBag.ofTaggedContent(
        elementByTag = removedTags.associateWith { removedTag ->
            oldContentView.getByTag(removedTag)
                ?: throw IllegalStateException("Removed tag $removedTag does not exist in the old content view.")
        },
    )
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
