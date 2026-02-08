package dev.azide.core.test_utils

import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulation

enum class TestStimulationSlot2 : TestStimulationSlot {
    Slot0, Slot1,
}

enum class TestSlottedStimulationScenario2x2(
    val slotA: TestStimulationSlot2,
    val slotB: TestStimulationSlot2,
) {
    Case00(
        TestStimulationSlot2.Slot0,
        TestStimulationSlot2.Slot0,
    ),

    Case01(
        TestStimulationSlot2.Slot0,
        TestStimulationSlot2.Slot1,
    ),

    Case11(
        TestStimulationSlot2.Slot1,
        TestStimulationSlot2.Slot1,
    ),
}

typealias TestSlottedStimulation2 = TestSlottedStimulation<TestSlotCount.Count2>
