package dev.azide.core.test_utils.stimulation_combinatorics

import dev.azide.core.impl.utils.list.uncons

data class TestStimulationBank(
    val stimulationSequences: List<TestStimulationScenario>,
) {
    companion object {
        fun build(
            vararg stimulationSequences: TestStimulationScenario,
        ): TestStimulationBank {
            val (firstStimulationSequence, otherStimulationSequences) = stimulationSequences.toList().uncons()
                ?: throw IllegalArgumentException("At least one stimulation sequence is required.")

            return TestStimulationBank(
                otherStimulationSequences.fold(
                    initial = sequenceOf(firstStimulationSequence),
                ) { combinedStimulationSequences: Sequence<TestStimulationScenario>, nextStimulationSequence: TestStimulationScenario ->
                    combinedStimulationSequences.flatMap { oldStimulationSequence ->
                        oldStimulationSequence.combineWith(
                            otherStimulationSequence = nextStimulationSequence,
                        )
                    }
                }.toList(),
            )
        }
    }

    fun <SlotCountT : TestSlotCount> distribute(
        slotCount: SlotCountT,
    ): TestSlottedStimulationBank<SlotCountT> = TestSlottedStimulationBank(
        slottedStimulationScenarios = stimulationSequences.flatMap { stimulationSequence ->
            stimulationSequence.distribute(
                slotCount = slotCount,
            )
        }.toList(),
    )
}
