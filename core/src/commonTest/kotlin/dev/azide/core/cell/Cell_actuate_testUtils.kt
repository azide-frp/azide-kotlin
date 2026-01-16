package dev.azide.core.cell

import dev.azide.core.Cell
import dev.azide.core.sampleExternally
import dev.azide.core.test_utils.ExpectedCellReactionTestUtils
import dev.azide.core.test_utils.ExpectedTestSubjectTransition
import dev.azide.core.test_utils.TestTargetEffect
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.effects.EffectTestUtils_step
import dev.azide.core.test_utils.effects.TestSubjectPerceptionStrategy
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

        EffectTestUtils_step.executeStepTransaction(
            subject = subjectCell,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
            inputStimulation = sourceCell.update(
                newValue = targetEffect,
            ),
            expectedSubjectTransition = ExpectedCellReactionTestUtils.expectNoTransition(
                expectedUnaffectedValue = preStimulationValue,
            ),
            expectedTargetImpact = targetEffect.expectIsNotStarted(),
        )
    }

    fun verifyEffectNotOngoing(
        sourceCell: TestInputCell<TestTargetEffect<Int>>,
    ) {
        val targetEffect = TestTargetEffect.pure(result = -1)

        EffectTestUtils_step.executeStepTransaction(
            subject = Unit,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
            inputStimulation = sourceCell.update(
                newValue = targetEffect,
            ),
            expectedSubjectTransition = ExpectedTestSubjectTransition.Noop,
            expectedTargetImpact = targetEffect.expectIsNotStarted(),
        )
    }

    fun verifyEffectOngoing(
        sourceCell: TestInputCell<TestTargetEffect<Int>>,
        subjectCell: Cell<Int>,
    ) {
        val preStimulationValue = subjectCell.sampleExternally()

        val targetEffect = TestTargetEffect.pure(result = 0)

        EffectTestUtils_step.executeStepTransaction(
            subject = subjectCell,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
            inputStimulation = sourceCell.update(
                newValue = targetEffect,
            ),
            expectedSubjectTransition = ExpectedCellReactionTestUtils.expectTransition(
                expectedOldValue = preStimulationValue,
                expectedNewValue = 0,
            ),
            expectedTargetImpact = targetEffect.expectIsStartedOnceButNotCancelled(),
        )
    }
}
