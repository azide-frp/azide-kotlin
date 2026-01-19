package dev.azide.core.test_utils

interface TestSlotDispatcher1xN {
    val slot: TestStimulationSlot
}

fun TestSlotDispatcher1xN.dispatch(
    testStimulation: TestInputStimulation,
    slot: TestStimulationSlot,
): TestInputStimulation? = when (slot) {
    this@dispatch.slot -> testStimulation

    else -> null
}
