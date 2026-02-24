package dev.azide.core.impl.collections.reactive_collection.abstract_vertices

import dev.azide.core.impl.ListenableVertex.Listener
import dev.azide.core.impl.ListenableVertex.ListenerHandle
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.collections.reactive_collection.FrozenTrackedGenericCollectionVertex

abstract class AbstractFrozenTrackedGenericCollectionVertex<out ContentT : Collection<*>> :
    FrozenTrackedGenericCollectionVertex<ContentT> {
    private object NoopListenerHandle : ListenerHandle

    final override val listenerCount: Int
        get() = 0

    final override fun registerListener(
        processingContext: Transactions.ProcessingContext,
        listener: Listener
    ): ListenerHandle = NoopListenerHandle

    final override fun unregisterListener(
        handle: ListenerHandle,
    ) {
    }

    final override val ongoingChange: Nothing?
        get() = null
}

typealias AbstractFrozenTrackedSetVertex<ElementT> = AbstractFrozenTrackedGenericCollectionVertex<Set<ElementT>>
