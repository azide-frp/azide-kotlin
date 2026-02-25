package dev.azide.core.cell

import dev.azide.core.Cell
import dev.azide.core.Effect
import dev.azide.core.executeEvery
import dev.azide.core.test_utils.TestTargetActionRecorder
import dev.azide.core.test_utils.cell.Cell_expectations_testUtils
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.cell.correctingUpdate_deprecated
import dev.azide.core.test_utils.cell.revokingUpdate_deprecated
import dev.azide.core.test_utils.cell.updating_deprecated
import dev.azide.core.test_utils.effect_cell.Effect_Cell_start_quickCancelledRevoked_testUtils
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy
import dev.azide.core.test_utils.expectIsExecutedOnce
import dev.azide.core.test_utils.expectIsNotExecuted
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulationScenario
import dev.azide.core.test_utils.stimulation_combinatorics.bind
import kotlin.test.Test

@Suppress("ClassName", "PrivatePropertyName")
class Cell_executeEvery_start_quickCancelledRevoked_tests {
    private typealias SuitableSlotCount = TestSlotCount.Count4

    private typealias SuitableTestSlottedStimulationScenario = TestSlottedStimulationScenario<SuitableSlotCount>

    private val slottedStimulationScenarioBank_sourceActionCellUpdates =
        Cell_executeEvery_testUtils.stimulationScenarioBank_sourceActionCellUpdates.distribute(slotCount = SuitableSlotCount)

    private val arbitrarySlottedStimulationScenario_sourceActionCellUpdates =
        slottedStimulationScenarioBank_sourceActionCellUpdates.first()

    private val slottedStimulationScenarioBank_sourceActionCellUpdatesRevoked =
        Cell_executeEvery_testUtils.stimulationScenarioBank_sourceActionCellUpdatesRevoked.distribute(slotCount = SuitableSlotCount)

    private val arbitrarySlottedStimulationScenario_sourceActionCellUpdatesRevoked =
        slottedStimulationScenarioBank_sourceActionCellUpdatesRevoked.first()

    private val slottedStimulationScenarioBank_sourceActionCellUpdatesCorrected =
        Cell_executeEvery_testUtils.stimulationScenarioBank_sourceActionCellUpdatesCorrected.distribute(slotCount = SuitableSlotCount)

    private val arbitrarySlottedStimulationScenario_sourceActionCellUpdatesCorrected =
        slottedStimulationScenarioBank_sourceActionCellUpdatesCorrected.first()

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
        val targetActionRecorder1 = TestTargetActionRecorder.pure(result = 10)

        val sourceCell = TestInputCell(
            initialValue = targetActionRecorder1.recordedAction,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.executeEvery()

        val subjectCell = Effect_Cell_start_quickCancelledRevoked_testUtils.testStart(
            subjectCellEffect = subjectEffect,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectNoValueTransition(
                expectedUnaffectedValue = 10,
            ),
            expectedTargetImpact = targetActionRecorder1.expectIsExecutedOnce(),
        )

        Cell_executeEvery_testUtils.verifyEffectOngoing(
            sourceCell = sourceCell,
            subjectCell = subjectCell,
        )
    }

