package dev.azide.core.test_utils.stimulation_combinatorics

import dev.azide.core.test_utils.TestStimulation

data class TestStimulationMap_deprecated(
    val stimulationByTag: Map<TestStimulationTag_deprecated, TestStimulation>,
) {
    companion object {
        val Empty = TestStimulationMap_deprecated(
            stimulationByTag = emptyMap(),
        )

        fun of(
            vararg pairs: Pair<TestStimulationTag_deprecated, TestStimulation>,
        ): TestStimulationMap_deprecated = TestStimulationMap_deprecated(
            stimulationByTag = pairs.toMap(),
        )

        fun union(
            vararg maps: TestStimulationMap_deprecated,
        ): TestStimulationMap_deprecated = TestStimulationMap_deprecated(
            stimulationByTag = maps.flatMap { it.stimulationByTag.entries }.associate { it.toPair() },
        )
    }

    operator fun get(
        tag: TestStimulationTag_deprecated,
    ): TestStimulation = stimulationByTag[tag] ?: throw IllegalArgumentException("No stimulation found for tag: $tag")
}
