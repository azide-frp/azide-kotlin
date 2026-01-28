package dev.azide.core.cell

import dev.azide.core.Cell
import dev.azide.core.sampleExternally
import dev.azide.core.test_utils.Cell_expectations_testUtils
import dev.azide.core.test_utils.ExpectedTestSubjectTransition
import dev.azide.core.test_utils.TestTargetEffect
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.effect_cell.Effect_Cell_step_testUtils
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy
import dev.azide.core.test_utils.expectIsNotStarted
import dev.azide.core.test_utils.expectIsStartedOnceButNotCancelled

@Suppress("ClassName")
data object Cell_actuate_testUtils {
    fun verifyEffectNotOngoing(
        sourceCell: TestInputCell<TestTargetEffect<Int>>,
        subjectCell: Cell<Int>,
    ) {
        val preStimulationValue = subjectCell.sampleExternally()

        val targetEffect = TestTargetEffect.pure(result = -1)

        Effect_Cell_step_testUtils.executeStepTransaction(
            subjectCell = subjectCell,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
            inputStimulation = sourceCell.update(
                newValue = targetEffect,
            ),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectNoValueTransition(
                expectedUnaffectedValue = preStimulationValue,
            ),
            expectedTargetImpact = targetEffect.expectIsNotStarted(),
        )
    }

    fun verifyEffectNotOngoing(
        sourceCell: TestInputCell<TestTargetEffect<Int>>,
    ) {
        val targetEffect = TestTargetEffect.pure(result = -1)

        Effect_Cell_step_testUtils.executeStepTransaction(
            subjectCell = Unit,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
            inputStimulation = sourceCell.update(
                newValue = targetEffect,
            ),
            expectedSubjectValueTransition = ExpectedTestSubjectTransition.None,
            expectedTargetImpact = targetEffect.expectIsNotStarted(),
        )
    }

    fun verifyEffectOngoing(
        sourceCell: TestInputCell<TestTargetEffect<Int>>,
        subjectCell: Cell<Int>,
    ) {
        val preStimulationValue = subjectCell.sampleExternally()

        val targetEffect = TestTargetEffect.pure(result = 0)

        Effect_Cell_step_testUtils.executeStepTransaction(
            subjectCell = subjectCell,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
            inputStimulation = sourceCell.update(
                newValue = targetEffect,
            ),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                expectedOldValue = preStimulationValue,
                expectedNewValue = 0,
            ),
            expectedTargetImpact = targetEffect.expectIsStartedOnceButNotCancelled(),
        )
    }
}
