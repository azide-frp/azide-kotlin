package dev.azide.core.test_utils.stimulation_combinatorics

import dev.azide.core.impl.utils.list.uncons

data class TestStimulationScenarioBank(
    val stimulationScenarios: Sequence<TestStimulationScenario>,
) {
    companion object {
        val Empty: TestStimulationScenarioBank = TestStimulationScenarioBank(
            stimulationScenarios = emptySequence(),
        )

        fun of(
            vararg stimulationScenarios: TestStimulationScenario,
        ): TestStimulationScenarioBank = TestStimulationScenarioBank(
            stimulationScenarios = stimulationScenarios.asSequence(),
        )

        fun build(
            vararg stimulationScenarios: TestStimulationScenario,
        ): TestStimulationScenarioBank {
            val (firstStimulationScenario, otherStimulationScenarios) = stimulationScenarios.toList().uncons()
                ?: throw IllegalArgumentException("At least one stimulation sequence is required.")

            return TestStimulationScenarioBank(
                otherStimulationScenarios.fold(
                    initial = sequenceOf(firstStimulationScenario),
                ) { combinedStimulationScenarios: Sequence<TestStimulationScenario>, nextStimulationScenario: TestStimulationScenario ->
                    combinedStimulationScenarios.flatMap { oldStimulationScenario ->
                        oldStimulationScenario.combineWith(
                            otherStimulationScenario = nextStimulationScenario,
                        )
                    }
                },
            )
        }
    }

    fun mixWith(
        other: TestStimulationScenarioBank,
    ): TestStimulationScenarioBank = TestStimulationScenarioBank(
        stimulationScenarios.fold(
            initial = other.stimulationScenarios,
        ) { combinedStimulationScenarios: Sequence<TestStimulationScenario>, nextStimulationScenario: TestStimulationScenario ->
            combinedStimulationScenarios.flatMap { oldStimulationScenario ->
                oldStimulationScenario.combineWith(
                    otherStimulationScenario = nextStimulationScenario,
                )
            }
        },
    )

    fun <SlotCountT : TestSlotCount> distribute(
        slotCount: SlotCountT,
    ): TestSlottedStimulationScenarioBank<SlotCountT> = TestSlottedStimulationScenarioBank(
        slottedStimulationScenarios = stimulationScenarios.flatMap { stimulationScenario ->
            stimulationScenario.distribute(
                slotCount = slotCount,
            )
        },
    )
}
