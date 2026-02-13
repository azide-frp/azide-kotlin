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
import dev.azide.core.test_utils.schedule.Schedule_startRevoked_testUtils
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulationScenario
import dev.azide.core.test_utils.stimulation_combinatorics.bind
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("ClassName", "PrivatePropertyName")
class ReactiveList_syncing_startRevoked_tests {
    private typealias SuitableSlotCount = TestSlotCount.Count3

    private typealias SuitableTestSlottedStimulationScenario = TestSlottedStimulationScenario<SuitableSlotCount>

    private val slottedStimulationScenarioBank_sourceListChanges =
        ReactiveList_generic_testUtils.stimulationScenarioBank_sourceListChanges.distribute(slotCount = SuitableSlotCount)

    private val slottedStimulationScenarioBank_sourceListChangesRevoked =
        ReactiveList_generic_testUtils.stimulationScenarioBank_sourceListChangesRevoked.distribute(slotCount = SuitableSlotCount)

    private val slottedStimulationScenarioBank_sourceListChangesCorrected =
        ReactiveList_generic_testUtils.stimulationScenarioBank_sourceListChangesCorrected.distribute(slotCount = SuitableSlotCount)
    val originalTargetListContent = listOf(-1, -2, -3)

    @Test
    fun test_startRevoked() {
        val targetMutableList = originalTargetListContent.toMutableList()

        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(10, 20, 30),
        )

        val subjectSchedule: Schedule = sourceReactiveList.syncing(
            externalMutableList = targetMutableList,
        )

        Schedule_startRevoked_testUtils.executeStartTransaction(
            subjectSchedule = subjectSchedule,
            expectedTargetImpact = ExpectedImpact.expectUnmodified(
                externalList = targetMutableList,
            ),
        )

        assertEquals(
            expected = originalTargetListContent,
            actual = targetMutableList,
        )

        ReactiveList_syncing_testUtils.verifyEffectNotOngoing(
            sourceReactiveList = sourceReactiveList,
            targetMutableList = targetMutableList,
        )
    }

    @Test
    fun test_startRevoked_sourceUpdatesSimultaneously() {
        slottedStimulationScenarioBank_sourceListChanges.forEach { slottedStimulationScenario ->
            test_startRevoked_sourceUpdatesSimultaneously(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_startRevoked_sourceUpdatesSimultaneously(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetMutableList = originalTargetListContent.toMutableList()

        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(10, 20, 30),
        )

        val subjectSchedule: Schedule = sourceReactiveList.syncing(
            externalMutableList = targetMutableList,
        )

        Schedule_startRevoked_testUtils.executeStartTransaction(
            subjectSchedule = subjectSchedule,
            slottedInputStimulation = sourceReactiveList.changing(
                tag = SourceReactiveListTag,
                changeDescription = ChangeDescription.of(
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
            expected = originalTargetListContent,
            actual = targetMutableList,
        )

        ReactiveList_syncing_testUtils.verifyEffectNotOngoing(
            sourceReactiveList = sourceReactiveList,
            targetMutableList = targetMutableList,
        )
    }

    @Test
    fun test_startRevoked_sourceUpdatesRevokedSimultaneously() {
        slottedStimulationScenarioBank_sourceListChangesRevoked.forEach { slottedStimulationScenario ->
            test_startRevoked_sourceUpdatesRevokedSimultaneously(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_startRevoked_sourceUpdatesRevokedSimultaneously(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetMutableList = originalTargetListContent.toMutableList()

        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(10, 20, 30),
        )

        val subjectSchedule: Schedule = sourceReactiveList.syncing(
            externalMutableList = targetMutableList,
        )

        Schedule_startRevoked_testUtils.executeStartTransaction(
            subjectSchedule = subjectSchedule,
            slottedInputStimulation = sourceReactiveList.revokingChange(
                tag = SourceReactiveListTag,
                temporaryChangeDescription = ChangeDescription.of(
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
            expected = originalTargetListContent,
            actual = targetMutableList,
        )

        ReactiveList_syncing_testUtils.verifyEffectNotOngoing(
            sourceReactiveList = sourceReactiveList,
            targetMutableList = targetMutableList,
        )
    }

    @Test
    fun test_startRevoked_sourceUpdatesCorrectedSimultaneously() {
        slottedStimulationScenarioBank_sourceListChangesCorrected.forEach { slottedStimulationScenario ->
            test_startRevoked_sourceUpdatesCorrectedSimultaneously(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_startRevoked_sourceUpdatesCorrectedSimultaneously(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetMutableList = originalTargetListContent.toMutableList()

        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(10, 20, 30),
        )

        val subjectSchedule: Schedule = sourceReactiveList.syncing(
            externalMutableList = targetMutableList,
        )

        Schedule_startRevoked_testUtils.executeStartTransaction(
            subjectSchedule = subjectSchedule,
            slottedInputStimulation = sourceReactiveList.correctingChange(
                tag = SourceReactiveListTag,
                intermediateChangeDescription = ChangeDescription.of(
                    ChangeDescription.Part.Insertion(
                        index = 2,
                        newElements = listOf(21, 22, 23),
                    ),
                ),
                correctedChangeDescription = ChangeDescription.of(
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
            expected = originalTargetListContent,
            actual = targetMutableList,
        )

        ReactiveList_syncing_testUtils.verifyEffectNotOngoing(
            sourceReactiveList = sourceReactiveList,
            targetMutableList = targetMutableList,
        )
    }
}
