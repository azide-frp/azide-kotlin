package dev.azide.core.cell

import dev.azide.core.Cell
import dev.azide.core.Effect
import dev.azide.core.actuate
import dev.azide.core.startExternally
import dev.azide.core.test_utils.ExpectedCellReactionTestUtils
import dev.azide.core.test_utils.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.ExpectedTestTargetImpact
import dev.azide.core.test_utils.TestCellObservationTrait
import dev.azide.core.test_utils.TestInputStimulation
import dev.azide.core.test_utils.TestTargetEffect
import dev.azide.core.test_utils.cell.CellTestUtils
import dev.azide.core.test_utils.cell.correctingUpdate
import dev.azide.core.test_utils.cell.revokingUpdate
import dev.azide.core.test_utils.expectIsCancelledOnce
import dev.azide.core.test_utils.expectIsNotCancelled
import dev.azide.core.test_utils.expectIsNotStarted
import dev.azide.core.test_utils.expectIsStartedOnceAndNotCancelled
import dev.azide.core.test_utils.stimulation_test_strategies.NonPerceivedStimulationTestStrategy
import dev.azide.core.test_utils.stimulation_test_strategies.PerceivedUpFrontStimulationTestStrategy
import dev.azide.core.test_utils.stimulation_test_strategies.PreStimulationTestStrategy
import dev.azide.core.test_utils.stimulation_test_strategies.PostStimulationTestStrategy
import dev.azide.core.test_utils.stimulation_test_strategies.StimulationTestStrategy
import kotlin.test.Test

@Suppress("ClassName")
class Cell_actuate_stimulation_tests {
    @Test
    fun test_sourceUpdates_observedUpFront() {
        test_sourceUpdates(
            stimulationTestStrategy = PerceivedUpFrontStimulationTestStrategy,
        )
    }

    @Test
    fun test_sourceUpdates_preObserved() {
        test_sourceUpdates(
            stimulationTestStrategy = PostStimulationTestStrategy,
        )
    }

    @Test
    fun test_sourceUpdates_preStimulated() {
        test_sourceUpdates(
            stimulationTestStrategy = PreStimulationTestStrategy,
        )
    }

    @Test
    fun test_sourceUpdates_nonObserved() {
        test_sourceUpdates(
            stimulationTestStrategy = NonPerceivedStimulationTestStrategy,
        )
    }

    private fun test_sourceUpdates(
        stimulationTestStrategy: StimulationTestStrategy,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        val subjectCell = subjectEffect.startExternally().result

        val targetEffect1StartRecord = targetEffect1.getAndResetStartRecords().single()

        stimulationTestStrategy.verifyStimulationEffectiveness(
            subjectPerceptionTrait = TestCellObservationTrait(),
            subject = subjectCell,
            inputStimulation = sourceCell.update(
                newValue = targetEffect2,
            ),
            expectedSubjectReaction = ExpectedCellReactionTestUtils.expectUpdate(
                expectedNewValue = 20,
            ),
            expectedTargetImpact = ExpectedTestTargetImpact.combine(
                targetEffect1StartRecord.expectIsCancelledOnce(),
                targetEffect2.expectIsStartedOnceAndNotCancelled(),
            ),
        )
    }

    @Test
    fun test_sourceUpdates_revoked_observedUpFront() {
        test_sourceUpdates_revoked(
            stimulationTestStrategy = PerceivedUpFrontStimulationTestStrategy,
        )
    }

    @Test
    fun test_sourceUpdates_revoked_preObserved() {
        test_sourceUpdates_revoked(
            stimulationTestStrategy = PostStimulationTestStrategy,
        )
    }

    @Test
    fun test_sourceUpdates_revoked_preStimulated() {
        test_sourceUpdates_revoked(
            stimulationTestStrategy = PreStimulationTestStrategy,
        )
    }

    @Test
    fun test_sourceUpdates_revoked_nonObserved() {
        test_sourceUpdates_revoked(
            stimulationTestStrategy = NonPerceivedStimulationTestStrategy,
        )
    }

    private fun test_sourceUpdates_revoked(
        stimulationTestStrategy: StimulationTestStrategy,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        val subjectCell = subjectEffect.startExternally().result

        val targetEffect1StartRecord = targetEffect1.getAndResetStartRecords().single()

        stimulationTestStrategy.verifyStimulationEffectiveness(
            subjectPerceptionTrait = TestCellObservationTrait(),
            subject = subjectCell,
            inputStimulation = sourceCell.revokingUpdate(
                newValue = targetEffect2,
            ),
            expectedSubjectReaction = ExpectedCellReactionTestUtils.expectNoUpdate(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
            ),
            expectedTargetImpact = ExpectedTestTargetImpact.combine(
                targetEffect1StartRecord.expectIsNotCancelled(),
                targetEffect2.expectIsNotStarted(),
            ),
        )
    }

    @Test
    fun test_sourceUpdates_corrected_observedUpFront() {
        test_sourceUpdates_corrected(
            stimulationTestStrategy = PerceivedUpFrontStimulationTestStrategy,
        )
    }

    @Test
    fun test_sourceUpdates_corrected_preObserved() {
        test_sourceUpdates_corrected(
            stimulationTestStrategy = PostStimulationTestStrategy,
        )
    }

