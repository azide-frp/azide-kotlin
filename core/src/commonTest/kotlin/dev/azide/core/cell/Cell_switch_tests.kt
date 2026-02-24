package dev.azide.core.cell

import dev.azide.core.Cell
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.cell.Cell_expectations_testUtils
import dev.azide.core.test_utils.cell.Cell_sampling_testUtils
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.generic.TestSubjectHealthCheckStrategy
import dev.azide.core.test_utils.generic.generic_reaction_testUtils
import kotlin.test.Ignore
import kotlin.test.Test

@Suppress("ClassName")
class Cell_switch_tests {

    // region Passive sampling

    @Test
    fun test_passiveSampling() {
        val innerCell = TestInputCell(initialValue = 10)
        val outerCell = TestInputCell(initialValue = innerCell)

        val subjectCell = Cell.switch(outerCell)

        Cell_sampling_testUtils.testPassiveSampling(
            subjectCell = subjectCell,
            expectedSubjectValue = Cell_expectations_testUtils.expectStableValue(
                expectedValue = 10,
            ),
        )
    }

    @Test
    fun test_passiveSampling_outerConst() {
        val innerCell = TestInputCell(initialValue = 10)
        val outerCell = Cell.Const(constValue = innerCell)

        val subjectCell = Cell.switch(outerCell)

        Cell_sampling_testUtils.testPassiveSampling(
            subjectCell = subjectCell,
            expectedSubjectValue = Cell_expectations_testUtils.expectStableValue(
                expectedValue = 10,
            ),
        )
    }

    @Test
    fun test_passiveSampling_outerAndInnerConst() {
        val innerCell = Cell.Const(constValue = 10)
        val outerCell = Cell.Const(constValue = innerCell)

        val subjectCell = Cell.switch(outerCell)

        Cell_sampling_testUtils.testPassiveSampling(
            subjectCell = subjectCell,
            expectedSubjectValue = Cell_expectations_testUtils.expectStableValue(
                expectedValue = 10,
            ),
        )
    }

    @Test
    fun test_passiveSampling_innerConst() {
        val innerCell = Cell.Const(constValue = 10)
        val outerCell = TestInputCell(initialValue = innerCell)

        val subjectCell = Cell.switch(outerCell)

        Cell_sampling_testUtils.testPassiveSampling(
            subjectCell = subjectCell,
            expectedSubjectValue = Cell_expectations_testUtils.expectStableValue(
                expectedValue = 10,
            ),
        )
    }

    // endregion

    // region Current inner cell updates

