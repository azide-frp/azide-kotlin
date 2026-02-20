package dev.azide.core.test_utils.stimulation_combinatorics

import kotlin.random.Random
import kotlin.test.Test

@Suppress("ClassName")
class DynamicInterleavingUtils_tests {
    @Test
    fun test_generateRandom() {
        val random = Random(0)

        val lists = listOf(
            listOf('a', 'b', 'c'),
            listOf(1, 2, 3, 4),
            listOf("x", "y", "z"),
        )

        val generatedInterleaving = DynamicInterleavingUtils.generateRandom(
            random = random,
            lists = lists,
        )

        assertIsProperInterleaving(
            lists = lists,
            potentialInterleaving = generatedInterleaving,
        )
    }
}
