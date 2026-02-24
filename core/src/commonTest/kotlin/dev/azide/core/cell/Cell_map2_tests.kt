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
class Cell_map2_tests {

    // region Passive sampling

    @Test
    fun test_passiveSampling() {
        val inputCell1 = TestInputCell(initialValue = 10)
        val inputCell2 = TestInputCell(initialValue = 'A')

        val subjectCell = Cell.map2(
            cell1 = inputCell1,
            cell2 = inputCell2,
        ) { v1, v2 -> "$v1:$v2" }

        Cell_sampling_testUtils.testPassiveSampling(
            subjectCell = subjectCell,
            expectedSubjectValue = Cell_expectations_testUtils.expectStableValue(
                expectedValue = "10:A",
            ),
        )
    }

    // endregion

    // region Source cell 1 updates

    @Test
    fun test_sourceCell1Updates_deactivated() {
        test_sourceCell1Updates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_sourceCell1Updates_keptAlive() {
        test_sourceCell1Updates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * Source cell 1 updates while source cell 2 remains unchanged.
     */
    private fun test_sourceCell1Updates(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val inputCell1 = TestInputCell(initialValue = 10)
        val inputCell2 = TestInputCell(initialValue = 'A')

        val subjectCell = Cell.map2(
            cell1 = inputCell1,
            cell2 = inputCell2,
        ) { v1, v2 -> "$v1:$v2" }

        Cell_map2_testUtils.testReaction(
            inputCell1 = inputCell1,
            inputCell2 = inputCell2,
            subjectCell = subjectCell,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = inputCell1.update(newValue = 20),
            ),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                expectedOldValue = "10:A",
                expectedNewValue = "20:A",
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    // endregion

    // region Source cell 2 updates

    @Test
    fun test_sourceCell2Updates_deactivated() {
        test_sourceCell2Updates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_sourceCell2Updates_keptAlive() {
        test_sourceCell2Updates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * Source cell 2 updates while source cell 1 remains unchanged.
     */
    private fun test_sourceCell2Updates(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val inputCell1 = TestInputCell(initialValue = 10)
        val inputCell2 = TestInputCell(initialValue = 'A')

        val subjectCell = Cell.map2(
            cell1 = inputCell1,
            cell2 = inputCell2,
        ) { v1, v2 -> "$v1:$v2" }

        Cell_map2_testUtils.testReaction(
            inputCell1 = inputCell1,
            inputCell2 = inputCell2,
            subjectCell = subjectCell,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = inputCell2.update(newValue = 'B'),
            ),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                expectedOldValue = "10:A",
                expectedNewValue = "10:B",
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    // endregion

    // region Both source cells update

    @Test
    fun test_bothSourceCellsUpdate_deactivated() {
        test_bothSourceCellsUpdate(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bothSourceCellsUpdate_keptAlive() {
        test_bothSourceCellsUpdate(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * Both source cells update in the same transaction.
     */
    private fun test_bothSourceCellsUpdate(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val inputCell1 = TestInputCell(initialValue = 10)
        val inputCell2 = TestInputCell(initialValue = 'A')

        val subjectCell = Cell.map2(
            cell1 = inputCell1,
            cell2 = inputCell2,
        ) { v1, v2 -> "$v1:$v2" }

        Cell_map2_testUtils.testReaction(
            inputCell1 = inputCell1,
            inputCell2 = inputCell2,
            subjectCell = subjectCell,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    inputCell1.update(newValue = 20),
                    inputCell2.update(newValue = 'B'),
                ),
            ),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                expectedOldValue = "10:A",
                expectedNewValue = "20:B",
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    // endregion

    // region Source cells update to the same value

    @Test
    fun test_sourceCellsUpdateToSameValue_deactivated() {
        test_sourceCellsUpdateToSameValue(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_sourceCellsUpdateToSameValue_keptAlive() {
        test_sourceCellsUpdateToSameValue(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * Source cells update to the same value they already hold (X → X, Y → Y). Cells do not rely on [equals] to
     * suppress redundant updates, so the mapped cell still observes a real update (Z → Z where Z = f(X, Y)).
     */
    private fun test_sourceCellsUpdateToSameValue(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val inputCell1 = TestInputCell(initialValue = 10)
        val inputCell2 = TestInputCell(initialValue = 'A')

        val subjectCell = Cell.map2(
            cell1 = inputCell1,
            cell2 = inputCell2,
        ) { v1, v2 -> "$v1:$v2" }

        Cell_map2_testUtils.testReaction(
            inputCell1 = inputCell1,
            inputCell2 = inputCell2,
            subjectCell = subjectCell,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    inputCell1.update(newValue = 10),
                    inputCell2.update(newValue = 'A'),
                ),
            ),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldValue = "10:A",
                expectedNewValue = "10:A",
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    // endregion

    // region Source cell updates revoked

    @Test
    fun test_sourceCell1Updates_revoked_deactivated() {
        test_sourceCell1Updates_revoked(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_sourceCell1Updates_revoked_keptAlive() {
        test_sourceCell1Updates_revoked(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * Source cell 1 is updated and then the update is revoked, so the mapped cell should reflect no net change.
     */
    private fun test_sourceCell1Updates_revoked(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val inputCell1 = TestInputCell(initialValue = 10)
        val inputCell2 = TestInputCell(initialValue = 'A')

        val subjectCell = Cell.map2(
            cell1 = inputCell1,
            cell2 = inputCell2,
        ) { v1, v2 -> "$v1:$v2" }

        Cell_map2_testUtils.testReaction(
            inputCell1 = inputCell1,
            inputCell2 = inputCell2,
            subjectCell = subjectCell,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    inputCell1.update(newValue = 20),
                    inputCell1.revokeUpdate(),
                ),
            ),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectNoValueTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedValue = "10:A",
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    // endregion

    // region Source cell updates corrected

    @Test
    fun test_sourceCell1Updates_corrected_deactivated() {
        test_sourceCell1Updates_corrected(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_sourceCell1Updates_corrected_keptAlive() {
        test_sourceCell1Updates_corrected(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * Source cell 1 is updated and then the update is corrected to a different value. The mapped cell should reflect
     * the corrected (final) value.
     */
    private fun test_sourceCell1Updates_corrected(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val inputCell1 = TestInputCell(initialValue = 10)
        val inputCell2 = TestInputCell(initialValue = 'A')

        val subjectCell = Cell.map2(
            cell1 = inputCell1,
            cell2 = inputCell2,
        ) { v1, v2 -> "$v1:$v2" }

        Cell_map2_testUtils.testReaction(
            inputCell1 = inputCell1,
            inputCell2 = inputCell2,
            subjectCell = subjectCell,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    inputCell1.update(newValue = 20),
                    inputCell1.correctUpdate(correctedNewValue = 30),
                ),
            ),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldValue = "10:A",
                expectedNewValue = "30:A",
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    // endregion

    // region Source cell updates corrected to the same (original) value

    @Test
    fun test_sourceCell1Updates_correctedToSameValue_deactivated() {
        test_sourceCell1Updates_correctedToSameValue(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_sourceCell1Updates_correctedToSameValue_keptAlive() {
        test_sourceCell1Updates_correctedToSameValue(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * Source cell 1 is updated and then the update is corrected back to the original value. Cells do not rely on
     * [equals] to suppress redundant updates, so the mapped cell still observes a real update
     * (Z → Z where Z = f(X, Y)).
     */
    private fun test_sourceCell1Updates_correctedToSameValue(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val inputCell1 = TestInputCell(initialValue = 10)
        val inputCell2 = TestInputCell(initialValue = 'A')

        val subjectCell = Cell.map2(
            cell1 = inputCell1,
            cell2 = inputCell2,
        ) { v1, v2 -> "$v1:$v2" }

        Cell_map2_testUtils.testReaction(
            inputCell1 = inputCell1,
            inputCell2 = inputCell2,
            subjectCell = subjectCell,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    inputCell1.update(newValue = 20),
                    inputCell1.correctUpdate(correctedNewValue = 10),
                ),
            ),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldValue = "10:A",
                expectedNewValue = "10:A",
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    // endregion

    // region Offline activation

    @Test
    @Ignore // FIXME: Fix offline activation
    fun test_offlineActivation() {
        val inputCell1 = TestInputCell(initialValue = 10)
        val inputCell2 = TestInputCell(initialValue = 'A')

        val subjectCell = Cell.map2(
            cell1 = inputCell1,
            cell2 = inputCell2,
        ) { v1, v2 -> "$v1:$v2" }

        Cell_map2_testUtils.testOfflineActivation(
            inputCell1 = inputCell1,
            inputCell2 = inputCell2,
            subjectCell = subjectCell,
        )
    }

    // endregion
}
