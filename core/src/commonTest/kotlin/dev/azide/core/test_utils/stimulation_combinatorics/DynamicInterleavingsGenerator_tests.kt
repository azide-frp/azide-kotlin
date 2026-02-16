package dev.azide.core.test_utils.stimulation_combinatorics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Suppress("ClassName")
class DynamicInterleavingsGenerator_tests {
    @Test
    fun test_noLists() {
        assertGeneratesExpectedInterleavings(
            lists = emptyList<List<Int>>(),
            expectedInterleavings = setOf(
                emptyList<Int>(),
            ),
        )
    }

    @Test
    fun test_singleEmptyList() {
        assertGeneratesExpectedInterleavings(
            lists = listOf(emptyList<Int>()),
            expectedInterleavings = setOf(
                emptyList(),
            ),
        )
    }

    @Test
    fun test_singleListWithOneElement() {
        assertGeneratesExpectedInterleavings(
            lists = listOf(listOf(1)),
            expectedInterleavings = setOf(
                listOf(1),
            ),
        )
    }

    @Test
    fun test_singleListWithMultipleElements() {
        assertGeneratesExpectedInterleavings(
            lists = listOf(listOf(1, 2, 3)),
            expectedInterleavings = setOf(
                listOf(1, 2, 3),
            ),
        )
    }

    @Test
    fun test_twoEmptyLists() {
        assertGeneratesExpectedInterleavings(
            lists = listOf(
                emptyList(),
                emptyList<Int>(),
            ),
            expectedInterleavings = setOf(
                emptyList(),
            ),
        )
    }

    @Test
    fun test_twoLists_oneEmpty() {
        assertGeneratesExpectedInterleavings(
            lists = listOf(
                listOf(1, 2),
                emptyList<Int>(),
            ),
            expectedInterleavings = setOf(
                listOf(1, 2),
            ),
        )
    }

    @Test
    fun test_twoLists_bothSingle() {
        assertGeneratesExpectedInterleavings(
            lists = listOf(
                listOf('a'),
                listOf(1),
            ),
            expectedInterleavings = setOf(
                listOf('a', 1),
                listOf(1, 'a'),
            ),
        )
    }

    @Test
    fun test_twoLists_twoAndOne() {
        assertGeneratesExpectedInterleavings(
            lists = listOf(
                listOf('a', 'b'),
                listOf(1),
            ),
            expectedInterleavings = setOf(
                listOf('a', 'b', 1),
                listOf('a', 1, 'b'),
                listOf(1, 'a', 'b'),
            ),
        )
    }

