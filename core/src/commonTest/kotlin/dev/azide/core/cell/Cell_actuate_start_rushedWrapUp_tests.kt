package dev.azide.core.cell

import dev.azide.core.Cell
import dev.azide.core.Effect
import dev.azide.core.actuate
import dev.azide.core.cell.Cell_actuate_testUtils.SourceEffectCellTag
import dev.azide.core.test_utils.TestTargetEffect
import dev.azide.core.test_utils.cell.Cell_expectations_testUtils
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.cell.updating
import dev.azide.core.test_utils.effect_cell.Effect_Cell_start_rushedWrapUp_testUtils
import dev.azide.core.test_utils.expectIsStartedOnceAndCancelledOnce
import dev.azide.core.test_utils.expectIsStartedOnceButNotCancelled
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulationScenario
import dev.azide.core.test_utils.stimulation_combinatorics.bind
import kotlin.test.Test

@Suppress("ClassName", "PrivatePropertyName")
class Cell_actuate_start_rushedWrapUp_tests {
    private typealias SuitableSlotCount = TestSlotCount.Count3

    private typealias SuitableTestSlottedStimulationScenario = TestSlottedStimulationScenario<SuitableSlotCount>

    private val slottedStimulationBank_sourceEffectCellUpdates =
        Cell_actuate_testUtils.stimulationBank_sourceEffectCellUpdates.distribute(slotCount = SuitableSlotCount)

    @Test
    fun test_start_rushedWrapUp() {
        val targetEffect1 = TestTargetEffect.pure(result = 10)

        val sourceCell = TestInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        Effect_Cell_start_rushedWrapUp_testUtils.executeStartTransaction(
            subjectCellEffect = subjectEffect,
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectNoValueTransition(
                expectedUnaffectedValue = 10,
            ),
            expectedTargetImpact = targetEffect1.expectIsStartedOnceButNotCancelled(),
        )
    }

    @Test
    fun test_start_rushedWrapUp_sourceUpdatesSimultaneously() {
        slottedStimulationBank_sourceEffectCellUpdates.forEach { slottedStimulationScenario ->
            test_start_rushedWrapUp_sourceUpdatesSimultaneously(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_start_rushedWrapUp_sourceUpdatesSimultaneously(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)

        val sourceCell = TestInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        Effect_Cell_start_rushedWrapUp_testUtils.executeStartTransaction(
            subjectCellEffect = subjectEffect,
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
}
