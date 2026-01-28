package dev.azide.core.cell

import dev.azide.core.Cell
import dev.azide.core.Effect
import dev.azide.core.actuate
import dev.azide.core.test_utils.ExpectedImpact
import dev.azide.core.test_utils.TestTargetEffect
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.effect_cell.Effect_Cell_startRevoked_quickCancelledRevoked_testUtils
import dev.azide.core.test_utils.expectIsNotStarted
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulationScenario
import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationBank
import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationMap
import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationScenario
import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationTag
import dev.azide.core.test_utils.stimulation_combinatorics.asTestSlottedStimulation5
import kotlin.test.Test

@Suppress("ClassName")
class Cell_actuate_startRevoked_quickCancelledRevoked_sourceUpdatesRevoked_tests {
    private enum class MyStimulationTag : TestStimulationTag {
        SourceCellUpdates, SourceCellRevokesUpdate,
    }

    private val sourceCellUpdatesRevokedStimulationSequence = TestStimulationScenario.of(
        MyStimulationTag.SourceCellUpdates,
        MyStimulationTag.SourceCellRevokesUpdate,
    )

    private val slottedStimulationBank = TestStimulationBank.build(
        sourceCellUpdatesRevokedStimulationSequence,
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
        slottedStimulationScenario: TestSlottedStimulationScenario<TestSlotCount.Count5>,
    ) {
        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)

        val sourceCell = TestInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.actuate()

        Effect_Cell_startRevoked_quickCancelledRevoked_testUtils.executeStartTransaction(
            subjectCellEffect = subjectEffect,
            slottedInputStimulation = slottedStimulationScenario.bind(
                stimulationMap = TestStimulationMap.of(
                    MyStimulationTag.SourceCellUpdates to sourceCell.update(
                        newValue = targetEffect2,
                    ),
                    MyStimulationTag.SourceCellRevokesUpdate to sourceCell.revokeUpdate(),
                ),
            ).asTestSlottedStimulation5,
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
