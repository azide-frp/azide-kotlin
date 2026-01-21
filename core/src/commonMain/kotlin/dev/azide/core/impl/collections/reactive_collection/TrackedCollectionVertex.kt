package dev.azide.core.impl.collections.reactive_collection

import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedCollectionVertex.CollectionChange
import dev.azide.core.impl.collections.reactive_collection.TrackedCollectionVertex.CollectionChangeObserver
import dev.azide.core.impl.collections.reactive_collection.TrackedCollectionVertex.CollectionObserverHandle

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

    interface CollectionChangeNotificationObserver {
        fun handleChangeNotification(
            propagationContext: PropagationContext,
        )
    }

    interface GenericCollectionChangeObserver<in ChangeT : CollectionChange<*>> {
        fun handleChange(
            propagationContext: PropagationContext,
            change: ChangeT?,
        )
    }

    typealias CollectionChangeObserver<ElementT> = GenericCollectionChangeObserver<CollectionChange<ElementT>>

    interface CollectionObserverHandle

    fun registerCollectionNotificationObserver(
        propagationContext: PropagationContext,
        observer: CollectionChangeNotificationObserver,
    ): CollectionObserverHandle

    fun unregisterCollectionObserver(
        handle: CollectionObserverHandle,
    )

    val ongoingChange: CollectionChange<ElementT>?

    fun getOldContentView(
        propagationContext: PropagationContext,
    ): Collection<ElementT>

    fun buildSizeVertex(): CellVertex<Int>
}

fun <ElementT> TrackedCollectionVertex<ElementT>.registerCollectionObserver(
    propagationContext: PropagationContext,
    observer: CollectionChangeObserver<ElementT>,
): CollectionObserverHandle = registerCollectionNotificationObserver(
    propagationContext,
    object : TrackedCollectionVertex.CollectionChangeNotificationObserver {
        override fun handleChangeNotification(
            propagationContext: PropagationContext,
        ) {
            observer.handleChange(
                propagationContext = propagationContext,
                change = ongoingChange,
            )
        }
    },
)

fun <ElementT, TransformedElementT> CollectionChange<ElementT>.map(
    transform: (ElementT) -> TransformedElementT,
): CollectionChange<TransformedElementT> = CollectionChange.of(
    addedElements = addedElements.map(transform),
    removedElements = removedElements.map(transform),
)

// TODO: Make this an abstract property
val <ElementT> CollectionChange<ElementT>.sizeDelta: Int
    get() = addedElements.size - removedElements.size
