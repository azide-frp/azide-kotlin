package dev.azide.core.cell

import dev.azide.core.Schedule
import dev.azide.core.test_utils.TestTargetActionRecorder
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.cell.correctingUpdate
import dev.azide.core.test_utils.cell.revokingUpdate
import dev.azide.core.test_utils.cell.updating
import dev.azide.core.test_utils.expectIsNotExecuted
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.schedule.Schedule_startRevoked_quickCancelledRevoked_testUtils
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulationScenario
import dev.azide.core.test_utils.stimulation_combinatorics.bind
import dev.azide.core.triggerEvery
import kotlin.test.Test

@Suppress("ClassName", "PrivatePropertyName")
class Cell_triggerEvery_startRevoked_quickCancelledRevoked_tests {
    private typealias SuitableSlotCount = TestSlotCount.Count5

    private typealias SuitableTestSlottedStimulationScenario = TestSlottedStimulationScenario<SuitableSlotCount>

    private val slottedStimulationBank_sourceTriggerCellUpdates =
        Cell_triggerEvery_testUtils.stimulationBank_sourceTriggerCellUpdates.distribute(slotCount = SuitableSlotCount)

    private val slottedStimulationBank_sourceTriggerCellUpdatesRevoked =
        Cell_triggerEvery_testUtils.stimulationBank_sourceTriggerCellUpdatesRevoked.distribute(slotCount = SuitableSlotCount)

    private val slottedStimulationBank_sourceTriggerCellUpdatesCorrected =
        Cell_triggerEvery_testUtils.stimulationBank_sourceTriggerCellUpdatesCorrected.distribute(slotCount = SuitableSlotCount)

    @Test
    fun test_startRevoked_quickCancelledRevoked() {
        val targetTriggerRecorder1 = TestTargetActionRecorder.TriggerRecorder()

        val sourceCell = TestInputCell(
            initialValue = targetTriggerRecorder1.recordedAction,
        )

        val subjectSchedule: Schedule = sourceCell.triggerEvery()

        Schedule_startRevoked_quickCancelledRevoked_testUtils.executeStartTransaction(
            subjectSchedule = subjectSchedule,
            expectedTargetImpact = ExpectedImpact.combine(
                targetTriggerRecorder1.expectIsNotExecuted(),
            ),
        )

        Cell_triggerEvery_testUtils.verifyScheduleNotOngoing(
            sourceTriggerCell = sourceCell,
        )
    }

    @Test
    fun test_startRevoked_quickCancelledRevoked_sourceUpdatesSimultaneously() {
        slottedStimulationBank_sourceTriggerCellUpdates.forEach { slottedStimulationScenario ->
            test_startRevoked_quickCancelledRevoked_sourceUpdatesSimultaneously(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_startRevoked_quickCancelledRevoked_sourceUpdatesSimultaneously(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetTriggerRecorder1 = TestTargetActionRecorder.TriggerRecorder()
        val targetTriggerRecorder2 = TestTargetActionRecorder.TriggerRecorder()

        val sourceCell = TestInputCell(
            initialValue = targetTriggerRecorder1.recordedAction,
        )

        val subjectSchedule: Schedule = sourceCell.triggerEvery()

        Schedule_startRevoked_quickCancelledRevoked_testUtils.executeStartTransaction(
            subjectSchedule = subjectSchedule,
            slottedInputStimulation = sourceCell.updating(
                tag = Cell_triggerEvery_testUtils.SourceTriggerCellTag,
                newValue = targetTriggerRecorder2.recordedAction,
            ).bind(slottedStimulationScenario),
            expectedTargetImpact = ExpectedImpact.combine(
                targetTriggerRecorder1.expectIsNotExecuted(),
                targetTriggerRecorder2.expectIsNotExecuted(),
            ),
        )

        Cell_triggerEvery_testUtils.verifyScheduleNotOngoing(
            sourceTriggerCell = sourceCell,
        )
    }

    @Test
    fun test_startRevoked_quickCancelledRevoked_sourceUpdatesRevokedSimultaneously() {
        slottedStimulationBank_sourceTriggerCellUpdatesRevoked.forEach { slottedStimulationScenario ->
            test_startRevoked_quickCancelledRevoked_sourceUpdatesRevokedSimultaneously(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_startRevoked_quickCancelledRevoked_sourceUpdatesRevokedSimultaneously(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetTriggerRecorder1 = TestTargetActionRecorder.TriggerRecorder()
        val targetTriggerRecorder2 = TestTargetActionRecorder.TriggerRecorder()

        val sourceCell = TestInputCell(
            initialValue = targetTriggerRecorder1.recordedAction,
        )

        val subjectSchedule: Schedule = sourceCell.triggerEvery()

        Schedule_startRevoked_quickCancelledRevoked_testUtils.executeStartTransaction(
            subjectSchedule = subjectSchedule,
            slottedInputStimulation = sourceCell.revokingUpdate(
                tag = Cell_triggerEvery_testUtils.SourceTriggerCellTag,
                newValue = targetTriggerRecorder2.recordedAction,
            ).bind(slottedStimulationScenario),
            expectedTargetImpact = ExpectedImpact.combine(
                targetTriggerRecorder1.expectIsNotExecuted(),
                targetTriggerRecorder2.expectIsNotExecuted(),
            ),
        )

        Cell_triggerEvery_testUtils.verifyScheduleNotOngoing(
            sourceTriggerCell = sourceCell,
        )
    }

    @Test
    fun test_startRevoked_quickCancelledRevoked_sourceUpdatesCorrectedSimultaneously() {
        slottedStimulationBank_sourceTriggerCellUpdatesCorrected.forEach { slottedStimulationScenario ->
            test_startRevoked_quickCancelledRevoked_sourceUpdatesCorrectedSimultaneously(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_startRevoked_quickCancelledRevoked_sourceUpdatesCorrectedSimultaneously(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetTriggerRecorder1 = TestTargetActionRecorder.TriggerRecorder()
        val targetTriggerRecorder2 = TestTargetActionRecorder.TriggerRecorder()
        val targetTriggerRecorder3 = TestTargetActionRecorder.TriggerRecorder()

        val sourceCell = TestInputCell(
            initialValue = targetTriggerRecorder1.recordedAction,
        )

        val subjectSchedule: Schedule = sourceCell.triggerEvery()

        Schedule_startRevoked_quickCancelledRevoked_testUtils.executeStartTransaction(
            subjectSchedule = subjectSchedule,
            slottedInputStimulation = sourceCell.correctingUpdate(
                tag = Cell_triggerEvery_testUtils.SourceTriggerCellTag,
                intermediateNewValue = targetTriggerRecorder2.recordedAction,
                correctedNewValue = targetTriggerRecorder3.recordedAction,
            ).bind(slottedStimulationScenario),
            expectedTargetImpact = ExpectedImpact.combine(
                targetTriggerRecorder1.expectIsNotExecuted(),
                targetTriggerRecorder2.expectIsNotExecuted(),
                targetTriggerRecorder3.expectIsNotExecuted(),
            ),
        )

        Cell_triggerEvery_testUtils.verifyScheduleNotOngoing(
            sourceTriggerCell = sourceCell,
        )
    }
}
