package dev.azide.core.impl.collections.reactive_bag

import dev.azide.core.collections.ReactiveBag.Tag

interface TaggedBag<out ElementT> : Collection<ElementT> {
    companion object {
        fun <ElementT> ofTaggedContent(
            elementByTag: Map<Tag, ElementT>,
        ): TaggedBag<ElementT> = TaggedBagImpl(
            initialElementByTag = elementByTag,
        )
    }

    val elementByTag: Map<Tag, ElementT>

    fun getByTag(tag: Tag): ElementT?

    fun containsTag(tag: Tag): Boolean
}

interface MutableTaggedBag<ElementT> : TaggedBag<ElementT>, MutableCollection<ElementT> {
    companion object {
        fun <ElementT> empty(): MutableTaggedBag<ElementT> = TaggedBagImpl(
            initialElementByTag = emptyMap(),
        )

        fun <ElementT> ofTaggedContent(
            elementByTag: Map<Tag, ElementT>,
        ): MutableTaggedBag<ElementT> = TaggedBagImpl(
            initialElementByTag = elementByTag,
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

fun <ElementT> MutableTaggedBag<ElementT>.getByTagOrAdd(
    tag: Tag,
    defaultElement: () -> ElementT,
): ElementT? {
    val element = getByTag(tag = tag)

    if (element == null) {
        val builtElement = defaultElement()

        addByTag(
            tag = tag,
            element = builtElement,
        )

        return builtElement
    } else {
        return element
    }
}

class TaggedBagImpl<ElementT>(
    initialElementByTag: Map<Tag, ElementT>,
) : AbstractMutableCollection<ElementT>(), MutableTaggedBag<ElementT> {
    private val _elementByTag: MutableMap<Tag, ElementT> = initialElementByTag.toMutableMap()

    override val elementByTag: Map<Tag, ElementT>
        get() = _elementByTag

    override fun getByTag(
        tag: Tag,
    ): ElementT? = _elementByTag[tag]

    override fun addByTag(
        tag: Tag,
        element: ElementT,
    ): ElementT? = _elementByTag.put(tag, element)

    override fun removeByTag(
        tag: Tag,
    ): ElementT? = _elementByTag.remove(tag)

    override val size: Int
        get() = elementByTag.size

    override fun iterator(): MutableIterator<ElementT> = _elementByTag.values.iterator()

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
    ): Boolean = _elementByTag.containsKey(tag)
}

fun <ElementT> taggedBagOf(
    vararg taggedElements: Pair<Tag, ElementT>,
): TaggedBag<ElementT> = TaggedBagImpl(
    initialElementByTag = taggedElements.toMap(),
)

fun <ElementT> TaggedBag<ElementT>.toMutableBag(): MutableTaggedBag<ElementT> =
    TaggedBagImpl(initialElementByTag = elementByTag)

inline fun <T, R> TaggedBag<T>.mapKeepingTags(
    transform: (T) -> R,
): TaggedBag<R> = mapToKeepingTags(
    destination = MutableTaggedBag.empty(),
    transform = transform,
)

inline fun <T, R> TaggedBag<T>.mapToKeepingTags(
    destination: MutableTaggedBag<R>,
    transform: (T) -> R,
): MutableTaggedBag<R> {
    for ((tag, element) in elementByTag) destination.addByTag(tag, transform(element))

    return destination
}
