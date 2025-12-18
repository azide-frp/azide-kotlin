package dev.azide.core.cell

import dev.azide.core.Cell
import dev.azide.core.test_utils.TestInputStimulation
import dev.azide.core.test_utils.TestUtils
import dev.azide.core.test_utils.cell.CellTestUtils
import kotlin.test.Test

@Suppress("ClassName")
class Cell_switch_tests {
    @Test
    fun test_passiveSample() {
        val innerSourceCell = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val outerSourceCell = CellTestUtils.createInputCell(
            initialValue = innerSourceCell,
        )

        val subjectCell = Cell.switch(outerSourceCell)

        CellTestUtils.verifyAtRest(
            subjectCell = subjectCell,
            expectedValue = 10,
        )
    }

    @Test
    fun test_passiveSample_outerConst() {
        val innerSourceCell = CellTestUtils.createInputCell(
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
    fun test_passiveSample_outerAndInnerConst() {
        val innerSourceCell = Cell.Const(
            constValue = 10,
        )

        val outerSourceCell = Cell.Const(
            constValue = innerSourceCell,
        )

        val subjectCell = Cell.switch(outerSourceCell)

        CellTestUtils.verifyFrozen(
            subjectCell = subjectCell,
            expectedFrozenValue = 10,
        )
    }

    @Test
    fun test_passiveSample_innerConst() {
        val innerSourceCell = Cell.Const(
            constValue = 10,
        )

        val outerSourceCell = CellTestUtils.createInputCell(
            initialValue = innerSourceCell,
        )

        val subjectCell = Cell.switch(outerSourceCell)

        CellTestUtils.verifyAtRest(
            subjectCell = subjectCell,
            expectedValue = 10,
        )
    }

    @Test
    fun test_update_onlyCurrentInnerUpdates_outerConst() {
        val innerSourceCell = CellTestUtils.createInputCell(
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
    fun test_update_onlyCurrentInnerUpdates_initial() {
        val innerSourceCell = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val outerSourceCell = CellTestUtils.createInputCell(
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
    fun test_update_onlyCurrentInnerUpdates_initial_revoked() {
        val innerSourceCell = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val outerSourceCell = CellTestUtils.createInputCell(
            initialValue = innerSourceCell,
        )

        val subjectCell = Cell.switch(outerSourceCell)

        CellTestUtils.verifyDoesNotUpdateEffectively(
            subjectCell = subjectCell,
            inputStimulation = TestInputStimulation.combine(
                innerSourceCell.update(
                    newValue = 11,
                ),
                innerSourceCell.revokeUpdate(),
            ),
            expectedUnaffectedValue = 10,
        )
    }

    @Test
    fun test_update_onlyCurrentInnerUpdates_initial_corrected() {
        val innerSourceCell = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val outerSourceCell = CellTestUtils.createInputCell(
            initialValue = innerSourceCell,
        )

        val subjectCell = Cell.switch(outerSourceCell)

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = TestInputStimulation.combine(
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
    fun test_update_onlyCurrentInnerUpdates_subsequent() {
        val earlierInnerSourceCell = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val laterInnerSourceCell = CellTestUtils.createInputCell(
            initialValue = 20,
        )

        val outerSourceCell = CellTestUtils.createInputCell(
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
    fun test_update_onlyCurrentInnerUpdates_subsequent_revoked() {
        val earlierInnerSourceCell = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val laterInnerSourceCell = CellTestUtils.createInputCell(
            initialValue = 20,
        )

        val outerSourceCell = CellTestUtils.createInputCell(
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
            inputStimulation = TestInputStimulation.combine(
                laterInnerSourceCell.update(
                    newValue = 21,
                ),
                laterInnerSourceCell.revokeUpdate(),
            ),
            expectedUnaffectedValue = 20,
        )
    }

    @Test
    fun test_update_onlyCurrentInnerUpdates_subsequent_corrected() {
        val earlierInnerSourceCell = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val laterInnerSourceCell = CellTestUtils.createInputCell(
            initialValue = 20,
        )

        val outerSourceCell = CellTestUtils.createInputCell(
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
            inputStimulation = TestInputStimulation.combine(
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
    fun test_update_onlyPreviousInnerUpdates() {
        val earlierInnerSourceCell = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val laterInnerSourceCell = CellTestUtils.createInputCell(
            initialValue = 20,
        )

        val outerSourceCell = CellTestUtils.createInputCell(
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
    fun test_update_onlyOuterUpdates() {
        val earlierInnerSourceCell = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val laterInnerSourceCell = CellTestUtils.createInputCell(
            initialValue = 20,
        )

        val outerSourceCell = CellTestUtils.createInputCell(
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
    fun test_update_onlyOuterUpdates_updatedInnerConst() {
        val earlierInnerSourceCell = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val laterInnerSourceCell = Cell.Const(
            constValue = 20,
        )

        val outerSourceCell = CellTestUtils.createInputCell<Cell<Int>>(
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
    fun test_update_onlyOuterUpdates_revoked() {
        val earlierInnerSourceCell = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val laterInnerSourceCell = CellTestUtils.createInputCell(
            initialValue = 20,
        )

        val outerSourceCell = CellTestUtils.createInputCell(
            initialValue = earlierInnerSourceCell,
        )

        val subjectCell = Cell.switch(outerSourceCell)

        CellTestUtils.verifyDoesNotUpdateEffectively(
            subjectCell = subjectCell,
            inputStimulation = TestInputStimulation.combine(
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
    fun test_update_onlyOuterUpdates_corrected() {
        val earlierInnerSourceCell = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val intermediateInnerSourceCell = CellTestUtils.createInputCell(
            initialValue = 20,
        )

        val laterInnerSourceCell = CellTestUtils.createInputCell(
            initialValue = 30,
        )

        val outerSourceCell = CellTestUtils.createInputCell(
            initialValue = earlierInnerSourceCell,
        )

        val subjectCell = Cell.switch(outerSourceCell)

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = TestInputStimulation.combine(
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
    fun test_update_outerAndNewInnerUpdate_outerFirst() {
        val earlierInnerSourceCell = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val laterInnerSourceCell = CellTestUtils.createInputCell(
            initialValue = 20,
        )

        val outerSourceCell = CellTestUtils.createInputCell(
            initialValue = earlierInnerSourceCell,
        )

        val subjectCell = Cell.switch(outerSourceCell)

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = TestInputStimulation.combine(
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
    fun test_update_outerAndNewInnerUpdate_innerFirst() {
        val earlierInnerSourceCell = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val laterInnerSourceCell = CellTestUtils.createInputCell(
            initialValue = 20,
        )

        val outerSourceCell = CellTestUtils.createInputCell(
            initialValue = earlierInnerSourceCell,
        )

        val subjectCell = Cell.switch(outerSourceCell)

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = TestInputStimulation.combine(
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
    fun test_update_outerAndNewInnerUpdate_newInnerUpdateRevoked() {
        val earlierInnerSourceCell = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val laterInnerSourceCell = CellTestUtils.createInputCell(
            initialValue = 20,
        )

        val outerSourceCell = CellTestUtils.createInputCell(
            initialValue = earlierInnerSourceCell,
        )

        val subjectCell = Cell.switch(outerSourceCell)

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = TestInputStimulation.combine(
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
    fun test_update_outerAndNewInnerUpdate_outerUpdateRevoked() {
        val earlierInnerSourceCell = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val laterInnerSourceCell = CellTestUtils.createInputCell(
            initialValue = 20,
        )

        val outerSourceCell = CellTestUtils.createInputCell(
            initialValue = earlierInnerSourceCell,
        )

        val subjectCell = Cell.switch(outerSourceCell)

        CellTestUtils.verifyDoesNotUpdateEffectively(
            subjectCell = subjectCell,
            inputStimulation = TestInputStimulation.combine(
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
    fun test_update_outerAndOldInnerUpdate() {
        val earlierInnerSourceCell = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val laterInnerSourceCell = CellTestUtils.createInputCell(
            initialValue = 20,
        )

        val outerSourceCell = CellTestUtils.createInputCell(
            initialValue = earlierInnerSourceCell,
        )

        val subjectCell = Cell.switch(outerSourceCell)

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = TestInputStimulation.combine(
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
    fun test_update_outerAndOldInnerUpdate_outerUpdateRevoked() {
        val earlierInnerSourceCell = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val laterInnerSourceCell = CellTestUtils.createInputCell(
            initialValue = 20,
        )

        val outerSourceCell = CellTestUtils.createInputCell(
            initialValue = earlierInnerSourceCell,
        )

        val subjectCell = Cell.switch(outerSourceCell)

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = TestInputStimulation.combine(
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
