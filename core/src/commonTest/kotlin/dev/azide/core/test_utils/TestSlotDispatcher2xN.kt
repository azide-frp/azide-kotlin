package dev.azide.core.test_utils

interface TestSlotDispatcher2xN {
    val slotA: TestStimulationSlot
    val slotB: TestStimulationSlot
}

fun TestSlotDispatcher2xN.dispatch(
    orderedTestStimulation: DoubleTestStimulation,
    slot: TestStimulationSlot,
): TestInputStimulation? = when (slot) {
    slotA if slotA == slotB -> TestInputStimulation.combine(
        orderedTestStimulation.firstStimulation,
        orderedTestStimulation.secondStimulation,
    )

    slotA -> orderedTestStimulation.firstStimulation

    slotB -> orderedTestStimulation.secondStimulation

    else -> null
}
