package dev.azide.core.test_utils.stimulation_combinatorics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@Suppress("ClassName")
class BucketSplitGenerator_tests {
    @Test
    fun test_invalidBucketCount_shouldThrow() {
        assertFailsWith<IllegalArgumentException> {
            BucketSplitGenerator(listOf(1, 2, 3), bucketCount = 0)
        }

        assertFailsWith<IllegalArgumentException> {
            BucketSplitGenerator(listOf(1, 2, 3), bucketCount = -1)
        }
    }

    @Test
    fun test_emptyList_oneBucket() {
        assertGeneratesCorrectSplits(
            list = emptyList<Int>(),
            bucketCount = 1,
            expectedSplits = setOf(
                listOf(emptyList<Int>()),
            ),
        )
    }

    @Test
    fun test_emptyList_multipleBuckets() {
        assertGeneratesCorrectSplits(
            list = emptyList<Int>(),
            bucketCount = 3,
            expectedSplits = setOf(
                listOf(emptyList(), emptyList(), emptyList<Int>()),
            ),
        )
    }

    @Test
    fun test_singleElement_oneBucket() {
        assertGeneratesCorrectSplits(
            list = listOf(42),
            bucketCount = 1,
            expectedSplits = setOf(
                listOf(listOf(42)),
            ),
        )
    }

    @Test
    fun test_singleElement_twoBuckets() {
        assertGeneratesCorrectSplits(
            list = listOf(42),
            bucketCount = 2,
            expectedSplits = setOf(
                listOf(emptyList(), listOf(42)),
                listOf(listOf(42), emptyList<Int>()),
            ),
        )
    }

    @Test
    fun test_twoElements_oneBucket() {
        assertGeneratesCorrectSplits(
            list = listOf(1, 2),
            bucketCount = 1,
            expectedSplits = setOf(
                listOf(listOf(1, 2)),
            ),
        )
    }

    @Test
    fun test_twoElements_twoBuckets() {
        assertGeneratesCorrectSplits(
            list = listOf(1, 2),
            bucketCount = 2,
            expectedSplits = setOf(
                listOf(emptyList(), listOf(1, 2)),
                listOf(listOf(1), listOf(2)),
                listOf(listOf(1, 2), emptyList<Int>()),
            ),
        )
    }

    @Test
    fun test_twoElements_threeBuckets() {
        assertGeneratesCorrectSplits(
            list = listOf(1, 2),
            bucketCount = 3,
            expectedSplits = setOf(
                listOf(emptyList(), emptyList(), listOf(1, 2)), // cuts at [0, 0]
                listOf(emptyList(), listOf(1), listOf(2)), // cuts at [0, 1]
                listOf(emptyList(), listOf(1, 2), emptyList()), // cuts at [0, 2]
                listOf(listOf(1), emptyList(), listOf(2)), // cuts at [1, 1]
                listOf(listOf(1), listOf(2), emptyList()), // cuts at [1, 2]
                listOf(listOf(1, 2), emptyList(), emptyList()), // cuts at [2, 2]
            ),
        )
    }

    @Test
    fun test_threeElements_twoBuckets() {
        assertGeneratesCorrectSplits(
            list = listOf('a', 'b', 'c'),
            bucketCount = 2,
            expectedSplits = setOf(
                listOf(emptyList(), listOf('a', 'b', 'c')), // cut at [0]
                listOf(listOf('a'), listOf('b', 'c')), // cut at [1]
                listOf(listOf('a', 'b'), listOf('c')), // cut at [2]
                listOf(listOf('a', 'b', 'c'), emptyList()), // cut at [3]
            ),
        )
    }

    @Test
    fun test_threeElements_threeBuckets() {
        assertGeneratesCorrectSplits(
            list = listOf('a', 'b', 'c'),
            bucketCount = 3,
            expectedSplits = setOf(
                listOf(emptyList(), emptyList(), listOf('a', 'b', 'c')),
                listOf(emptyList(), listOf('a'), listOf('b', 'c')),
                listOf(emptyList(), listOf('a', 'b'), listOf('c')),
                listOf(emptyList(), listOf('a', 'b', 'c'), emptyList()),
                listOf(listOf('a'), emptyList(), listOf('b', 'c')),
                listOf(listOf('a'), listOf('b'), listOf('c')),
                listOf(listOf('a'), listOf('b', 'c'), emptyList()),
                listOf(listOf('a', 'b'), emptyList(), listOf('c')),
                listOf(listOf('a', 'b'), listOf('c'), emptyList()),
                listOf(listOf('a', 'b', 'c'), emptyList(), emptyList()),
            ),
        )
    }

    @Test
    fun test_fourElements_twoBuckets() {
        assertGeneratesCorrectSplits(
            list = listOf(1, 2, 3, 4),
            bucketCount = 2,
            expectedSplits = setOf(
                listOf(emptyList(), listOf(1, 2, 3, 4)),
                listOf(listOf(1), listOf(2, 3, 4)),
                listOf(listOf(1, 2), listOf(3, 4)),
                listOf(listOf(1, 2, 3), listOf(4)),
                listOf(listOf(1, 2, 3, 4), emptyList()),
            ),
        )
    }

