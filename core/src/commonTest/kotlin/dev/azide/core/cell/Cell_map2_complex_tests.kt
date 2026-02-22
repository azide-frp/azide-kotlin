package dev.azide.core.cell

import dev.azide.core.Cell
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.cell.CellTestUtils
import dev.azide.core.test_utils.cell.TestInputCell
import kotlin.test.Test

@Suppress("ClassName")
class Cell_map2_complex_tests {
    @Test
    fun test_passiveSample() {
        val sourceCell1 = TestInputCell(
            initialValue = 10,
        )

        val sourceCell2 = TestInputCell(
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
        val sourceCell1 = TestInputCell(
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
    fun test_sharedSourceUpdates() {
        val sourceCell = TestInputCell(
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
    fun test_onlySource1Updates() {
        val sourceCell1 = TestInputCell(
            initialValue = 10,
        )

        val sourceCell2 = TestInputCell(
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
    fun test_onlySource2Updates() {
        val sourceCell1 = TestInputCell(
            initialValue = 10,
        )

        val sourceCell2 = TestInputCell(
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
    fun test_singleSourceUpdates_otherSourceConst() {
        val sourceCell1 = Cell.Const(
            constValue = 10,
        )

        val sourceCell2 = TestInputCell(
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
    fun test_singleSourceUpdates_revoked() {
        val sourceCell1 = TestInputCell(
            initialValue = 10,
        )

        val sourceCell2 = TestInputCell(
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
            inputStimulation = TestStimulation.combine(
                sourceCell2.update(
                    newValue = 'B',
                ),
                sourceCell2.revokeUpdate(),
            ),
            expectedUnaffectedValue = "10:A",
        )
    }

    @Test
    fun test_singleSourceUpdates_corrected() {
        val sourceCell1 = TestInputCell(
            initialValue = 10,
        )

        val sourceCell2 = TestInputCell(
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
            inputStimulation = TestStimulation.combine(
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
    fun test_bothSourcesUpdate() {
        val sourceCell1 = TestInputCell(
            initialValue = 10,
        )

        val sourceCell2 = TestInputCell(
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
            inputStimulation = TestStimulation.combine(
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
    fun test_bothSourcesUpdate_oneRevoked() {
        val sourceCell1 = TestInputCell(
            initialValue = 10,
        )

        val sourceCell2 = TestInputCell(
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
            inputStimulation = TestStimulation.combine(
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
    fun test_bothSourcesUpdate_bothRevoked() {
        val sourceCell1 = TestInputCell(
            initialValue = 10,
        )

        val sourceCell2 = TestInputCell(
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
            inputStimulation = TestStimulation.combine(
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
