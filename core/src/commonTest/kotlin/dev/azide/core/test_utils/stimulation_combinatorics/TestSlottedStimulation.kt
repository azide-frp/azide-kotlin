package dev.azide.core.test_utils.stimulation_combinatorics

import dev.azide.core.impl.Transactions
import dev.azide.core.test_utils.TestSlottedStimulation2
import dev.azide.core.test_utils.TestSlottedStimulation3
import dev.azide.core.test_utils.TestSlottedStimulation4
import dev.azide.core.test_utils.TestSlottedStimulation5
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.TestStimulationSlot2
import dev.azide.core.test_utils.TestStimulationSlot3
import dev.azide.core.test_utils.TestStimulationSlot4
import dev.azide.core.test_utils.TestStimulationSlot5

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

val TestSlottedStimulation<TestSlotCount.Count2>.asTestSlottedStimulation2: TestSlottedStimulation2
    get() = object : TestSlottedStimulation2 {
        override fun stimulate(
            propagationContext: Transactions.PropagationContext,
            slot: TestStimulationSlot2,
        ) {
            when (slot) {
                TestStimulationSlot2.Slot0 -> slotStimulation0.stimulate(propagationContext)
                TestStimulationSlot2.Slot1 -> slotStimulation1.stimulate(propagationContext)
            }
        }
    }

val TestSlottedStimulation<TestSlotCount.Count3>.asTestSlottedStimulation3: TestSlottedStimulation3
    get() = object : TestSlottedStimulation3 {
        override fun stimulate(
            propagationContext: Transactions.PropagationContext,
            slot: TestStimulationSlot3,
        ) {
            when (slot) {
                TestStimulationSlot3.Slot0 -> slotStimulation0.stimulate(propagationContext)
                TestStimulationSlot3.Slot1 -> slotStimulation1.stimulate(propagationContext)
                TestStimulationSlot3.Slot2 -> slotStimulation2.stimulate(propagationContext)
            }
        }
    }

val TestSlottedStimulation<TestSlotCount.Count4>.asTestSlottedStimulation4: TestSlottedStimulation4
    get() = object : TestSlottedStimulation4 {
        override fun stimulate(
            propagationContext: Transactions.PropagationContext,
            slot: TestStimulationSlot4,
        ) {
            when (slot) {
                TestStimulationSlot4.Slot0 -> slotStimulation0.stimulate(propagationContext)
                TestStimulationSlot4.Slot1 -> slotStimulation1.stimulate(propagationContext)
                TestStimulationSlot4.Slot2 -> slotStimulation2.stimulate(propagationContext)
                TestStimulationSlot4.Slot3 -> slotStimulation3.stimulate(propagationContext)
            }
        }
    }

val TestSlottedStimulation<TestSlotCount.Count5>.asTestSlottedStimulation5: TestSlottedStimulation5
    get() = object : TestSlottedStimulation5 {
        override fun stimulate(
            propagationContext: Transactions.PropagationContext,
            slot: TestStimulationSlot5,
        ) {
            when (slot) {
                TestStimulationSlot5.Slot0 -> slotStimulation0.stimulate(propagationContext)
                TestStimulationSlot5.Slot1 -> slotStimulation1.stimulate(propagationContext)
                TestStimulationSlot5.Slot2 -> slotStimulation2.stimulate(propagationContext)
                TestStimulationSlot5.Slot3 -> slotStimulation3.stimulate(propagationContext)
                TestStimulationSlot5.Slot4 -> slotStimulation4.stimulate(propagationContext)
            }
        }
    }
