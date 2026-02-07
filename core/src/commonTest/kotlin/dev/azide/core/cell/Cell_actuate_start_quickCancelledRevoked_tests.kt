package dev.azide.core.cell

import dev.azide.core.Cell
import dev.azide.core.Effect
import dev.azide.core.actuate
import dev.azide.core.test_utils.Cell_expectations_testUtils
import dev.azide.core.test_utils.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.ExpectedImpact
import dev.azide.core.test_utils.TestSlotDispatcher1x4
import dev.azide.core.test_utils.TestSlotDispatcher2x4
import dev.azide.core.test_utils.TestTargetEffect
import dev.azide.core.test_utils.bind
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.cell.correctingUpdate
import dev.azide.core.test_utils.cell.revokingUpdate
import dev.azide.core.test_utils.effect_cell.Effect_Cell_start_quickCancelledRevoked_testUtils
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy
import dev.azide.core.test_utils.expectIsNotStarted
import dev.azide.core.test_utils.expectIsStartedOnceAndCancelledOnce
import dev.azide.core.test_utils.expectIsStartedOnceButNotCancelled
import kotlin.test.Test

@Suppress("ClassName")
class Cell_actuate_start_quickCancelledRevoked_tests {
    @Test
    fun test_start_quickCancelledRevoked_observed() {
        test_start_quickCancelledRevoked(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
        )
    }

    @Test
    fun test_start_quickCancelledRevoked_nonObserved() {
        test_start_quickCancelledRevoked(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
        )
    }

    private fun test_start_quickCancelledRevoked(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)

        val sourceCell = TestInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        val subjectCell = Effect_Cell_start_quickCancelledRevoked_testUtils.executeStartTransaction(
            subjectCellEffect = subjectEffect,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectNoValueTransition(
                expectedUnaffectedValue = 10,
            ),
            expectedTargetImpact = targetEffect1.expectIsStartedOnceButNotCancelled(),
        )

        Cell_actuate_testUtils.verifyEffectOngoing(
            sourceCell = sourceCell,
            subjectCell = subjectCell,
        )
    }

    @Test
    fun test_start_quickCancelledRevoked_sourceUpdatesSimultaneously_observed() {
        TestSlotDispatcher1x4.entries.forEach { dispatcher ->
            test_start_quickCancelledRevoked_sourceUpdatesSimultaneously(
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                dispatcher = dispatcher,
            )
        }
    }

    @Test
    fun test_start_quickCancelledRevoked_sourceUpdatesSimultaneously_nonObserved() {
        test_start_quickCancelledRevoked_sourceUpdatesSimultaneously(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            dispatcher = TestSlotDispatcher1x4.Case2,
        )
    }

    private fun test_start_quickCancelledRevoked_sourceUpdatesSimultaneously(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        dispatcher: TestSlotDispatcher1x4,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)

        val sourceCell = TestInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        val subjectCell = Effect_Cell_start_quickCancelledRevoked_testUtils.executeStartTransaction(
            subjectCellEffect = subjectEffect,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = sourceCell.update(
                newValue = targetEffect2,
            ).bind(dispatcher),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldValue = 10,
                expectedNewValue = 20,
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1.expectIsStartedOnceAndCancelledOnce(),
                targetEffect2.expectIsStartedOnceButNotCancelled(),
            ),
        )

        Cell_actuate_testUtils.verifyEffectOngoing(
            sourceCell = sourceCell,
            subjectCell = subjectCell,
        )
    }

    @Test
    fun test_start_quickCancelledRevoked_sourceUpdatesRevokedSimultaneously_observed() {
        TestSlotDispatcher2x4.entries.forEach { dispatcher ->
            test_start_quickCancelledRevoked_sourceUpdatesRevokedSimultaneously(
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                dispatcher = dispatcher,
            )
        }
    }

    @Test
    fun test_start_quickCancelledRevoked_sourceUpdatesRevokedSimultaneously_nonObserved() {
        test_start_quickCancelledRevoked_sourceUpdatesRevokedSimultaneously(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            dispatcher = TestSlotDispatcher2x4.Case22,
        )
    }

    private fun test_start_quickCancelledRevoked_sourceUpdatesRevokedSimultaneously(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        dispatcher: TestSlotDispatcher2x4,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)

        val sourceCell = TestInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        val subjectCell = Effect_Cell_start_quickCancelledRevoked_testUtils.executeStartTransaction(
            subjectCellEffect = subjectEffect,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = sourceCell.revokingUpdate(
                newValue = targetEffect2,
            ).bind(dispatcher),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectNoValueTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedValue = 10,
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1.expectIsStartedOnceButNotCancelled(),
                targetEffect2.expectIsNotStarted(),
            ),
        )

        Cell_actuate_testUtils.verifyEffectOngoing(
            sourceCell = sourceCell,
            subjectCell = subjectCell,
        )
    }

    @Test
    fun test_start_quickCancelledRevoked_sourceUpdatesCorrectedSimultaneously_observed() {
        TestSlotDispatcher2x4.entries.forEach { dispatcher ->
            test_start_quickCancelledRevoked_sourceUpdatesCorrectedSimultaneously(
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                dispatcher = dispatcher,
            )
        }
    }

    @Test
    fun test_start_quickCancelledRevoked_sourceUpdatesCorrectedSimultaneously_nonObserved() {
        test_start_quickCancelledRevoked_sourceUpdatesCorrectedSimultaneously(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            dispatcher = TestSlotDispatcher2x4.Case23,
        )
    }

    private fun test_start_quickCancelledRevoked_sourceUpdatesCorrectedSimultaneously(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        dispatcher: TestSlotDispatcher2x4,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)
        val targetEffect3 = TestTargetEffect.pure(result = 30)

        val sourceCell = TestInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        val subjectCell = Effect_Cell_start_quickCancelledRevoked_testUtils.executeStartTransaction(
            subjectCellEffect = subjectEffect,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = sourceCell.correctingUpdate(
                intermediateNewValue = targetEffect2,
                correctedNewValue = targetEffect3,
            ).bind(dispatcher),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldValue = 10,
                expectedNewValue = 30,
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1.expectIsStartedOnceAndCancelledOnce(),
                targetEffect2.expectIsNotStarted(),
                targetEffect3.expectIsStartedOnceButNotCancelled(),
            ),
        )

        Cell_actuate_testUtils.verifyEffectOngoing(
            sourceCell = sourceCell,
            subjectCell = subjectCell,
        )
    }
}
