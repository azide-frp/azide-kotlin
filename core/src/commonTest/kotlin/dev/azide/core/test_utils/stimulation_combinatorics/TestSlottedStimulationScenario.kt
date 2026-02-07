package dev.azide.core.test_utils.stimulation_combinatorics

data class TestSlottedStimulationScenario<@Suppress("unused") SlotCountT : TestSlotCount> private constructor(
    val slotStimulationScenarios: List<TestStimulationScenario>,
) {
    companion object {
        fun <SlotCountT : TestSlotCount> of(
            slotCount: SlotCountT,
            slotStimulations: List<TestStimulationScenario>,
        ): TestSlottedStimulationScenario<SlotCountT> {
            require(slotStimulations.size == slotCount.count) {
                "Expected ${slotCount.count} slot stimulations, but got ${slotStimulations.size}."
            }

            return TestSlottedStimulationScenario(
                slotStimulationScenarios = slotStimulations,
            )
        }
    }

    fun bind(
        stimulationMap: TestStimulationMap,
    ): TestSlottedStimulation<SlotCountT> = TestSlottedStimulation(
        slotStimulations = slotStimulationScenarios.map { stimulationScenario: TestStimulationScenario ->
            stimulationScenario.bind(stimulationMap = stimulationMap)
        },
    )
}

val TestSlottedStimulationScenario<TestSlotCount.Count1Plus>.slotStimulation0: TestStimulationScenario
    get() = slotStimulationScenarios[0]

val TestSlottedStimulationScenario<TestSlotCount.Count2Plus>.slotStimulation1: TestStimulationScenario
    get() = slotStimulationScenarios[1]

val TestSlottedStimulationScenario<TestSlotCount.Count3Plus>.slotStimulation2: TestStimulationScenario
    get() = slotStimulationScenarios[2]

val TestSlottedStimulationScenario<TestSlotCount.Count4Plus>.slotStimulation3: TestStimulationScenario
    get() = slotStimulationScenarios[3]

val TestSlottedStimulationScenario<TestSlotCount.Count5Plus>.slotStimulation4: TestStimulationScenario
    get() = slotStimulationScenarios[4]