    @Test
    fun test_fourElements_threeBuckets() {
        assertGeneratesCorrectSplits(
            list = listOf(1, 2, 3, 4),
            bucketCount = 3,
            expectedSplits = setOf(
                listOf(emptyList(), emptyList(), listOf(1, 2, 3, 4)),
                listOf(emptyList(), listOf(1), listOf(2, 3, 4)),
                listOf(emptyList(), listOf(1, 2), listOf(3, 4)),
                listOf(emptyList(), listOf(1, 2, 3), listOf(4)),
                listOf(emptyList(), listOf(1, 2, 3, 4), emptyList()),
                listOf(listOf(1), emptyList(), listOf(2, 3, 4)),
                listOf(listOf(1), listOf(2), listOf(3, 4)),
                listOf(listOf(1), listOf(2, 3), listOf(4)),
                listOf(listOf(1), listOf(2, 3, 4), emptyList()),
                listOf(listOf(1, 2), emptyList(), listOf(3, 4)),
                listOf(listOf(1, 2), listOf(3), listOf(4)),
                listOf(listOf(1, 2), listOf(3, 4), emptyList()),
                listOf(listOf(1, 2, 3), emptyList(), listOf(4)),
                listOf(listOf(1, 2, 3), listOf(4), emptyList()),
                listOf(listOf(1, 2, 3, 4), emptyList(), emptyList()),
            ),
        )
    }

    @Test
    fun test_threeElements_fourBuckets() {
        assertGeneratesCorrectSplits(
            list = listOf('x', 'y', 'z'),
            bucketCount = 4,
            expectedSplits = setOf(
                listOf(emptyList(), emptyList(), emptyList(), listOf('x', 'y', 'z')),
                listOf(emptyList(), emptyList(), listOf('x'), listOf('y', 'z')),
                listOf(emptyList(), emptyList(), listOf('x', 'y'), listOf('z')),
                listOf(emptyList(), emptyList(), listOf('x', 'y', 'z'), emptyList()),
                listOf(emptyList(), listOf('x'), emptyList(), listOf('y', 'z')),
                listOf(emptyList(), listOf('x'), listOf('y'), listOf('z')),
                listOf(emptyList(), listOf('x'), listOf('y', 'z'), emptyList()),
                listOf(emptyList(), listOf('x', 'y'), emptyList(), listOf('z')),
                listOf(emptyList(), listOf('x', 'y'), listOf('z'), emptyList()),
                listOf(emptyList(), listOf('x', 'y', 'z'), emptyList(), emptyList()),
                listOf(listOf('x'), emptyList(), emptyList(), listOf('y', 'z')),
                listOf(listOf('x'), emptyList(), listOf('y'), listOf('z')),
                listOf(listOf('x'), emptyList(), listOf('y', 'z'), emptyList()),
                listOf(listOf('x'), listOf('y'), emptyList(), listOf('z')),
                listOf(listOf('x'), listOf('y'), listOf('z'), emptyList()),
                listOf(listOf('x'), listOf('y', 'z'), emptyList(), emptyList()),
                listOf(listOf('x', 'y'), emptyList(), emptyList(), listOf('z')),
                listOf(listOf('x', 'y'), emptyList(), listOf('z'), emptyList()),
                listOf(listOf('x', 'y'), listOf('z'), emptyList(), emptyList()),
                listOf(listOf('x', 'y', 'z'), emptyList(), emptyList(), emptyList()),
            ),
        )
    }

    @Test
    fun test_twoElements_fourBuckets() {
        assertGeneratesCorrectSplits(
            list = listOf(10, 20),
            bucketCount = 4,
            expectedSplits = setOf(
                listOf(emptyList(), emptyList(), emptyList(), listOf(10, 20)),
                listOf(emptyList(), emptyList(), listOf(10), listOf(20)),
                listOf(emptyList(), emptyList(), listOf(10, 20), emptyList()),
                listOf(emptyList(), listOf(10), emptyList(), listOf(20)),
                listOf(emptyList(), listOf(10), listOf(20), emptyList()),
                listOf(emptyList(), listOf(10, 20), emptyList(), emptyList()),
                listOf(listOf(10), emptyList(), emptyList(), listOf(20)),
                listOf(listOf(10), emptyList(), listOf(20), emptyList()),
                listOf(listOf(10), listOf(20), emptyList(), emptyList()),
                listOf(listOf(10, 20), emptyList(), emptyList(), emptyList()),
            ),
        )
    }
}

private fun <T> assertGeneratesCorrectSplits(
    list: List<T>,
    bucketCount: Int,
    expectedSplits: Set<List<List<T>>>,
) {
    val expectedSplitCount = expectedSplits.size

    val generator = BucketSplitGenerator(
        list = list,
        bucketCount = bucketCount,
    )

    val actualCount = generator.count()

    assertEquals(
        expected = expectedSplitCount,
        actual = actualCount,
        message = "Expected $expectedSplitCount splits for list $list with $bucketCount buckets, but the calculated count was $actualCount",
    )

    val actualAllSplits = generator.generateAll().toList()

    val actualGeneratedSplitCount = actualAllSplits.size

    assertEquals(
        expected = expectedSplitCount,
        actual = actualGeneratedSplitCount,
        message = "Expected $expectedSplitCount splits for list $list with $bucketCount buckets, but actually generated $actualGeneratedSplitCount splits",
    )

    actualAllSplits.forEachIndexed { index: Int, generatedSplit: List<List<T>> ->
        assertTrue(
            expectedSplits.contains(generatedSplit),
            message = "Generated split #$index: $generatedSplit is not in expected splits for list $list with $bucketCount buckets.",
        )

        val singleGeneratedSplit = generator.generateSingle(index)

        assertEquals(
            expected = generatedSplit,
            actual = singleGeneratedSplit,
            message = "Inconsistent generation for split #$index",
        )
    }
}
