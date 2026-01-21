package dev.azide.core.impl.collections.reactive_collection

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedCollectionVertex.CollectionChange

interface TrackedCollectionVertex<out ElementT> : Vertex {
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

    interface GenericCollectionObserver<in ChangeT : CollectionChange<*>> {
        fun handleChange(
            propagationContext: Transactions.PropagationContext,
            change: ChangeT?,
        )
    }

    typealias CollectionObserver<ElementT> = GenericCollectionObserver<CollectionChange<ElementT>>

    interface CollectionObserverHandle

    fun registerCollectionObserver(
        propagationContext: Transactions.PropagationContext,
        observer: CollectionObserver<ElementT>,
    ): CollectionObserverHandle

    fun unregisterCollectionObserver(
        handle: CollectionObserverHandle,
    )

    val ongoingChange: CollectionChange<ElementT>?

    fun getOldContentView(
        propagationContext: Transactions.PropagationContext,
    ): Collection<ElementT>

    fun buildSizeVertex(): CellVertex<Int>
}

fun <ElementT, TransformedElementT> CollectionChange<ElementT>.map(
    transform: (ElementT) -> TransformedElementT,
): CollectionChange<TransformedElementT> = CollectionChange.of(
    addedElements = addedElements.map(transform),
    removedElements = removedElements.map(transform),
)

// TODO: Make this an abstract property
val <ElementT> CollectionChange<ElementT>.sizeDelta: Int
    get() = addedElements.size - removedElements.size
