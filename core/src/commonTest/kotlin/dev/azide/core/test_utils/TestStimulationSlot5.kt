package dev.azide.core.test_utils

import dev.azide.core.impl.Transactions

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

interface TestSlottedStimulation5 {
    fun stimulate(
        propagationContext: Transactions.PropagationContext,
        slot: TestStimulationSlot5,
    )
}

fun TestInputStimulation.bind(
    dispatcher: TestSlotDispatcher1x5,
): TestSlottedStimulation5 = object : TestSlottedStimulation5 {
    override fun stimulate(
        propagationContext: Transactions.PropagationContext,
        slot: TestStimulationSlot5,
    ) {
        dispatcher.dispatch(
            testStimulation = this@bind,
            slot = slot,
        )?.stimulate(
            propagationContext = propagationContext,
        )
    }
}

fun DoubleTestStimulation.bind(
    dispatcher: TestSlotDispatcher2x5,
): TestSlottedStimulation5 = object : TestSlottedStimulation5 {
    override fun stimulate(
        propagationContext: Transactions.PropagationContext,
        slot: TestStimulationSlot5,
    ) {
        dispatcher.dispatch(
            orderedTestStimulation = this@bind,
            slot = slot,
        )?.stimulate(
            propagationContext = propagationContext,
        )
    }
}
