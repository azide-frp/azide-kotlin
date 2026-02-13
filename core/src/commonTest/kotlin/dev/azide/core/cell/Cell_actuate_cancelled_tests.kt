package dev.azide.core.cell

import dev.azide.core.Cell
import dev.azide.core.Effect
import dev.azide.core.actuate
import dev.azide.core.cell.Cell_actuate_testUtils.SourceEffectCellTag
import dev.azide.core.startExternally
import dev.azide.core.test_utils.TestTargetEffect
import dev.azide.core.test_utils.cell.Cell_expectations_testUtils
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.cell.correctingUpdate
import dev.azide.core.test_utils.cell.revokingUpdate
import dev.azide.core.test_utils.cell.updating
import dev.azide.core.test_utils.effect_cell.Effect_Cell_cancelled_testUtils
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy
import dev.azide.core.test_utils.expectIsCancelledOnce
import dev.azide.core.test_utils.expectIsNotStarted
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.getAndResetSingleStartRecord
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulationScenario
import dev.azide.core.test_utils.stimulation_combinatorics.bind
import kotlin.test.Test

@Suppress("ClassName", "PrivatePropertyName")
class Cell_actuate_cancelled_tests {
    private typealias SuitableSlotCount = TestSlotCount.Count2

    private typealias SuitableTestSlottedStimulationScenario = TestSlottedStimulationScenario<SuitableSlotCount>

    private val slottedStimulationScenarioBank_sourceEffectCellUpdates =
        Cell_actuate_testUtils.stimulationScenarioBank_sourceEffectCellUpdates.distribute(slotCount = SuitableSlotCount)

    private val arbitrarySlottedStimulationScenario_sourceEffectCellUpdates =
        slottedStimulationScenarioBank_sourceEffectCellUpdates.get(0)

    private val slottedStimulationScenarioBank_sourceEffectCellUpdatesRevoked =
        Cell_actuate_testUtils.stimulationScenarioBank_sourceEffectCellUpdatesRevoked.distribute(slotCount = SuitableSlotCount)

    private val arbitrarySlottedStimulationScenario_sourceEffectCellUpdatesRevoked =
        slottedStimulationScenarioBank_sourceEffectCellUpdatesRevoked.get(0)

    private val slottedStimulationScenarioBank_sourceEffectCellUpdatesCorrected =
        Cell_actuate_testUtils.stimulationScenarioBank_sourceEffectCellUpdatesCorrected.distribute(slotCount = SuitableSlotCount)

    private val arbitrarySlottedStimulationScenario_sourceEffectCellUpdatesCorrected =
        slottedStimulationScenarioBank_sourceEffectCellUpdatesCorrected.get(0)

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

    @Test
    fun test_cancelled_twice() {
        test_cancelled(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
            cancelCount = 2,
        )
    }

    private fun test_cancelled(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        cancelCount: Int = 1,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)

        val sourceCell = TestInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        val subjectOutcome = subjectEffect.startExternally()

        val targetEffect1StartRecord = targetEffect1.getAndResetSingleStartRecord()

        Effect_Cell_cancelled_testUtils.executeCancelTransaction(
            subjectEffectOutcome = subjectOutcome,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectNoValueTransition(
                expectedUnaffectedValue = 10,
            ),
            expectedTargetImpact = targetEffect1StartRecord.expectIsCancelledOnce(),
            cancelCount = cancelCount,
        )

