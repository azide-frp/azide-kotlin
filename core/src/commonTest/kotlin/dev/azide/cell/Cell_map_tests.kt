package dev.azide.cell

import dev.azide.Cell
import dev.azide.map
import dev.azide.test_utils.cell.CellTestUtils
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
}
