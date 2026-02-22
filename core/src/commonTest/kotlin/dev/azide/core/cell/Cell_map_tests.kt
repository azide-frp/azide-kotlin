package dev.azide.core.cell

import dev.azide.core.map
import dev.azide.core.test_utils.cell.Cell_expectations_testUtils
import dev.azide.core.test_utils.cell.Cell_generic_testUtils
import dev.azide.core.test_utils.cell.Cell_generic_testUtils.SourceCellTag
import dev.azide.core.test_utils.cell.Cell_reaction_testUtils
import dev.azide.core.test_utils.cell.Cell_sampling_testUtils
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.cell.correctingUpdate
import dev.azide.core.test_utils.cell.revokingUpdate
import dev.azide.core.test_utils.cell.updating
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulationScenario
import dev.azide.core.test_utils.stimulation_combinatorics.bind
import kotlin.test.Ignore
import kotlin.test.Test

@Suppress("ClassName", "PrivatePropertyName")
class Cell_map_tests {
    private typealias SuitableSlotCount = TestSlotCount.Count2

    private typealias SuitableTestSlottedStimulationScenario = TestSlottedStimulationScenario<SuitableSlotCount>

    private val slottedStimulationScenarioBank_sourceCellUpdates =
        Cell_generic_testUtils.stimulationScenarioBank_sourceCellUpdates.distribute(slotCount = SuitableSlotCount)

    private val slottedStimulationScenarioBank_sourceCellUpdatesRevoked =
        Cell_generic_testUtils.stimulationScenarioBank_sourceCellUpdatesRevoked.distribute(slotCount = SuitableSlotCount)

    private val slottedStimulationScenarioBank_sourceCellUpdatesCorrected =
        Cell_generic_testUtils.stimulationScenarioBank_sourceCellUpdatesCorrected.distribute(slotCount = SuitableSlotCount)

    @Test
    fun test_passiveSample() {
        val sourceCell = TestInputCell(
            initialValue = 10,
        )

        val subjectCell = sourceCell.map { it.toString() }

        Cell_sampling_testUtils.executeSamplingTransaction(
            subjectCell = subjectCell,
            expectedSubjectValue = Cell_expectations_testUtils.expectStableValue(
                expectedValue = "10",
            ),
        )
    }

    @Test
    fun test_sourceUpdates() {
        slottedStimulationScenarioBank_sourceCellUpdates.forEach { slottedStimulationScenario ->
            test_sourceUpdates(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_sourceUpdates(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceCell = TestInputCell(
            initialValue = 10,
        )

        val subjectCell = sourceCell.map { it.toString() }

        Cell_reaction_testUtils.executeReactionTransaction(
            subjectCell = subjectCell,
            slottedInputStimulation = sourceCell.updating(
                tag = SourceCellTag,
                newValue = 11,
            ).bind(slottedStimulationScenario),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                expectedOldValue = "10",
                expectedNewValue = "11",
            ),
        )
    }

    @Test
    fun test_sourceUpdates_revoked() {
        slottedStimulationScenarioBank_sourceCellUpdatesRevoked.forEach { slottedStimulationScenario ->
            test_sourceUpdates_revoked(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_sourceUpdates_revoked(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceCell = TestInputCell(
            initialValue = 10,
        )

        val subjectCell = sourceCell.map { it.toString() }

        Cell_reaction_testUtils.executeReactionTransaction(
            subjectCell = subjectCell,
            slottedInputStimulation = sourceCell.revokingUpdate(
                tag = SourceCellTag,
                newValue = 11,
            ).bind(slottedStimulationScenario),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectNoValueTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedValue = "10",
            ),
        )
    }

    @Test
    fun test_sourceUpdates_corrected() {
        slottedStimulationScenarioBank_sourceCellUpdatesCorrected.forEach { slottedStimulationScenario ->
            test_sourceUpdates_corrected(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_sourceUpdates_corrected(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceCell = TestInputCell(
            initialValue = 10,
        )

        val subjectCell = sourceCell.map { it.toString() }

        Cell_reaction_testUtils.executeReactionTransaction(
            subjectCell = subjectCell,
            slottedInputStimulation = sourceCell.correctingUpdate(
                tag = SourceCellTag,
                intermediateNewValue = 11,
                correctedNewValue = 12,
            ).bind(slottedStimulationScenario),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldValue = "10",
                expectedNewValue = "12",
            ),
        )
    }

    @Test
    @Ignore // FIXME: Fix offline activation
    fun test_offlineActivation() {
        val inputCell = TestInputCell(
            initialValue = 10,
        )

        val subjectCell = inputCell.map { it.toString() }

        Cell_map_testUtils.executeOfflineActivationTransaction(
            inputCell = inputCell,
            subjectCell = subjectCell,
        )
    }
}
