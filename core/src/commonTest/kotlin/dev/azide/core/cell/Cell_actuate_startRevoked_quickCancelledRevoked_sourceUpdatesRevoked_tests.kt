package dev.azide.core.cell

import dev.azide.core.Cell
import dev.azide.core.Effect
import dev.azide.core.actuate
import dev.azide.core.test_utils.TestTargetEffect
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.cell.TestInputCellTag
import dev.azide.core.test_utils.cell.revokingUpdate
import dev.azide.core.test_utils.effect_cell.Effect_Cell_startRevoked_quickCancelledRevoked_testUtils
import dev.azide.core.test_utils.expectIsNotStarted
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulationScenario
import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationBank
import dev.azide.core.test_utils.stimulation_combinatorics.bind
import kotlin.test.Test

@Suppress("ClassName")
class Cell_actuate_startRevoked_quickCancelledRevoked_sourceUpdatesRevoked_tests {
    private data object SourceEffectCellTag : TestInputCellTag

    private typealias SuitableSlotCount = TestSlotCount.Count5

    private typealias SuitableTestSlottedStimulationScenario = TestSlottedStimulationScenario<SuitableSlotCount>

    private val slottedStimulationBank = TestStimulationBank.build(
        TestInputCellTag.revokedUpdateScenario(inputCellTag = SourceEffectCellTag),
    ).distribute(
        slotCount = TestSlotCount.Count5,
    )

    @Test
    fun test_startRevoked_quickCancelledRevoked_sourceUpdatesRevoked() {
        slottedStimulationBank.forEach {
            test_startRevoked_quickCancelledRevoked_sourceUpdatesRevoked(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_startRevoked_quickCancelledRevoked_sourceUpdatesRevoked(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)

        val sourceCell = TestInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        Effect_Cell_startRevoked_quickCancelledRevoked_testUtils.executeStartTransaction(
            subjectCellEffect = subjectEffect,
            slottedInputStimulation = sourceCell.revokingUpdate(
                tag = SourceEffectCellTag,
                newValue = targetEffect2,
            ).bind(
                slottedStimulationScenario,
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetEffect1.expectIsNotStarted(),
                targetEffect2.expectIsNotStarted(),
            ),
        )

        Cell_actuate_testUtils.verifyEffectNotOngoing(
            sourceCell = sourceCell,
        )
    }
}
