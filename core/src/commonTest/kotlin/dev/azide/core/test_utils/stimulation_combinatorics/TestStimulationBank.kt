package dev.azide.core.test_utils.stimulation_combinatorics

import dev.azide.core.impl.utils.list.uncons

data class TestStimulationBank(
    val stimulationScenarios: List<TestStimulationScenario>,
) {
    companion object {
        fun build(
            vararg stimulationScenarios: TestStimulationScenario,
        ): TestStimulationBank {
            val (firstStimulationScenario, otherStimulationScenarios) = stimulationScenarios.toList().uncons()
                ?: throw IllegalArgumentException("At least one stimulation sequence is required.")

            return TestStimulationBank(
                otherStimulationScenarios.fold(
                    initial = sequenceOf(firstStimulationScenario),
                ) { combinedStimulationScenarios: Sequence<TestStimulationScenario>, nextStimulationScenario: TestStimulationScenario ->
                    combinedStimulationScenarios.flatMap { oldStimulationScenario ->
                        oldStimulationScenario.combineWith(
                            otherStimulationScenario = nextStimulationScenario,
                        )
                    }
                }.toList(),
            )
        }
    }

    fun <SlotCountT : TestSlotCount> distribute(
        slotCount: SlotCountT,
    ): TestSlottedStimulationBank<SlotCountT> = TestSlottedStimulationBank(
        slottedStimulationScenarios = stimulationScenarios.flatMap { stimulationScenario ->
            stimulationScenario.distribute(
                slotCount = slotCount,
            )
        }.toList(),
    )
}
