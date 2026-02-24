package dev.azide.core.cell

import dev.azide.core.map
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
class Cell_map_tests {

    // region Passive sampling

    @Test
    fun test_passiveSampling() {
        val inputCell = TestInputCell(initialValue = 10)

        val subjectCell = inputCell.map { it.toString() }

        Cell_sampling_testUtils.testPassiveSampling(
            subjectCell = subjectCell,
            expectedSubjectValue = Cell_expectations_testUtils.expectStableValue(
                expectedValue = "10",
            ),
        )
    }

    // endregion

    // region Source cell updates

    @Test
    fun test_sourceUpdates_deactivated() {
        test_sourceUpdates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_sourceUpdates_keptAlive() {
        test_sourceUpdates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The source cell updates.
     */
    private fun test_sourceUpdates(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val inputCell = TestInputCell(initialValue = 10)

        val subjectCell = inputCell.map { it.toString() }

        Cell_map_testUtils.testReaction(
            inputCell = inputCell,
            subjectCell = subjectCell,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = inputCell.update(newValue = 20),
            ),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                expectedOldValue = "10",
                expectedNewValue = "20",
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    // endregion

    // region Source cell updates to the same value

    @Test
    fun test_sourceUpdatesToSameValue_deactivated() {
        test_sourceUpdatesToSameValue(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_sourceUpdatesToSameValue_keptAlive() {
        test_sourceUpdatesToSameValue(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The source cell updates to the same value it already holds (X → X). Cells do not rely on [equals] to suppress
     * redundant updates, so the mapped cell still observes a real update (Y → Y where Y = f(X)).
     */
    private fun test_sourceUpdatesToSameValue(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val inputCell = TestInputCell(initialValue = 10)

        val subjectCell = inputCell.map { it.toString() }

        Cell_map_testUtils.testReaction(
            inputCell = inputCell,
            subjectCell = subjectCell,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = inputCell.update(newValue = 10),
            ),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldValue = "10",
                expectedNewValue = "10",
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    // endregion

    // region Source cell update revoked

    @Test
    fun test_sourceUpdates_revoked_deactivated() {
        test_sourceUpdates_revoked(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_sourceUpdates_revoked_keptAlive() {
        test_sourceUpdates_revoked(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The source cell is updated and then the update is revoked, so the mapped cell should reflect no net change.
     */
    private fun test_sourceUpdates_revoked(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val inputCell = TestInputCell(initialValue = 10)

        val subjectCell = inputCell.map { it.toString() }

        Cell_map_testUtils.testReaction(
            inputCell = inputCell,
            subjectCell = subjectCell,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    inputCell.update(newValue = 20),
                    inputCell.revokeUpdate(),
                ),
            ),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectNoValueTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedValue = "10",
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    // endregion

    // region Source cell update corrected

    @Test
    fun test_sourceUpdates_corrected_deactivated() {
        test_sourceUpdates_corrected(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_sourceUpdates_corrected_keptAlive() {
        test_sourceUpdates_corrected(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The source cell is updated and then the update is corrected to a different value. The mapped cell should reflect
     * the corrected (final) value.
     */
    private fun test_sourceUpdates_corrected(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val inputCell = TestInputCell(initialValue = 10)

        val subjectCell = inputCell.map { it.toString() }

        Cell_map_testUtils.testReaction(
            inputCell = inputCell,
            subjectCell = subjectCell,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    inputCell.update(newValue = 20),
                    inputCell.correctUpdate(correctedNewValue = 30),
                ),
            ),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldValue = "10",
                expectedNewValue = "30",
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    // endregion

    // region Source cell update corrected to the same (original) value

    @Test
    fun test_sourceUpdates_correctedToSameValue_deactivated() {
        test_sourceUpdates_correctedToSameValue(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_sourceUpdates_correctedToSameValue_keptAlive() {
        test_sourceUpdates_correctedToSameValue(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The source cell is updated and then the update is corrected back to the original value. Cells do not rely on
     * [equals] to suppress redundant updates, so the mapped cell still observes a real update (Y → Y where Y = f(X)).
     */
    private fun test_sourceUpdates_correctedToSameValue(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val inputCell = TestInputCell(initialValue = 10)

        val subjectCell = inputCell.map { it.toString() }

        Cell_map_testUtils.testReaction(
            inputCell = inputCell,
            subjectCell = subjectCell,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    inputCell.update(newValue = 20),
                    inputCell.correctUpdate(correctedNewValue = 10),
                ),
            ),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldValue = "10",
                expectedNewValue = "10",
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    // endregion

    // region Offline activation

    @Test
    @Ignore // FIXME: Fix offline activation
    fun test_offlineActivation() {
        val inputCell = TestInputCell(initialValue = 10)

        val subjectCell = inputCell.map { it.toString() }

        Cell_map_testUtils.testOfflineActivation(
            inputCell = inputCell,
            subjectCell = subjectCell,
        )
    }

    // endregion
}
