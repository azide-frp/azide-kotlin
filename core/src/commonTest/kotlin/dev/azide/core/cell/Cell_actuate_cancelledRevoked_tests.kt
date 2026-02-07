package dev.azide.core.cell

import dev.azide.core.Cell
import dev.azide.core.Effect
import dev.azide.core.actuate
import dev.azide.core.startExternally
import dev.azide.core.test_utils.TestSlotDispatcher1x3
import dev.azide.core.test_utils.TestSlotDispatcher2x3
import dev.azide.core.test_utils.TestTargetEffect
import dev.azide.core.test_utils.bind
import dev.azide.core.test_utils.cell.Cell_expectations_testUtils
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.cell.correctingUpdate
import dev.azide.core.test_utils.cell.revokingUpdate
import dev.azide.core.test_utils.effect_cell.Effect_Cell_cancelledRevoked_testUtils
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy
import dev.azide.core.test_utils.expectIsCancelledOnce
import dev.azide.core.test_utils.expectIsNotCancelled
import dev.azide.core.test_utils.expectIsNotStarted
import dev.azide.core.test_utils.expectIsStartedOnceButNotCancelled
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import kotlin.test.Test

@Suppress("ClassName")
class Cell_actuate_cancelledRevoked_tests {
    @Test
    fun test_cancelledRevoked_observed() {
        test_cancelledRevoked(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
        )
    }

    @Test
    fun test_cancelledRevoked_nonObserved() {
        test_cancelledRevoked(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
        )
    }

    private fun test_cancelledRevoked(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)

        val sourceCell = TestInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        val subjectOutcome = subjectEffect.startExternally()

        val targetEffect1StartRecord = targetEffect1.getAndResetStartRecords().single()

        Effect_Cell_cancelledRevoked_testUtils.executeCancelTransaction(
            subjectEffectOutcome = subjectOutcome,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectNoValueTransition(
                expectedUnaffectedValue = 10,
            ),
            expectedTargetImpact = targetEffect1StartRecord.expectIsNotCancelled(),
        )

        Cell_actuate_testUtils.verifyEffectOngoing(
            sourceCell = sourceCell,
            subjectCell = subjectOutcome.result,
        )
    }

    @Test
    fun test_cancelledRevoked_sourceUpdates_observed() {
        TestSlotDispatcher1x3.entries.forEach { dispatcher ->
            test_cancelledRevoked_sourceUpdates(
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                dispatcher = dispatcher,
            )
        }
    }

    @Test
    fun test_cancelledRevoked_sourceUpdates_nonObserved() {
        test_cancelledRevoked_sourceUpdates(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            dispatcher = TestSlotDispatcher1x3.Case1,
        )
    }

    private fun test_cancelledRevoked_sourceUpdates(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        dispatcher: TestSlotDispatcher1x3,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)

        val sourceCell = TestInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        val subjectOutcome = subjectEffect.startExternally()

        val targetEffect1StartRecord = targetEffect1.getAndResetStartRecords().single()

        Effect_Cell_cancelledRevoked_testUtils.executeCancelTransaction(
            subjectEffectOutcome = subjectOutcome,
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
                targetEffect1StartRecord.expectIsCancelledOnce(),
                targetEffect2.expectIsStartedOnceButNotCancelled(),
            ),
        )

        Cell_actuate_testUtils.verifyEffectOngoing(
            sourceCell = sourceCell,
            subjectCell = subjectOutcome.result,
        )
    }

    @Test
    fun test_cancelledRevoked_sourceUpdatesRevoked_observed() {
        TestSlotDispatcher2x3.entries.forEach { dispatcher ->
            test_cancelledRevoked_sourceUpdatesRevoked(
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                dispatcher = dispatcher,
            )
        }
    }

    @Test
    fun test_cancelledRevoked_sourceUpdatesRevoked_nonObserved() {
        test_cancelledRevoked_sourceUpdatesRevoked(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            dispatcher = TestSlotDispatcher2x3.Case11,
        )
    }

    private fun test_cancelledRevoked_sourceUpdatesRevoked(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        dispatcher: TestSlotDispatcher2x3,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)

        val sourceCell = TestInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        val subjectOutcome = subjectEffect.startExternally()

        val targetEffect1StartRecord = targetEffect1.getAndResetStartRecords().single()

        Effect_Cell_cancelledRevoked_testUtils.executeCancelTransaction(
            subjectEffectOutcome = subjectOutcome,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = sourceCell.revokingUpdate(
                newValue = targetEffect2,
            ).bind(dispatcher),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectNoValueTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedValue = 10,
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1StartRecord.expectIsNotCancelled(),
                targetEffect2.expectIsNotStarted(),
            ),
        )

        Cell_actuate_testUtils.verifyEffectOngoing(
            sourceCell = sourceCell,
            subjectCell = subjectOutcome.result,
        )
    }

    @Test
    fun test_cancelledRevoked_sourceUpdatesCorrected_observed() {
        TestSlotDispatcher2x3.entries.forEach { dispatcher ->
            test_cancelledRevoked_sourceUpdatesCorrected(
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                dispatcher = dispatcher,
            )
        }
    }

    @Test
    fun test_cancelledRevoked_sourceUpdatesCorrected_nonObserved() {
        test_cancelledRevoked_sourceUpdatesCorrected(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            dispatcher = TestSlotDispatcher2x3.Case11,
        )
    }

    private fun test_cancelledRevoked_sourceUpdatesCorrected(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        dispatcher: TestSlotDispatcher2x3,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)
        val targetEffect3 = TestTargetEffect.pure(result = 30)

        val sourceCell = TestInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        val subjectOutcome = subjectEffect.startExternally()

        val targetEffect1StartRecord = targetEffect1.getAndResetStartRecords().single()

        Effect_Cell_cancelledRevoked_testUtils.executeCancelTransaction(
            subjectEffectOutcome = subjectOutcome,
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
                targetEffect1StartRecord.expectIsCancelledOnce(),
                targetEffect2.expectIsNotStarted(),
                targetEffect3.expectIsStartedOnceButNotCancelled(),
            ),
        )

        Cell_actuate_testUtils.verifyEffectOngoing(
            sourceCell = sourceCell,
            subjectCell = subjectOutcome.result,
        )
    }

}
