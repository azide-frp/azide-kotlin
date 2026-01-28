package dev.azide.core.cell

import dev.azide.core.Cell
import dev.azide.core.pullExternally
import dev.azide.core.sampleEvery
import dev.azide.core.sampling
import dev.azide.core.test_utils.ExpectedCellReactionTestUtils
import dev.azide.core.test_utils.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.TestSlotDispatcher1x2
import dev.azide.core.test_utils.TestSlotDispatcher2x2
import dev.azide.core.test_utils.bind
import dev.azide.core.test_utils.cell.CellTestUtils
import dev.azide.core.test_utils.cell.correctingUpdate
import dev.azide.core.test_utils.cell.revokingUpdate
import dev.azide.core.test_utils.stateful.TestUtils_reaction
import kotlin.test.Test

@Suppress("ClassName")
class Cell_sampleEvery_reaction_tests {
    @Test
    fun test_step_sourceUpdates() {
        TestSlotDispatcher1x2.entries.forEach { dispatcher ->
            test_step_sourceUpdates(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_step_sourceUpdates(
        dispatcher: TestSlotDispatcher1x2,
    ) {
        val helperCell1 = CellTestUtils.createInputCell(initialValue = 10)
        val helperCell2 = CellTestUtils.createInputCell(initialValue = 20)

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = helperCell1.sampling,
        )

        val subjectCell: Cell<Int> = sourceCell.sampleEvery().pullExternally()

        TestUtils_reaction.executeReactionTransaction(
            subject = subjectCell,
            slottedInputStimulation = sourceCell.update(
                newValue = helperCell2.sampling,
            ).bind(dispatcher),
            expectedSubjectTransition = ExpectedCellReactionTestUtils.expectTransition(
                expectedOldValue = 10,
                expectedNewValue = 20,
            ),
        )
    }

    @Test
    fun test_step_sourceUpdatesRevoked() {
        TestSlotDispatcher2x2.entries.forEach { dispatcher ->
            test_step_sourceUpdatesRevoked(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_step_sourceUpdatesRevoked(
        dispatcher: TestSlotDispatcher2x2,
    ) {
        val helperCell1 = CellTestUtils.createInputCell(initialValue = 10)
        val helperCell2 = CellTestUtils.createInputCell(initialValue = 20)

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = helperCell1.sampling,
        )

        val subjectCell: Cell<Int> = sourceCell.sampleEvery().pullExternally()

        TestUtils_reaction.executeReactionTransaction(
            subject = subjectCell,
            slottedInputStimulation = sourceCell.revokingUpdate(
                newValue = helperCell2.sampling,
            ).bind(dispatcher),
            expectedSubjectTransition = ExpectedCellReactionTestUtils.expectNoTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedValue = 10,
            ),
        )
    }

    @Test
    fun test_step_sourceUpdatesCorrected() {
        TestSlotDispatcher2x2.entries.forEach { dispatcher ->
            test_step_sourceUpdatesCorrected(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_step_sourceUpdatesCorrected(
        dispatcher: TestSlotDispatcher2x2,
    ) {
        val helperCell1 = CellTestUtils.createInputCell(initialValue = 10)
        val helperCell2 = CellTestUtils.createInputCell(initialValue = 20)
        val helperCell3 = CellTestUtils.createInputCell(initialValue = 30)

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = helperCell1.sampling,
        )

        val subjectCell: Cell<Int> = sourceCell.sampleEvery().pullExternally()

        TestUtils_reaction.executeReactionTransaction(
            subject = subjectCell,
            slottedInputStimulation = sourceCell.correctingUpdate(
                intermediateNewValue = helperCell2.sampling,
                correctedNewValue = helperCell3.sampling,
            ).bind(dispatcher),
            expectedSubjectTransition = ExpectedCellReactionTestUtils.expectTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldValue = 10,
                expectedNewValue = 30,
            ),
        )
    }
}
