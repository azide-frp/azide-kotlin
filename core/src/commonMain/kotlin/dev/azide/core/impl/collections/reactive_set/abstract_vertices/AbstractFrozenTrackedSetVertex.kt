package dev.azide.core.impl.collections.reactive_set.abstract_vertices

import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedCollectionVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedCollectionVertex.CollectionObserverHandle
import dev.azide.core.impl.collections.reactive_set.FrozenTrackedSetVertex
import dev.azide.core.impl.collections.reactive_set.TrackedSetVertex.SetChangeObserver
import dev.azide.core.impl.collections.reactive_set.TrackedSetVertex.SetObserverHandle

abstract class AbstractFrozenTrackedSetVertex<ElementT> : FrozenTrackedSetVertex<ElementT> {
    private object NoopSetObserverHandle : SetObserverHandle

    override fun registerCollectionNotificationObserver(
        propagationContext: PropagationContext,
        observer: TrackedCollectionVertex.CollectionChangeNotificationObserver,
    ): CollectionObserverHandle = NoopSetObserverHandle

    final override fun unregisterCollectionObserver(
        handle: CollectionObserverHandle,
    ) {
    }

    final override fun buildContainsVertex(
        element: ElementT,
    ): CellVertex<Boolean> {
        TODO()
    }

    final override fun buildSizeVertex(): CellVertex<Int> {
        TODO()
    }

    final override val ongoingChange: Nothing?
        get() = null
}
