package dev.azide.core.cell

import dev.azide.core.Trigger
import dev.azide.core.test_utils.TestTargetActionRecorder
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.cell.TestInputCellTag
import dev.azide.core.test_utils.expectIsExecutedOnce
import dev.azide.core.test_utils.expectIsNotExecuted
import dev.azide.core.test_utils.schedule.Schedule_step_testUtils
import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationScenarioBank

@Suppress("ClassName")
data object Cell_triggerEvery_testUtils {
    data object SourceTriggerCellTag : TestInputCellTag

    val stimulationScenarioBank_sourceTriggerCellUpdates = TestStimulationScenarioBank.mixAll(
        TestInputCellTag.updateScenario(
            inputCellTag = SourceTriggerCellTag,
        ),
    )

    val stimulationScenarioBank_sourceTriggerCellUpdatesRevoked = TestStimulationScenarioBank.mixAll(
        TestInputCellTag.revokedUpdateScenario(
            inputCellTag = SourceTriggerCellTag,
        ),
    )

    val stimulationScenarioBank_sourceTriggerCellUpdatesCorrected = TestStimulationScenarioBank.mixAll(
        TestInputCellTag.correctedUpdateScenario(
            inputCellTag = SourceTriggerCellTag,
        ),
    )

    fun verifyScheduleNotOngoing(
        sourceTriggerCell: TestInputCell<Trigger>,
    ) {
        val targetTriggerRecorder = TestTargetActionRecorder.TriggerRecorder()

        Schedule_step_testUtils.executeStepTransaction(
            inputStimulation = sourceTriggerCell.update(
                newValue = targetTriggerRecorder.recordedAction,
            ),
            expectedTargetImpact = targetTriggerRecorder.expectIsNotExecuted(),
        )
    }

    fun verifyScheduleOngoing(
        sourceTriggerCell: TestInputCell<Trigger>,
    ) {
        val targetTriggerRecorder = TestTargetActionRecorder.TriggerRecorder()

        Schedule_step_testUtils.executeStepTransaction(
            inputStimulation = sourceTriggerCell.update(
                newValue = targetTriggerRecorder.recordedAction,
            ),
            expectedTargetImpact = targetTriggerRecorder.expectIsExecutedOnce(),
        )
    }
}
