package dev.azide.core.test_utils

import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulation

fun TestStimulation.bind(
    slottedStimulationScenario: TestSlottedStimulationScenario1x2,
): TestSlottedStimulation2 = TestSlottedStimulationBuilder(TestSlotCount.Count2).apply {
    add(
        slot = slottedStimulationScenario.slot,
        testStimulation = this@bind,
    )
}.build()

fun DoubleTestStimulation.bind(
    slottedStimulationScenario: TestSlottedStimulationScenario2x2,
): TestSlottedStimulation2 = TestSlottedStimulationBuilder(TestSlotCount.Count2).apply {
    add(
        slot = slottedStimulationScenario.slotA,
        testStimulation = this@bind.firstStimulation,
    )

    add(
        slot = slottedStimulationScenario.slotB,
        testStimulation = this@bind.secondStimulation,
    )
}.build()

fun TestStimulation.bind(
    slottedStimulationScenario: TestSlottedStimulationScenario1x3,
): TestSlottedStimulation3 = TestSlottedStimulationBuilder(TestSlotCount.Count3).apply {
    add(
        slot = slottedStimulationScenario.slot,
        testStimulation = this@bind,
    )
}.build()

fun DoubleTestStimulation.bind(
    slottedStimulationScenario: TestSlottedStimulationScenario2x3,
): TestSlottedStimulation3 = TestSlottedStimulationBuilder(TestSlotCount.Count3).apply {
    add(
        slot = slottedStimulationScenario.slotA,
        testStimulation = this@bind.firstStimulation,
    )

    add(
        slot = slottedStimulationScenario.slotB,
        testStimulation = this@bind.secondStimulation,
    )
}.build()

fun TestStimulation.bind(
    slottedStimulationScenario: TestSlottedStimulationScenario1x4,
): TestSlottedStimulation4 = TestSlottedStimulationBuilder(TestSlotCount.Count4).apply {
    add(
        slot = slottedStimulationScenario.slot,
        testStimulation = this@bind,
    )
}.build()

fun DoubleTestStimulation.bind(
    slottedStimulationScenario: TestSlottedStimulationScenario2x4,
): TestSlottedStimulation4 = TestSlottedStimulationBuilder(TestSlotCount.Count4).apply {
    add(
        slot = slottedStimulationScenario.slotA,
        testStimulation = this@bind.firstStimulation,
    )

    add(
        slot = slottedStimulationScenario.slotB,
        testStimulation = this@bind.secondStimulation,
    )
}.build()

fun TestStimulation.bind(
    slottedStimulationScenario: TestSlottedStimulationScenario1x5,
): TestSlottedStimulation5 = TestSlottedStimulationBuilder(TestSlotCount.Count5).apply {
    add(
        slot = slottedStimulationScenario.slot,
        testStimulation = this@bind,
    )
}.build()

fun DoubleTestStimulation.bind(
    slottedStimulationScenario: TestSlottedStimulationScenario2x5,
): TestSlottedStimulation5 = TestSlottedStimulationBuilder(TestSlotCount.Count5).apply {
    add(
        slot = slottedStimulationScenario.slotA,
        testStimulation = this@bind.firstStimulation,
    )

    add(
        slot = slottedStimulationScenario.slotB,
        testStimulation = this@bind.secondStimulation,
    )
}.build()

private class MutableCombinedTestStimulation : TestStimulation {
    private val testStimulations = mutableListOf<TestStimulation>()

    fun append(
        testStimulation: TestStimulation,
    ) {
        testStimulations.add(testStimulation)
    }

    override fun stimulate(
        propagationContext: PropagationContext,
    ) {
        testStimulations.forEach { testStimulation ->
            testStimulation.stimulate(propagationContext = propagationContext)
        }
    }
}

private class TestSlottedStimulationBuilder<SlotCountT : TestSlotCount>(
    slotCount: SlotCountT,
) {
    private val combinedSlotStimulations = MutableList(slotCount.count) {
        MutableCombinedTestStimulation()
    }

    fun add(
        slot: TestStimulationSlot,
        testStimulation: TestStimulation,
    ) {
        combinedSlotStimulations[slot.ordinal].append(testStimulation)
    }

    fun build(): TestSlottedStimulation<SlotCountT> = TestSlottedStimulation(
        slotStimulations = combinedSlotStimulations,
    )
}
