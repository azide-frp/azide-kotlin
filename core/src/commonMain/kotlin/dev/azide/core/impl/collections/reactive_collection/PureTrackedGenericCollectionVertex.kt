package dev.azide.core.impl.collections.reactive_collection

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.collections.reactive_bag.TaggedBag
import dev.azide.core.impl.collections.reactive_collection.abstract_vertices.AbstractFrozenTrackedGenericCollectionVertex

class PureTrackedGenericCollectionVertex<out ContentT: Collection<*>>(
    val elements: ContentT,
) : AbstractFrozenTrackedGenericCollectionVertex<ContentT>() {
    override fun getOldContentView(
        processingContext: Transactions.ProcessingContext,
    ): ContentT = elements
}

typealias PureTrackedSetVertex<ElementT> = PureTrackedGenericCollectionVertex<Set<ElementT>>

typealias PureTrackedTaggedBagVertex<ElementT> = PureTrackedGenericCollectionVertex<TaggedBag<ElementT>>

typealias PureTrackedListVertex<ElementT> = PureTrackedGenericCollectionVertex<List<ElementT>>
