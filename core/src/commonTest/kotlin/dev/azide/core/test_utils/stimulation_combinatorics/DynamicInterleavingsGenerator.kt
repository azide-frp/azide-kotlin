package dev.azide.core.test_utils.stimulation_combinatorics

import kotlin.random.Random

/**
 * Generates all interleavings (order‑preserving shuffles) of the given lists.
 *
 * An interleaving of k lists is a sequence containing all elements from all lists,
 * where elements from each individual list appear in their original order.
 */
class DynamicInterleavingsGenerator<T>(
    val lists: List<List<T>>,
) {
    private val sizes = lists.map { it.size }
    private val totalSize = sizes.sum()

    /**
     * Counts the number of interleavings of all lists.
     * This is the multinomial coefficient: (n1 + n2 + ... + nk)! / (n1! * n2! * ... * nk!)
     */
    fun count(): Long {
        return multinomialCoefficient(sizes)
    }

    /**
     * Generates the [interleavingIndex]-th interleaving (0-indexed) in lexicographic order.
     */
    fun generateSingle(interleavingIndex: Long): List<T>? {
        require(interleavingIndex >= 0) { "interleavingIndex must be >= 0" }

        if (interleavingIndex >= count()) return null

        return generateNthInterleaving(interleavingIndex)
    }

    /**
     * Generates a random interleaving.
     */
    fun generateRandom(random: Random): List<T> {
        val totalCount = count()
        val randomIndex = random.nextLong(totalCount)

        return generateNthInterleaving(index = randomIndex)
    }

    /**
     * Generates all interleavings of the lists.
     */
    fun generateAll(): Sequence<List<T>> = sequence {
        if (lists.isEmpty()) {
            yield(emptyList())
            return@sequence
        }

        val currentInterleaving = ArrayList<T>(totalSize)
        val indices = IntArray(lists.size) { 0 }

        suspend fun SequenceScope<List<T>>.recurse() {
            // Check if all lists are exhausted
            if (indices.indices.all { indices[it] == sizes[it] }) {
                yield(currentInterleaving.toList())
                return
            }

            // Try taking the next element from each non-exhausted list
            for (listIndex in lists.indices) {
                val currentIndex = indices[listIndex]
                if (currentIndex < sizes[listIndex]) {
                    currentInterleaving.add(lists[listIndex][currentIndex])
                    indices[listIndex]++

                    recurse()

                    indices[listIndex]--
                    currentInterleaving.removeLast()
                }
            }
        }

        recurse()
    }

    /**
     * Generates the n-th interleaving directly without generating all previous ones.
     */
    private fun generateNthInterleaving(index: Long): List<T> {
        val result = ArrayList<T>(totalSize)
        var remaining = index
        val currentIndices = IntArray(lists.size) { 0 }
        val remainingSizes = sizes.toIntArray()

        repeat(totalSize) {
            // Find which list to take the next element from
            for (listIndex in lists.indices) {
                if (remainingSizes[listIndex] == 0) continue

                // Count interleavings if we take from this list
                remainingSizes[listIndex]--
                val countIfTakeFromThis = multinomialCoefficient(remainingSizes.toList())
                remainingSizes[listIndex]++

                if (remaining < countIfTakeFromThis) {
                    // Take from this list
                    result.add(lists[listIndex][currentIndices[listIndex]])
                    currentIndices[listIndex]++
                    remainingSizes[listIndex]--
                    break
                } else {
                    // Skip this choice
                    remaining -= countIfTakeFromThis
                }
            }
        }

        return result
    }

    /**
     * Computes the multinomial coefficient: (sum of sizes)! / (size1! * size2! * ... * sizeK!)
     */
    private fun multinomialCoefficient(sizes: List<Int>): Long {
        val total = sizes.sum()
        if (total == 0) return 1

        var result = 1L
        var numeratorCounter = total

        for (size in sizes) {
            // Multiply by C(numeratorCounter, size)
            result *= binomialCoefficient(numeratorCounter, size)
            numeratorCounter -= size
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