    @Test
    fun test_sourceUpdates_corrected_preStimulated() {
        test_sourceUpdates_corrected(
            stimulationTestStrategy = PreStimulationTestStrategy,
        )
    }

    @Test
    fun test_sourceUpdates_corrected_nonObserved() {
        test_sourceUpdates_corrected(
            stimulationTestStrategy = NonPerceivedStimulationTestStrategy,
        )
    }

    private fun test_sourceUpdates_corrected(
        stimulationTestStrategy: StimulationTestStrategy,
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

        stimulationTestStrategy.verifyStimulationEffectiveness(
            subjectPerceptionTrait = TestCellObservationTrait(),
            subject = subjectCell,
            inputStimulation = sourceCell.correctingUpdate(
                intermediateNewValue = targetEffect2,
                correctedNewValue = targetEffect3,
            ),
            expectedSubjectReaction = ExpectedCellReactionTestUtils.expectUpdate(
                expectedNewValue = 30,
            ),
            expectedTargetImpact = ExpectedTestTargetImpact.combine(
                targetEffect1StartRecord.expectIsCancelledOnce(),
                targetEffect2.expectIsNotStarted(),
                targetEffect3.expectIsStartedOnceAndNotCancelled(),
            ),
        )
    }

    @Test
    fun test_cancelled_observedUpFront() {
        test_cancelled(
            stimulationTestStrategy = PerceivedUpFrontStimulationTestStrategy,
        )
    }

    @Test
    fun test_cancelled_preObserved() {
        test_cancelled(
            stimulationTestStrategy = PostStimulationTestStrategy,
        )
    }

    @Test
    fun test_cancelled_preStimulated() {
        test_cancelled(
            stimulationTestStrategy = PreStimulationTestStrategy,
        )
    }

    @Test
    fun test_cancelled_nonObserved() {
        test_cancelled(
            stimulationTestStrategy = NonPerceivedStimulationTestStrategy,
        )
    }

    @Test
    fun test_cancelled_twice() {
        test_cancelled(
            stimulationTestStrategy = PostStimulationTestStrategy,
            cancelCount = 2,
        )
    }

    private fun test_cancelled(
        stimulationTestStrategy: StimulationTestStrategy,
        cancelCount: Int = 1,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        val subjectEffectOutcome = subjectEffect.startExternally()
        val subjectCell = subjectEffectOutcome.result
        val subjectEffectHandle = subjectEffectOutcome.handle

        val targetEffect1StartRecord = targetEffect1.getAndResetStartRecords().single()

        stimulationTestStrategy.verifyStimulationEffectiveness(
            subjectPerceptionTrait = TestCellObservationTrait(),
            subject = subjectCell,
            inputStimulation = TestInputStimulation.combine(
                List(cancelCount) {
                    TestInputStimulation.executing(subjectEffectHandle.cancel)
                },
            ),
            expectedSubjectReaction = ExpectedCellReactionTestUtils.expectNoUpdate(),
            expectedTargetImpact = targetEffect1StartRecord.expectIsCancelledOnce(),
        )
    }

    @Test
    fun test_cancelled_sourceUpdates_observedUpFront() {
        test_cancelled_sourceUpdates(
            stimulationTestStrategy = PerceivedUpFrontStimulationTestStrategy,
        )
    }

    @Test
    fun test_cancelled_sourceUpdates_preObserved() {
        test_cancelled_sourceUpdates(
            stimulationTestStrategy = PostStimulationTestStrategy,
        )
    }

    @Test
    fun test_cancelled_sourceUpdates_preStimulated() {
        test_cancelled_sourceUpdates(
            stimulationTestStrategy = PreStimulationTestStrategy,
        )
    }

    @Test
    fun test_cancelled_sourceUpdates_nonObserved() {
        test_cancelled_sourceUpdates(
            stimulationTestStrategy = NonPerceivedStimulationTestStrategy,
        )
    }

    @Test
    fun test_cancelled_sourceUpdates_updatedAfterCancel() {
        test_cancelled_sourceUpdates(
            stimulationTestStrategy = PostStimulationTestStrategy,
            shouldUpdateAfterCancel = true,
        )
    }

    private fun test_cancelled_sourceUpdates(
        stimulationTestStrategy: StimulationTestStrategy,
        shouldUpdateAfterCancel: Boolean = false,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        val subjectEffectOutcome = subjectEffect.startExternally()
        val subjectCell = subjectEffectOutcome.result
        val subjectEffectHandle = subjectEffectOutcome.handle

        val targetEffect1StartRecord = targetEffect1.getAndResetStartRecords().single()

        val inputStimulations = listOf(
            sourceCell.update(
                newValue = targetEffect2,
            ),
            TestInputStimulation.executing(subjectEffectHandle.cancel),
        )

        stimulationTestStrategy.verifyStimulationEffectiveness(
            subjectPerceptionTrait = TestCellObservationTrait(),
            subject = subjectCell,
            inputStimulation = TestInputStimulation.combine(
                when {
                    shouldUpdateAfterCancel -> inputStimulations.reversed()
                    else -> inputStimulations
                },
            ),
            expectedSubjectReaction = ExpectedCellReactionTestUtils.expectNoUpdate(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
            ),
            expectedTargetImpact = ExpectedTestTargetImpact.combine(
                targetEffect1StartRecord.expectIsCancelledOnce(),
                targetEffect2.expectIsNotStarted(),
            ),
        )
    }
}
