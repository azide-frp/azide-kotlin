package dev.azide.core.impl.collections.reactive_collection

import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.ListenableVertex
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.collections.reactive_bag.TaggedBag
import dev.azide.core.impl.collections.reactive_bag.TaggedBagChange
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex.GenericCollectionChange
import dev.azide.core.impl.collections.reactive_collection.operated_vertices.helpers.TrackedCollectionContainsCellVertex
import dev.azide.core.impl.collections.reactive_collection.operated_vertices.helpers.TrackedCollectionSizeCellVertex
import dev.azide.core.impl.collections.reactive_list.ListChange
import dev.azide.core.impl.collections.reactive_set.SetChange

interface TrackedGenericCollectionVertex<out ContentT : Collection<*>, out ChangeT : GenericCollectionChange<*>> :
    ListenableVertex {
    interface GenericCollectionChange<out ContentT : Collection<*>> {
        val sizeDelta: Int

        /**
         * A view of the introduced content (i.e. added and replacement elements).
         */
        val introducedContentView: ContentT

        /**
         * A view of the abolished content (i.e. removed and replaced elements).
         */
        fun getAbolishedContentView(
            oldContentView: @UnsafeVariance ContentT,
        ): ContentT
    }

    typealias CollectionChange<ElementT> = GenericCollectionChange<Collection<ElementT>>

    val ongoingChange: ChangeT?

    fun getOldContentView(
        propagationContext: PropagationContext,
    ): ContentT
}

typealias TrackedCollectionVertex<ElementT> = TrackedGenericCollectionVertex<Collection<ElementT>, GenericCollectionChange<Collection<ElementT>>>

typealias TrackedSetVertex<ElementT> = TrackedGenericCollectionVertex<Set<ElementT>, SetChange<ElementT>>

typealias TrackedListVertex<ElementT> = TrackedGenericCollectionVertex<List<ElementT>, ListChange<ElementT>>

typealias TrackedTaggedBagVertex<ElementT> = TrackedGenericCollectionVertex<TaggedBag<ElementT>, TaggedBagChange<ElementT>>

fun TrackedCollectionVertex<*>.buildSizeVertex(): CellVertex<Int> =
    TrackedCollectionSizeCellVertex(
        sourceVertex = this@buildSizeVertex,
    )

fun <ElementT> TrackedCollectionVertex<ElementT>.buildContainsVertex(
    element: ElementT,
): CellVertex<Boolean> = TrackedCollectionContainsCellVertex(
    sourceVertex = this,
    element = element,
)
