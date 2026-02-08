package dev.azide.core.test_utils

import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulation

enum class TestStimulationSlot3 : TestStimulationSlot {
    Slot0, Slot1, Slot2,
}

enum class TestSlotDispatcher1x3(
    override val slot: TestStimulationSlot3,
) : TestSlotDispatcher1xN {
    Case0(
        TestStimulationSlot3.Slot0,
    ),

    Case1(
        TestStimulationSlot3.Slot1,
    ),

    Case2(
        TestStimulationSlot3.Slot2,
    ),
}

enum class TestSlotDispatcher2x3(
    override val slotA: TestStimulationSlot3,
    override val slotB: TestStimulationSlot3,
) : TestSlotDispatcher2xN {
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
