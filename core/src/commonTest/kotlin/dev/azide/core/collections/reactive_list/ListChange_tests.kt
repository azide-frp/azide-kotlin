package dev.azide.core.collections.reactive_list

import dev.azide.core.impl.collections.reactive_list.ListChange
import dev.azide.core.impl.collections.reactive_list.applyTo
import dev.azide.core.impl.collections.reactive_list.filter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@Suppress("ClassName")
class ListChange_tests {
    @Test
    fun test_applyTo_toEmpty() {
        val mutableList = mutableListOf<Int>()

        ListChange(
            parts = listOf(
                ListChange.Part(
                    firstIndexInclusive = 0,
                    lastIndexExclusive = 0,
                    newElements = listOf(1, 2, 3, 4),
                )
            ),
        ).applyTo(
            mutableList = mutableList,
        )

        assertEquals(
            expected = listOf(1, 2, 3, 4),
            actual = mutableList,
        )
    }

    @Test
    fun test_applyTo_nonEmpty_addedOnly_singlePart() {
        val mutableList = mutableListOf(0, 10, 20, 30, 40)

        ListChange(
            parts = listOf(
                ListChange.Part(
                    firstIndexInclusive = 3,
                    lastIndexExclusive = 3,
                    newElements = listOf(21, 22),
                )
            ),
        ).applyTo(
            mutableList = mutableList,
        )

        assertEquals(
            expected = listOf(0, 10, 20, 21, 22, 30, 40),
            actual = mutableList,
        )
    }

    @Test
    fun test_applyTo_nonEmpty_addedOnly_multipleParts() {
        val mutableList = mutableListOf(0, 10, 20, 30, 40, 50, 60)

        ListChange(
            parts = listOf(
                ListChange.Part(
                    firstIndexInclusive = 2,
                    lastIndexExclusive = 2,
                    newElements = listOf(11, 12, 13),
                ),
                ListChange.Part(
                    firstIndexInclusive = 4,
                    lastIndexExclusive = 4,
                    newElements = listOf(31, 32),
                ),
                ListChange.Part(
                    firstIndexInclusive = 5,
                    lastIndexExclusive = 5,
                    newElements = listOf(41),
                ),
            ),
        ).applyTo(
            mutableList = mutableList,
        )

        assertEquals(
            expected = listOf(0, 10, 11, 12, 13, 20, 30, 31, 32, 40, 41, 50, 60),
            actual = mutableList,
        )
    }

    @Test
    fun test_applyTo_nonEmpty_removedOnly_singlePart() {
        val mutableList = mutableListOf(0, 10, 20, 30, 40, 50, 60)

        ListChange(
            parts = listOf(
                ListChange.Part(
                    firstIndexInclusive = 2,
                    lastIndexExclusive = 5,
                    newElements = emptyList<Int>(),
                )
            ),
        ).applyTo(
            mutableList = mutableList,
        )

        assertEquals(
            expected = listOf(0, 10, 50, 60),
            actual = mutableList,
        )
    }

    @Test
    fun test_applyTo_nonEmpty_removedOnly_multipleParts() {
        val mutableList = mutableListOf(0, 10, 20, 30, 40, 50, 60, 70, 80)

        ListChange(
            parts = listOf(
                ListChange.Part(
                    firstIndexInclusive = 2,
                    lastIndexExclusive = 4,
                    newElements = emptyList<Int>(),
                ),
                ListChange.Part(
                    firstIndexInclusive = 6,
                    lastIndexExclusive = 7,
                    newElements = emptyList<Int>(),
                ),
            ),
        ).applyTo(
            mutableList = mutableList,
        )

        assertEquals(
            expected = listOf(0, 10, 40, 50, 70, 80),
            actual = mutableList,
        )
    }

    @Test
    fun test_applyTo_nonEmpty_replaced_singlePart() {
        val mutableList = mutableListOf(0, 10, 20, 30, 40, 50, 60)

        ListChange(
            parts = listOf(
                ListChange.Part(
                    firstIndexInclusive = 2,
                    lastIndexExclusive = 5,
                    newElements = listOf(11, 12, 13),
                )
            ),
        ).applyTo(
            mutableList = mutableList,
        )

        assertEquals(
            expected = listOf(0, 10, 11, 12, 13, 50, 60),
            actual = mutableList,
        )
    }

    @Test
    fun test_applyTo_nonEmpty_replaced_multipleParts() {
        val mutableList = mutableListOf(0, 10, 20, 30, 40, 50, 60, 70, 80, 90)

        ListChange(
            parts = listOf(
                ListChange.Part(
                    firstIndexInclusive = 2,
                    lastIndexExclusive = 4,
                    newElements = listOf(11, 12, 13, 14),
                ),
                ListChange.Part(
                    firstIndexInclusive = 6,
                    lastIndexExclusive = 7,
                    newElements = listOf(51),
                ),
            ),
        ).applyTo(
            mutableList = mutableList,
        )

        assertEquals(
            expected = listOf(0, 10, 11, 12, 13, 14, 40, 50, 51, 70, 80, 90),
            actual = mutableList,
        )
    }

    @Test
    fun test_applyTo_nonEmpty_mixed() {
        val mutableList = mutableListOf(0, 10, 20, 30, 40, 50, 60, 70, 80, 90)

        ListChange(
            parts = listOf(
                ListChange.Part(
                    firstIndexInclusive = 2,
                    lastIndexExclusive = 4,
                    newElements = listOf(11, 12),
                ),
                ListChange.Part(
                    firstIndexInclusive = 5,
                    lastIndexExclusive = 7,
                    newElements = emptyList(),
                ),
                ListChange.Part(
                    firstIndexInclusive = 9,
                    lastIndexExclusive = 9,
                    newElements = listOf(81, 82),
                ),
            ),
        ).applyTo(
            mutableList = mutableList,
        )

        assertEquals(
            expected = listOf(0, 10, 11, 12, 40, 70, 80, 81, 82, 90),
            actual = mutableList,
        )
    }
}
