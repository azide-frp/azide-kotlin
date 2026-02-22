package dev.azide.core.impl.collections.reactive_bag.utils

import dev.azide.core.collections.ReactiveBag
import dev.azide.core.impl.collections.reactive_bag.TaggedBag

class LazyMappedTaggedBag<ElementT, TransformedElementT>(
    private val sourceTaggedBag: TaggedBag<ElementT>,
    private val transform: (ElementT) -> TransformedElementT,
) : AbstractTaggedBag<TransformedElementT>() {
    override val size: Int
        get() = sourceTaggedBag.size

    override fun iterator(): Iterator<TransformedElementT> {
        TODO("Not yet implemented")
    }

    override val elementByTag: Map<ReactiveBag.Tag, TransformedElementT>
        get() = sourceTaggedBag.elementByTag.mapValues { transform(it.value) }

    override fun getByTag(tag: ReactiveBag.Tag): TransformedElementT? {
        return sourceTaggedBag.getByTag(tag)?.let(transform)
    }

    override fun containsTag(tag: ReactiveBag.Tag): Boolean = sourceTaggedBag.containsTag(tag)

}

abstract class AbstractTaggedBag<ElementT> : AbstractCollection<ElementT>(), TaggedBag<ElementT> {

}
