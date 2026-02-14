package dev.azide.core.test_utils.stimulation_combinatorics

import dev.azide.core.test_utils.TestStimulation

data class TestStimulationScenario(
    val stimulationTags: List<TestStimulationTag>,
) {
    companion object {
        val Empty = TestStimulationScenario(
            stimulationTags = emptyList(),
        )

        fun of(
            vararg stimulationTags: TestStimulationTag,
        ): TestStimulationScenario = TestStimulationScenario(
            stimulationTags = stimulationTags.toList(),
        )
    }

    fun toBank(): TestStimulationScenarioBank = TestStimulationScenarioBank(
        stimulationScenarios = sequenceOf(this),
    )

    fun combineWith(
        otherStimulationScenario: TestStimulationScenario,
    ): Sequence<TestStimulationScenario> = generateInterleavings(
        firstList = this.stimulationTags,
        secondList = otherStimulationScenario.stimulationTags,
    ).map { interleavedTags ->
        TestStimulationScenario(
            stimulationTags = interleavedTags,
        )
    }

    fun <SlotCountT : TestSlotCount> distribute(
        slotCount: SlotCountT,
    ): Sequence<TestSlottedStimulationScenario<SlotCountT>> = generateBucketSplits(
        list = stimulationTags,
        n = slotCount.count,
    ).map { tagBuckets: List<List<TestStimulationTag>> ->
        TestSlottedStimulationScenario.of(
            slotCount = slotCount,
            slotStimulationScenarios = tagBuckets.map { stimulationTags: List<TestStimulationTag> ->
                TestStimulationScenario(stimulationTags = stimulationTags)
            },
        )
    }

    fun bind(
        stimulationMap: TestStimulationMap,
    ): TestStimulation = TestStimulation.combine(
        stimulations = stimulationTags.map { stimulationTag: TestStimulationTag ->
            stimulationMap[stimulationTag]
        },
    )
}
