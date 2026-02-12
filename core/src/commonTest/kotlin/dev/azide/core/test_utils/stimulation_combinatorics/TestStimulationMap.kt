package dev.azide.core.test_utils.stimulation_combinatorics

import dev.azide.core.test_utils.TestStimulation

data class TestStimulationMap(
    val stimulationByTag: Map<TestStimulationTag, TestStimulation>,
) {
    companion object {
        val Empty = TestStimulationMap(
            stimulationByTag = emptyMap(),
        )

        fun of(
            vararg pairs: Pair<TestStimulationTag, TestStimulation>,
        ): TestStimulationMap = TestStimulationMap(
            stimulationByTag = pairs.toMap(),
        )

        fun union(
            vararg maps: TestStimulationMap,
        ): TestStimulationMap = TestStimulationMap(
            stimulationByTag = maps.flatMap { it.stimulationByTag.entries }.associate { it.toPair() },
        )
    }

    operator fun get(
        tag: TestStimulationTag,
    ): TestStimulation = stimulationByTag[tag] ?: throw IllegalArgumentException("No stimulation found for tag: $tag")
}
