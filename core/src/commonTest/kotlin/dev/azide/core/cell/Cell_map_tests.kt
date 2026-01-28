package dev.azide.core.cell

import dev.azide.core.map
import dev.azide.core.test_utils.Cell_expectations_testUtils
import dev.azide.core.test_utils.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.TestSlotDispatcher1x2
import dev.azide.core.test_utils.TestSlotDispatcher2x2
import dev.azide.core.test_utils.bind
import dev.azide.core.test_utils.cell.CellTestUtils
import dev.azide.core.test_utils.cell.Cell_reaction_testUtils
import dev.azide.core.test_utils.cell.Cell_sampling_testUtils
import dev.azide.core.test_utils.cell.correctingUpdate
import dev.azide.core.test_utils.cell.revokingUpdate
import kotlin.test.Test

@Suppress("ClassName")
class Cell_map_tests {
    @Test
    fun test_passiveSample() {
        val sourceCell = CellTestUtils.createInputCell(
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
    fun test_update() {
        TestSlotDispatcher1x2.entries.forEach { dispatcher ->
            test_update(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_update(
        dispatcher: TestSlotDispatcher1x2,
    ) {
        val sourceCell = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val subjectCell = sourceCell.map { it.toString() }

        Cell_reaction_testUtils.executeReactionTransaction(
            subjectCell = subjectCell,
            slottedInputStimulation = sourceCell.update(
                newValue = 11,
            ).bind(dispatcher),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                expectedOldValue = "10",
                expectedNewValue = "11",
            ),
        )
    }

    @Test
    fun test_update_revoked() {
        TestSlotDispatcher2x2.entries.forEach { dispatcher ->
            test_update_revoked(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_update_revoked(
        dispatcher: TestSlotDispatcher2x2,
    ) {
        val sourceCell = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val subjectCell = sourceCell.map { it.toString() }

        Cell_reaction_testUtils.executeReactionTransaction(
            subjectCell = subjectCell,
            slottedInputStimulation = sourceCell.revokingUpdate(
                newValue = 11,
            ).bind(dispatcher),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectNoValueTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedValue = "10",
            ),
        )
    }

    @Test
    fun test_update_corrected() {
        TestSlotDispatcher2x2.entries.forEach { dispatcher ->
            test_update_corrected(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_update_corrected(
        dispatcher: TestSlotDispatcher2x2,
    ) {
        val sourceCell = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val subjectCell = sourceCell.map { it.toString() }

        Cell_reaction_testUtils.executeReactionTransaction(
            subjectCell = subjectCell,
            slottedInputStimulation = sourceCell.correctingUpdate(
                intermediateNewValue = 11,
                correctedNewValue = 12,
            ).bind(dispatcher),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldValue = "10",
                expectedNewValue = "12",
            ),
        )
    }
}
