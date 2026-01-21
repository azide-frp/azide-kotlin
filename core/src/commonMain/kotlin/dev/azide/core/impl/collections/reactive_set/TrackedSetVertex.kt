package dev.azide.core.impl.collections.reactive_set

import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex.CollectionChange
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex.Listener
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex.ListenerHandle

typealias TrackedSetVertex<ElementT> = TrackedGenericCollectionVertex<Set<ElementT>, SetChange<ElementT>>

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


fun <ElementT> TrackedSetVertex<ElementT>.registerSetChangeListener(
    propagationContext: PropagationContext,
    listener: Listener,
): ListenerHandle = registerListener(
    propagationContext = propagationContext,
    listener = object : Listener {
        override fun handle(
            propagationContext: PropagationContext,
        ) {
            listener.handle(
                propagationContext = propagationContext,
            )
        }
    },
)
