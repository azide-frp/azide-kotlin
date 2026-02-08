package dev.azide.core.test_utils

import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulation

enum class TestStimulationSlot4 : TestStimulationSlot {
    Slot0, Slot1, Slot2, Slot3,
}

enum class TestSlotDispatcher1x4(
    override val slot: TestStimulationSlot4,
) : TestSlotDispatcher1xN {
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

enum class TestSlotDispatcher2x4(
    override val slotA: TestStimulationSlot4,
    override val slotB: TestStimulationSlot4,
) : TestSlotDispatcher2xN {
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
