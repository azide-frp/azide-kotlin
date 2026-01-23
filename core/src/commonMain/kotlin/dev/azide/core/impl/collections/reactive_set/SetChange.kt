package dev.azide.core.impl.collections.reactive_set

import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex.CollectionChange

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

    override val sizeDelta: Int
        get() = addedElements.size - removedElements.size
}

fun <ElementT> SetChange<ElementT>.applyTo(
    mutableSet: MutableSet<ElementT>,
) {
    for (removedElement in this.removedElements) {
        mutableSet.remove(removedElement)
    }

    for (addedElement in this.addedElements) {
        mutableSet.add(addedElement)
    }
}
