package dev.azide.core.impl.collections.reactive_set.abstract_vertices

import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex.CollectionChangeNotificationObserver
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex.CollectionObserverHandle
import dev.azide.core.impl.collections.reactive_set.FrozenTrackedSetVertex

abstract class AbstractFrozenTrackedSetVertex<ElementT> : FrozenTrackedSetVertex<ElementT> {
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
