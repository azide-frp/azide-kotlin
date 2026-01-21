package dev.azide.core.impl.collections.reactive_set

import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex.CollectionChange
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex.CollectionObserverHandle
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex.GenericCollectionChangeObserver
import dev.azide.core.impl.collections.reactive_set.TrackedSetVertex.SetChange
import dev.azide.core.impl.collections.reactive_set.TrackedSetVertex.SetChangeObserver
import dev.azide.core.impl.collections.reactive_set.TrackedSetVertex.SetObserverHandle

sealed interface TrackedSetVertex<out ElementT> : TrackedGenericCollectionVertex<Set<ElementT>, SetChange<ElementT>> {
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

    typealias SetChangeObserver<ElementT> = GenericCollectionChangeObserver<SetChange<ElementT>>

    typealias SetObserverHandle = CollectionObserverHandle

    override val ongoingChange: SetChange<ElementT>?

    fun buildContainsVertex(
        element: @UnsafeVariance ElementT,
    ): CellVertex<Boolean>

    override fun getOldContentView(
        propagationContext: PropagationContext,
    ): Set<ElementT>
}

fun <ElementT> TrackedSetVertex<ElementT>.registerSetChangeObserver(
    propagationContext: PropagationContext,
    observer: SetChangeObserver<ElementT>,
): SetObserverHandle = registerCollectionNotificationObserver(
    propagationContext = propagationContext,
    observer = object : TrackedGenericCollectionVertex.CollectionChangeNotificationObserver {
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
