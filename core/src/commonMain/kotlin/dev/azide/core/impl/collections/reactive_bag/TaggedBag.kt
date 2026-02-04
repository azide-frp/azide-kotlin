package dev.azide.core.impl.collections.reactive_bag

import dev.azide.core.collections.ReactiveBag.Tag

interface TaggedBag<out ElementT> : Collection<ElementT> {
    companion object {
        fun <ElementT> ofTaggedContent(
            elementByTag: Map<Tag, ElementT>,
        ): TaggedBag<ElementT> = TaggedBagImpl(
            elementByTag = elementByTag.toMutableMap(),
        )
    }

    fun containsTag(tag: Tag): Boolean
}

interface MutableTaggedBag<ElementT> : TaggedBag<ElementT>, MutableCollection<ElementT> {
    companion object {
        fun <ElementT> ofTaggedContent(
            elementByTag: Map<Tag, ElementT>,
        ): MutableTaggedBag<ElementT> = TaggedBagImpl(
            elementByTag = elementByTag.toMutableMap(),
        )
    }

    fun addByTag(
        tag: Tag,
        element: ElementT,
    ): ElementT?

    fun removeByTag(
        tag: Tag,
    ): ElementT?
}

data class TaggedBagImpl<ElementT>(
    val elementByTag: MutableMap<Tag, ElementT>,
) : AbstractMutableCollection<ElementT>(), MutableTaggedBag<ElementT> {
    override fun addByTag(
        tag: Tag,
        element: ElementT,
    ): ElementT? = elementByTag.put(tag, element)

    override fun removeByTag(
        tag: Tag,
    ): ElementT? = elementByTag.remove(tag)

    override val size: Int
        get() = elementByTag.size

    override fun iterator(): MutableIterator<ElementT> = elementByTag.values.iterator()

    override fun add(
        element: ElementT,
    ): Boolean {
        addByTag(
            tag = object {},
            element = element,
        )

        return true
    }

    override fun containsTag(
        tag: Tag,
    ): Boolean = elementByTag.containsKey(tag)
}
