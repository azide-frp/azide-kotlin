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
private fun <ElementT> ListChange.Part<ElementT>.filter(
    filterMask: CountingBooleanList,
    previousPartLastIndexExclusive: Int,
    accumulatedShift: Int,
    predicate: (ElementT) -> Boolean,
): Pair<ListChange.Part<ElementT>?, Int> {
    // TODO: Optimize this
    val baseShift = filterMask.count(
        firstIndexInclusive = previousPartLastIndexExclusive,
        lastIndexExclusive = firstIndexInclusive,
        element = false,
    )

    // TODO: Optimize this
    val extraShift = filterMask.count(
        firstIndexInclusive = firstIndexInclusive,
        lastIndexExclusive = lastIndexExclusive,
        element = false,
    )

    val totalShift = baseShift + extraShift

    val shiftedFirstIndexInclusive = firstIndexInclusive - (accumulatedShift + baseShift)
    val shiftedLastIndexExclusive = lastIndexExclusive - (accumulatedShift + totalShift)

    return Pair(
        ListChange.Part.of(
            firstIndexInclusive = shiftedFirstIndexInclusive,
            lastIndexExclusive = shiftedLastIndexExclusive,
            newElements = newElements.filter(predicate),
        ),
        totalShift,
    )
}

/**
 * Filters this [ListChange] based on the given [predicate], adjusting indices accordingly.
 */
fun <ElementT> ListChange<ElementT>.filter(
    filterMask: CountingBooleanList,
    predicate: (ElementT) -> Boolean,
): ListChange<ElementT>? {
    var accumulatedShift = 0
    var previousPartLastIndexExclusive = 0

    val resultParts = mutableListOf<ListChange.Part<ElementT>>()

    parts.forEach { part ->
        val (filteredPart, totalPartShift) = part.filter(
            filterMask = filterMask,
            previousPartLastIndexExclusive = previousPartLastIndexExclusive,
            accumulatedShift = accumulatedShift,
            predicate = predicate,
        )

        if (filteredPart != null) {
            resultParts.add(filteredPart)
        }

        accumulatedShift += totalPartShift
        previousPartLastIndexExclusive = part.lastIndexExclusive
    }

    return ListChange.of(
        parts = resultParts,
    )
}

private fun <ElementT, TransformedElementT> ListChange.Part<ElementT>.map(
    transform: (ElementT) -> TransformedElementT,
): ListChange.Part<TransformedElementT> = ListChange.Part(
    firstIndexInclusive = firstIndexInclusive,
    lastIndexExclusive = lastIndexExclusive,
    newElements = newElements.map(transform),
)

fun <ElementT, TransformedElementT> ListChange<ElementT>.map(
    transform: (ElementT) -> TransformedElementT,
): ListChange<TransformedElementT> = ListChange(
    parts = parts.map { it.map(transform) },
)

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

private typealias CountingBooleanList = List<Boolean>

private fun CountingBooleanList.count(
    firstIndexInclusive: Int,
    lastIndexExclusive: Int,
    element: Boolean,
): Int = subList(
    fromIndex = firstIndexInclusive,
    toIndex = lastIndexExclusive,
).count { it == element }
