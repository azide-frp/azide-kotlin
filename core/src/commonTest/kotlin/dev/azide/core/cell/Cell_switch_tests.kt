package dev.azide.core.cell

import dev.azide.core.Cell
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.TestUtils
import dev.azide.core.test_utils.cell.CellTestUtils
import dev.azide.core.test_utils.cell.TestInputCell
import kotlin.test.Test

@Suppress("ClassName")
class Cell_switch_tests {
    @Test
    fun test_passiveSampling() {
        val innerSourceCell = TestInputCell(
            initialValue = 10,
        )

        val outerSourceCell = TestInputCell(
            initialValue = innerSourceCell,
        )

        val subjectCell = Cell.switch(outerSourceCell)

        CellTestUtils.verifyAtRest(
            subjectCell = subjectCell,
            expectedValue = 10,
        )
    }

    @Test
    fun test_passiveSampling_outerConst() {
        val innerSourceCell = TestInputCell(
            initialValue = 10,
        )

        val outerSourceCell = Cell.Const(
            constValue = innerSourceCell,
        )

        val subjectCell = Cell.switch(outerSourceCell)

        CellTestUtils.verifyAtRest(
            subjectCell = subjectCell,
            expectedValue = 10,
        )
    }

    @Test
    fun test_passiveSampling_outerAndInnerConst() {
        val innerSourceCell = Cell.Const(
            constValue = 10,
        )

        val outerSourceCell = Cell.Const(
            constValue = innerSourceCell,
        )

        val subjectCell = Cell.switch(outerSourceCell)

        CellTestUtils.verifyAtRest(
            subjectCell = subjectCell,
            expectedValue = 10,
        )
    }

    @Test
    fun test_passiveSampling_innerConst() {
        val innerSourceCell = Cell.Const(
            constValue = 10,
        )

        val outerSourceCell = TestInputCell(
            initialValue = innerSourceCell,
        )

        val subjectCell = Cell.switch(outerSourceCell)

        CellTestUtils.verifyAtRest(
            subjectCell = subjectCell,
            expectedValue = 10,
        )
    }

