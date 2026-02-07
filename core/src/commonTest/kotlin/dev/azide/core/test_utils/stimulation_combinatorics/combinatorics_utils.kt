package dev.azide.core.test_utils.stimulation_combinatorics

/**
 * Generates all interleavings (order‑preserving shuffles) of two lists.
 */
fun <T> generateInterleavings(
    firstList: List<T>,
    secondList: List<T>,
): Sequence<List<T>> = sequence {
    val currentInterleaving = ArrayList<T>(firstList.size + secondList.size)

    suspend fun SequenceScope<List<T>>.recurse(i: Int, j: Int) {
        if (i == firstList.size && j == secondList.size) {
            yield(currentInterleaving.toList())

            return
        }

        if (i < firstList.size) {
            currentInterleaving.add(firstList[i])

            recurse(i = i + 1, j = j)

            currentInterleaving.removeLast()
        }

        if (j < secondList.size) {
            currentInterleaving.add(secondList[j])

            recurse(i = i, j = j + 1)

            currentInterleaving.removeLast()
        }
    }

    recurse(i = 0, j = 0)
}

/**
 * Generates all ordered [n]-tuples of contiguous segments whose concatenation is the given [list].
 *
 * [n] - the number of buckets.
 */
fun <T> generateBucketSplits(list: List<T>, n: Int): Sequence<List<List<T>>> = sequence {
    require(n >= 1) { "n must be >= 1" }

    val m = list.size // The number of elements in the list
    val kLimit = n - 1 // The number of cuts (one less than the number of buckets)

    // Nondecreasing indices within the given list (cut identifier [0 until c] -> cut index [0 .. m])
    val cutIndices = IntArray(kLimit)

    /**
     * [k] - the cut identifier for which we'll generate all possible cut indices at this recursion level
     * [minCutIndex] - the minimum cut index allowed for this cut (to ensure nondecreasing order)
     */
    suspend fun SequenceScope<List<List<T>>>.recurse(k: Int, minCutIndex: Int) {
        when (k) { // We're at the bottom of the recursion, a single combination of cut indices is generated
            kLimit -> {
                val buckets: List<List<T>> = cutIndices.asIterable().zipWithNextExtra(
                    extraFirst = 0,
                    extraLast = m,
                ) { i, j ->
                    list.subList(i, j)
                }

                yield(buckets)
            }

            else -> { // Continue generating cut combinations
                for (cutIndex in minCutIndex..m) { // nondecreasing => empties allowed
                    cutIndices[k] = cutIndex

                    recurse(k = k + 1, minCutIndex = cutIndex)
                }
            }
        }
    }

    when (n) {
        1 -> yield(listOf(list))
        else -> recurse(k = 0, minCutIndex = 0)
    }
}

/**
 * Like [zipWithNext], but with extra implicit prepended and appended elements.
 */
private fun <T, R> Iterable<T>.zipWithNextExtra(
    extraFirst: T,
    extraLast: T,
    transform: (a: T, b: T) -> R,
): List<R> {
    val iterator = iterator()

    // Empty iterable case
    if (!iterator.hasNext()) return listOf(transform(extraFirst, extraLast))

    val result = mutableListOf<R>()
    var current = extraFirst

    while (iterator.hasNext()) {
        val next = iterator.next()

        result.add(transform(current, next))

        current = next
    }

    result.add(transform(current, extraLast))

    return result
}
