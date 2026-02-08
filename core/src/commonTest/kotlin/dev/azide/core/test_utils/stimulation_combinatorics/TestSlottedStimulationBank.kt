package dev.azide.core.test_utils.stimulation_combinatorics

data class TestSlottedStimulationBank<SlotCountT : TestSlotCount>(
    val slottedStimulationScenarios: List<TestSlottedStimulationScenario<SlotCountT>>,
) {
    fun bind(
        stimulationMap: TestStimulationMap,
    ): Sequence<TestSlottedStimulation<SlotCountT>> = slottedStimulationScenarios.asSequence()
        .map { slottedStimulationScenario: TestSlottedStimulationScenario<SlotCountT> ->
            stimulationMap.bind(slottedStimulationScenario)
        }

    fun forEach(
        block: (TestSlottedStimulationScenario<SlotCountT>) -> Unit,
    ) {
        slottedStimulationScenarios.forEach(block)
    }
}
