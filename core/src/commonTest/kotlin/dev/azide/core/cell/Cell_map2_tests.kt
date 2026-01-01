package dev.azide.core.cell

import dev.azide.core.Cell
import dev.azide.core.test_utils.TestInputStimulation
import dev.azide.core.test_utils.cell.CellTestUtils
import kotlin.test.Test

@Suppress("ClassName")
class Cell_map2_tests {
    @Test
    fun test_passiveSample() {
        val sourceCell1 = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val sourceCell2 = CellTestUtils.createInputCell(
            initialValue = 'A',
        )

        val subjectCell = Cell.map2(
            cell1 = sourceCell1,
            cell2 = sourceCell2,
        ) { value1, value2 ->
            "$value1:$value2"
        }

        CellTestUtils.verifyAtRest(
            subjectCell = subjectCell,
            expectedValue = "10:A",
        )
    }

    @Test
    fun test_passiveSample_singleSourceConst() {
        val sourceCell1 = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val sourceCell2 = Cell.Const(
            constValue = 'A',
        )

        val subjectCell = Cell.map2(
            cell1 = sourceCell1,
            cell2 = sourceCell2,
        ) { value1, value2 ->
            "$value1:$value2"
        }

        CellTestUtils.verifyAtRest(
            subjectCell = subjectCell,
            expectedValue = "10:A",
        )
    }

    @Test
    fun test_passiveSample_allSourcesConst() {
        val sourceCell1 = Cell.Const(
            constValue = 10,
        )

        val sourceCell2 = Cell.Const(
            constValue = 'A',
        )

        val subjectCell = Cell.map2(
            cell1 = sourceCell1,
            cell2 = sourceCell2,
        ) { value1, value2 ->
            "$value1:$value2"
        }

        CellTestUtils.verifyAtRest(
            subjectCell = subjectCell,
            expectedValue = "10:A",
        )
    }

    @Test
    fun test_update_sharedSource() {
        val sourceCell = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val subjectCell = Cell.map2(
            cell1 = sourceCell,
            cell2 = sourceCell,
        ) { value1, value2 ->
            "$value1:$value2"
        }

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = sourceCell.update(
                newValue = 11,
            ),
            expectedOldValue = "10:10",
            expectedNewValue = "11:11",
        )
    }

    @Test
    fun test_update_onlySource1() {
        val sourceCell1 = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val sourceCell2 = CellTestUtils.createInputCell(
            initialValue = 'A',
        )

        val subjectCell = Cell.map2(
            cell1 = sourceCell1,
            cell2 = sourceCell2,
        ) { value1, value2 ->
            "$value1:$value2"
        }

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = sourceCell1.update(
                newValue = 11,
            ),
            expectedOldValue = "10:A",
            expectedNewValue = "11:A",
        )
    }

    @Test
    fun test_update_onlySource2() {
        val sourceCell1 = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val sourceCell2 = CellTestUtils.createInputCell(
            initialValue = 'A',
        )

        val subjectCell = Cell.map2(
            cell1 = sourceCell1,
            cell2 = sourceCell2,
        ) { value1, value2 ->
            "$value1:$value2"
        }

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = sourceCell2.update(
                newValue = 'B',
            ),
            expectedOldValue = "10:A",
            expectedNewValue = "10:B",
        )
    }

    @Test
    fun test_update_singleSource_otherSourceConst() {
        val sourceCell1 = Cell.Const(
            constValue = 10,
        )

        val sourceCell2 = CellTestUtils.createInputCell(
            initialValue = 'A',
        )

        val subjectCell = Cell.map2(
            cell1 = sourceCell1,
            cell2 = sourceCell2,
        ) { value1, value2 ->
            "$value1:$value2"
        }

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = sourceCell2.update(
                newValue = 'B',
            ),
            expectedOldValue = "10:A",
            expectedNewValue = "10:B",
        )
    }

    @Test
    fun test_update_singleSource_revoked() {
        val sourceCell1 = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val sourceCell2 = CellTestUtils.createInputCell(
            initialValue = 'A',
        )

        val subjectCell = Cell.map2(
            cell1 = sourceCell1,
            cell2 = sourceCell2,
        ) { value1, value2 ->
            "$value1:$value2"
        }

        CellTestUtils.verifyDoesNotUpdateEffectively(
            subjectCell = subjectCell,
            inputStimulation = TestInputStimulation.combine(
                sourceCell2.update(
                    newValue = 'B',
                ),
                sourceCell2.revokeUpdate(),
            ),
            expectedUnaffectedValue = "10:A",
        )
    }

    @Test
    fun test_update_singleSource_corrected() {
        val sourceCell1 = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val sourceCell2 = CellTestUtils.createInputCell(
            initialValue = 'A',
        )

        val subjectCell = Cell.map2(
            cell1 = sourceCell1,
            cell2 = sourceCell2,
        ) { value1, value2 ->
            "$value1:$value2"
        }

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = TestInputStimulation.combine(
                sourceCell2.update(
                    newValue = 'B',
                ),
                sourceCell1.update(
                    newValue = 11,
                ),
                sourceCell2.correctUpdate(
                    correctedNewValue = 'C',
                ),
            ),
            expectedOldValue = "10:A",
            expectedNewValue = "11:C",
        )
    }

    @Test
    fun test_update_bothSources() {
        val sourceCell1 = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val sourceCell2 = CellTestUtils.createInputCell(
            initialValue = 'A',
        )

        val subjectCell = Cell.map2(
            cell1 = sourceCell1,
            cell2 = sourceCell2,
        ) { value1, value2 ->
            "$value1:$value2"
        }

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = TestInputStimulation.combine(
                sourceCell1.update(
                    newValue = 11,
                ),
                sourceCell2.update(
                    newValue = 'B',
                ),
            ),
            expectedOldValue = "10:A",
            expectedNewValue = "11:B",
        )
    }

    @Test
    fun test_update_bothSources_oneRevoked() {
        val sourceCell1 = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val sourceCell2 = CellTestUtils.createInputCell(
            initialValue = 'A',
        )

        val subjectCell = Cell.map2(
            cell1 = sourceCell1,
            cell2 = sourceCell2,
        ) { value1, value2 ->
            "$value1:$value2"
        }

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = TestInputStimulation.combine(
                sourceCell1.update(
                    newValue = 11,
                ),
                sourceCell2.update(
                    newValue = 'B',
                ),
                sourceCell1.revokeUpdate(),
            ),
            expectedOldValue = "10:A",
            expectedNewValue = "10:B",
        )
    }

    @Test
    fun test_update_bothSources_bothRevoked() {
        val sourceCell1 = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val sourceCell2 = CellTestUtils.createInputCell(
            initialValue = 'A',
        )

        val subjectCell = Cell.map2(
            cell1 = sourceCell1,
            cell2 = sourceCell2,
        ) { value1, value2 ->
            "$value1:$value2"
        }

        CellTestUtils.verifyDoesNotUpdateEffectively(
            subjectCell = subjectCell,
            inputStimulation = TestInputStimulation.combine(
                sourceCell1.update(
                    newValue = 11,
                ),
                sourceCell2.update(
                    newValue = 'B',
                ),
                sourceCell1.revokeUpdate(),
                sourceCell2.revokeUpdate(),
            ),
            expectedUnaffectedValue = "10:A",
        )
    }
}
