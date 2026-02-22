package dev.azide.core.cell

import dev.azide.core.Schedule
import dev.azide.core.triggerEvery
import dev.azide.core.startExternally
import dev.azide.core.test_utils.TestTargetActionRecorder
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.cell.correctingUpdate
import dev.azide.core.test_utils.cell.revokingUpdate
import dev.azide.core.test_utils.cell.updating
import dev.azide.core.test_utils.schedule.Schedule_cancelledRevoked_testUtils
import dev.azide.core.test_utils.expectIsNotExecuted
import dev.azide.core.test_utils.expectIsExecutedOnce
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulationScenario
import dev.azide.core.test_utils.stimulation_combinatorics.bind
import kotlin.test.Test

@Suppress("ClassName", "PrivatePropertyName")
class Cell_triggerEvery_cancelledRevoked_tests {
    private typealias SuitableSlotCount = TestSlotCount.Count3

    private typealias SuitableTestSlottedStimulationScenario = TestSlottedStimulationScenario<SuitableSlotCount>

    private val slottedStimulationScenarioBank_sourceTriggerCellUpdates =
        Cell_triggerEvery_testUtils.stimulationScenarioBank_sourceTriggerCellUpdates.distribute(slotCount = SuitableSlotCount)

    private val slottedStimulationScenarioBank_sourceTriggerCellUpdatesRevoked =
        Cell_triggerEvery_testUtils.stimulationScenarioBank_sourceTriggerCellUpdatesRevoked.distribute(slotCount = SuitableSlotCount)

    private val slottedStimulationScenarioBank_sourceTriggerCellUpdatesCorrected =
        Cell_triggerEvery_testUtils.stimulationScenarioBank_sourceTriggerCellUpdatesCorrected.distribute(slotCount = SuitableSlotCount)

    @Test
    fun test_cancelledRevoked() {
        val targetTriggerRecorder1 = TestTargetActionRecorder.TriggerRecorder()

        val sourceCell = TestInputCell(
            initialValue = targetTriggerRecorder1.recordedAction,
        )

        val subjectSchedule: Schedule = sourceCell.triggerEvery()

        val subjectOutcome = subjectSchedule.startExternally()

        Schedule_cancelledRevoked_testUtils.testCancel(
            subjectOutcome = subjectOutcome,
            expectedTargetImpact = targetTriggerRecorder1.expectIsNotExecuted(),
        )

        Cell_triggerEvery_testUtils.verifyScheduleOngoing(
            sourceTriggerCell = sourceCell,
        )
    }

    @Test
    fun test_cancelledRevoked_sourceUpdates() {
        slottedStimulationScenarioBank_sourceTriggerCellUpdates.forEach { slottedStimulationScenario ->
            test_cancelledRevoked_sourceUpdates(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_cancelledRevoked_sourceUpdates(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetTriggerRecorder1 = TestTargetActionRecorder.TriggerRecorder()
        val targetTriggerRecorder2 = TestTargetActionRecorder.TriggerRecorder()

        val sourceCell = TestInputCell(
            initialValue = targetTriggerRecorder1.recordedAction,
        )

        val subjectSchedule: Schedule = sourceCell.triggerEvery()

        val subjectOutcome = subjectSchedule.startExternally()

        Schedule_cancelledRevoked_testUtils.testCancel(
            subjectOutcome = subjectOutcome,
            slottedInputStimulation = sourceCell.updating(
                tag = Cell_triggerEvery_testUtils.SourceTriggerCellTag,
                newValue = targetTriggerRecorder2.recordedAction,
            ).bind(slottedStimulationScenario),
            expectedTargetImpact = ExpectedImpact.combine(
                targetTriggerRecorder1.expectIsNotExecuted(),
                targetTriggerRecorder2.expectIsExecutedOnce(),
            ),
        )

        Cell_triggerEvery_testUtils.verifyScheduleOngoing(
            sourceTriggerCell = sourceCell,
        )
    }

    @Test
    fun test_cancelledRevoked_sourceUpdatesRevoked() {
        slottedStimulationScenarioBank_sourceTriggerCellUpdatesRevoked.forEach { slottedStimulationScenario ->
            test_cancelledRevoked_sourceUpdatesRevoked(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_cancelledRevoked_sourceUpdatesRevoked(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetTriggerRecorder1 = TestTargetActionRecorder.TriggerRecorder()
        val targetTriggerRecorder2 = TestTargetActionRecorder.TriggerRecorder()

        val sourceCell = TestInputCell(
            initialValue = targetTriggerRecorder1.recordedAction,
        )

        val subjectSchedule: Schedule = sourceCell.triggerEvery()

        val subjectOutcome = subjectSchedule.startExternally()

        Schedule_cancelledRevoked_testUtils.testCancel(
            subjectOutcome = subjectOutcome,
            slottedInputStimulation = sourceCell.revokingUpdate(
                tag = Cell_triggerEvery_testUtils.SourceTriggerCellTag,
                newValue = targetTriggerRecorder2.recordedAction,
            ).bind(slottedStimulationScenario),
            expectedTargetImpact = ExpectedImpact.combine(
                targetTriggerRecorder1.expectIsNotExecuted(),
                targetTriggerRecorder2.expectIsNotExecuted(),
            ),
        )

        Cell_triggerEvery_testUtils.verifyScheduleOngoing(
            sourceTriggerCell = sourceCell,
        )
    }

    @Test
    fun test_cancelledRevoked_sourceUpdatesCorrected() {
        slottedStimulationScenarioBank_sourceTriggerCellUpdatesCorrected.forEach { slottedStimulationScenario ->
            test_cancelledRevoked_sourceUpdatesCorrected(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_cancelledRevoked_sourceUpdatesCorrected(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetTriggerRecorder1 = TestTargetActionRecorder.TriggerRecorder()
        val targetTriggerRecorder2 = TestTargetActionRecorder.TriggerRecorder()
        val targetTriggerRecorder3 = TestTargetActionRecorder.TriggerRecorder()

        val sourceCell = TestInputCell(
            initialValue = targetTriggerRecorder1.recordedAction,
        )

        val subjectSchedule: Schedule = sourceCell.triggerEvery()

        val subjectOutcome = subjectSchedule.startExternally()

        Schedule_cancelledRevoked_testUtils.testCancel(
            subjectOutcome = subjectOutcome,
            slottedInputStimulation = sourceCell.correctingUpdate(
                tag = Cell_triggerEvery_testUtils.SourceTriggerCellTag,
                intermediateNewValue = targetTriggerRecorder2.recordedAction,
                correctedNewValue = targetTriggerRecorder3.recordedAction,
            ).bind(slottedStimulationScenario),
            expectedTargetImpact = ExpectedImpact.combine(
                targetTriggerRecorder1.expectIsNotExecuted(),
                targetTriggerRecorder2.expectIsNotExecuted(),
                targetTriggerRecorder3.expectIsExecutedOnce(),
            ),
        )

        Cell_triggerEvery_testUtils.verifyScheduleOngoing(
            sourceTriggerCell = sourceCell,
        )
    }
}
