package dev.azide.core.cell

import dev.azide.core.Action
import dev.azide.core.Cell
import dev.azide.core.sampleExternally
import dev.azide.core.test_utils.TestTargetActionRecorder
import dev.azide.core.test_utils.cell.Cell_expectations_testUtils
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.cell.TestInputCellTag
import dev.azide.core.test_utils.effect_cell.Effect_Cell_step_testUtils
import dev.azide.core.test_utils.effect_generic.Effect_generic_step_testUtils
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy
import dev.azide.core.test_utils.expectIsNotExecuted
import dev.azide.core.test_utils.expectIsExecutedOnce
import dev.azide.core.test_utils.generic.ExpectedTestSubjectTransition
import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationScenarioBank

@Suppress("ClassName")
data object Cell_executeEvery_testUtils {
    data object SourceActionCellTag : TestInputCellTag

    val stimulationScenarioBank_sourceActionCellUpdates = TestStimulationScenarioBank.mixAll(
        TestInputCellTag.updateScenario(
            inputCellTag = SourceActionCellTag,
        ),
    )

    val stimulationScenarioBank_sourceActionCellUpdatesRevoked = TestStimulationScenarioBank.mixAll(
        TestInputCellTag.revokedUpdateScenario(
            inputCellTag = SourceActionCellTag,
        ),
    )

    val stimulationScenarioBank_sourceActionCellUpdatesCorrected = TestStimulationScenarioBank.mixAll(
        TestInputCellTag.correctedUpdateScenario(
            inputCellTag = SourceActionCellTag,
        ),
    )

    fun verifyEffectNotOngoing(
        sourceCell: TestInputCell<Action<Int>>,
        subjectCell: Cell<Int>,
    ) {
        val preStimulationValue = subjectCell.sampleExternally()

        val targetActionRecorder = TestTargetActionRecorder.pure(result = -1)

        Effect_Cell_step_testUtils.executeStepTransaction(
            subjectCell = subjectCell,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
            inputStimulation = sourceCell.update(
                newValue = targetActionRecorder.recordedAction,
            ),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectNoValueTransition(
                expectedUnaffectedValue = preStimulationValue,
            ),
            expectedTargetImpact = targetActionRecorder.expectIsNotExecuted(),
        )
    }

    fun verifyEffectNotOngoing(
        sourceCell: TestInputCell<Action<Int>>,
    ) {
        val targetActionRecorder = TestTargetActionRecorder.pure(result = -1)

        Effect_generic_step_testUtils.executeStepTransaction(
            subject = Unit,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
            inputStimulation = sourceCell.update(
                newValue = targetActionRecorder.recordedAction,
            ),
            expectedSubjectTransition = ExpectedTestSubjectTransition.None,
            expectedTargetImpact = targetActionRecorder.expectIsNotExecuted(),
        )
    }

    fun verifyEffectOngoing(
        sourceCell: TestInputCell<Action<Int>>,
        subjectCell: Cell<Int>,
    ) {
        val preStimulationValue = subjectCell.sampleExternally()

        val targetActionRecorder = TestTargetActionRecorder.pure(result = 0)

        Effect_Cell_step_testUtils.executeStepTransaction(
            subjectCell = subjectCell,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
            inputStimulation = sourceCell.update(
                newValue = targetActionRecorder.recordedAction,
            ),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                expectedOldValue = preStimulationValue,
                expectedNewValue = 0,
            ),
            expectedTargetImpact = targetActionRecorder.expectIsExecutedOnce(),
        )
    }
}
