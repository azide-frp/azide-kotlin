package dev.azide.core.cell

import dev.azide.core.Cell
import dev.azide.core.pullExternally
import dev.azide.core.sampleEvery
import dev.azide.core.sampling
import dev.azide.core.test_utils.TestSlottedStimulationScenario1x2
import dev.azide.core.test_utils.TestSlottedStimulationScenario2x2
import dev.azide.core.test_utils.bind
import dev.azide.core.test_utils.cell.Cell_expectations_testUtils
import dev.azide.core.test_utils.cell.Cell_reaction_testUtils
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.cell.correctingUpdate
import dev.azide.core.test_utils.cell.revokingUpdate
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import kotlin.test.Test

@Suppress("ClassName")
class Cell_sampleEvery_reaction_tests {
    @Test
    fun test_step_sourceUpdates() {
        TestSlottedStimulationScenario1x2.entries.forEach { slottedStimulationScenario ->
            test_step_sourceUpdates(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_step_sourceUpdates(
        slottedStimulationScenario: TestSlottedStimulationScenario1x2,
    ) {
        val helperCell1 = TestInputCell(initialValue = 10)
        val helperCell2 = TestInputCell(initialValue = 20)

        val sourceCell = TestInputCell(
            initialValue = helperCell1.sampling,
        )

        val subjectCell: Cell<Int> = sourceCell.sampleEvery().pullExternally()

        Cell_reaction_testUtils.executeReactionTransaction(
            subjectCell = subjectCell,
            slottedInputStimulation = sourceCell.update(
                newValue = helperCell2.sampling,
            ).bind(slottedStimulationScenario),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                expectedOldValue = 10,
                expectedNewValue = 20,
            ),
        )
    }

    @Test
    fun test_step_sourceUpdatesRevoked() {
        TestSlottedStimulationScenario2x2.entries.forEach { slottedStimulationScenario ->
            test_step_sourceUpdatesRevoked(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_step_sourceUpdatesRevoked(
        slottedStimulationScenario: TestSlottedStimulationScenario2x2,
    ) {
        val helperCell1 = TestInputCell(initialValue = 10)
        val helperCell2 = TestInputCell(initialValue = 20)

        val sourceCell = TestInputCell(
            initialValue = helperCell1.sampling,
        )

        val subjectCell: Cell<Int> = sourceCell.sampleEvery().pullExternally()

        Cell_reaction_testUtils.executeReactionTransaction(
            subjectCell = subjectCell,
            slottedInputStimulation = sourceCell.revokingUpdate(
                newValue = helperCell2.sampling,
            ).bind(slottedStimulationScenario),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectNoValueTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedValue = 10,
            ),
        )
    }

    @Test
    fun test_step_sourceUpdatesCorrected() {
        TestSlottedStimulationScenario2x2.entries.forEach { slottedStimulationScenario ->
            test_step_sourceUpdatesCorrected(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_step_sourceUpdatesCorrected(
        slottedStimulationScenario: TestSlottedStimulationScenario2x2,
    ) {
        val helperCell1 = TestInputCell(initialValue = 10)
        val helperCell2 = TestInputCell(initialValue = 20)
        val helperCell3 = TestInputCell(initialValue = 30)

        val sourceCell = TestInputCell(
            initialValue = helperCell1.sampling,
        )

        val subjectCell: Cell<Int> = sourceCell.sampleEvery().pullExternally()

        Cell_reaction_testUtils.executeReactionTransaction(
            subjectCell = subjectCell,
            slottedInputStimulation = sourceCell.correctingUpdate(
                intermediateNewValue = helperCell2.sampling,
                correctedNewValue = helperCell3.sampling,
            ).bind(slottedStimulationScenario),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldValue = 10,
                expectedNewValue = 30,
            ),
        )
    }
}