    @Test
    fun test_currentInnerUpdates_deactivated() {
        test_currentInnerUpdates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_currentInnerUpdates_keptAlive() {
        test_currentInnerUpdates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The current inner cell updates while the outer cell remains unchanged.
     */
    private fun test_currentInnerUpdates(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val innerCell = TestInputCell(initialValue = 10)
        val outerCell = TestInputCell(initialValue = innerCell)

        val subjectCell = Cell.switch(outerCell)

        Cell_switch_testUtils.testReaction(
            outerCell = outerCell,
            newInnerCell = innerCell,
            subjectCell = subjectCell,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = innerCell.update(newValue = 20),
            ),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                expectedOldValue = 10,
                expectedNewValue = 20,
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    // endregion

    // region Current inner cell updates to the same value

    @Test
    fun test_currentInnerUpdatesToSameValue_deactivated() {
        test_currentInnerUpdatesToSameValue(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_currentInnerUpdatesToSameValue_keptAlive() {
        test_currentInnerUpdatesToSameValue(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The current inner cell updates to the same value it already holds (X → X). Cells do not rely on [equals] to
     * suppress redundant updates, so the switched cell still observes a real update (X → X).
     */
    private fun test_currentInnerUpdatesToSameValue(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val innerCell = TestInputCell(initialValue = 10)
        val outerCell = TestInputCell(initialValue = innerCell)

        val subjectCell = Cell.switch(outerCell)

        Cell_switch_testUtils.testReaction(
            outerCell = outerCell,
            newInnerCell = innerCell,
            subjectCell = subjectCell,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = innerCell.update(newValue = 10),
            ),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldValue = 10,
                expectedNewValue = 10,
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    // endregion

    // region Current inner cell update revoked

    @Test
    fun test_currentInnerUpdate_revoked_deactivated() {
        test_currentInnerUpdate_revoked(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_currentInnerUpdate_revoked_keptAlive() {
        test_currentInnerUpdate_revoked(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The current inner cell is updated and then the update is revoked, so the switched cell should reflect no net
     * change.
     */
    private fun test_currentInnerUpdate_revoked(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val innerCell = TestInputCell(initialValue = 10)
        val outerCell = TestInputCell(initialValue = innerCell)

        val subjectCell = Cell.switch(outerCell)

        Cell_switch_testUtils.testReaction(
            outerCell = outerCell,
            newInnerCell = innerCell,
            subjectCell = subjectCell,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    innerCell.update(newValue = 20),
                    innerCell.revokeUpdate(),
                ),
            ),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectNoValueTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedValue = 10,
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    // endregion

    // region Current inner cell update corrected

    @Test
    fun test_currentInnerUpdate_corrected_deactivated() {
        test_currentInnerUpdate_corrected(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_currentInnerUpdate_corrected_keptAlive() {
        test_currentInnerUpdate_corrected(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The current inner cell is updated and then the update is corrected to a different value. The switched cell
     * should reflect the corrected (final) value.
     */
    private fun test_currentInnerUpdate_corrected(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val innerCell = TestInputCell(initialValue = 10)
        val outerCell = TestInputCell(initialValue = innerCell)

        val subjectCell = Cell.switch(outerCell)

        Cell_switch_testUtils.testReaction(
            outerCell = outerCell,
            newInnerCell = innerCell,
            subjectCell = subjectCell,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    innerCell.update(newValue = 20),
                    innerCell.correctUpdate(correctedNewValue = 30),
                ),
            ),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldValue = 10,
                expectedNewValue = 30,
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    // endregion

    // region Outer cell updates to new inner cell

    @Test
    fun test_outerUpdatesToNewInner_deactivated() {
        test_outerUpdatesToNewInner(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_outerUpdatesToNewInner_keptAlive() {
        test_outerUpdatesToNewInner(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The outer cell updates to point to a different inner cell with a different value. The switched cell should
     * reflect the new inner cell's value.
     */
    private fun test_outerUpdatesToNewInner(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val innerCell1 = TestInputCell(initialValue = 10)
        val innerCell2 = TestInputCell(initialValue = 20)
        val outerCell = TestInputCell(initialValue = innerCell1)

        val subjectCell = Cell.switch(outerCell)

        Cell_switch_testUtils.testReaction(
            outerCell = outerCell,
            newInnerCell = innerCell2,
            subjectCell = subjectCell,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = outerCell.update(newValue = innerCell2),
            ),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                expectedOldValue = 10,
                expectedNewValue = 20,
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    // endregion

    // region Outer cell updates to the same inner cell (const)

    @Test
    fun test_outerUpdatesToSameInner_outerConst_deactivated() {
        test_outerUpdatesToSameInner_outerConst(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_outerUpdatesToSameInner_outerConst_keptAlive() {
        test_outerUpdatesToSameInner_outerConst(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The outer cell updates to point to the same inner cell it already references. Since the inner cell is constant,
     * there is no effective value change. Cells do not rely on [equals] to suppress redundant updates, so the switched
     * cell still observes a real update (X → X).
     */
    private fun test_outerUpdatesToSameInner_outerConst(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val singleInnerCell = TestInputCell(initialValue = 10)
        val outerCell = TestInputCell(initialValue = singleInnerCell)

        val subjectCell = Cell.switch(outerCell)

        Cell_switch_testUtils.testReaction(
            outerCell = outerCell,
            newInnerCell = singleInnerCell,
            subjectCell = subjectCell,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = outerCell.update(newValue = singleInnerCell),
            ),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldValue = 10,
                expectedNewValue = 10,
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    // endregion

    // region Outer cell update revoked

    @Test
    fun test_outerUpdate_revoked_deactivated() {
        test_outerUpdate_revoked(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_outerUpdate_revoked_keptAlive() {
        test_outerUpdate_revoked(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The outer cell is updated to point to a new inner cell and then the update is revoked. The switched cell should
     * fall back to tracking the original inner cell.
     */
    private fun test_outerUpdate_revoked(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val innerCell1 = TestInputCell(initialValue = 10)
        val innerCell2 = TestInputCell(initialValue = 20)
        val outerCell = TestInputCell(initialValue = innerCell1)

        val subjectCell = Cell.switch(outerCell)

        Cell_switch_testUtils.testReaction(
            outerCell = outerCell,
            newInnerCell = innerCell1,
            subjectCell = subjectCell,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    outerCell.update(newValue = innerCell2),
                    outerCell.revokeUpdate(),
                ),
            ),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectNoValueTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedValue = 10,
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    // endregion

    // region Outer cell update corrected to different inner cell

    @Test
    fun test_outerUpdate_correctedToDifferentInner_deactivated() {
        test_outerUpdate_correctedToDifferentInner(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_outerUpdate_correctedToDifferentInner_keptAlive() {
        test_outerUpdate_correctedToDifferentInner(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The outer cell is updated to point to one new inner cell and then the update is corrected to point to a
     * different inner cell. The switched cell should reflect the corrected (final) inner cell's value.
     */
    private fun test_outerUpdate_correctedToDifferentInner(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val innerCell1 = TestInputCell(initialValue = 10)
        val innerCell2 = TestInputCell(initialValue = 20)
        val innerCell3 = TestInputCell(initialValue = 30)
        val outerCell = TestInputCell(initialValue = innerCell1)

        val subjectCell = Cell.switch(outerCell)

        Cell_switch_testUtils.testReaction(
            outerCell = outerCell,
            newInnerCell = innerCell3,
            subjectCell = subjectCell,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    outerCell.update(newValue = innerCell2),
                    outerCell.correctUpdate(correctedNewValue = innerCell3),
                ),
            ),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldValue = 10,
                expectedNewValue = 30,
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    // endregion

    // region Offline activation

    @Test
    @Ignore // FIXME: Fix offline activation
    fun test_offlineActivation() {
        val innerCell = TestInputCell(initialValue = 10)
        val outerCell = TestInputCell(initialValue = innerCell)

        val subjectCell = Cell.switch(outerCell)

        Cell_switch_testUtils.testOfflineActivation(
            outerCell = outerCell,
            newInnerCell = innerCell,
            subjectCell = subjectCell,
        )
    }

    // endregion
}
