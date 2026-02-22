package dev.azide.core.cell

import dev.azide.core.test_utils.cell.Cell_generic_testUtils
import dev.azide.core.test_utils.cell.Cell_generic_testUtils.SourceCellTag
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.cell.correctingUpdate
import dev.azide.core.test_utils.cell.revokingUpdate
import dev.azide.core.test_utils.cell.updating
import dev.azide.core.test_utils.event_stream.EventStream_expectations_testUtils
import dev.azide.core.test_utils.event_stream.EventStream_reaction_testUtils
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulationScenario
import dev.azide.core.test_utils.stimulation_combinatorics.bind
import dev.azide.core.updatedValues
import kotlin.test.Test

@Suppress("ClassName", "PrivatePropertyName")
class Cell_updatedValues_tests {
    private typealias SuitableSlotCount = TestSlotCount.Count2

    private typealias SuitableTestSlottedStimulationScenario = TestSlottedStimulationScenario<SuitableSlotCount>

    private val slottedStimulationScenarioBank_sourceCellUpdates =
        Cell_generic_testUtils.stimulationScenarioBank_sourceCellUpdates.distribute(slotCount = SuitableSlotCount)

    private val slottedStimulationScenarioBank_sourceCellUpdatesRevoked =
        Cell_generic_testUtils.stimulationScenarioBank_sourceCellUpdatesRevoked.distribute(slotCount = SuitableSlotCount)

    private val slottedStimulationScenarioBank_sourceCellUpdatesCorrected =
        Cell_generic_testUtils.stimulationScenarioBank_sourceCellUpdatesCorrected.distribute(slotCount = SuitableSlotCount)
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

        val subjectEventStream = sourceCell.updatedValues

        EventStream_reaction_testUtils.testReaction(
            subjectEventStream = subjectEventStream,
            slottedInputStimulation = sourceCell.updating(
                tag = SourceCellTag,
                newValue = 20,
            ).bind(slottedStimulationScenario),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectEmission(
                expectedEmittedEvent = 20,
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

        val subjectEventStream = sourceCell.updatedValues

        EventStream_reaction_testUtils.testReaction(
            subjectEventStream = subjectEventStream,
            slottedInputStimulation = sourceCell.revokingUpdate(
                tag = SourceCellTag,
                newValue = 20,
            ).bind(slottedStimulationScenario),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectNoEmission(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
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

        val subjectEventStream = sourceCell.updatedValues

        EventStream_reaction_testUtils.testReaction(
            subjectEventStream = subjectEventStream,
            slottedInputStimulation = sourceCell.correctingUpdate(
                tag = SourceCellTag,
                intermediateNewValue = 20,
                correctedNewValue = 21,
            ).bind(slottedStimulationScenario),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectEmission(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedEmittedEvent = 21,
            ),
        )
    }
}
