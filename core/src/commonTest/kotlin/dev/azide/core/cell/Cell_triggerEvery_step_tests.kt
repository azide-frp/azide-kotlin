package dev.azide.core.cell

import dev.azide.core.Schedule
import dev.azide.core.triggerEvery
import dev.azide.core.startExternally
import dev.azide.core.test_utils.TestTargetActionRecorder
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.cell.correctingUpdate
import dev.azide.core.test_utils.cell.revokingUpdate
import dev.azide.core.test_utils.schedule.Schedule_step_testUtils
import dev.azide.core.test_utils.expectIsNotExecuted
import dev.azide.core.test_utils.expectIsExecutedOnce
import dev.azide.core.test_utils.generic.ExpectedImpact
import kotlin.test.Test

@Suppress("ClassName")
class Cell_triggerEvery_step_tests {
    @Test
    fun test_step_sourceUpdates() {
        val targetTriggerRecorder1 = TestTargetActionRecorder.TriggerRecorder()
        val targetTriggerRecorder2 = TestTargetActionRecorder.TriggerRecorder()

        val sourceCell = TestInputCell(
            initialValue = targetTriggerRecorder1.recordedAction,
        )

        val subjectSchedule: Schedule = sourceCell.triggerEvery()

        subjectSchedule.startExternally()

        Schedule_step_testUtils.testStep(
            inputStimulation = sourceCell.update(
                newValue = targetTriggerRecorder2.recordedAction,
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetTriggerRecorder1.expectIsNotExecuted(),
                targetTriggerRecorder2.expectIsExecutedOnce(),
            ),
        )
    }

    @Test
    fun test_step_sourceUpdatesRevoked() {
        val targetTriggerRecorder1 = TestTargetActionRecorder.TriggerRecorder()
        val targetTriggerRecorder2 = TestTargetActionRecorder.TriggerRecorder()

        val sourceCell = TestInputCell(
            initialValue = targetTriggerRecorder1.recordedAction,
        )

        val subjectSchedule: Schedule = sourceCell.triggerEvery()

        subjectSchedule.startExternally()

        Schedule_step_testUtils.testStep(
            inputStimulation = sourceCell.revokingUpdate(
                newValue = targetTriggerRecorder2.recordedAction,
            ).joint(),
            expectedTargetImpact = ExpectedImpact.combine(
                targetTriggerRecorder1.expectIsNotExecuted(),
                targetTriggerRecorder2.expectIsNotExecuted(),
            ),
        )
    }

    @Test
    fun test_step_sourceUpdatesCorrected() {
        val targetTriggerRecorder1 = TestTargetActionRecorder.TriggerRecorder()
        val targetTriggerRecorder2 = TestTargetActionRecorder.TriggerRecorder()
        val targetTriggerRecorder3 = TestTargetActionRecorder.TriggerRecorder()

        val sourceCell = TestInputCell(
            initialValue = targetTriggerRecorder1.recordedAction,
        )

        val subjectSchedule: Schedule = sourceCell.triggerEvery()

        subjectSchedule.startExternally()

        Schedule_step_testUtils.testStep(
            inputStimulation = sourceCell.correctingUpdate(
                intermediateNewValue = targetTriggerRecorder2.recordedAction,
                correctedNewValue = targetTriggerRecorder3.recordedAction,
            ).joint(),
            expectedTargetImpact = ExpectedImpact.combine(
                targetTriggerRecorder1.expectIsNotExecuted(),
                targetTriggerRecorder2.expectIsNotExecuted(),
                targetTriggerRecorder3.expectIsExecutedOnce(),
            ),
        )
    }
}
