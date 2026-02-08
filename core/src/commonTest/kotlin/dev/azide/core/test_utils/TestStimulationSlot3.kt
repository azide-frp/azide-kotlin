package dev.azide.core.test_utils

import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulation

enum class TestStimulationSlot3 : TestStimulationSlot {
    Slot0, Slot1, Slot2,
}

enum class TestSlottedStimulationScenario2x3(
    val slotA: TestStimulationSlot3,
    val slotB: TestStimulationSlot3,
) {
    Case00(
        TestStimulationSlot3.Slot0,
        TestStimulationSlot3.Slot0,
    ),

    Case01(
        TestStimulationSlot3.Slot0,
        TestStimulationSlot3.Slot1,
    ),

    Case02(
        TestStimulationSlot3.Slot0,
        TestStimulationSlot3.Slot2,
    ),

    Case11(
        TestStimulationSlot3.Slot1,
        TestStimulationSlot3.Slot1,
    ),

    Case12(
        TestStimulationSlot3.Slot1,
        TestStimulationSlot3.Slot2,
    ),

    Case22(
        TestStimulationSlot3.Slot2,
        TestStimulationSlot3.Slot2,
    ),
}

typealias TestSlottedStimulation3 = TestSlottedStimulation<TestSlotCount.Count3>
