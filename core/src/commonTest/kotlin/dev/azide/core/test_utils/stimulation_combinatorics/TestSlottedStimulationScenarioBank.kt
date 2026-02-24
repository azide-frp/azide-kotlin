package dev.azide.core.test_utils.stimulation_combinatorics

data class TestSlottedStimulationScenarioBank<SlotCountT : TestSlotCount>(
    private val slottedStimulationScenarios: Sequence<TestSlottedStimulationScenario<SlotCountT>>,
) {
    fun first(): TestSlottedStimulationScenario<SlotCountT> = slottedStimulationScenarios.first()

    fun bind(
        stimulationMap: TestStimulationMap_deprecated,
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
