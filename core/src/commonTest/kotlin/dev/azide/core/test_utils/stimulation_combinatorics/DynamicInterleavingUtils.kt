package dev.azide.core.test_utils.stimulation_combinatorics

import kotlin.random.Random

object DynamicInterleavingUtils {
    fun <T> generateRandom(
        random: Random,
        lists: List<List<T>>,
    ): List<T> {
        val iterators = lists.flatMapTo(mutableListOf()) { list ->
            val iterator = list.iterator()
            List(list.size) { iterator }
        }

        iterators.shuffle(random)

        return iterators.map { it.next() }
    }
}
