package dev.azide.core.impl.collections.reactive_set.abstract_vertices

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.cell.PureCellVertex
import dev.azide.core.impl.collections.reactive_collection.ReactiveCollectionVertex
import dev.azide.core.impl.collections.reactive_set.FrozenReactiveSetVertex
import dev.azide.core.impl.collections.reactive_set.ReactiveSetVertex

abstract class AbstractFrozenReactiveSetVertex<ElementT> : FrozenReactiveSetVertex<ElementT> {
    private object NoopSetObserverHandle : ReactiveSetVertex.SetObserverHandle

    final override fun registerCollectionObserver(
        propagationContext: Transactions.PropagationContext,
        observer: ReactiveCollectionVertex.CollectionObserver<ElementT>,
    ): ReactiveCollectionVertex.CollectionObserverHandle = NoopSetObserverHandle

    final override fun unregisterCollectionObserver(
        handle: ReactiveCollectionVertex.CollectionObserverHandle,
    ) {
    }

    final override fun registerSetObserver(
        propagationContext: Transactions.PropagationContext,
        observer: ReactiveSetVertex.SetObserver<ElementT>,
    ): ReactiveSetVertex.SetObserverHandle {
        return NoopSetObserverHandle
    }

    final override fun unregisterSetObserver(
        handle: ReactiveSetVertex.SetObserverHandle,
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
