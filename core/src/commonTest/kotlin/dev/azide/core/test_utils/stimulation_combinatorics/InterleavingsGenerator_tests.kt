package dev.azide.core.test_utils.stimulation_combinatorics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Suppress("ClassName")
class InterleavingsGenerator_tests {
    @Test
    fun test_bothEmpty() {
        assertGeneratesExpectedInterleavings(
            firstList = emptyList<Int>(),
            secondList = emptyList<Int>(),
            expectedInterleavings = setOf(
                emptyList(),
            ),
        )
    }

    @Test
    fun test_firstEmpty_secondSingle() {
        assertGeneratesExpectedInterleavings(
            firstList = emptyList<Int>(),
            secondList = listOf(1),
            expectedInterleavings = setOf(
                listOf(1),
            ),
        )
    }

    @Test
    fun test_firstSingle_secondEmpty() {
        assertGeneratesExpectedInterleavings(
            firstList = listOf(1),
            secondList = emptyList<Int>(),
            expectedInterleavings = setOf(
                listOf(1),
            ),
        )
    }

    @Test
    fun test_bothSingle() {
        assertGeneratesExpectedInterleavings(
            firstList = listOf('a'),
            secondList = listOf('x'),
            expectedInterleavings = setOf(
                listOf('a', 'x'),
                listOf('x', 'a'),
            ),
        )
    }

    @Test
    fun test_firstTwo_secondOne() {
        assertGeneratesExpectedInterleavings(
            firstList = listOf(1, 2),
            secondList = listOf('x'),
            expectedInterleavings = setOf(
                listOf(1, 2, 'x'),
                listOf(1, 'x', 2),
                listOf('x', 1, 2),
            ),
        )
    }

    @Test
    fun test_firstOne_secondTwo() {
        assertGeneratesExpectedInterleavings(
            firstList = listOf('a'),
            secondList = listOf(1, 2),
            expectedInterleavings = setOf(
                listOf('a', 1, 2),
                listOf(1, 'a', 2),
                listOf(1, 2, 'a'),
            ),
        )
    }

    @Test
    fun test_bothTwo() {
        assertGeneratesExpectedInterleavings(
            firstList = listOf('a', 'b'),
            secondList = listOf(1, 2),
            expectedInterleavings = setOf(
                listOf('a', 'b', 1, 2),
                listOf('a', 1, 'b', 2),
                listOf('a', 1, 2, 'b'),
                listOf(1, 'a', 'b', 2),
                listOf(1, 'a', 2, 'b'),
                listOf(1, 2, 'a', 'b'),
            ),
        )
    }

    @Test
    fun test_firstThree_secondTwo() {
        assertGeneratesExpectedInterleavings(
            firstList = listOf('a', 'b', 'c'),
            secondList = listOf(1, 2),
            expectedInterleavings = setOf(
                listOf('a', 'b', 'c', 1, 2),
                listOf('a', 'b', 1, 'c', 2),
                listOf('a', 'b', 1, 2, 'c'),
                listOf('a', 1, 'b', 'c', 2),
                listOf('a', 1, 'b', 2, 'c'),
                listOf('a', 1, 2, 'b', 'c'),
                listOf(1, 'a', 'b', 'c', 2),
                listOf(1, 'a', 'b', 2, 'c'),
                listOf(1, 'a', 2, 'b', 'c'),
                listOf(1, 2, 'a', 'b', 'c'),
            ),
        )
    }

    @Test
    fun test_firstTwo_secondThree() {
        assertGeneratesExpectedInterleavings(
            firstList = listOf('x', 'y'),
            secondList = listOf(1, 2, 3),
            expectedInterleavings = setOf(
                listOf('x', 'y', 1, 2, 3),
                listOf('x', 1, 'y', 2, 3),
                listOf('x', 1, 2, 'y', 3),
                listOf('x', 1, 2, 3, 'y'),
                listOf(1, 'x', 'y', 2, 3),
                listOf(1, 'x', 2, 'y', 3),
                listOf(1, 'x', 2, 3, 'y'),
                listOf(1, 2, 'x', 'y', 3),
                listOf(1, 2, 'x', 3, 'y'),
                listOf(1, 2, 3, 'x', 'y'),
            ),
        )
    }

    @Test
    fun test_bothThree() {
        // C(6, 3) = 20
        assertGeneratesExpectedInterleavings(
            firstList = listOf('a', 'b', 'c'),
            secondList = listOf(1, 2, 3),
            expectedInterleavingCount = 20,
        )
    }

    @Test
    fun test_generateSingle_invalidIndex() {
        val generator = InterleavingsGenerator(
            firstList = listOf(1, 2),
            secondList = listOf('a', 'b'),
        )

        assertFailsWith<IllegalArgumentException> {
            generator.generateSingle(-1)
        }

        assertNull(generator.generateSingle(generator.count()))
        assertNull(generator.generateSingle(generator.count() + 10))
    }


