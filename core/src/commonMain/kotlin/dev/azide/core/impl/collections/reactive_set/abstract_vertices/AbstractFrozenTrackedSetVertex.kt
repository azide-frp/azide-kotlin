package dev.azide.core.impl.collections.reactive_set.abstract_vertices

import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedCollectionVertex.CollectionObserver
import dev.azide.core.impl.collections.reactive_collection.TrackedCollectionVertex.CollectionObserverHandle
import dev.azide.core.impl.collections.reactive_set.FrozenTrackedSetVertex
import dev.azide.core.impl.collections.reactive_set.TrackedSetVertex.SetObserver
import dev.azide.core.impl.collections.reactive_set.TrackedSetVertex.SetObserverHandle

abstract class AbstractFrozenTrackedSetVertex<ElementT> : FrozenTrackedSetVertex<ElementT> {
    private object NoopSetObserverHandle : SetObserverHandle

    final override fun registerCollectionObserver(
        propagationContext: PropagationContext,
        observer: CollectionObserver<ElementT>,
    ): CollectionObserverHandle = NoopSetObserverHandle

    final override fun unregisterCollectionObserver(
        handle: CollectionObserverHandle,
    ) {
    }

    final override fun registerSetObserver(
        propagationContext: PropagationContext,
        observer: SetObserver<ElementT>,
    ): SetObserverHandle = NoopSetObserverHandle

    final override fun unregisterSetObserver(
        handle: SetObserverHandle,
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
