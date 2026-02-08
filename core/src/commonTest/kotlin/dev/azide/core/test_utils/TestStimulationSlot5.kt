package dev.azide.core.test_utils

import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulation

enum class TestStimulationSlot5 : TestStimulationSlot {
    Slot0, Slot1, Slot2, Slot3, Slot4,
}

enum class TestSlotDispatcher1x5(
    override val slot: TestStimulationSlot5,
) : TestSlotDispatcher1xN {
    Case0(
        TestStimulationSlot5.Slot0,
    ),

    Case1(
        TestStimulationSlot5.Slot1,
    ),

    Case2(
        TestStimulationSlot5.Slot2,
    ),

    Case3(
        TestStimulationSlot5.Slot3,
    ),

    Case4(
        TestStimulationSlot5.Slot4,
    ),
}

enum class TestSlotDispatcher2x5(
    override val slotA: TestStimulationSlot5,
    override val slotB: TestStimulationSlot5,
) : TestSlotDispatcher2xN {
    Case00(
        TestStimulationSlot5.Slot0,
        TestStimulationSlot5.Slot0,
    ),

    Case01(
        TestStimulationSlot5.Slot0,
        TestStimulationSlot5.Slot1,
    ),

    Case02(
        TestStimulationSlot5.Slot0,
        TestStimulationSlot5.Slot2,
    ),

    Case03(
        TestStimulationSlot5.Slot0,
        TestStimulationSlot5.Slot3,
    ),

    Case04(
        TestStimulationSlot5.Slot0,
        TestStimulationSlot5.Slot4,
    ),

    Case11(
        TestStimulationSlot5.Slot1,
        TestStimulationSlot5.Slot1,
    ),

    Case12(
        TestStimulationSlot5.Slot1,
        TestStimulationSlot5.Slot2,
    ),

    Case13(
        TestStimulationSlot5.Slot1,
        TestStimulationSlot5.Slot3,
    ),

    Case14(
        TestStimulationSlot5.Slot1,
        TestStimulationSlot5.Slot4,
    ),

    Case22(
        TestStimulationSlot5.Slot2,
        TestStimulationSlot5.Slot2,
    ),

    Case23(
        TestStimulationSlot5.Slot2,
        TestStimulationSlot5.Slot3,
    ),

    Case24(
        TestStimulationSlot5.Slot2,
        TestStimulationSlot5.Slot4,
    ),

    Case33(
        TestStimulationSlot5.Slot3,
        TestStimulationSlot5.Slot3,
    ),

    Case34(
        TestStimulationSlot5.Slot3,
        TestStimulationSlot5.Slot4,
    ),

    Case44(
        TestStimulationSlot5.Slot4,
        TestStimulationSlot5.Slot4,
    ),
}

typealias TestSlottedStimulation5 = TestSlottedStimulation<TestSlotCount.Count5>
