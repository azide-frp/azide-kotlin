package dev.azide.core.cell

import dev.azide.core.Cell
import dev.azide.core.Moment
import dev.azide.core.holding
import dev.azide.core.sampleEvery
import dev.azide.core.sampling
import dev.azide.core.test_utils.ExpectedCellReactionTestUtils
import dev.azide.core.test_utils.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.TestSlotDispatcher1x3
import dev.azide.core.test_utils.bind
import dev.azide.core.test_utils.cell.CellTestUtils
import dev.azide.core.test_utils.cell.revokingUpdate
import dev.azide.core.test_utils.event_stream.EventStreamTestUtils
import dev.azide.core.test_utils.stateful.StatefulTestUtils_spawn_rushedWrapUp
import kotlin.test.Test

@Suppress("ClassName")
class Cell_sampleEvery_spawn_rushedWrapUp_tests {
    @Test
    fun test_spawn_rushedWrapUp() {
        val helperCell1 = CellTestUtils.createInputCell(initialValue = 10)

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = helperCell1.sampling,
        )

        val subjectSpawnMoment: Moment<Cell<Int>> = sourceCell.sampleEvery()

        StatefulTestUtils_spawn_rushedWrapUp.executeSpawnTransaction(
            subjectSpawnMoment = subjectSpawnMoment,
            expectedSubjectTransition = ExpectedCellReactionTestUtils.expectNoTransition(
                expectedUnaffectedValue = 10,
            ),
        )
    }

    @Test
    fun test_spawn_rushedWrapUp_sourceEmitsSimultaneously() {
        TestSlotDispatcher1x3.entries.forEach { dispatcher ->
            test_spawn_rushedWrapUp_sourceEmitsSimultaneously(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_spawn_rushedWrapUp_sourceEmitsSimultaneously(
        dispatcher: TestSlotDispatcher1x3,
    ) {
        val helperCell1 = CellTestUtils.createInputCell(initialValue = 10)
        val helperCell2 = CellTestUtils.createInputCell(initialValue = 20)

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = helperCell1.sampling,
        )

        val subjectSpawnMoment: Moment<Cell<Int>> = sourceCell.sampleEvery()

        StatefulTestUtils_spawn_rushedWrapUp.executeSpawnTransaction(
            subjectSpawnMoment = subjectSpawnMoment,
            slottedInputStimulation = sourceCell.update(
                newValue = helperCell2.sampling,
            ).bind(dispatcher),
            expectedSubjectTransition = ExpectedCellReactionTestUtils.expectTransition(
                expectedOldValue = 10,
                expectedNewValue = 20,
            ),
        )
    }
}