    @Test
    fun test_start_quickCancelledRevoked_sourceUpdatesSimultaneously_observed() {
        slottedStimulationScenarioBank_sourceActionCellUpdates.forEach { slottedStimulationScenario ->
            test_start_quickCancelledRevoked_sourceUpdatesSimultaneously(
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    @Test
    fun test_start_quickCancelledRevoked_sourceUpdatesSimultaneously_nonObserved() {
        test_start_quickCancelledRevoked_sourceUpdatesSimultaneously(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            slottedStimulationScenario = arbitrarySlottedStimulationScenario_sourceActionCellUpdates,
        )
    }

    private fun test_start_quickCancelledRevoked_sourceUpdatesSimultaneously(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetActionRecorder1 = TestTargetActionRecorder.pure(result = 10)
        val targetActionRecorder2 = TestTargetActionRecorder.pure(result = 20)

        val sourceCell = TestInputCell(
            initialValue = targetActionRecorder1.recordedAction,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.executeEvery()

        val subjectCell = Effect_Cell_start_quickCancelledRevoked_testUtils.testStart(
            subjectCellEffect = subjectEffect,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = sourceCell.updating_deprecated(
                tag = Cell_executeEvery_testUtils.SourceActionCellTag,
                newValue = targetActionRecorder2.recordedAction,
            ).bind(slottedStimulationScenario),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldValue = 10,
                expectedNewValue = 20,
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetActionRecorder1.expectIsExecutedOnce(),
                targetActionRecorder2.expectIsExecutedOnce(),
            ),
        )

        Cell_executeEvery_testUtils.verifyEffectOngoing(
            sourceCell = sourceCell,
            subjectCell = subjectCell,
        )
    }

    @Test
    fun test_start_quickCancelledRevoked_sourceUpdatesRevokedSimultaneously_observed() {
        slottedStimulationScenarioBank_sourceActionCellUpdatesRevoked.forEach { slottedStimulationScenario ->
            test_start_quickCancelledRevoked_sourceUpdatesRevokedSimultaneously(
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    @Test
    fun test_start_quickCancelledRevoked_sourceUpdatesRevokedSimultaneously_nonObserved() {
        test_start_quickCancelledRevoked_sourceUpdatesRevokedSimultaneously(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            slottedStimulationScenario = arbitrarySlottedStimulationScenario_sourceActionCellUpdatesRevoked,
        )
    }

    private fun test_start_quickCancelledRevoked_sourceUpdatesRevokedSimultaneously(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetActionRecorder1 = TestTargetActionRecorder.pure(result = 10)
        val targetActionRecorder2 = TestTargetActionRecorder.pure(result = 20)

        val sourceCell = TestInputCell(
            initialValue = targetActionRecorder1.recordedAction,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.executeEvery()

        val subjectCell = Effect_Cell_start_quickCancelledRevoked_testUtils.testStart(
            subjectCellEffect = subjectEffect,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = sourceCell.revokingUpdate_deprecated(
                tag = Cell_executeEvery_testUtils.SourceActionCellTag,
                newValue = targetActionRecorder2.recordedAction,
            ).bind(slottedStimulationScenario),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectNoValueTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedValue = 10,
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetActionRecorder1.expectIsExecutedOnce(),
                targetActionRecorder2.expectIsNotExecuted(),
            ),
        )

        Cell_executeEvery_testUtils.verifyEffectOngoing(
            sourceCell = sourceCell,
            subjectCell = subjectCell,
        )
    }

    @Test
    fun test_start_quickCancelledRevoked_sourceUpdatesCorrectedSimultaneously_observed() {
        slottedStimulationScenarioBank_sourceActionCellUpdatesCorrected.forEach { slottedStimulationScenario ->
            test_start_quickCancelledRevoked_sourceUpdatesCorrectedSimultaneously(
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    @Test
    fun test_start_quickCancelledRevoked_sourceUpdatesCorrectedSimultaneously_nonObserved() {
        test_start_quickCancelledRevoked_sourceUpdatesCorrectedSimultaneously(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            slottedStimulationScenario = arbitrarySlottedStimulationScenario_sourceActionCellUpdatesCorrected,
        )
    }

    private fun test_start_quickCancelledRevoked_sourceUpdatesCorrectedSimultaneously(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetActionRecorder1 = TestTargetActionRecorder.pure(result = 10)
        val targetActionRecorder2 = TestTargetActionRecorder.pure(result = 20)
        val targetActionRecorder3 = TestTargetActionRecorder.pure(result = 30)

        val sourceCell = TestInputCell(
            initialValue = targetActionRecorder1.recordedAction,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.executeEvery()

        val subjectCell = Effect_Cell_start_quickCancelledRevoked_testUtils.testStart(
            subjectCellEffect = subjectEffect,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = sourceCell.correctingUpdate_deprecated(
                tag = Cell_executeEvery_testUtils.SourceActionCellTag,
                intermediateNewValue = targetActionRecorder2.recordedAction,
                correctedNewValue = targetActionRecorder3.recordedAction,
            ).bind(slottedStimulationScenario),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldValue = 10,
                expectedNewValue = 30,
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetActionRecorder1.expectIsExecutedOnce(),
                targetActionRecorder2.expectIsNotExecuted(),
                targetActionRecorder3.expectIsExecutedOnce(),
            ),
        )

        Cell_executeEvery_testUtils.verifyEffectOngoing(
            sourceCell = sourceCell,
            subjectCell = subjectCell,
        )
    }
}
