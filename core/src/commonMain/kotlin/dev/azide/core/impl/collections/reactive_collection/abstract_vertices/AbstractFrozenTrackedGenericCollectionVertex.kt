package dev.azide.core.impl.collections.reactive_collection.abstract_vertices

import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex.Listener
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex.ListenerHandle
import dev.azide.core.impl.collections.reactive_collection.FrozenTrackedGenericCollectionVertex

abstract class AbstractFrozenTrackedGenericCollectionVertex<ContentT : Collection<*>> :
    FrozenTrackedGenericCollectionVertex<ContentT> {
    private object NoopListenerHandle : ListenerHandle

    override fun registerListener(
        propagationContext: PropagationContext,
        listener: Listener,
    ): ListenerHandle = NoopListenerHandle

    final override fun unregisterListener(
        handle: ListenerHandle,
    ) {
    }

    final override fun buildSizeVertex(): CellVertex<Int> {
        TODO()
    }

    final override val ongoingChange: Nothing?
        get() = null
}

typealias AbstractFrozenTrackedSetVertex<ElementT> = AbstractFrozenTrackedGenericCollectionVertex<Set<ElementT>>
