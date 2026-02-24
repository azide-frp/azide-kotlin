package dev.azide.core.test_utils.stimulation_combinatorics

data class TestSlottedStimulationScenario<@Suppress("unused") SlotCountT : TestSlotCount> private constructor(
    val slotStimulationScenarios: List<TestStimulationScenario_deprecated>,
) {
    companion object {
        fun <SlotCountT : TestSlotCount> of(
            slotCount: SlotCountT,
            slotStimulationScenarios: List<TestStimulationScenario_deprecated>,
        ): TestSlottedStimulationScenario<SlotCountT> {
            require(slotStimulationScenarios.size == slotCount.count) {
                "Expected ${slotCount.count} slot stimulations, but got ${slotStimulationScenarios.size}."
            }

            return TestSlottedStimulationScenario(
                slotStimulationScenarios = slotStimulationScenarios,
            )
        }
    }
}

fun <@Suppress("unused") SlotCountT : TestSlotCount> TestStimulationMap.bind(
    scenario: TestSlottedStimulationScenario<SlotCountT>,
): TestSlottedStimulation<SlotCountT> = TestSlottedStimulation(
    slotStimulations = scenario.slotStimulationScenarios.map { stimulationScenario: TestStimulationScenario_deprecated ->
        stimulationScenario.bind(stimulationMap = this)
    },
)

val TestSlottedStimulationScenario<TestSlotCount.Count1Plus>.slotStimulation0: TestStimulationScenario_deprecated
    get() = slotStimulationScenarios[0]

val TestSlottedStimulationScenario<TestSlotCount.Count2Plus>.slotStimulation1: TestStimulationScenario_deprecated
    get() = slotStimulationScenarios[1]

val TestSlottedStimulationScenario<TestSlotCount.Count3Plus>.slotStimulation2: TestStimulationScenario_deprecated
    get() = slotStimulationScenarios[2]

val TestSlottedStimulationScenario<TestSlotCount.Count4Plus>.slotStimulation3: TestStimulationScenario_deprecated
    get() = slotStimulationScenarios[3]

val TestSlottedStimulationScenario<TestSlotCount.Count5Plus>.slotStimulation4: TestStimulationScenario_deprecated
    get() = slotStimulationScenarios[4]
