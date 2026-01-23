package dev.azide.core.impl.collections.reactive_list

import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex.CollectionChange

data class ListChange<out ElementT>(
    val parts: List<Part<ElementT>>,
) : CollectionChange<ElementT> {
    data class Part<out ElementT>(
        val firstIndexInclusive: Int,
        val lastIndexExclusive: Int,
        val newElements: List<ElementT>,
    ) {
        companion object {
            fun <ElementT> of(
                firstIndexInclusive: Int,
                lastIndexExclusive: Int,
                newElements: List<ElementT>,
            ): Part<ElementT>? = when {
                firstIndexInclusive < lastIndexExclusive || newElements.isNotEmpty() -> Part(
                    firstIndexInclusive = firstIndexInclusive,
                    lastIndexExclusive = lastIndexExclusive,
                    newElements = newElements,
                )

                else -> null
            }
        }

        init {
            require(firstIndexInclusive <= lastIndexExclusive) {
                "lastIndexExclusive must be greater than or equal to firstIndexInclusive."
            }

            require(newElements.isNotEmpty() || firstIndexInclusive < lastIndexExclusive) {
                "A Part must have at least one new element or represent a removal."
            }
        }

        val sizeDelta: Int
            get() = newElements.size - (lastIndexExclusive - firstIndexInclusive)
    }

    companion object {
        fun <ElementT> of(
            parts: List<Part<ElementT>>,
        ): ListChange<ElementT>? = when {
            parts.isEmpty() -> null

            else -> ListChange(
                parts = parts,
            )
        }
    }

    init {
        require(parts.isNotEmpty()) {
            "A ListChange must have at least one part."
        }
    }

    override val sizeDelta: Int
        get() = parts.sumOf { it.sizeDelta }

    override val addedElements: List<ElementT>
        get() = parts.flatMap { it.newElements }

    override val removedElements: List<ElementT>
        get() = TODO()
}

/**
 * Filters this [ListChange.Part] based on the given [predicate], adjusting indices accordingly.
 *
 * @return A pair containing the filtered [ListChange.Part] (or null if no elements remain) and the extra shift in
 * indices introduced by the filtering of this part.
 */
fun <ElementT> ListChange.Part<ElementT>.filter(
    oldContentView: List<ElementT>,
    shift: Int,
    predicate: (ElementT) -> Boolean,
): Pair<ListChange.Part<ElementT>?, Int> {
    val shiftedFirstIndexInclusive = firstIndexInclusive - shift
    val shiftedLastIndexExclusive = lastIndexExclusive - shift

    val removedElements: List<ElementT> = oldContentView.subList(
        fromIndex = shiftedFirstIndexInclusive,
        toIndex = shiftedLastIndexExclusive,
    )

    val extraShift = removedElements.count { !predicate(it) }

    return Pair(
        ListChange.Part.of(
            firstIndexInclusive = shiftedFirstIndexInclusive,
            lastIndexExclusive = shiftedLastIndexExclusive - extraShift,
            newElements = newElements.filter(predicate)
        ),
        extraShift,
    )
}

/**
 * Filters this [ListChange] based on the given [predicate], adjusting indices accordingly.
 */
fun <ElementT> ListChange<ElementT>.filter(
    oldContentView: List<ElementT>,
    predicate: (ElementT) -> Boolean,
): ListChange<ElementT>? {
    var accumulatedShift = 0

    val resultParts = mutableListOf<ListChange.Part<ElementT>>()

    parts.forEach { part ->
        val (filteredPart, extraShift) = part.filter(
            oldContentView = oldContentView,
            shift = accumulatedShift,
            predicate = predicate,
        )

        if (filteredPart != null) {
            resultParts.add(filteredPart)
        }

        accumulatedShift += extraShift
    }

    return ListChange.of(
        parts = resultParts,
    )
}

fun <ElementT> ListChange.Part<ElementT>.applyTo(
    mutableList: MutableList<ElementT>,
) {
    val sizeDelta = this.sizeDelta

    when {
        sizeDelta > 0 -> {
            val replacementSize = newElements.size - sizeDelta

            for (i in 0 until replacementSize) {
                mutableList[firstIndexInclusive + i] = newElements[i]
            }

            mutableList.addAll(
                index = firstIndexInclusive + replacementSize,
                elements = newElements.subList(replacementSize, newElements.size),
            )
        }

        sizeDelta == 0 -> {
            for (i in 0 until newElements.size) {
                mutableList[firstIndexInclusive + i] = newElements[i]
            }
        }

        else -> {
            val removalSize = -sizeDelta

            for (i in 0 until newElements.size) {
                mutableList[firstIndexInclusive + i] = newElements[i]
            }

            repeat(removalSize) {
                mutableList.removeAt(firstIndexInclusive)
            }
        }
    }
}

fun <ElementT> ListChange<ElementT>.applyTo(
    mutableList: MutableList<ElementT>,
) {
    parts.asReversed().forEach { part ->
        part.applyTo(mutableList = mutableList)
    }
}
