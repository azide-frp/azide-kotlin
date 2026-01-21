package dev.azide.core.impl.collections.reactive_collection

import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex.CollectionChange
import dev.azide.core.impl.collections.reactive_collection.operated_vertices.helpers.TrackedCollectionContainsCellVertex

interface TrackedGenericCollectionVertex<out ContentT : Collection<*>, out ChangeT : CollectionChange<*>> : Vertex {
    interface CollectionChange<out ElementT> {
        companion object {
            fun <ElementT> of(
                addedElements: Collection<ElementT>,
                removedElements: Collection<ElementT>,
            ): CollectionChange<ElementT> = object : CollectionChange<ElementT> {
                override val addedElements: Collection<ElementT> = addedElements
                override val removedElements: Collection<ElementT> = removedElements
            }
        }

        val addedElements: Collection<ElementT>
        val removedElements: Collection<ElementT>
    }

    interface Listener {
        fun handle(
            propagationContext: PropagationContext,
        )
    }

    interface ListenerHandle

    fun registerListener(
        propagationContext: PropagationContext,
        listener: Listener,
    ): ListenerHandle

    fun unregisterListener(
        handle: ListenerHandle,
    )

    val ongoingChange: ChangeT?

    fun getOldContentView(
        propagationContext: PropagationContext,
    ): ContentT

    fun buildSizeVertex(): CellVertex<Int>
}

fun <ElementT> TrackedCollectionVertex<ElementT>.buildContainsVertex(
    element: ElementT,
): CellVertex<Boolean> = TrackedCollectionContainsCellVertex(
    sourceVertex = this,
    element = element,
)

typealias TrackedCollectionVertex<ElementT> = TrackedGenericCollectionVertex<Collection<ElementT>, CollectionChange<ElementT>>

fun <ElementT, TransformedElementT> CollectionChange<ElementT>.map(
    transform: (ElementT) -> TransformedElementT,
): CollectionChange<TransformedElementT> = CollectionChange.of(
    addedElements = addedElements.map(transform),
    removedElements = removedElements.map(transform),
)

// TODO: Make this an abstract property
val <ElementT> CollectionChange<ElementT>.sizeDelta: Int
    get() = addedElements.size - removedElements.size
