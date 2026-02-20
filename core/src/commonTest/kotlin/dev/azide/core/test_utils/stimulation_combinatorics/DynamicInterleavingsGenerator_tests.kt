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
            lists = emptyList<List<String>>(),
            expectedInterleavings = setOf(
                emptyList<String>(),
            ),
        )
    }

    @Test
    fun test_singleEmptyList() {
        assertGeneratesExpectedInterleavings(
            lists = listOf(emptyList<String>()),
            expectedInterleavings = setOf(
                emptyList(),
            ),
        )
    }

    @Test
    fun test_singleListWithOneElement() {
        assertGeneratesExpectedInterleavings(
            lists = listOf(listOf("A")),
            expectedInterleavings = setOf(
                listOf("A"),
            ),
        )
    }

    @Test
    fun test_singleListWithMultipleElements() {
        assertGeneratesExpectedInterleavings(
            lists = listOf(listOf("A", "B", "C")),
            expectedInterleavings = setOf(
                listOf("A", "B", "C"),
            ),
        )
    }

    @Test
    fun test_twoEmptyLists() {
        assertGeneratesExpectedInterleavings(
            lists = listOf(
                emptyList<String>(),
                emptyList<String>(),
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
                listOf("ONE", "TWO"),
                emptyList<String>(),
            ),
            expectedInterleavings = setOf(
                listOf("ONE", "TWO"),
            ),
        )
    }

    @Test
    fun test_twoLists_bothSingle() {
        assertGeneratesExpectedInterleavings(
            lists = listOf(
                listOf("A"),
                listOf("ONE"),
            ),
            expectedInterleavings = setOf(
                listOf("A", "ONE"),
                listOf("ONE", "A"),
            ),
        )
    }

    @Test
    fun test_twoLists_twoAndOne() {
        assertGeneratesExpectedInterleavings(
            lists = listOf(
                listOf("A", "B"),
                listOf("ONE"),
            ),
            expectedInterleavings = setOf(
                listOf("A", "B", "ONE"),
                listOf("A", "ONE", "B"),
                listOf("ONE", "A", "B"),
            ),
        )
    }

    @Test
    fun test_twoLists_twoAndTwo() {
        assertGeneratesExpectedInterleavings(
            lists = listOf(
                listOf("A", "B"),
                listOf("ONE", "TWO"),
            ),
            expectedInterleavings = setOf(
                listOf("A", "B", "ONE", "TWO"),
                listOf("A", "ONE", "B", "TWO"),
                listOf("A", "ONE", "TWO", "B"),
                listOf("ONE", "A", "B", "TWO"),
                listOf("ONE", "A", "TWO", "B"),
                listOf("ONE", "TWO", "A", "B"),
            ),
        )
    }

    @Test
    fun test_threeLists_allSingle() {
        // 3! / (1! * 1! * 1!) = 6
        assertGeneratesExpectedInterleavings(
            lists = listOf(
                listOf("A"),
                listOf("ONE"),
                listOf("@@"),
            ),
            expectedInterleavings = setOf(
                listOf("A", "ONE", "@@"),
                listOf("A", "@@", "ONE"),
                listOf("ONE", "A", "@@"),
                listOf("ONE", "@@", "A"),
                listOf("@@", "A", "ONE"),
                listOf("@@", "ONE", "A"),
            ),
        )
    }

    @Test
    fun test_threeLists_oneEmpty() {
        assertGeneratesExpectedInterleavings(
            lists = listOf(
                listOf("A"),
                emptyList<String>(),
                listOf("ONE"),
            ),
            expectedInterleavings = setOf(
                listOf("A", "ONE"),
                listOf("ONE", "A"),
            ),
        )
    }

    @Test
    fun test_threeLists_twoOneOne() {
        // 4! / (2! * 1! * 1!) = 12
        assertGeneratesExpectedInterleavings(
            lists = listOf(
                listOf("A", "B"),
                listOf("ONE"),
                listOf("@@"),
            ),
            expectedInterleavings = setOf(
                listOf("A", "B", "ONE", "@@"),
                listOf("A", "B", "@@", "ONE"),
                listOf("A", "ONE", "B", "@@"),
                listOf("A", "ONE", "@@", "B"),
                listOf("A", "@@", "B", "ONE"),
                listOf("A", "@@", "ONE", "B"),
                listOf("ONE", "A", "B", "@@"),
                listOf("ONE", "A", "@@", "B"),
                listOf("ONE", "@@", "A", "B"),
                listOf("@@", "A", "B", "ONE"),
                listOf("@@", "A", "ONE", "B"),
                listOf("@@", "ONE", "A", "B"),
            ),
        )
    }

    @Test
    fun test_threeLists_twoTwoOne() {
        // 5! / (2! * 2! * 1!) = 30
        assertGeneratesExpectedInterleavings(
            lists = listOf(
                listOf("A", "B"),
                listOf("ONE", "TWO"),
                listOf("@@"),
            ),
            expectedInterleavingCount = 30,
        )
    }

    @Test
    fun test_threeLists_twoTwoTwo() {
        // 6! / (2! * 2! * 2!) = 90
        assertGeneratesExpectedInterleavings(
            lists = listOf(
                listOf("A", "B"),
                listOf("ONE", "TWO"),
                listOf("@@", "&&"),
            ),
            expectedInterleavingCount = 90,
        )
    }

    @Test
    fun test_fourLists_allSingle() {
        // 4! / (1! * 1! * 1! * 1!) = 24
        assertGeneratesExpectedInterleavings(
            lists = listOf(
                listOf("A"),
                listOf("ONE"),
                listOf("@@"),
                listOf("&&"),
            ),
            expectedInterleavingCount = 24,
        )
    }

    @Test
    fun test_fourLists_twoOneOneOne() {
        // 5! / (2! * 1! * 1! * 1!) = 60
        assertGeneratesExpectedInterleavings(
            lists = listOf(
                listOf("A", "B"),
                listOf("ONE"),
                listOf("@@"),
                listOf("&&"),
            ),
            expectedInterleavingCount = 60,
        )
    }

    @Test
    fun test_fourLists_twoTwoOneOne() {
        // 6! / (2! * 2! * 1! * 1!) = 180
        assertGeneratesExpectedInterleavings(
            lists = listOf(
                listOf("A", "B"),
                listOf("ONE", "TWO"),
                listOf("@@"),
                listOf("&&"),
            ),
            expectedInterleavingCount = 180,
        )
    }

    @Test
    fun test_fiveLists_allSingle() {
        // 5! / (1! * 1! * 1! * 1! * 1!) = 120
        assertGeneratesExpectedInterleavings(
            lists = listOf(
                listOf("A"),
                listOf("ONE"),
                listOf("@@"),
                listOf("&&"),
                listOf("%%"),
            ),
            expectedInterleavingCount = 120,
        )
    }

    @Test
    fun test_generateSingle_invalidIndex() {
        val generator = DynamicInterleavingsGenerator(
            lists = listOf(
                listOf("A", "B"),
                listOf("ONE", "TWO"),
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
        val firstList = listOf("A", "B", "C")
        val secondList = listOf("ONE", "TWO")

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

        assertIsProperInterleaving(lists, generatedInterleaving)
    }
}

/**
 * Asserts that the given [potentialInterleaving] is a proper interleaving of the input [lists], meaning that it
 * contains all elements from all lists, and the order of elements from each individual list is preserved.
 */
fun <T> assertIsProperInterleaving(
    lists: List<List<T>>,
    potentialInterleaving: List<T>,
) {
    // Verify order preservation for each input list
    lists.forEach { inputList ->
        val recoveredList = potentialInterleaving.filter { it in inputList }

        assertEquals(
            expected = inputList,
            actual = recoveredList,
            message = "Interleaving $potentialInterleaving does not preserve order of list $inputList",
        )
    }

    // Verify total size
    val expectedSize = lists.sumOf { it.size }

    assertEquals(
        expected = expectedSize,
        actual = potentialInterleaving.size,
        message = "Interleaving $potentialInterleaving has wrong size (expected $expectedSize)",
    )
}
