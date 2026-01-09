package dev.azide.core.impl.collections.reactive_collection

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.cell.CellVertex

interface ReactiveCollectionVertex<out ElementT> : Vertex {
    interface CollectionChange<out ElementT> {
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
