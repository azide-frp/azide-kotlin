package dev.azide.core.collections.reactive_list

import dev.azide.core.impl.collections.reactive_list.ListChange
import dev.azide.core.impl.collections.reactive_list.applyTo
import dev.azide.core.impl.collections.reactive_list.filter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@Suppress("ClassName")
class ListChange_filter_tests {
    // Filter in even numbers, filter out odd numbers
    private fun predicate(n: Int) = n % 2 == 0

    /**
     * Filtered out elements are present in the mask, all new elements are filtered in.
     */
    @Test
    fun test_filter_maskOnly() {
        val originalList = listOf(0, 10, 21, 30, 41, 51, 60, 71, 81, 90)
        val filterMask = originalList.map(::predicate)

        // Filtered list: [0, 10, 30, 60, 90]

        val originalChange = ListChange(
            parts = listOf(
                // Added, after 21: [] -> [22, 24]
                ListChange.Part(
                    firstIndexInclusive = 3,
                    lastIndexExclusive = 3,
                    newElements = listOf(22, 24),
                ),
                // Removed, [51] -> []
                ListChange.Part(
                    firstIndexInclusive = 5,
                    lastIndexExclusive = 6,
                    newElements = emptyList(),
                ),
                // Replaced, [71, 81] -> [72, 74, 76]
                ListChange.Part(
                    firstIndexInclusive = 7,
                    lastIndexExclusive = 9,
                    newElements = listOf(72, 74, 76),
                ),
            ),
        )

        val filteredChange = assertNotNull(
            originalChange.filter(
                filterMask = filterMask,
                predicate = ::predicate,
            ),
        )

        assertEquals(
            expected = ListChange(
                parts = listOf(
                    // Added, after 10: [] -> [22, 24]
                    ListChange.Part(
                        firstIndexInclusive = 2, // 3 -> 2 (-1) [21]
                        lastIndexExclusive = 2, // 3 -> 2 (-1) [21]
                        newElements = listOf(22, 24),
                    ),
                    // Added, after 60: [] -> [72, 74, 76]
                    ListChange.Part(
                        firstIndexInclusive = 4, // 7 -> 4 (-3) [21, 41, 51]
                        lastIndexExclusive = 4, // 9 -> 4 (-5) [21, 41, 51, 71, 81]
                        newElements = listOf(72, 74, 76),
                    ),
                ),
            ),
            actual = filteredChange,
        )

        crossCheck(
            sourceList = originalList,
            originalChange = originalChange,
            predicate = ::predicate,
            filteredChange = filteredChange,
        )
    }

    /**
     * The mask doesn't contain any filtered out elements, all new elements are filtered out.
     */
    @Test
    fun test_filter_newElementsOnly() {
        val originalList = listOf(0, 10, 20, 30, 40, 50, 60, 70, 80, 90)
        val filterMask = originalList.map(::predicate)

        // Filtered list: (unchanged)

        val originalChange = ListChange(
            parts = listOf(
                // Added, after 20: [] -> [21, 23]
                ListChange.Part(
                    firstIndexInclusive = 3,
                    lastIndexExclusive = 3,
                    newElements = listOf(21, 23),
                ),
                // Removed, [50] -> []
                ListChange.Part(
                    firstIndexInclusive = 5,
                    lastIndexExclusive = 6,
                    newElements = emptyList(),
                ),
                // Replaced, [70, 80] -> [71, 73, 75]
                ListChange.Part(
                    firstIndexInclusive = 7,
                    lastIndexExclusive = 9,
                    newElements = listOf(71, 73, 75),
                ),
            ),
        )

        val filteredChange = assertNotNull(
            originalChange.filter(
                filterMask = filterMask,
                predicate = ::predicate,
            ),
        )

        assertEquals(
            expected = ListChange(
                parts = listOf(
                    // Removed, [50] -> []
                    ListChange.Part(
                        firstIndexInclusive = 5,
                        lastIndexExclusive = 6,
                        newElements = emptyList(),
                    ),
                    // Removed, [70, 80] -> []
                    ListChange.Part(
                        firstIndexInclusive = 7,
                        lastIndexExclusive = 9,
                        newElements = emptyList(),
                    ),
                ),
            ),
            actual = filteredChange,
        )

        crossCheck(
            sourceList = originalList,
            originalChange = originalChange,
            predicate = ::predicate,
            filteredChange = filteredChange,
        )
    }

    private fun crossCheck(
        sourceList: List<Int>,
        originalChange: ListChange<Int>,
        predicate: (Int) -> Boolean,
        filteredChange: ListChange<Int>,
    ) {
        assertEquals(
            expected = sourceList.toMutableList().also { mutableOriginalList ->
                originalChange.applyTo(mutableOriginalList)
            }.filter(predicate),
            actual = sourceList.filter(predicate).toMutableList().also { mutableOriginalFilteredList ->
                filteredChange.applyTo(mutableOriginalFilteredList)
            },
            message = "Cross-check failed: applying filtered change to filtered source list did not yield expected result.",
        )
    }
}
