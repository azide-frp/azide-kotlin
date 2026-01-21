package dev.azide.core.impl.collections.reactive_collection.abstract_vertices

import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex.CollectionChangeNotificationObserver
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex.CollectionObserverHandle
import dev.azide.core.impl.collections.reactive_collection.FrozenTrackedGenericCollectionVertex

abstract class AbstractFrozenTrackedGenericCollectionVertex<ContentT : Collection<*>> :
    FrozenTrackedGenericCollectionVertex<ContentT> {
    private object NoopCollectionObserverHandle : CollectionObserverHandle

    override fun registerCollectionNotificationObserver(
        propagationContext: PropagationContext,
        observer: CollectionChangeNotificationObserver,
    ): CollectionObserverHandle = NoopCollectionObserverHandle

    final override fun unregisterCollectionObserver(
        handle: CollectionObserverHandle,
    ) {
    }

    final override fun buildSizeVertex(): CellVertex<Int> {
        TODO()
    }

    final override val ongoingChange: Nothing?
        get() = null
}

typealias AbstractFrozenTrackedSetVertex<ElementT> = AbstractFrozenTrackedGenericCollectionVertex<Set<ElementT>>
