package dev.azide.core.cell

import dev.azide.core.map
import dev.azide.core.test_utils.TestSlotDispatcher1x2
import dev.azide.core.test_utils.TestSlotDispatcher2x2
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
        TestSlotDispatcher1x2.entries.forEach { dispatcher ->
            test_sourceUpdates(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_sourceUpdates(
        dispatcher: TestSlotDispatcher1x2,
    ) {
        val sourceCell = TestInputCell(
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
    fun test_sourceUpdates_revoked() {
        TestSlotDispatcher2x2.entries.forEach { dispatcher ->
            test_sourceUpdates_revoked(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_sourceUpdates_revoked(
        dispatcher: TestSlotDispatcher2x2,
    ) {
        val sourceCell = TestInputCell(
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
    fun test_sourceUpdates_corrected() {
        TestSlotDispatcher2x2.entries.forEach { dispatcher ->
            test_sourceUpdates_corrected(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_sourceUpdates_corrected(
        dispatcher: TestSlotDispatcher2x2,
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
            ).bind(dispatcher),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldValue = "10",
                expectedNewValue = "12",
            ),
        )
    }
}
