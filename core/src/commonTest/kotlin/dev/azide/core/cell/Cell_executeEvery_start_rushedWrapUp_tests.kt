package dev.azide.core.cell

import dev.azide.core.Cell
import dev.azide.core.Effect
import dev.azide.core.executeEvery
import dev.azide.core.test_utils.TestTargetActionRecorder
import dev.azide.core.test_utils.cell.Cell_expectations_testUtils
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.cell.updating_deprecated
import dev.azide.core.test_utils.effect_cell.Effect_Cell_start_rushedWrapUp_testUtils
import dev.azide.core.test_utils.expectIsExecutedOnce
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulationScenario
import dev.azide.core.test_utils.stimulation_combinatorics.bind
import kotlin.test.Test

@Suppress("ClassName", "PrivatePropertyName")
class Cell_executeEvery_start_rushedWrapUp_tests {
    private typealias SuitableSlotCount = TestSlotCount.Count3

    private typealias SuitableTestSlottedStimulationScenario = TestSlottedStimulationScenario<SuitableSlotCount>

    private val slottedStimulationScenarioBank_sourceActionCellUpdates =
        Cell_executeEvery_testUtils.stimulationScenarioBank_sourceActionCellUpdates.distribute(slotCount = SuitableSlotCount)

    @Test
    fun test_start_rushedWrapUp() {
        val targetActionRecorder1 = TestTargetActionRecorder.pure(result = 10)

        val sourceCell = TestInputCell(
            initialValue = targetActionRecorder1.recordedAction,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.executeEvery()

        Effect_Cell_start_rushedWrapUp_testUtils.testStart(
            subjectCellEffect = subjectEffect,
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectNoValueTransition(
                expectedUnaffectedValue = 10,
            ),
            expectedTargetImpact = targetActionRecorder1.expectIsExecutedOnce(),
        )
    }

    @Test
    fun test_start_rushedWrapUp_sourceUpdatesSimultaneously() {
        slottedStimulationScenarioBank_sourceActionCellUpdates.forEach { slottedStimulationScenario ->
            test_start_rushedWrapUp_sourceUpdatesSimultaneously(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_start_rushedWrapUp_sourceUpdatesSimultaneously(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetActionRecorder1 = TestTargetActionRecorder.pure(result = 10)
        val targetActionRecorder2 = TestTargetActionRecorder.pure(result = 20)

        val sourceCell = TestInputCell(
            initialValue = targetActionRecorder1.recordedAction,
        )

        val subjectEffect: Effect<Cell<Int>> = sourceCell.executeEvery()

        Effect_Cell_start_rushedWrapUp_testUtils.testStart(
            subjectCellEffect = subjectEffect,
            slottedInputStimulation = sourceCell.updating_deprecated(
                tag = Cell_executeEvery_testUtils.SourceActionCellTag,
                newValue = targetActionRecorder2.recordedAction,
            ).bind(slottedStimulationScenario),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                expectedOldValue = 10,
                expectedNewValue = 20,
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetActionRecorder1.expectIsExecutedOnce(),
                targetActionRecorder2.expectIsExecutedOnce(),
            ),
        )
    }
}
