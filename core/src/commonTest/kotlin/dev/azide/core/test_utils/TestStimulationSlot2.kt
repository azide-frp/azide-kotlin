package dev.azide.core.test_utils

import dev.azide.core.impl.Transactions

enum class TestStimulationSlot2 : TestStimulationSlot {
    Slot0, Slot1,
}

enum class TestSlotDispatcher1x2(
    override val slot: TestStimulationSlot2,
) : TestSlotDispatcher1xN {
    Case0(
        TestStimulationSlot2.Slot0,
    ),

    Case1(
        TestStimulationSlot2.Slot1,
    ),
}

enum class TestSlotDispatcher2x2(
    override val slotA: TestStimulationSlot2,
    override val slotB: TestStimulationSlot2,
) : TestSlotDispatcher2xN {
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

interface TestSlottedStimulation2 {
    fun stimulate(
        propagationContext: Transactions.PropagationContext,
        slot: TestStimulationSlot2,
    )
}

fun TestStimulation.bind(
    dispatcher: TestSlotDispatcher1x2,
): TestSlottedStimulation2 = object : TestSlottedStimulation2 {
    override fun stimulate(
        propagationContext: Transactions.PropagationContext,
        slot: TestStimulationSlot2,
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
    dispatcher: TestSlotDispatcher2x2,
): TestSlottedStimulation2 = object : TestSlottedStimulation2 {
    override fun stimulate(
        propagationContext: Transactions.PropagationContext,
        slot: TestStimulationSlot2,
    ) {
        dispatcher.dispatch(
            orderedTestStimulation = this@bind,
            slot = slot,
        )?.stimulate(
            propagationContext = propagationContext,
        )
    }
}