    @Test
    fun test_fourElements_three() {
        // C(7, 3) = 35
        assertGeneratesExpectedInterleavings(
            firstList = listOf(1, 2, 3, 4),
            secondList = listOf('a', 'b', 'c'),
            expectedInterleavingCount = 35,
        )
    }

    @Test
    fun test_fourElements_four() {
        // C(8, 4) = 70
        assertGeneratesExpectedInterleavings(
            firstList = listOf(1, 2, 3, 4),
            secondList = listOf('a', 'b', 'c', 'd'),
            expectedInterleavingCount = 70,
        )
    }

    @Test
    fun test_fiveElements_three() {
        // C(8, 3) = 56
        assertGeneratesExpectedInterleavings(
            firstList = listOf(1, 2, 3, 4, 5),
            secondList = listOf('a', 'b', 'c'),
            expectedInterleavingCount = 56,
        )
    }

    @Test
    fun test_threeElements_five() {
        // C(8, 5) = 56
        assertGeneratesExpectedInterleavings(
            firstList = listOf(1, 2, 3),
            secondList = listOf('a', 'b', 'c', 'd', 'e'),
            expectedInterleavingCount = 56,
        )
    }

    @Test
    fun test_fiveElements_five() {
        // C(10, 5) = 252
        assertGeneratesExpectedInterleavings(
            firstList = listOf(1, 2, 3, 4, 5),
            secondList = listOf('a', 'b', 'c', 'd', 'e'),
            expectedInterleavingCount = 252,
        )
    }

    @Test
    fun test_sixElements_four() {
        // C(10, 4) = 210
        assertGeneratesExpectedInterleavings(
            firstList = listOf(1, 2, 3, 4, 5, 6),
            secondList = listOf('a', 'b', 'c', 'd'),
            expectedInterleavingCount = 210,
        )
    }
}

/**
 * A test utility function that asserts that the [InterleavingsGenerator] generates exactly the expected interleavings
 * for the given input lists, suitable for smaller cases where we can explicitly enumerate all interleavings.
 */
private fun <T> assertGeneratesExpectedInterleavings(
    firstList: List<T>,
    secondList: List<T>,
    expectedInterleavings: Set<List<T>>,
) {
    assertGeneratesConsistentInterleavings(
        firstList = firstList,
        secondList = secondList,
        expectedCount = expectedInterleavings.size,
    ) { generatedInterleaving ->
        assertTrue(
            expectedInterleavings.contains(generatedInterleaving),
            message = "Generated interleaving $generatedInterleaving is not in expected interleavings for lists $firstList and $secondList.",
        )
    }
}

/**
 * A test utility function that asserts that the [InterleavingsGenerator] generates the expected number of interleavings
 * for the given input lists, and that all generated interleavings are consistent with the definition of interleaving.
 * Suitable for larger cases where it's not convenient to explicitly enumerate all interleavings.
 */
private fun <T> assertGeneratesExpectedInterleavings(
    firstList: List<T>,
    secondList: List<T>,
    expectedInterleavingCount: Int,
) {
    assertGeneratesConsistentInterleavings(
        firstList = firstList,
        secondList = secondList,
        expectedCount = expectedInterleavingCount,
    ) { _ ->
        // Assume that the interleaving is fine if it was verified to be consistent
    }
}

private fun <T> assertGeneratesConsistentInterleavings(
    firstList: List<T>,
    secondList: List<T>,
    expectedCount: Int,
    validateInterleaving: (List<T>) -> Unit,
) {
    val generator = InterleavingsGenerator(
        firstList = firstList,
        secondList = secondList,
    )

    val actualCount = generator.count()

    assertEquals(
        expected = expectedCount,
        actual = actualCount,
        message = "Expected $expectedCount interleavings for lists $firstList and $secondList, but the calculated count was $actualCount",
    )

    val actualAllInterleavings = generator.generateAll().toList()

    val actualGeneratedCount = actualAllInterleavings.size

    assertEquals(
        expected = expectedCount,
        actual = actualGeneratedCount,
        message = "Expected $expectedCount interleavings for lists $firstList and $secondList, but actually generated $actualGeneratedCount interleavings",
    )

    actualAllInterleavings.forEachIndexed { index: Int, generatedInterleaving: List<T> ->
        validateInterleaving(generatedInterleaving)

        val singleGeneratedInterleaving = generator.generateSingle(index)

        assertEquals(
            expected = generatedInterleaving,
            actual = singleGeneratedInterleaving,
            message = "Inconsistent generation for interleaving #$index",
        )

        val recoveredFirstList = generatedInterleaving.filter { it in firstList }
        val recoveredSecondList = generatedInterleaving.filter { it in secondList }

        assertEquals(
            expected = firstList,
            actual = recoveredFirstList,
            message = "Interleaving $generatedInterleaving does not preserve order of first list $firstList",
        )

        assertEquals(
            expected = secondList,
            actual = recoveredSecondList,
            message = "Interleaving $generatedInterleaving does not preserve order of second list $secondList",
        )
    }
}
