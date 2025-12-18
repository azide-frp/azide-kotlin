package dev.azide.core.cell

import dev.azide.core.Cell
import dev.azide.core.map
import dev.azide.core.test_utils.cell.CellTestUtils
import kotlin.test.Test

@Suppress("ClassName")
class Cell_map_tests {
    @Test
    fun test_passiveSample() {
        val sourceCell = _root_ide_package_.dev.azide.core.test_utils.cell.CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val subjectCell = sourceCell.map { it.toString() }

        _root_ide_package_.dev.azide.core.test_utils.cell.CellTestUtils.verifyAtRest(
            subjectCell = subjectCell,
            expectedValue = "10",
        )
    }

    @Test
    fun test_passiveSample_sourceConst() {
        val subjectCell = Cell.Const(
            constValue = 10,
        ).map { it.toString() }

        _root_ide_package_.dev.azide.core.test_utils.cell.CellTestUtils.verifyFrozen(
            subjectCell = subjectCell,
            expectedFrozenValue = "10",
        )
    }

    @Test
    fun test_update() {
        val sourceCell = _root_ide_package_.dev.azide.core.test_utils.cell.CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val subjectCell = sourceCell.map { it.toString() }

        _root_ide_package_.dev.azide.core.test_utils.cell.CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = sourceCell.update(
                newValue = 11,
            ),
            expectedOldValue = "10",
            expectedNewValue = "11",
        )
    }
}
