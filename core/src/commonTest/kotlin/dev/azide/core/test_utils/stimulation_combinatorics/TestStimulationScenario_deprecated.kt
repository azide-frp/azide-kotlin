package dev.azide.core.test_utils.stimulation_combinatorics

import dev.azide.core.test_utils.TestStimulation

data class TestStimulationScenario_deprecated(
    val stimulationTags: List<TestStimulationTag_deprecated>,
) {
    companion object {
        val Empty = TestStimulationScenario_deprecated(
            stimulationTags = emptyList(),
        )

        fun of(
            vararg stimulationTags: TestStimulationTag_deprecated,
        ): TestStimulationScenario_deprecated = TestStimulationScenario_deprecated(
            stimulationTags = stimulationTags.toList(),
        )
    }

    fun toBank(): TestStimulationScenarioBank_deprecated = TestStimulationScenarioBank_deprecated(
        stimulationScenarios = sequenceOf(this),
    )

    fun combineWith(
        otherStimulationScenario: TestStimulationScenario_deprecated,
    ): Sequence<TestStimulationScenario_deprecated> = generateInterleavings(
        firstList = this.stimulationTags,
        secondList = otherStimulationScenario.stimulationTags,
    ).map { interleavedTags ->
        TestStimulationScenario_deprecated(
            stimulationTags = interleavedTags,
        )
    }

    fun <SlotCountT : TestSlotCount> distribute(
        slotCount: SlotCountT,
    ): Sequence<TestSlottedStimulationScenario<SlotCountT>> = generateBucketSplits(
        list = stimulationTags,
        bucketCount = slotCount.count,
    ).map { tagBuckets: List<List<TestStimulationTag_deprecated>> ->
        TestSlottedStimulationScenario.of(
            slotCount = slotCount,
            slotStimulationScenarios = tagBuckets.map { stimulationTags: List<TestStimulationTag_deprecated> ->
                TestStimulationScenario_deprecated(stimulationTags = stimulationTags)
            },
        )
    }

    fun bind(
        stimulationMap: TestStimulationMap,
    ): TestStimulation = TestStimulation.combineInProvidedOrder(
        stimulations = stimulationTags.map { stimulationTag: TestStimulationTag_deprecated ->
            stimulationMap[stimulationTag]
        },
    )
}