    @Test
    fun test_onlyCurrentInnerUpdates_outerConst() {
        val innerSourceCell = TestInputCell(
            initialValue = 10,
        )

        val outerSourceCell = Cell.Const(
            constValue = innerSourceCell,
        )

        val subjectCell = Cell.switch(outerSourceCell)

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = innerSourceCell.update(
                newValue = 11,
            ),
            expectedOldValue = 10,
            expectedNewValue = 11,
        )
    }

    @Test
    fun test_onlyCurrentInnerUpdates_initial() {
        val innerSourceCell = TestInputCell(
            initialValue = 10,
        )

        val outerSourceCell = TestInputCell(
            initialValue = innerSourceCell,
        )

        val subjectCell = Cell.switch(outerSourceCell)

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = innerSourceCell.update(
                newValue = 11,
            ),
            expectedOldValue = 10,
            expectedNewValue = 11,
        )
    }

    @Test
    fun test_onlyCurrentInnerUpdates_initial_revoked() {
        val innerSourceCell = TestInputCell(
            initialValue = 10,
        )

        val outerSourceCell = TestInputCell(
            initialValue = innerSourceCell,
        )

        val subjectCell = Cell.switch(outerSourceCell)

        CellTestUtils.verifyDoesNotUpdateEffectively(
            subjectCell = subjectCell,
            inputStimulation = TestStimulation.combineInProvidedOrder(
                innerSourceCell.update(
                    newValue = 11,
                ),
                innerSourceCell.revokeUpdate(),
            ),
            expectedUnaffectedValue = 10,
        )
    }

    @Test
    fun test_onlyCurrentInnerUpdates_initial_corrected() {
        val innerSourceCell = TestInputCell(
            initialValue = 10,
        )

        val outerSourceCell = TestInputCell(
            initialValue = innerSourceCell,
        )

        val subjectCell = Cell.switch(outerSourceCell)

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = TestStimulation.combineInProvidedOrder(
                innerSourceCell.update(
                    newValue = 11,
                ),
                innerSourceCell.correctUpdate(
                    correctedNewValue = 12,
                ),
            ),
            expectedOldValue = 10,
            expectedNewValue = 12,
        )
    }

    @Test
    fun test_onlyCurrentInnerUpdates_toSameValue() {
        val innerSourceCell = TestInputCell(
            initialValue = 10,
        )

        val outerSourceCell = TestInputCell(
            initialValue = innerSourceCell,
        )

        val subjectCell = Cell.switch(outerSourceCell)

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = innerSourceCell.update(
                newValue = 10,
            ),
            expectedOldValue = 10,
            expectedNewValue = 10,
        )
    }

    @Test
    fun test_onlyCurrentInnerUpdates_subsequent() {
        val earlierInnerSourceCell = TestInputCell(
            initialValue = 10,
        )

        val laterInnerSourceCell = TestInputCell(
            initialValue = 20,
        )

        val outerSourceCell = TestInputCell(
            initialValue = earlierInnerSourceCell,
        )

        val subjectCell = Cell.switch(outerSourceCell)

        val observingVerifier = CellTestUtils.observeForVerification(
            subjectCell = subjectCell,
        )

        TestUtils.stimulateSeparately(
            outerSourceCell.update(
                newValue = laterInnerSourceCell,
            ),
        )

        observingVerifier.verifyUpdatesAsExpected(
            inputStimulation = laterInnerSourceCell.update(
                newValue = 21,
            ),
            expectedOldValue = 20,
            expectedNewValue = 21,
        )
    }

    @Test
    fun test_onlyCurrentInnerUpdates_subsequent_revoked() {
        val earlierInnerSourceCell = TestInputCell(
            initialValue = 10,
        )

        val laterInnerSourceCell = TestInputCell(
            initialValue = 20,
        )

        val outerSourceCell = TestInputCell(
            initialValue = earlierInnerSourceCell,
        )

        val subjectCell = Cell.switch(outerSourceCell)

        val observingVerifier = CellTestUtils.observeForVerification(
            subjectCell = subjectCell,
        )

        TestUtils.stimulateSeparately(
            outerSourceCell.update(
                newValue = laterInnerSourceCell,
            ),
        )

        observingVerifier.verifyDoesNotUpdateEffectively(
            inputStimulation = TestStimulation.combineInProvidedOrder(
                laterInnerSourceCell.update(
                    newValue = 21,
                ),
                laterInnerSourceCell.revokeUpdate(),
            ),
            expectedUnaffectedValue = 20,
        )
    }

    @Test
    fun test_onlyCurrentInnerUpdates_subsequent_corrected() {
        val earlierInnerSourceCell = TestInputCell(
            initialValue = 10,
        )

        val laterInnerSourceCell = TestInputCell(
            initialValue = 20,
        )

        val outerSourceCell = TestInputCell(
            initialValue = earlierInnerSourceCell,
        )

        val subjectCell = Cell.switch(outerSourceCell)

        val observingVerifier = CellTestUtils.observeForVerification(
            subjectCell = subjectCell,
        )

        TestUtils.stimulateSeparately(
            outerSourceCell.update(
                newValue = laterInnerSourceCell,
            ),
        )

        observingVerifier.verifyUpdatesAsExpected(
            inputStimulation = TestStimulation.combineInProvidedOrder(
                laterInnerSourceCell.update(
                    newValue = 21,
                ),
                laterInnerSourceCell.correctUpdate(
                    correctedNewValue = 22,
                ),
            ),
            expectedOldValue = 20,
            expectedNewValue = 22,
        )
    }

    @Test
    fun test_onlyPreviousInnerUpdates() {
        val earlierInnerSourceCell = TestInputCell(
            initialValue = 10,
        )

        val laterInnerSourceCell = TestInputCell(
            initialValue = 20,
        )

        val outerSourceCell = TestInputCell(
            initialValue = earlierInnerSourceCell,
        )

        val subjectCell = Cell.switch(outerSourceCell)

        val observingVerifier = CellTestUtils.observeForVerification(
            subjectCell = subjectCell,
        )

        TestUtils.stimulateSeparately(
            outerSourceCell.update(
                newValue = laterInnerSourceCell,
            ),
        )

        observingVerifier.verifyDoesNotUpdateAtAll(
            inputStimulation = earlierInnerSourceCell.update(
                newValue = 11,
            ),
            expectedUnaffectedValue = 20,
        )
    }

    @Test
    fun test_onlyOuterUpdates() {
        val earlierInnerSourceCell = TestInputCell(
            initialValue = 10,
        )

        val laterInnerSourceCell = TestInputCell(
            initialValue = 20,
        )

        val outerSourceCell = TestInputCell(
            initialValue = earlierInnerSourceCell,
        )

        val subjectCell = Cell.switch(outerSourceCell)

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = outerSourceCell.update(
                newValue = laterInnerSourceCell,
            ),
            expectedOldValue = 10,
            expectedNewValue = 20,
        )
    }

    @Test
    fun test_onlyOuterUpdates_toSameCell() {
        val singleInnerSourceCell = TestInputCell(
            initialValue = 10,
        )

        val outerSourceCell = TestInputCell(
            initialValue = singleInnerSourceCell,
        )

        val subjectCell = Cell.switch(outerSourceCell)

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = outerSourceCell.update(
                newValue = singleInnerSourceCell,
            ),
            expectedOldValue = 10,
            expectedNewValue = 10,
        )
    }

    @Test
    fun test_onlyOuterUpdates_updatedInnerConst() {
        val earlierInnerSourceCell = TestInputCell(
            initialValue = 10,
        )

        val laterInnerSourceCell = Cell.Const(
            constValue = 20,
        )

        val outerSourceCell = TestInputCell<Cell<Int>>(
            initialValue = earlierInnerSourceCell,
        )

        val subjectCell = Cell.switch(outerSourceCell)

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = outerSourceCell.update(
                newValue = laterInnerSourceCell,
            ),
            expectedOldValue = 10,
            expectedNewValue = 20,
        )
    }

    @Test
    fun test_onlyOuterUpdates_revoked() {
        val earlierInnerSourceCell = TestInputCell(
            initialValue = 10,
        )

        val laterInnerSourceCell = TestInputCell(
            initialValue = 20,
        )

        val outerSourceCell = TestInputCell(
            initialValue = earlierInnerSourceCell,
        )

        val subjectCell = Cell.switch(outerSourceCell)

        CellTestUtils.verifyDoesNotUpdateEffectively(
            subjectCell = subjectCell,
            inputStimulation = TestStimulation.combineInProvidedOrder(
                outerSourceCell.update(
                    newValue = laterInnerSourceCell,
                ),
                outerSourceCell.revokeUpdate(),
            ),
            expectedUnaffectedValue = 10,
        )

        CellTestUtils.verifyDoesNotUpdateAtAll(
            subjectCell = subjectCell,
            inputStimulation = laterInnerSourceCell.update(
                newValue = 21,
            ),
            expectedUnaffectedValue = 10,
        )
    }

    @Test
    fun test_onlyOuterUpdates_corrected() {
        val earlierInnerSourceCell = TestInputCell(
            initialValue = 10,
        )

        val intermediateInnerSourceCell = TestInputCell(
            initialValue = 20,
        )

        val laterInnerSourceCell = TestInputCell(
            initialValue = 30,
        )

        val outerSourceCell = TestInputCell(
            initialValue = earlierInnerSourceCell,
        )

        val subjectCell = Cell.switch(outerSourceCell)

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = TestStimulation.combineInProvidedOrder(
                outerSourceCell.update(
                    newValue = intermediateInnerSourceCell,
                ),
                outerSourceCell.correctUpdate(
                    correctedNewValue = laterInnerSourceCell,
                ),
            ),
            expectedOldValue = 10,
            expectedNewValue = 30,
        )

        CellTestUtils.verifyDoesNotUpdateAtAll(
            subjectCell = subjectCell,
            inputStimulation = intermediateInnerSourceCell.update(
                newValue = 21,
            ),
            expectedUnaffectedValue = 30,
        )
    }

    @Test
    fun test_outerAndNewInnerUpdate_outerFirst() {
        val earlierInnerSourceCell = TestInputCell(
            initialValue = 10,
        )

        val laterInnerSourceCell = TestInputCell(
            initialValue = 20,
        )

        val outerSourceCell = TestInputCell(
            initialValue = earlierInnerSourceCell,
        )

        val subjectCell = Cell.switch(outerSourceCell)

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = TestStimulation.combineInProvidedOrder(
                outerSourceCell.update(
                    newValue = laterInnerSourceCell,
                ),
                laterInnerSourceCell.update(
                    newValue = 21,
                ),
            ),
            expectedOldValue = 10,
            expectedNewValue = 21,
        )
    }

    @Test
    fun test_outerAndNewInnerUpdate_innerFirst() {
        val earlierInnerSourceCell = TestInputCell(
            initialValue = 10,
        )

        val laterInnerSourceCell = TestInputCell(
            initialValue = 20,
        )

        val outerSourceCell = TestInputCell(
            initialValue = earlierInnerSourceCell,
        )

        val subjectCell = Cell.switch(outerSourceCell)

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = TestStimulation.combineInProvidedOrder(
                laterInnerSourceCell.update(
                    newValue = 21,
                ),
                outerSourceCell.update(
                    newValue = laterInnerSourceCell,
                ),
            ),
            expectedOldValue = 10,
            expectedNewValue = 21,
        )
    }

    @Test
    fun test_outerAndInnerUpdate_toSameCell() {
        val singleInnerSourceCell = TestInputCell(
            initialValue = 10,
        )

        val outerSourceCell = TestInputCell(
            initialValue = singleInnerSourceCell,
        )

        val subjectCell = Cell.switch(outerSourceCell)

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = TestStimulation.combineInArbitraryOrder(
                setOf(
                    outerSourceCell.update(
                        newValue = singleInnerSourceCell,
                    ),
                    singleInnerSourceCell.update(
                        newValue = 11,
                    ),
                ),
            ),
            expectedOldValue = 10,
            expectedNewValue = 11,
        )
    }

    @Test
    fun test_outerAndNewInnerUpdate_newInnerUpdateRevoked() {
        val earlierInnerSourceCell = TestInputCell(
            initialValue = 10,
        )

        val laterInnerSourceCell = TestInputCell(
            initialValue = 20,
        )

        val outerSourceCell = TestInputCell(
            initialValue = earlierInnerSourceCell,
        )

        val subjectCell = Cell.switch(outerSourceCell)

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = TestStimulation.combineInProvidedOrder(
                outerSourceCell.update(
                    newValue = laterInnerSourceCell,
                ),
                laterInnerSourceCell.update(
                    newValue = 21,
                ),
                // Revoke the update of the outer cell update, to verify that the vertex falls back to the stable value
                // of the new inner cell
                laterInnerSourceCell.revokeUpdate(),
            ),
            expectedOldValue = 10,
            expectedNewValue = 20,
        )
    }

    @Test
    fun test_outerAndNewInnerUpdate_outerUpdateRevoked() {
        val earlierInnerSourceCell = TestInputCell(
            initialValue = 10,
        )

        val laterInnerSourceCell = TestInputCell(
            initialValue = 20,
        )

        val outerSourceCell = TestInputCell(
            initialValue = earlierInnerSourceCell,
        )

        val subjectCell = Cell.switch(outerSourceCell)

        CellTestUtils.verifyDoesNotUpdateEffectively(
            subjectCell = subjectCell,
            inputStimulation = TestStimulation.combineInProvidedOrder(
                outerSourceCell.update(
                    newValue = laterInnerSourceCell,
                ),
                laterInnerSourceCell.update(
                    newValue = 21,
                ),
                // Revoke the outer update after the new inner update, to verify that the vertex ignores the new value
                // of the new (revoked) inner cell
                outerSourceCell.revokeUpdate(),
            ),
            expectedUnaffectedValue = 10,
        )

        CellTestUtils.verifyDoesNotUpdateAtAll(
            subjectCell = subjectCell,
            inputStimulation = laterInnerSourceCell.update(
                newValue = 22,
            ),
            expectedUnaffectedValue = 10,
        )
    }

    @Test
    fun test_outerAndOldInnerUpdate() {
        val earlierInnerSourceCell = TestInputCell(
            initialValue = 10,
        )

        val laterInnerSourceCell = TestInputCell(
            initialValue = 20,
        )

        val outerSourceCell = TestInputCell(
            initialValue = earlierInnerSourceCell,
        )

        val subjectCell = Cell.switch(outerSourceCell)

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = TestStimulation.combineInProvidedOrder(
                outerSourceCell.update(
                    newValue = laterInnerSourceCell,
                ),
                earlierInnerSourceCell.update(
                    newValue = 11,
                ),
            ),
            expectedOldValue = 10,
            expectedNewValue = 20,
        )
    }

    @Test
    fun test_outerAndOldInnerUpdate_outerUpdateRevoked() {
        val earlierInnerSourceCell = TestInputCell(
            initialValue = 10,
        )

        val laterInnerSourceCell = TestInputCell(
            initialValue = 20,
        )

        val outerSourceCell = TestInputCell(
            initialValue = earlierInnerSourceCell,
        )

        val subjectCell = Cell.switch(outerSourceCell)

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = TestStimulation.combineInProvidedOrder(
                outerSourceCell.update(
                    newValue = laterInnerSourceCell,
                ),
                earlierInnerSourceCell.update(
                    newValue = 11,
                ),
                // Revoke the outer update after the old inner update, to verify that the vertex falls back to the
                // up-to-date value of the stable inner cell
                outerSourceCell.revokeUpdate(),
            ),
            expectedOldValue = 10,
            expectedNewValue = 11,
        )
    }
}
