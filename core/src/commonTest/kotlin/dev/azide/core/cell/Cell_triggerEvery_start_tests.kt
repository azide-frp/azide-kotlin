package dev.azide.core.cell

import dev.azide.core.Schedule
import dev.azide.core.test_utils.TestTargetActionRecorder
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.cell.correctingUpdate
import dev.azide.core.test_utils.cell.revokingUpdate
import dev.azide.core.test_utils.cell.updating
import dev.azide.core.test_utils.expectIsExecutedOnce
import dev.azide.core.test_utils.expectIsNotExecuted
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.schedule.Schedule_start_testUtils
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.bind
import dev.azide.core.triggerEvery
import kotlin.test.Test

@Suppress("ClassName", "PrivatePropertyName")
class Cell_triggerEvery_start_tests {
    private typealias SuitableSlotCount = TestSlotCount.Count2

    private val slottedStimulationScenarioBank_sourceTriggerCellUpdates =
        Cell_triggerEvery_testUtils.stimulationScenarioBank_sourceTriggerCellUpdates.distribute(slotCount = SuitableSlotCount)

    private val slottedStimulationScenarioBank_sourceTriggerCellUpdatesRevoked =
        Cell_triggerEvery_testUtils.stimulationScenarioBank_sourceTriggerCellUpdatesRevoked.distribute(slotCount = SuitableSlotCount)

    private val slottedStimulationScenarioBank_sourceTriggerCellUpdatesCorrected =
        Cell_triggerEvery_testUtils.stimulationScenarioBank_sourceTriggerCellUpdatesCorrected.distribute(slotCount = SuitableSlotCount)

    @Test
    fun test_start() {
        val targetTriggerRecorder1 = TestTargetActionRecorder.TriggerRecorder()

        val sourceCell = TestInputCell(
            initialValue = targetTriggerRecorder1.recordedAction,
        )

        val subjectSchedule: Schedule = sourceCell.triggerEvery()

        Schedule_start_testUtils.executeStartTransaction(
            subjectSchedule = subjectSchedule,
            expectedTargetImpact = targetTriggerRecorder1.expectIsExecutedOnce(),
        )
    }

    @Test
    fun test_start_sourceUpdatesSimultaneously() {
        slottedStimulationScenarioBank_sourceTriggerCellUpdates.forEach { slottedStimulationScenario ->
            val targetTriggerRecorder1 = TestTargetActionRecorder.TriggerRecorder()
            val targetTriggerRecorder2 = TestTargetActionRecorder.TriggerRecorder()

            val sourceCell = TestInputCell(
                initialValue = targetTriggerRecorder1.recordedAction,
            )

            val subjectSchedule: Schedule = sourceCell.triggerEvery()

            Schedule_start_testUtils.executeStartTransaction(
                subjectSchedule = subjectSchedule,
                slottedInputStimulation = sourceCell.updating(
                    tag = Cell_triggerEvery_testUtils.SourceTriggerCellTag,
                    newValue = targetTriggerRecorder2.recordedAction,
                ).bind(slottedStimulationScenario),
                expectedTargetImpact = ExpectedImpact.combine(
                    targetTriggerRecorder1.expectIsExecutedOnce(),
                    targetTriggerRecorder2.expectIsExecutedOnce(),
                ),
            )
        }
    }

    @Test
    fun test_start_sourceUpdatesRevokedSimultaneously() {
        slottedStimulationScenarioBank_sourceTriggerCellUpdatesRevoked.forEach { slottedStimulationScenario ->
            val targetTriggerRecorder1 = TestTargetActionRecorder.TriggerRecorder()
            val targetTriggerRecorder2 = TestTargetActionRecorder.TriggerRecorder()

            val sourceCell = TestInputCell(
                initialValue = targetTriggerRecorder1.recordedAction,
            )

            val subjectSchedule: Schedule = sourceCell.triggerEvery()

            Schedule_start_testUtils.executeStartTransaction(
                subjectSchedule = subjectSchedule,
                slottedInputStimulation = sourceCell.revokingUpdate(
                    tag = Cell_triggerEvery_testUtils.SourceTriggerCellTag,
                    newValue = targetTriggerRecorder2.recordedAction,
                ).bind(slottedStimulationScenario),
                expectedTargetImpact = ExpectedImpact.combine(
                    targetTriggerRecorder1.expectIsExecutedOnce(),
                    targetTriggerRecorder2.expectIsNotExecuted(),
                ),
            )
        }
    }

    @Test
    fun test_start_sourceUpdatesCorrectedSimultaneously() {
        slottedStimulationScenarioBank_sourceTriggerCellUpdatesCorrected.forEach { slottedStimulationScenario ->
            val targetTriggerRecorder1 = TestTargetActionRecorder.TriggerRecorder()
            val targetTriggerRecorder2 = TestTargetActionRecorder.TriggerRecorder()
            val targetTriggerRecorder3 = TestTargetActionRecorder.TriggerRecorder()

            val sourceCell = TestInputCell(
                initialValue = targetTriggerRecorder1.recordedAction,
            )

            val subjectSchedule: Schedule = sourceCell.triggerEvery()

            Schedule_start_testUtils.executeStartTransaction(
                subjectSchedule = subjectSchedule,
                slottedInputStimulation = sourceCell.correctingUpdate(
                    tag = Cell_triggerEvery_testUtils.SourceTriggerCellTag,
                    intermediateNewValue = targetTriggerRecorder2.recordedAction,
                    correctedNewValue = targetTriggerRecorder3.recordedAction,
                ).bind(slottedStimulationScenario),
                expectedTargetImpact = ExpectedImpact.combine(
                    targetTriggerRecorder1.expectIsExecutedOnce(),
                    targetTriggerRecorder2.expectIsNotExecuted(),
                    targetTriggerRecorder3.expectIsExecutedOnce(),
                ),
            )
        }
    }
}
