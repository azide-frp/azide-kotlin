package dev.azide.core.test_utils.collections.reactive_bag

import dev.azide.core.collections.ReactiveBag
import dev.azide.core.impl.collections.reactive_bag.TaggedBagChange

@Suppress("ClassName")
object TaggedBagChange_testUtils {
    /**
     * Compute the [dev.azide.core.impl.collections.reactive_bag.TaggedBagChange] that would transform a tagged bag with [oldTaggedContent] into a tagged bag ith
     * [newTaggedContent], or return `null` if no change would be needed (i.e. if the expected old and new tagged
     * content are equal).
     */
    fun <ElementT> diff(
        oldTaggedContent: Map<ReactiveBag.Tag, ElementT>,
        newTaggedContent: Map<ReactiveBag.Tag, ElementT>,
    ): TaggedBagChange<ElementT>? {
        val addedElementByTag = mutableMapOf<ReactiveBag.Tag, ElementT>()
        val replacedElementByTag = mutableMapOf<ReactiveBag.Tag, ElementT>()
        for ((tag, newElement) in newTaggedContent) {
            val oldElement = oldTaggedContent[tag]
            if (oldElement != newElement) {
                if (tag in oldTaggedContent) {
                    replacedElementByTag[tag] = newElement
                } else {
                    addedElementByTag[tag] = newElement
                }
            }
        }

        val removedTags = oldTaggedContent.keys - newTaggedContent.keys

        if (addedElementByTag.isEmpty() && replacedElementByTag.isEmpty() && removedTags.isEmpty()) {
            return null
        }

        return TaggedBagChange(
            addedElementByTag = addedElementByTag,
            replacedElementByTag = replacedElementByTag,
            removedTags = removedTags,
        )
    }
}