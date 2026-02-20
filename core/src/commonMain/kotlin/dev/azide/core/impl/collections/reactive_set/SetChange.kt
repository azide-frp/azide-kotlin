package dev.azide.core.impl.collections.reactive_set

import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex.GenericCollectionChange

data class SetChange<out ElementT>(
    val addedElements: Set<ElementT>,
    val removedElements: Set<ElementT>,
) : GenericCollectionChange<Set<ElementT>> {
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
        addedElements = addedElements.filterToSet(predicate),
        removedElements = removedElements.filterToSet(predicate),
    )

    override val sizeDelta: Int
        get() = addedElements.size - removedElements.size

    override val addedContent: Set<ElementT>
        get() = addedElements

    override fun getRemovedContentView(
        oldContentView: Set<@UnsafeVariance ElementT>,
    ): Set<ElementT> = removedElements
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

private fun <ElementT> Iterable<ElementT>.filterToSet(
    predicate: (ElementT) -> Boolean,
): Set<ElementT> = filterTo(mutableSetOf(), predicate)
