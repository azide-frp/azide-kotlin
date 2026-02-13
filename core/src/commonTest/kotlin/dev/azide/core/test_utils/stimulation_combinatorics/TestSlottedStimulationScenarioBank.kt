package dev.azide.core.test_utils.stimulation_combinatorics

data class TestSlottedStimulationScenarioBank<SlotCountT : TestSlotCount>(
    private val slottedStimulationScenarios: List<TestSlottedStimulationScenario<SlotCountT>>,
) {
    fun first(): TestSlottedStimulationScenario<SlotCountT> = slottedStimulationScenarios[0]

    fun bind(
        stimulationMap: TestStimulationMap,
    ): Sequence<TestSlottedStimulation<SlotCountT>> = slottedStimulationScenarios.asSequence()
        .map { slottedStimulationScenario: TestSlottedStimulationScenario<SlotCountT> ->
            stimulationMap.bind(slottedStimulationScenario)
        }

    fun forEachIndexed(
        block: (index: Int, TestSlottedStimulationScenario<SlotCountT>) -> Unit,
    ) {
        slottedStimulationScenarios.forEachIndexed(block)
    }

    fun forEach(
        block: (TestSlottedStimulationScenario<SlotCountT>) -> Unit,
    ) {
        slottedStimulationScenarios.forEach(block)
    }
}
