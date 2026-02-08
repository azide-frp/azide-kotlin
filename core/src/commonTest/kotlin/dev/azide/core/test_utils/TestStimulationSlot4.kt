package dev.azide.core.test_utils

import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulation

enum class TestStimulationSlot4 : TestStimulationSlot {
    Slot0, Slot1, Slot2, Slot3,
}

enum class TestSlottedStimulationScenario1x4(
    val slot: TestStimulationSlot4,
) {
    Case0(
        TestStimulationSlot4.Slot0,
    ),

    Case1(
        TestStimulationSlot4.Slot1,
    ),

    Case2(
        TestStimulationSlot4.Slot2,
    ),

    Case3(
        TestStimulationSlot4.Slot3,
    ),
}

enum class TestSlottedStimulationScenario2x4(
    val slotA: TestStimulationSlot4,
    val slotB: TestStimulationSlot4,
) {
    Case00(
        TestStimulationSlot4.Slot0,
        TestStimulationSlot4.Slot0,
    ),

    Case01(
        TestStimulationSlot4.Slot0,
        TestStimulationSlot4.Slot1,
    ),

    Case02(
        TestStimulationSlot4.Slot0,
        TestStimulationSlot4.Slot2,
    ),

    Case03(
        TestStimulationSlot4.Slot0,
        TestStimulationSlot4.Slot3,
    ),

    Case11(
        TestStimulationSlot4.Slot1,
        TestStimulationSlot4.Slot1,
    ),

    Case12(
        TestStimulationSlot4.Slot1,
        TestStimulationSlot4.Slot2,
    ),

    Case13(
        TestStimulationSlot4.Slot1,
        TestStimulationSlot4.Slot3,
    ),

    Case22(
        TestStimulationSlot4.Slot2,
        TestStimulationSlot4.Slot2,
    ),

    Case23(
        TestStimulationSlot4.Slot2,
        TestStimulationSlot4.Slot3,
    ),

    Case33(
        TestStimulationSlot4.Slot3,
        TestStimulationSlot4.Slot3,
    ),
}

typealias TestSlottedStimulation4 = TestSlottedStimulation<TestSlotCount.Count4>
