package dev.azide.core.collections.reactive_list

import dev.azide.core.Schedule
import dev.azide.core.collections.reactive_list.ReactiveList_generic_testUtils.SourceReactiveListTag
import dev.azide.core.collections.syncing
import dev.azide.core.test_utils.collections.reactive_list.TestInputReactiveList
import dev.azide.core.test_utils.collections.reactive_list.TestInputReactiveList.ChangeDescription
import dev.azide.core.test_utils.collections.reactive_list.changing
import dev.azide.core.test_utils.collections.reactive_list.correctingChange
import dev.azide.core.test_utils.collections.reactive_list.revokingChange
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.schedule.Schedule_start_quickCancelledRevoked_testUtils
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulationScenario
import dev.azide.core.test_utils.stimulation_combinatorics.bind
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("ClassName", "PrivatePropertyName")
class ReactiveList_syncing_start_quickCancelledRevoked_tests {
    private typealias SuitableSlotCount = TestSlotCount.Count4

    private typealias SuitableTestSlottedStimulationScenario = TestSlottedStimulationScenario<SuitableSlotCount>

    private val slottedStimulationBank_sourceListChanges =
        ReactiveList_generic_testUtils.stimulationBank_sourceListChanges.distribute(slotCount = SuitableSlotCount)

    private val slottedStimulationBank_sourceListChangesRevoked =
        ReactiveList_generic_testUtils.stimulationBank_sourceListChangesRevoked.distribute(slotCount = SuitableSlotCount)

    private val slottedStimulationBank_sourceListChangesCorrected =
        ReactiveList_generic_testUtils.stimulationBank_sourceListChangesCorrected.distribute(slotCount = SuitableSlotCount)

    @Test
    fun test_start_quickCancelledRevoked() {
        val targetMutableList = mutableListOf<Int>()

        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(10, 20, 30),
        )

        val subjectSchedule: Schedule = sourceReactiveList.syncing(
            externalMutableList = targetMutableList,
        )

        Schedule_start_quickCancelledRevoked_testUtils.executeStartTransaction(
            subjectSchedule = subjectSchedule,
            expectedTargetImpact = ExpectedImpact.expectUnmodified(
                externalList = targetMutableList,
            ),
        )

        assertEquals(
            expected = listOf(10, 20, 30),
            actual = targetMutableList,
        )

        ReactiveList_syncing_testUtils.verifyEffectOngoing(
            sourceReactiveList = sourceReactiveList,
            targetMutableList = targetMutableList,
        )
    }

    @Test
    fun test_start_quickCancelledRevoked_sourceUpdatesSimultaneously() {
        slottedStimulationBank_sourceListChanges.forEach { slottedStimulationScenario ->
            test_start_quickCancelledRevoked_sourceUpdatesSimultaneously(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_start_quickCancelledRevoked_sourceUpdatesSimultaneously(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetMutableList = mutableListOf<Int>()

        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(10, 20, 30),
        )

        val subjectSchedule: Schedule = sourceReactiveList.syncing(
            externalMutableList = targetMutableList,
        )

        Schedule_start_quickCancelledRevoked_testUtils.executeStartTransaction(
            subjectSchedule = subjectSchedule,
            slottedInputStimulation = sourceReactiveList.changing(
                tag = SourceReactiveListTag,
                description = ChangeDescription.of(
                    ChangeDescription.Part.Insertion(
                        index = 2,
                        newElements = listOf(21, 22, 23),
                    ),
                ),
            ).bind(slottedStimulationScenario),
            expectedTargetImpact = ExpectedImpact.expectUnmodified(
                externalList = targetMutableList,
            ),
        )

        assertEquals(
            expected = listOf(10, 20, 21, 22, 23, 30),
            actual = targetMutableList,
        )

        ReactiveList_syncing_testUtils.verifyEffectOngoing(
            sourceReactiveList = sourceReactiveList,
            targetMutableList = targetMutableList,
        )
    }

    @Test
    fun test_start_quickCancelledRevoked_sourceUpdatesRevokedSimultaneously() {
        slottedStimulationBank_sourceListChangesRevoked.forEach { slottedStimulationScenario ->
            test_start_quickCancelledRevoked_sourceUpdatesRevokedSimultaneously(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_start_quickCancelledRevoked_sourceUpdatesRevokedSimultaneously(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetMutableList = mutableListOf<Int>()

        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(10, 20, 30),
        )

        val subjectSchedule: Schedule = sourceReactiveList.syncing(
            externalMutableList = targetMutableList,
        )

        Schedule_start_quickCancelledRevoked_testUtils.executeStartTransaction(
            subjectSchedule = subjectSchedule,
            slottedInputStimulation = sourceReactiveList.revokingChange(
                tag = SourceReactiveListTag,
                intermediateDescription = ChangeDescription.of(
                    ChangeDescription.Part.Insertion(
                        index = 2,
                        newElements = listOf(21, 22, 23),
                    ),
                ),
            ).bind(slottedStimulationScenario),
            expectedTargetImpact = ExpectedImpact.expectUnmodified(
                externalList = targetMutableList,
            ),
        )

        assertEquals(
            expected = listOf(10, 20, 30),
            actual = targetMutableList,
        )

        ReactiveList_syncing_testUtils.verifyEffectOngoing(
            sourceReactiveList = sourceReactiveList,
            targetMutableList = targetMutableList,
        )
    }

    @Test
    fun test_start_quickCancelledRevoked_sourceUpdatesCorrectedSimultaneously() {
        slottedStimulationBank_sourceListChangesCorrected.forEach { slottedStimulationScenario ->
            test_start_quickCancelledRevoked_sourceUpdatesCorrectedSimultaneously(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_start_quickCancelledRevoked_sourceUpdatesCorrectedSimultaneously(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetMutableList = mutableListOf<Int>()

        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(10, 20, 30),
        )

        val subjectSchedule: Schedule = sourceReactiveList.syncing(
            externalMutableList = targetMutableList,
        )

        Schedule_start_quickCancelledRevoked_testUtils.executeStartTransaction(
            subjectSchedule = subjectSchedule,
            slottedInputStimulation = sourceReactiveList.correctingChange(
                tag = SourceReactiveListTag,
                intermediateDescription = ChangeDescription.of(
                    ChangeDescription.Part.Insertion(
                        index = 2,
                        newElements = listOf(21, 22, 23),
                    ),
                ),
                correctedDescription = ChangeDescription.of(
                    ChangeDescription.Part.Insertion(
                        index = 2,
                        newElements = listOf(24, 25),
                    ),
                ),
            ).bind(slottedStimulationScenario),
            expectedTargetImpact = ExpectedImpact.expectUnmodified(
                externalList = targetMutableList,
            ),
        )

        assertEquals(
            expected = listOf(10, 20, 24, 25, 30),
            actual = targetMutableList,
        )

        ReactiveList_syncing_testUtils.verifyEffectOngoing(
            sourceReactiveList = sourceReactiveList,
            targetMutableList = targetMutableList,
        )
    }
}
