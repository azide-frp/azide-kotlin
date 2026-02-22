package dev.azide.core.cell

import dev.azide.core.Cell
import dev.azide.core.Effect
import dev.azide.core.executeEvery
import dev.azide.core.startExternally
import dev.azide.core.test_utils.TestTargetActionRecorder
import dev.azide.core.test_utils.cell.Cell_expectations_testUtils
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.cell.correctingUpdate
import dev.azide.core.test_utils.cell.revokingUpdate
import dev.azide.core.test_utils.effect_cell.Effect_Cell_step_testUtils
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy
import dev.azide.core.test_utils.expectIsExecutedOnce
import dev.azide.core.test_utils.expectIsNotExecuted
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import kotlin.test.Test

@Suppress("ClassName")
class Cell_executeEvery_step_tests {
    @Test
    fun test_step_sourceUpdates_observed() {
        test_step_sourceUpdates(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
        )
    }

    @Test
    fun test_step_sourceUpdates_nonObserved() {
        test_step_sourceUpdates(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
        )
    }

    private fun test_step_sourceUpdates(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
    ) {
        val targetActionRecorder1 = TestTargetActionRecorder.pure(result = 10)
        val targetActionRecorder2 = TestTargetActionRecorder.pure(result = 20)

        val sourceCell = TestInputCell(
            initialValue = targetActionRecorder1.recordedAction,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.executeEvery()

        val subjectCell = subjectEffect.startExternally().result

        Effect_Cell_step_testUtils.testStep(
            subjectCell = subjectCell,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            inputStimulation = sourceCell.update(
                newValue = targetActionRecorder2.recordedAction,
            ),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                expectedOldValue = 10,
                expectedNewValue = 20,
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetActionRecorder1.expectIsNotExecuted(),
                targetActionRecorder2.expectIsExecutedOnce(),
            ),
        )
    }

    @Test
    fun test_step_sourceUpdatesRevoked_observed() {
        test_step_sourceUpdatesRevoked(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
        )
    }

    @Test
    fun test_step_sourceUpdatesRevoked_nonObserved() {
        test_step_sourceUpdatesRevoked(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
        )
    }

    private fun test_step_sourceUpdatesRevoked(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
    ) {
        val targetActionRecorder1 = TestTargetActionRecorder.pure(result = 10)
        val targetActionRecorder2 = TestTargetActionRecorder.pure(result = 20)

        val sourceCell = TestInputCell(
            initialValue = targetActionRecorder1.recordedAction,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.executeEvery()

        val subjectCell = subjectEffect.startExternally().result

        Effect_Cell_step_testUtils.testStep(
            subjectCell = subjectCell,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            inputStimulation = sourceCell.revokingUpdate(
                newValue = targetActionRecorder2.recordedAction,
            ).joint(),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectNoValueTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedValue = 10,
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetActionRecorder1.expectIsNotExecuted(),
                targetActionRecorder2.expectIsNotExecuted(),
            ),
        )
    }

    @Test
    fun test_step_sourceUpdatesCorrected_observed() {
        test_step_sourceUpdatesCorrected(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
        )
    }

    @Test
    fun test_step_sourceUpdatesCorrected_nonObserved() {
        test_step_sourceUpdatesCorrected(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
        )
    }

    private fun test_step_sourceUpdatesCorrected(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
    ) {
        val targetActionRecorder1 = TestTargetActionRecorder.pure(result = 10)
        val targetActionRecorder2 = TestTargetActionRecorder.pure(result = 20)
        val targetActionRecorder3 = TestTargetActionRecorder.pure(result = 30)

        val sourceCell = TestInputCell(
            initialValue = targetActionRecorder1.recordedAction,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.executeEvery()

        val subjectCell = subjectEffect.startExternally().result

        Effect_Cell_step_testUtils.testStep(
            subjectCell = subjectCell,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            inputStimulation = sourceCell.correctingUpdate(
                intermediateNewValue = targetActionRecorder2.recordedAction,
                correctedNewValue = targetActionRecorder3.recordedAction,
            ).joint(),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldValue = 10,
                expectedNewValue = 30,
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetActionRecorder1.expectIsNotExecuted(),
                targetActionRecorder2.expectIsNotExecuted(),
                targetActionRecorder3.expectIsExecutedOnce(),
            ),
        )
    }
}