    @Test
    fun test_twoLists_twoAndTwo() {
        assertGeneratesExpectedInterleavings(
            lists = listOf(
                listOf('a', 'b'),
                listOf(1, 2),
            ),
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
    fun test_threeLists_allSingle() {
        // 3! / (1! * 1! * 1!) = 6
        assertGeneratesExpectedInterleavings(
            lists = listOf(
                listOf('a'),
                listOf(1),
                listOf("x"),
            ),
            expectedInterleavings = setOf(
                listOf('a', 1, "x"),
                listOf('a', "x", 1),
                listOf(1, 'a', "x"),
                listOf(1, "x", 'a'),
                listOf("x", 'a', 1),
                listOf("x", 1, 'a'),
            ),
        )
    }

    @Test
    fun test_threeLists_oneEmpty() {
        assertGeneratesExpectedInterleavings(
            lists = listOf(
                listOf('a'),
                emptyList(),
                listOf(1),
            ),
            expectedInterleavings = setOf(
                listOf('a', 1),
                listOf(1, 'a'),
            ),
        )
    }

    @Test
    fun test_threeLists_twoOneOne() {
        // 4! / (2! * 1! * 1!) = 12
        assertGeneratesExpectedInterleavings(
            lists = listOf(
                listOf('a', 'b'),
                listOf(1),
                listOf("x"),
            ),
            expectedInterleavings = setOf(
                listOf('a', 'b', 1, "x"),
                listOf('a', 'b', "x", 1),
                listOf('a', 1, 'b', "x"),
                listOf('a', 1, "x", 'b'),
                listOf('a', "x", 'b', 1),
                listOf('a', "x", 1, 'b'),
                listOf(1, 'a', 'b', "x"),
                listOf(1, 'a', "x", 'b'),
                listOf(1, "x", 'a', 'b'),
                listOf("x", 'a', 'b', 1),
                listOf("x", 'a', 1, 'b'),
                listOf("x", 1, 'a', 'b'),
            ),
        )
    }

    @Test
    fun test_threeLists_twoTwoOne() {
        // 5! / (2! * 2! * 1!) = 30
        assertGeneratesExpectedInterleavings(
            lists = listOf(
                listOf('a', 'b'),
                listOf(1, 2),
                listOf("x"),
            ),
            expectedInterleavingCount = 30,
        )
    }

    @Test
    fun test_threeLists_twoTwoTwo() {
        // 6! / (2! * 2! * 2!) = 90
        assertGeneratesExpectedInterleavings(
            lists = listOf(
                listOf('a', 'b'),
                listOf(1, 2),
                listOf("x", "y"),
            ),
            expectedInterleavingCount = 90,
        )
    }

    @Test
    fun test_fourLists_allSingle() {
        // 4! / (1! * 1! * 1! * 1!) = 24
        assertGeneratesExpectedInterleavings(
            lists = listOf(
                listOf('a'),
                listOf(1),
                listOf("x"),
                listOf(true),
            ),
            expectedInterleavingCount = 24,
        )
    }

    @Test
    fun test_fourLists_twoOneOneOne() {
        // 5! / (2! * 1! * 1! * 1!) = 60
        assertGeneratesExpectedInterleavings(
            lists = listOf(
                listOf('a', 'b'),
                listOf(1),
                listOf("x"),
                listOf(true),
            ),
            expectedInterleavingCount = 60,
        )
    }

    @Test
    fun test_fourLists_twoTwoOneOne() {
        // 6! / (2! * 2! * 1! * 1!) = 180
        assertGeneratesExpectedInterleavings(
            lists = listOf(
                listOf('a', 'b'),
                listOf(1, 2),
                listOf("x"),
                listOf(true),
            ),
            expectedInterleavingCount = 180,
        )
    }

    @Test
    fun test_fiveLists_allSingle() {
        // 5! / (1! * 1! * 1! * 1! * 1!) = 120
        assertGeneratesExpectedInterleavings(
            lists = listOf(
                listOf('a'),
                listOf(1),
                listOf("x"),
                listOf(true),
                listOf(1.0),
            ),
            expectedInterleavingCount = 120,
        )
    }

    @Test
    fun test_generateSingle_invalidIndex() {
        val generator = DynamicInterleavingsGenerator(
            lists = listOf(
                listOf(1, 2),
                listOf('a', 'b'),
            ),
        )

        assertFailsWith<IllegalArgumentException> {
            generator.generateSingle(-1)
        }

        assertNull(generator.generateSingle(generator.count()))
        assertNull(generator.generateSingle(generator.count() + 10))
    }

    @Test
    fun test_equivalenceWithInterleavingsGenerator() {
        // Verify that DynamicInterleavingsGenerator with two lists produces the same results
        // as InterleavingsGenerator
        val firstList = listOf(1, 2, 3)
        val secondList = listOf('a', 'b')

        val twoListGenerator = InterleavingsGenerator(
            firstList = firstList,
            secondList = secondList,
        )

        val dynamicGenerator = DynamicInterleavingsGenerator(
            lists = listOf(
                firstList,
                secondList,
            ),
        )

        assertEquals(
            expected = twoListGenerator.count().toLong(),
            actual = dynamicGenerator.count(),
        )

        val twoListResults = twoListGenerator.generateAll().toList()
        val dynamicResults = dynamicGenerator.generateAll().toList()

        assertEquals(
            expected = twoListResults,
            actual = dynamicResults,
        )
    }
}

/**
 * A test utility function that asserts that the [DynamicInterleavingsGenerator] generates exactly the expected
 * interleavings for the given input lists, suitable for smaller cases where we can explicitly enumerate all
 * interleavings.
 */
private fun <T> assertGeneratesExpectedInterleavings(
    lists: List<List<T>>,
    expectedInterleavings: Set<List<T>>,
) {
    assertGeneratesConsistentInterleavings(
        lists = lists,
        expectedCount = expectedInterleavings.size.toLong(),
    ) { generatedInterleaving ->
        assertTrue(
            expectedInterleavings.contains(generatedInterleaving),
            message = "Generated interleaving $generatedInterleaving is not in expected interleavings for lists $lists.",
        )
    }
}

/**
 * A test utility function that asserts that the [DynamicInterleavingsGenerator] generates the expected number of
 * interleavings for the given input lists, and that all generated interleavings are consistent with the definition
 * of interleaving. Suitable for larger cases where it's not convenient to explicitly enumerate all interleavings.
 */
private fun <T> assertGeneratesExpectedInterleavings(
    lists: List<List<T>>,
    expectedInterleavingCount: Long,
) {
    assertGeneratesConsistentInterleavings(
        lists = lists,
        expectedCount = expectedInterleavingCount,
    ) { _ ->
        // Assume that the interleaving is fine if it was verified to be consistent
    }
}

private fun <T> assertGeneratesConsistentInterleavings(
    lists: List<List<T>>,
    expectedCount: Long,
    validateInterleaving: (List<T>) -> Unit,
) {
    val generator = DynamicInterleavingsGenerator(
        lists = lists,
    )

    val actualCount = generator.count()

    assertEquals(
        expected = expectedCount,
        actual = actualCount,
        message = "Expected $expectedCount interleavings for lists $lists, but the calculated count was $actualCount",
    )

    val actualAllInterleavings = generator.generateAll().toList()

    val actualGeneratedCount = actualAllInterleavings.size.toLong()

    assertEquals(
        expected = expectedCount,
        actual = actualGeneratedCount,
        message = "Expected $expectedCount interleavings for lists $lists, but actually generated $actualGeneratedCount interleavings",
    )

    // Verify all interleavings are unique
    assertEquals(
        expected = expectedCount,
        actual = actualAllInterleavings.toSet().size.toLong(),
        message = "Generated interleavings contain duplicates",
    )

    actualAllInterleavings.forEachIndexed { index: Int, generatedInterleaving: List<T> ->
        validateInterleaving(generatedInterleaving)

        val singleGeneratedInterleaving = generator.generateSingle(index.toLong())

        assertEquals(
            expected = generatedInterleaving,
            actual = singleGeneratedInterleaving,
            message = "Inconsistent generation for interleaving #$index",
        )

        // Verify order preservation for each input list
        lists.forEach { inputList ->
            val recoveredList = generatedInterleaving.filter { it in inputList }

            assertEquals(
                expected = inputList,
                actual = recoveredList,
                message = "Interleaving $generatedInterleaving does not preserve order of list $inputList",
            )
        }

        // Verify total size
        val expectedSize = lists.sumOf { it.size }

        assertEquals(
            expected = expectedSize,
            actual = generatedInterleaving.size,
            message = "Interleaving $generatedInterleaving has wrong size (expected $expectedSize)",
        )
    }
}

