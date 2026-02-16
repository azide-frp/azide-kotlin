package dev.azide.core.test_utils.stimulation_combinatorics

import kotlin.random.Random

/**
 * Generates all interleavings (order‑preserving shuffles) of two lists.
 */
class InterleavingsGenerator<T>(
    val firstList: List<T>,
    val secondList: List<T>,
) {
    private val m = firstList.size
    private val n = secondList.size

    /**
     * Counts the number of interleavings of the two lists.
     * This is the binomial coefficient C(m + n, m) = C(m + n, n).
     */
    fun count(): Int {
        return binomialCoefficient(m + n, m).toInt()
    }

    /**
     * Generates the [interleavingIndex]-th interleaving (0-indexed) in lexicographic order.
     */
    fun generateSingle(interleavingIndex: Int): List<T>? {
        require(interleavingIndex >= 0) { "interleavingIndex must be >= 0" }

        if (interleavingIndex >= count()) return null

        return generateNthInterleaving(interleavingIndex)
    }

    fun generateRandom(random: Random): List<T> {
        val totalCount = count()
        val randomIndex = random.nextInt(totalCount)

        return generateNthInterleaving(index = randomIndex)
    }

    /**
     * Generates all interleavings of the two lists.
     */
    fun generateAll(): Sequence<List<T>> = sequence {
        val currentInterleaving = ArrayList<T>(m + n)

        suspend fun SequenceScope<List<T>>.recurse(i: Int, j: Int) {
            if (i == m && j == n) {
                yield(currentInterleaving.toList())
                return
            }

            if (i < m) {
                currentInterleaving.add(firstList[i])
                recurse(i = i + 1, j = j)
                currentInterleaving.removeLast()
            }

            if (j < n) {
                currentInterleaving.add(secondList[j])
                recurse(i = i, j = j + 1)
                currentInterleaving.removeLast()
            }
        }

        recurse(i = 0, j = 0)
    }

    /**
     * Generates the n-th interleaving directly without generating all previous ones.
     * Uses the fact that interleavings correspond to binary sequences of length m+n
     * with exactly m ones (or n zeros).
     */
    private fun generateNthInterleaving(index: Int): List<T> {
        val result = ArrayList<T>(m + n)
        var remaining = index.toLong()
        var i = 0 // Current position in firstList
        var j = 0 // Current position in secondList

        while (i < m || j < n) {
            if (i == m) {
                // All elements from firstList used, append remaining from secondList
                result.add(secondList[j])
                j++
            } else if (j == n) {
                // All elements from secondList used, append remaining from firstList
                result.add(firstList[i])
                i++
            } else {
                // Both lists have remaining elements
                // Count how many interleavings start with firstList[i]
                val countWithFirst = binomialCoefficient(m - i - 1 + n - j, m - i - 1)

                if (remaining < countWithFirst) {
                    // This interleaving starts with firstList[i]
                    result.add(firstList[i])
                    i++
                } else {
                    // This interleaving starts with secondList[j]
                    result.add(secondList[j])
                    j++
                    remaining -= countWithFirst
                }
            }
        }

        return result
    }

    private fun binomialCoefficient(n: Int, k: Int): Long {
        if (k !in 0..n) return 0
        if (k == 0 || k == n) return 1

        val kOptimized = minOf(k, n - k)

        var result = 1L
        for (i in 0 until kOptimized) {
            result = result * (n - i) / (i + 1)
        }

        return result
    }
}
