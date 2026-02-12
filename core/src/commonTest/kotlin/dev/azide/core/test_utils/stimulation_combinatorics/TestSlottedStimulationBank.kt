package dev.azide.core.test_utils.stimulation_combinatorics

data class TestSlottedStimulationBank<SlotCountT : TestSlotCount>(
    val slottedStimulationScenarios: List<TestSlottedStimulationScenario<SlotCountT>>,
) {
    fun get(
        index: Int,
    ): TestSlottedStimulationScenario<SlotCountT> = slottedStimulationScenarios[index]

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
