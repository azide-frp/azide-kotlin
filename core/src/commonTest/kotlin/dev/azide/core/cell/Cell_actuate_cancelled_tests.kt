package dev.azide.core.cell

import dev.azide.core.Cell
import dev.azide.core.Effect
import dev.azide.core.actuate
import dev.azide.core.startExternally
import dev.azide.core.test_utils.ExpectedCellReactionTestUtils
import dev.azide.core.test_utils.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.ExpectedTestTargetImpact
import dev.azide.core.test_utils.TestSlotDispatcher1x2
import dev.azide.core.test_utils.TestSlotDispatcher2x2
import dev.azide.core.test_utils.TestTargetEffect
import dev.azide.core.test_utils.bind
import dev.azide.core.test_utils.cell.CellTestUtils
import dev.azide.core.test_utils.cell.correctingUpdate
import dev.azide.core.test_utils.cell.revokingUpdate
import dev.azide.core.test_utils.effects.EffectTestUtils_cancelled
import dev.azide.core.test_utils.effects.TestSubjectPerceptionStrategy
import dev.azide.core.test_utils.expectIsCancelledOnce
import dev.azide.core.test_utils.expectIsNotStarted
import kotlin.test.Test

@Suppress("ClassName")
class Cell_actuate_cancelled_tests {
    @Test
    fun test_cancelled_observed() {
        test_cancelled(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
        )
    }

    @Test
    fun test_cancelled_nonObserved() {
        test_cancelled(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
        )
    }

    private fun test_cancelled(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        val subjectOutcome = subjectEffect.startExternally()

        val targetEffect1StartRecord = targetEffect1.getAndResetStartRecords().single()

        EffectTestUtils_cancelled.executeCancelTransaction(
            subjectOutcome = subjectOutcome,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            expectedSubjectTransition = ExpectedCellReactionTestUtils.expectNoTransition(
                expectedUnaffectedValue = 10,
            ),
            expectedTargetImpact = targetEffect1StartRecord.expectIsCancelledOnce(),
        )

        // TODO: Check that the observation is closed
    }

    @Test
    fun test_cancelled_sourceUpdates_observed() {
        TestSlotDispatcher1x2.entries.forEach { dispatcher ->
            test_cancelled_sourceUpdates(
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                dispatcher = dispatcher,
            )
        }
    }

    @Test
    fun test_cancelled_sourceUpdates_nonObserved() {
        test_cancelled_sourceUpdates(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            dispatcher = TestSlotDispatcher1x2.Case1,
        )
    }

    private fun test_cancelled_sourceUpdates(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        dispatcher: TestSlotDispatcher1x2,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        val subjectOutcome = subjectEffect.startExternally()

        val targetEffect1StartRecord = targetEffect1.getAndResetStartRecords().single()

        EffectTestUtils_cancelled.executeCancelTransaction(
            subjectOutcome = subjectOutcome,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = sourceCell.update(
                newValue = targetEffect2,
            ).bind(dispatcher),
            expectedSubjectTransition = ExpectedCellReactionTestUtils.expectNoTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedValue = 10,
            ),
            expectedTargetImpact = ExpectedTestTargetImpact.combine(
                targetEffect1StartRecord.expectIsCancelledOnce(),
                targetEffect2.expectIsNotStarted(),
            )
        )

        // TODO: Check that the observation is closed
    }

    @Test
    fun test_cancelled_sourceUpdatesRevoked_observed() {
        TestSlotDispatcher2x2.entries.forEach { dispatcher ->
            test_cancelled_sourceUpdatesRevoked(
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                dispatcher = dispatcher,
            )
        }
    }

    @Test
    fun test_cancelled_sourceUpdatesRevoked_nonObserved() {
        test_cancelled_sourceUpdatesRevoked(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            dispatcher = TestSlotDispatcher2x2.Case11,
        )
    }

    private fun test_cancelled_sourceUpdatesRevoked(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        dispatcher: TestSlotDispatcher2x2,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        val subjectOutcome = subjectEffect.startExternally()

        val targetEffect1StartRecord = targetEffect1.getAndResetStartRecords().single()

        EffectTestUtils_cancelled.executeCancelTransaction(
            subjectOutcome = subjectOutcome,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = sourceCell.revokingUpdate(
                newValue = targetEffect2,
            ).bind(dispatcher),
            expectedSubjectTransition = ExpectedCellReactionTestUtils.expectNoTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedValue = 10,
            ),
            expectedTargetImpact = ExpectedTestTargetImpact.combine(
                targetEffect1StartRecord.expectIsCancelledOnce(),
                targetEffect2.expectIsNotStarted(),
            ),
        )

        // TODO: Check that the observation is closed
    }

    @Test
    fun test_cancelled_sourceUpdatesCorrected_observed() {
        TestSlotDispatcher2x2.entries.forEach { dispatcher ->
            test_cancelled_sourceUpdatesCorrected(
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                dispatcher = dispatcher,
            )
        }
    }

    @Test
    fun test_cancelled_sourceUpdatesCorrected_nonObserved() {
        test_cancelled_sourceUpdatesCorrected(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            dispatcher = TestSlotDispatcher2x2.Case11,
        )
    }

    private fun test_cancelled_sourceUpdatesCorrected(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        dispatcher: TestSlotDispatcher2x2,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)
        val targetEffect3 = TestTargetEffect.pure(result = 30)

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        val subjectOutcome = subjectEffect.startExternally()

        val targetEffect1StartRecord = targetEffect1.getAndResetStartRecords().single()

        EffectTestUtils_cancelled.executeCancelTransaction(
            subjectOutcome = subjectOutcome,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = sourceCell.correctingUpdate(
                intermediateNewValue = targetEffect2,
                correctedNewValue = targetEffect3,
            ).bind(dispatcher),
            expectedSubjectTransition = ExpectedCellReactionTestUtils.expectNoTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedValue = 10,
            ),
            expectedTargetImpact = ExpectedTestTargetImpact.combine(
                targetEffect1StartRecord.expectIsCancelledOnce(),
                targetEffect2.expectIsNotStarted(),
                targetEffect3.expectIsNotStarted(),
            ),
        )

        // TODO: Check that the observation is closed
    }
}
