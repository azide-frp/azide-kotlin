package dev.azide.core.cell

import dev.azide.core.map
import dev.azide.core.test_utils.TestSlottedStimulationScenario1x2
import dev.azide.core.test_utils.TestSlottedStimulationScenario2x2
import dev.azide.core.test_utils.bind
import dev.azide.core.test_utils.cell.Cell_expectations_testUtils
import dev.azide.core.test_utils.cell.Cell_reaction_testUtils
import dev.azide.core.test_utils.cell.Cell_sampling_testUtils
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.cell.correctingUpdate
import dev.azide.core.test_utils.cell.revokingUpdate
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import kotlin.test.Test

@Suppress("ClassName")
class Cell_map_tests {
    @Test
    fun test_passiveSample() {
        val sourceCell = TestInputCell(
            initialValue = 10,
        )

        val subjectCell = sourceCell.map { it.toString() }

        Cell_sampling_testUtils.executeReactionTransaction(
            subjectCell = subjectCell,
            expectedSubjectValue = Cell_expectations_testUtils.expectStableValue(
                expectedValue = "10",
            ),
        )
    }

    @Test
    fun test_sourceUpdates() {
        TestSlottedStimulationScenario1x2.entries.forEach { slottedStimulationScenario ->
            test_sourceUpdates(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_sourceUpdates(
        slottedStimulationScenario: TestSlottedStimulationScenario1x2,
    ) {
        val sourceCell = TestInputCell(
            initialValue = 10,
        )

        val subjectCell = sourceCell.map { it.toString() }

        Cell_reaction_testUtils.executeReactionTransaction(
            subjectCell = subjectCell,
            slottedInputStimulation = sourceCell.update(
                newValue = 11,
            ).bind(slottedStimulationScenario),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                expectedOldValue = "10",
                expectedNewValue = "11",
            ),
        )
    }

    @Test
    fun test_sourceUpdates_revoked() {
        TestSlottedStimulationScenario2x2.entries.forEach { slottedStimulationScenario ->
            test_sourceUpdates_revoked(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_sourceUpdates_revoked(
        slottedStimulationScenario: TestSlottedStimulationScenario2x2,
    ) {
        val sourceCell = TestInputCell(
            initialValue = 10,
        )

        val subjectCell = sourceCell.map { it.toString() }

        Cell_reaction_testUtils.executeReactionTransaction(
            subjectCell = subjectCell,
            slottedInputStimulation = sourceCell.revokingUpdate(
                newValue = 11,
            ).bind(slottedStimulationScenario),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectNoValueTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedValue = "10",
            ),
        )
    }

    @Test
    fun test_sourceUpdates_corrected() {
        TestSlottedStimulationScenario2x2.entries.forEach { slottedStimulationScenario ->
            test_sourceUpdates_corrected(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_sourceUpdates_corrected(
        slottedStimulationScenario: TestSlottedStimulationScenario2x2,
    ) {
        val sourceCell = TestInputCell(
            initialValue = 10,
        )

        val subjectCell = sourceCell.map { it.toString() }

        Cell_reaction_testUtils.executeReactionTransaction(
            subjectCell = subjectCell,
            slottedInputStimulation = sourceCell.correctingUpdate(
                intermediateNewValue = 11,
                correctedNewValue = 12,
            ).bind(slottedStimulationScenario),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldValue = "10",
                expectedNewValue = "12",
            ),
        )
    }
}