        Cell_actuate_testUtils.verifyEffectNotOngoing(
            sourceCell = sourceCell,
            subjectCell = subjectOutcome.result,
        )
    }

    @Test
    fun test_cancelled_sourceUpdates_observed() {
        slottedStimulationScenarioBank_sourceEffectCellUpdates.forEach { slottedStimulationScenario ->
            test_cancelled_sourceUpdates(
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    @Test
    fun test_cancelled_sourceUpdates_nonObserved() {
        test_cancelled_sourceUpdates(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            slottedStimulationScenario = arbitrarySlottedStimulationScenario_sourceEffectCellUpdates,
        )
    }

    private fun test_cancelled_sourceUpdates(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)

        val sourceCell = TestInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        val subjectOutcome = subjectEffect.startExternally()

        val targetEffect1StartRecord = targetEffect1.getAndResetSingleStartRecord()

        Effect_Cell_cancelled_testUtils.executeCancelTransaction(
            subjectEffectOutcome = subjectOutcome,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = sourceCell.updating(
                tag = SourceEffectCellTag,
                newValue = targetEffect2,
            ).bind(slottedStimulationScenario),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectNoValueTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedValue = 10,
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1StartRecord.expectIsCancelledOnce(),
                targetEffect2.expectIsNotStarted(),
            )
        )

        Cell_actuate_testUtils.verifyEffectNotOngoing(
            sourceCell = sourceCell,
            subjectCell = subjectOutcome.result,
        )
    }

    @Test
    fun test_cancelled_sourceUpdatesRevoked_observed() {
        slottedStimulationScenarioBank_sourceEffectCellUpdatesRevoked.forEach { slottedStimulationScenario ->
            test_cancelled_sourceUpdatesRevoked(
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    @Test
    fun test_cancelled_sourceUpdatesRevoked_nonObserved() {
        test_cancelled_sourceUpdatesRevoked(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            slottedStimulationScenario = arbitrarySlottedStimulationScenario_sourceEffectCellUpdatesRevoked,
        )
    }

    private fun test_cancelled_sourceUpdatesRevoked(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)

        val sourceCell = TestInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        val subjectOutcome = subjectEffect.startExternally()

        val targetEffect1StartRecord = targetEffect1.getAndResetSingleStartRecord()

        Effect_Cell_cancelled_testUtils.executeCancelTransaction(
            subjectEffectOutcome = subjectOutcome,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = sourceCell.revokingUpdate(
                tag = SourceEffectCellTag,
                newValue = targetEffect2,
            ).bind(slottedStimulationScenario),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectNoValueTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedValue = 10,
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1StartRecord.expectIsCancelledOnce(),
                targetEffect2.expectIsNotStarted(),
            ),
        )

        Cell_actuate_testUtils.verifyEffectNotOngoing(
            sourceCell = sourceCell,
            subjectCell = subjectOutcome.result,
        )
    }

    @Test
    fun test_cancelled_sourceUpdatesCorrected_observed() {
        slottedStimulationScenarioBank_sourceEffectCellUpdatesCorrected.forEach { slottedStimulationScenario ->
            test_cancelled_sourceUpdatesCorrected(
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    @Test
    fun test_cancelled_sourceUpdatesCorrected_nonObserved() {
        test_cancelled_sourceUpdatesCorrected(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            slottedStimulationScenario = arbitrarySlottedStimulationScenario_sourceEffectCellUpdatesCorrected,
        )
    }

    private fun test_cancelled_sourceUpdatesCorrected(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)
        val targetEffect3 = TestTargetEffect.pure(result = 30)

        val sourceCell = TestInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        val subjectOutcome = subjectEffect.startExternally()

        val targetEffect1StartRecord = targetEffect1.getAndResetSingleStartRecord()

        Effect_Cell_cancelled_testUtils.executeCancelTransaction(
            subjectEffectOutcome = subjectOutcome,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = sourceCell.correctingUpdate(
                tag = SourceEffectCellTag,
                intermediateNewValue = targetEffect2,
                correctedNewValue = targetEffect3,
            ).bind(slottedStimulationScenario),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectNoValueTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedValue = 10,
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1StartRecord.expectIsCancelledOnce(),
                targetEffect2.expectIsNotStarted(),
                targetEffect3.expectIsNotStarted(),
            ),
        )

        Cell_actuate_testUtils.verifyEffectNotOngoing(
            sourceCell = sourceCell,
            subjectCell = subjectOutcome.result,
        )
    }
}
