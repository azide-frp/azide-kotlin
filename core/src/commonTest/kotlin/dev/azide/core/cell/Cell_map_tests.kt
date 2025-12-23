package dev.azide.core.cell

import dev.azide.core.Cell
import dev.azide.core.map
import dev.azide.core.test_utils.TestInputStimulation
import dev.azide.core.test_utils.cell.CellTestUtils
import kotlin.test.Test

@Suppress("ClassName")
class Cell_map_tests {
    @Test
    fun test_passiveSample() {
        val sourceCell = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val subjectCell = sourceCell.map { it.toString() }

        CellTestUtils.verifyAtRest(
            subjectCell = subjectCell,
            expectedValue = "10",
        )
    }

    @Test
    fun test_passiveSample_sourceConst() {
        val subjectCell = Cell.Const(
            constValue = 10,
        ).map { it.toString() }

        CellTestUtils.verifyFrozen(
            subjectCell = subjectCell,
            expectedFrozenValue = "10",
        )
    }

    @Test
    fun test_update() {
        val sourceCell = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val subjectCell = sourceCell.map { it.toString() }

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = sourceCell.update(
                newValue = 11,
            ),
            expectedOldValue = "10",
            expectedNewValue = "11",
        )
    }

    @Test
    fun test_update_revoked() {
        val sourceCell = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val subjectCell = sourceCell.map { it.toString() }

        CellTestUtils.verifyDoesNotUpdateEffectively(
            subjectCell = subjectCell,
            inputStimulation = TestInputStimulation.combine(
                sourceCell.update(
                    newValue = 11,
                ),
                sourceCell.revokeUpdate(),
            ),
            expectedUnaffectedValue = "10",
        )
    }


    @Test
    fun test_update_corrected() {
        val sourceCell = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val subjectCell = sourceCell.map { it.toString() }

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = TestInputStimulation.combine(
                sourceCell.update(
                    newValue = 11,
                ),
                sourceCell.correctUpdate(
                    correctedNewValue = 12,
                ),
            ),
            expectedOldValue = "10",
            expectedNewValue = "12",
        )
    }
}
