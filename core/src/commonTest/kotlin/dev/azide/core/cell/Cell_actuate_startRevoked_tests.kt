package dev.azide.core.cell

import dev.azide.core.Cell
import dev.azide.core.Effect
import dev.azide.core.actuate
import dev.azide.core.cell.Cell_actuate_testUtils.SourceEffectCellTag
import dev.azide.core.test_utils.TestTargetEffect
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.cell.correctingUpdate
import dev.azide.core.test_utils.cell.revokingUpdate
import dev.azide.core.test_utils.cell.updating
import dev.azide.core.test_utils.effect_cell.Effect_Cell_startRevoked_testUtils
import dev.azide.core.test_utils.expectIsNotStarted
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulationScenario
import dev.azide.core.test_utils.stimulation_combinatorics.bind
import kotlin.test.Test

@Suppress("ClassName", "PrivatePropertyName")
class Cell_actuate_startRevoked_tests {
    private typealias SuitableSlotCount = TestSlotCount.Count3

    private typealias SuitableTestSlottedStimulationScenario = TestSlottedStimulationScenario<SuitableSlotCount>

    private val slottedStimulationScenarioBank_sourceEffectCellUpdates =
        Cell_actuate_testUtils.stimulationScenarioBank_sourceEffectCellUpdates.distribute(slotCount = SuitableSlotCount)

    private val slottedStimulationScenarioBank_sourceEffectCellUpdatesRevoked =
        Cell_actuate_testUtils.stimulationScenarioBank_sourceEffectCellUpdatesRevoked.distribute(slotCount = SuitableSlotCount)

    private val slottedStimulationScenarioBank_sourceEffectCellUpdatesCorrected =
        Cell_actuate_testUtils.stimulationScenarioBank_sourceEffectCellUpdatesCorrected.distribute(slotCount = SuitableSlotCount)

    @Test
    fun test_startRevoked() {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)

        val sourceCell = TestInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        Effect_Cell_startRevoked_testUtils.executeStartTransaction(
            subjectCellEffect = subjectEffect,
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1.expectIsNotStarted(),
                targetEffect2.expectIsNotStarted(),
            ),
        )
    }

    @Test
    fun test_startRevoked_sourceUpdatesSimultaneously() {
        slottedStimulationScenarioBank_sourceEffectCellUpdates.forEach { slottedStimulationScenario ->
            test_startRevoked_sourceUpdatesSimultaneously(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_startRevoked_sourceUpdatesSimultaneously(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)

        val sourceCell = TestInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        Effect_Cell_startRevoked_testUtils.executeStartTransaction(
            subjectCellEffect = subjectEffect,
            slottedInputStimulation = sourceCell.updating(
                tag = SourceEffectCellTag,
                newValue = targetEffect2,
            ).bind(slottedStimulationScenario),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1.expectIsNotStarted(),
                targetEffect2.expectIsNotStarted(),
            ),
        )
    }

    @Test
    fun test_startRevoked_sourceUpdatesRevokedSimultaneously() {
        slottedStimulationScenarioBank_sourceEffectCellUpdatesRevoked.forEach { slottedStimulationScenario ->
            test_startRevoked_sourceUpdatesRevokedSimultaneously(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_startRevoked_sourceUpdatesRevokedSimultaneously(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)

        val sourceCell = TestInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        Effect_Cell_startRevoked_testUtils.executeStartTransaction(
            subjectCellEffect = subjectEffect,
            slottedInputStimulation = sourceCell.revokingUpdate(
                tag = SourceEffectCellTag,
                newValue = targetEffect2,
            ).bind(slottedStimulationScenario),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1.expectIsNotStarted(),
                targetEffect2.expectIsNotStarted(),
            ),
        )
    }

    @Test
    fun test_startRevoked_sourceUpdatesCorrectedSimultaneously() {
        slottedStimulationScenarioBank_sourceEffectCellUpdatesCorrected.forEach { slottedStimulationScenario ->
            test_startRevoked_sourceUpdatesCorrectedSimultaneously(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_startRevoked_sourceUpdatesCorrectedSimultaneously(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)
        val targetEffect3 = TestTargetEffect.pure(result = 30)

        val sourceCell = TestInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        Effect_Cell_startRevoked_testUtils.executeStartTransaction(
            subjectCellEffect = subjectEffect,
            slottedInputStimulation = sourceCell.correctingUpdate(
                tag = SourceEffectCellTag,
                intermediateNewValue = targetEffect2,
                correctedNewValue = targetEffect3,
            ).bind(slottedStimulationScenario),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1.expectIsNotStarted(),
                targetEffect2.expectIsNotStarted(),
                targetEffect3.expectIsNotStarted(),
            ),
        )
    }
}
