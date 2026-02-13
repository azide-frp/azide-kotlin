package dev.azide.core.cell

import dev.azide.core.Cell
import dev.azide.core.Effect
import dev.azide.core.actuate
import dev.azide.core.cell.Cell_actuate_testUtils.SourceEffectCellTag
import dev.azide.core.test_utils.TestTargetEffect
import dev.azide.core.test_utils.cell.Cell_expectations_testUtils
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.cell.correctingUpdate
import dev.azide.core.test_utils.cell.revokingUpdate
import dev.azide.core.test_utils.cell.updating
import dev.azide.core.test_utils.effect_cell.Effect_Cell_start_testUtils
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy
import dev.azide.core.test_utils.expectIsNotStarted
import dev.azide.core.test_utils.expectIsStartedOnceAndCancelledOnce
import dev.azide.core.test_utils.expectIsStartedOnceButNotCancelled
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulationScenario
import dev.azide.core.test_utils.stimulation_combinatorics.bind
import kotlin.test.Test

@Suppress("ClassName", "PrivatePropertyName")
class Cell_actuate_start_tests {
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
    fun test_start_observed() {
        test_start(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
        )
    }

    @Test
    fun test_start_nonObserved() {
        test_start(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
        )
    }

    private fun test_start(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)

        val sourceCell = TestInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        Effect_Cell_start_testUtils.executeStartTransaction(
            subjectCellEffect = subjectEffect,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectNoValueTransition(
                expectedUnaffectedValue = 10,
            ),
            expectedTargetImpact = targetEffect1.expectIsStartedOnceButNotCancelled(),
        )
    }

    @Test
    fun test_start_sourceUpdatesSimultaneously_observed() {
        slottedStimulationScenarioBank_sourceEffectCellUpdates.forEach { slottedStimulationScenario ->
            test_start_sourceUpdatesSimultaneously(
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    @Test
    fun test_start_sourceUpdatesSimultaneously_nonObserved() {
        test_start_sourceUpdatesSimultaneously(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            slottedStimulationScenario = arbitrarySlottedStimulationScenario_sourceEffectCellUpdates,
        )
    }

    private fun test_start_sourceUpdatesSimultaneously(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)

        val sourceCell = TestInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        Effect_Cell_start_testUtils.executeStartTransaction(
            subjectCellEffect = subjectEffect,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = sourceCell.updating(
                tag = SourceEffectCellTag,
                newValue = targetEffect2,
            ).bind(slottedStimulationScenario),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                expectedOldValue = 10,
                expectedNewValue = 20,
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1.expectIsStartedOnceAndCancelledOnce(),
                targetEffect2.expectIsStartedOnceButNotCancelled(),
            ),
        )
    }

    @Test
    fun test_start_sourceUpdatesRevokedSimultaneously_observed() {
        slottedStimulationScenarioBank_sourceEffectCellUpdatesRevoked.forEach { slottedStimulationScenario ->
            test_start_sourceUpdatesRevokedSimultaneously(
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    @Test
    fun test_start_sourceUpdatesRevokedSimultaneously_nonObserved() {
        test_start_sourceUpdatesRevokedSimultaneously(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            slottedStimulationScenario = arbitrarySlottedStimulationScenario_sourceEffectCellUpdatesRevoked,
        )
    }

    private fun test_start_sourceUpdatesRevokedSimultaneously(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)

        val sourceCell = TestInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        Effect_Cell_start_testUtils.executeStartTransaction(
            subjectCellEffect = subjectEffect,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = sourceCell.revokingUpdate(
                tag = SourceEffectCellTag,
                newValue = targetEffect2,
            ).bind(slottedStimulationScenario),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectNoValueTransition(
                expectedUnaffectedValue = 10,
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1.expectIsStartedOnceButNotCancelled(),
                targetEffect2.expectIsNotStarted(),
            ),
        )
    }

    @Test
    fun test_start_sourceUpdatesCorrectedSimultaneously_observed() {
        slottedStimulationScenarioBank_sourceEffectCellUpdatesCorrected.forEach { slottedStimulationScenario ->
            test_start_sourceUpdatesCorrectedSimultaneously(
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    @Test
    fun test_start_sourceUpdatesCorrectedSimultaneously_nonObserved() {
        test_start_sourceUpdatesCorrectedSimultaneously(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            slottedStimulationScenario = arbitrarySlottedStimulationScenario_sourceEffectCellUpdatesCorrected,
        )
    }

    private fun test_start_sourceUpdatesCorrectedSimultaneously(
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

        Effect_Cell_start_testUtils.executeStartTransaction(
            subjectCellEffect = subjectEffect,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = sourceCell.correctingUpdate(
                tag = SourceEffectCellTag,
                intermediateNewValue = targetEffect2,
                correctedNewValue = targetEffect3,
            ).bind(slottedStimulationScenario),
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
    }
}
