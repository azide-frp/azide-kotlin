package dev.azide.core.test_utils.stimulation_combinatorics

/**
 * Generates all ordered [bucketCount]-tuples of contiguous segments whose concatenation is the given [list].
 */
class BucketSplitGenerator<T>(
    val list: List<T>,
    val bucketCount: Int,
) {
    init {
        require(bucketCount >= 1) { "bucketCount must be >= 1" }
    }

    private val m = list.size
    private val k = bucketCount - 1 // number of cuts

    /**
     * Counts the number of ways to split the list into [bucketCount] contiguous segments.
     * Uses the "stars and bars" formula: C(m + k, k) where m = list.size, k = bucketCount - 1
     */
    fun count(): Int {
        return binomialCoefficient(m + k, k).toInt()
    }

    /**
     * Generates the [splitIndex]-th ordered [bucketCount]-tuple of contiguous segments whose concatenation is the
     * given [list].
     */
    fun generateSingle(splitIndex: Int): List<List<T>>? {
        require(splitIndex >= 0) { "splitIndex must be >= 0" }

        if (splitIndex >= count()) return null

        if (bucketCount == 1) {
            return listOf(list)
        }

        // Generate the splitIndex-th combination directly
        val cutPositions = generateNthCutCombination(splitIndex, k, m + 1) ?: return null

        return buildBucketsFromCuts(cutPositions)
    }

    /**
     * Generates all ordered [bucketCount]-tuples of contiguous segments whose concatenation is the given [list].
     */
    fun generateAll(): Sequence<List<List<T>>> = sequence {
        if (bucketCount == 1) {
            yield(listOf(list))
            return@sequence
        }

        // Generate all combinations of k cuts with repetition allowed at positions 0..m
        yieldAll(generateCutCombinations(k, m + 1).map { cutPositions ->
            buildBucketsFromCuts(cutPositions)
        })
    }

    private fun buildBucketsFromCuts(cutPositions: IntArray): List<List<T>> {
        val buckets = mutableListOf<List<T>>()
        var start = 0
        for (cut in cutPositions) {
            buckets.add(list.subList(start, cut))
            start = cut
        }
        buckets.add(list.subList(start, m))
        return buckets
    }

    /**
     * Generates all k-combinations with repetition of positions 0..maxPosition-1 in non-decreasing order.
     */
    private fun generateCutCombinations(k: Int, maxPosition: Int): Sequence<IntArray> = sequence {
        val current = IntArray(k) { 0 }

        while (true) {
            yield(current.copyOf())

            // Find rightmost position that can be incremented
            var i = k - 1
            while (i >= 0 && current[i] == maxPosition - 1) {
                i--
            }

            if (i < 0) break

            // Increment and reset all positions to the right
            current[i]++
            for (j in i + 1 until k) {
                current[j] = current[i]
            }
        }
    }

    /**
     * Generates the n-th k-combination with repetition of positions 0..maxPosition-1 in non-decreasing order.
     */
    private fun generateNthCutCombination(n: Int, k: Int, maxPosition: Int): IntArray? {
        if (n < 0) return null

        val current = IntArray(k) { 0 }
        var currentIndex = 0

        while (currentIndex < n) {
            // Find rightmost position that can be incremented
            var i = k - 1
            while (i >= 0 && current[i] == maxPosition - 1) {
                i--
            }

            if (i < 0) return null // No more combinations

            // Increment and reset all positions to the right
            current[i]++
            for (j in i + 1 until k) {
                current[j] = current[i]
            }

            currentIndex++
        }

        return current
    }
}
