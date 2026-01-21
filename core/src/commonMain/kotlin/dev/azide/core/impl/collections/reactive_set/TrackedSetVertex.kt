package dev.azide.core.impl.collections.reactive_set

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedCollectionVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedCollectionVertex.CollectionChange

sealed interface TrackedSetVertex<out ElementT> : TrackedCollectionVertex<ElementT> {
    data class SetChange<out ElementT>(
        override val addedElements: Set<ElementT>,
        override val removedElements: Set<ElementT>,
    ) : CollectionChange<ElementT> {
        companion object {
            fun <ElementT> of(
                addedElements: Set<ElementT>,
                removedElements: Set<ElementT>,
            ): SetChange<ElementT>? = when {
                addedElements.isEmpty() && removedElements.isEmpty() -> null

                else -> SetChange(
                    addedElements = addedElements,
                    removedElements = removedElements,
                )
            }
        }

        init {
            require(addedElements.isNotEmpty() || removedElements.isNotEmpty()) {
                "A SetChange must have at least one added or removed element."
            }
        }

        fun filter(
            predicate: (ElementT) -> Boolean,
        ): SetChange<ElementT>? = of(
            addedElements = addedElements.filterTo(mutableSetOf(), predicate),
            removedElements = removedElements.filterTo(mutableSetOf(), predicate),
        )

        val sizeDelta: Int
            get() = addedElements.size - removedElements.size
    }

    typealias SetObserver<ElementT> = TrackedCollectionVertex.GenericCollectionObserver<SetChange<ElementT>>

    interface SetObserverHandle : TrackedCollectionVertex.CollectionObserverHandle

    fun registerSetObserver(
        propagationContext: Transactions.PropagationContext,
        observer: SetObserver<ElementT>,
    ): SetObserverHandle

    fun unregisterSetObserver(
        handle: SetObserverHandle,
    )

    override val ongoingChange: SetChange<ElementT>?

    fun buildContainsVertex(
        element: @UnsafeVariance ElementT,
    ): CellVertex<Boolean>

    override fun getOldContentView(
        propagationContext: Transactions.PropagationContext,
    ): Set<ElementT>
}
