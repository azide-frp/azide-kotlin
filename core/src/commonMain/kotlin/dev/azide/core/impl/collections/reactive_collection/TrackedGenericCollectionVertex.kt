package dev.azide.core.impl.collections.reactive_collection

import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.collections.reactive_bag.TaggedBag
import dev.azide.core.impl.collections.reactive_bag.TaggedBagChange
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex.CollectionChange
import dev.azide.core.impl.collections.reactive_collection.operated_vertices.helpers.TrackedCollectionContainsCellVertex
import dev.azide.core.impl.collections.reactive_collection.operated_vertices.helpers.TrackedCollectionSizeCellVertex
import dev.azide.core.impl.collections.reactive_list.ListChange
import dev.azide.core.impl.collections.reactive_set.SetChange

interface TrackedGenericCollectionVertex<out ContentT : Collection<*>, out ChangeT : CollectionChange<*>> : Vertex {
    interface CollectionChange<out ElementT> {
        val sizeDelta: Int

        val addedElements: Collection<ElementT>

        val removedElements: Collection<ElementT>
    }

    val ongoingChange: ChangeT?

    fun getOldContentView(
        propagationContext: PropagationContext,
    ): ContentT
}

typealias TrackedCollectionVertex<ElementT> = TrackedGenericCollectionVertex<Collection<ElementT>, CollectionChange<ElementT>>

typealias TrackedSetVertex<ElementT> = TrackedGenericCollectionVertex<Set<ElementT>, SetChange<ElementT>>

typealias TrackedListVertex<ElementT> = TrackedGenericCollectionVertex<List<ElementT>, ListChange<ElementT>>

typealias TrackedTaggedBagVertex<ElementT> = TrackedGenericCollectionVertex<TaggedBag<ElementT>, TaggedBagChange<ElementT>>

fun <ContentT : Collection<*>, ChangeT : CollectionChange<*>> TrackedGenericCollectionVertex<ContentT, ChangeT>.buildSizeVertex(): CellVertex<Int> =
    TrackedCollectionSizeCellVertex(
        sourceVertex = this@buildSizeVertex,
    )

fun <ElementT> TrackedCollectionVertex<ElementT>.buildContainsVertex(
    element: ElementT,
): CellVertex<Boolean> = TrackedCollectionContainsCellVertex(
    sourceVertex = this,
    element = element,
)
