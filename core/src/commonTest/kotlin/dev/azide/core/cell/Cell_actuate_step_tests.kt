package dev.azide.core.cell

import dev.azide.core.Cell
import dev.azide.core.Effect
import dev.azide.core.actuate
import dev.azide.core.startExternally
import dev.azide.core.test_utils.Cell_expectations_testUtils
import dev.azide.core.test_utils.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.ExpectedImpact
import dev.azide.core.test_utils.TestTargetEffect
import dev.azide.core.test_utils.cell.CellTestUtils
import dev.azide.core.test_utils.cell.correctingUpdate
import dev.azide.core.test_utils.cell.revokingUpdate
import dev.azide.core.test_utils.effect_generic.Effect_Cell_step_testUtils
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy
import dev.azide.core.test_utils.expectIsCancelledOnce
import dev.azide.core.test_utils.expectIsNotCancelled
import dev.azide.core.test_utils.expectIsNotStarted
import dev.azide.core.test_utils.expectIsStartedOnceButNotCancelled
import kotlin.test.Test

@Suppress("ClassName")
class Cell_actuate_step_tests {
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
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        val subjectCell = subjectEffect.startExternally().result

        val targetEffect1StartRecord = targetEffect1.getAndResetStartRecords().single()

        Effect_Cell_step_testUtils.executeStepTransaction(
            subjectCell = subjectCell,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            inputStimulation = sourceCell.update(
                newValue = targetEffect2,
            ),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectTransition(
                expectedOldValue = 10,
                expectedNewValue = 20,
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1StartRecord.expectIsCancelledOnce(),
                targetEffect2.expectIsStartedOnceButNotCancelled(),
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
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        val subjectCell = subjectEffect.startExternally().result

        val targetEffect1StartRecord = targetEffect1.getAndResetStartRecords().single()

        Effect_Cell_step_testUtils.executeStepTransaction(
            subjectCell = subjectCell,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            inputStimulation = sourceCell.revokingUpdate(
                newValue = targetEffect2,
            ).joint(),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectNoTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedValue = 10,
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1StartRecord.expectIsNotCancelled(),
                targetEffect2.expectIsNotStarted(),
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
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)
        val targetEffect3 = TestTargetEffect.pure(result = 30)

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        val subjectCell = subjectEffect.startExternally().result

        val targetEffect1StartRecord = targetEffect1.getAndResetStartRecords().single()

        Effect_Cell_step_testUtils.executeStepTransaction(
            subjectCell = subjectCell,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            inputStimulation = sourceCell.correctingUpdate(
                intermediateNewValue = targetEffect2,
                correctedNewValue = targetEffect3,
            ).joint(),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldValue = 10,
                expectedNewValue = 30,
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1StartRecord.expectIsCancelledOnce(),
                targetEffect2.expectIsNotStarted(),
                targetEffect3.expectIsStartedOnceButNotCancelled(),
            ),
        )
    }
}
