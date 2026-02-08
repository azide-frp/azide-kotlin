package dev.azide.core.test_utils.stimulation_combinatorics

import dev.azide.core.test_utils.TestStimulation

data class TestSlottedStimulation<@Suppress("unused") out CountT : TestSlotCount>(
    val slotStimulations: List<TestStimulation>,
)

val TestSlottedStimulation<TestSlotCount.Count1Plus>.slotStimulation0: TestStimulation
    get() = slotStimulations[0]

val TestSlottedStimulation<TestSlotCount.Count2Plus>.slotStimulation1: TestStimulation
    get() = slotStimulations[1]

val TestSlottedStimulation<TestSlotCount.Count3Plus>.slotStimulation2: TestStimulation
    get() = slotStimulations[2]

val TestSlottedStimulation<TestSlotCount.Count4Plus>.slotStimulation3: TestStimulation
    get() = slotStimulations[3]

val TestSlottedStimulation<TestSlotCount.Count5Plus>.slotStimulation4: TestStimulation
    get() = slotStimulations[4]

