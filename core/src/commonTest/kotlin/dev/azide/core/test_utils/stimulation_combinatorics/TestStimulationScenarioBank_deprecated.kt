package dev.azide.core.test_utils.stimulation_combinatorics

import dev.azide.core.impl.utils.list.uncons

data class TestStimulationScenarioBank_deprecated(
    val stimulationScenarios: Sequence<TestStimulationScenario_deprecated>,
) {
    companion object {
        val Empty: TestStimulationScenarioBank_deprecated = TestStimulationScenarioBank_deprecated(
            stimulationScenarios = emptySequence(),
        )

        fun of(
            vararg stimulationScenarios: TestStimulationScenario_deprecated,
        ): TestStimulationScenarioBank_deprecated = TestStimulationScenarioBank_deprecated(
            stimulationScenarios = stimulationScenarios.asSequence(),
        )

        fun mixAll(
            vararg stimulationScenarios: TestStimulationScenario_deprecated,
        ): TestStimulationScenarioBank_deprecated {
            val (firstStimulationScenario, otherStimulationScenarios) = stimulationScenarios.toList().uncons()
                ?: throw IllegalArgumentException("At least one stimulation sequence is required.")

            return TestStimulationScenarioBank_deprecated(
                otherStimulationScenarios.fold(
                    initial = sequenceOf(firstStimulationScenario),
                ) { combinedStimulationScenarios: Sequence<TestStimulationScenario_deprecated>, nextStimulationScenario: TestStimulationScenario_deprecated ->
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
        other: TestStimulationScenarioBank_deprecated,
    ): TestStimulationScenarioBank_deprecated = TestStimulationScenarioBank_deprecated(
        stimulationScenarios.fold(
            initial = other.stimulationScenarios,
        ) { combinedStimulationScenarios: Sequence<TestStimulationScenario_deprecated>, nextStimulationScenario: TestStimulationScenario_deprecated ->
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
