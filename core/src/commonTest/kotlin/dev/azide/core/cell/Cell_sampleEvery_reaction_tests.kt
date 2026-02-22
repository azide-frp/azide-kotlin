package dev.azide.core.cell

import dev.azide.core.Cell
import dev.azide.core.pullExternally
import dev.azide.core.sampleEvery
import dev.azide.core.sampling
import dev.azide.core.test_utils.cell.Cell_expectations_testUtils
import dev.azide.core.test_utils.cell.Cell_generic_testUtils
import dev.azide.core.test_utils.cell.Cell_generic_testUtils.SourceCellTag
import dev.azide.core.test_utils.cell.Cell_reaction_testUtils
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.cell.correctingUpdate
import dev.azide.core.test_utils.cell.revokingUpdate
import dev.azide.core.test_utils.cell.updating
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulationScenario
import dev.azide.core.test_utils.stimulation_combinatorics.bind
import kotlin.test.Test

@Suppress("ClassName", "PrivatePropertyName")
class Cell_sampleEvery_reaction_tests {
    private typealias SuitableSlotCount = TestSlotCount.Count2

    private typealias SuitableTestSlottedStimulationScenario = TestSlottedStimulationScenario<SuitableSlotCount>

    private val slottedStimulationScenarioBank_sourceCellUpdates =
        Cell_generic_testUtils.stimulationScenarioBank_sourceCellUpdates.distribute(slotCount = SuitableSlotCount)

    private val slottedStimulationScenarioBank_sourceCellUpdatesRevoked =
        Cell_generic_testUtils.stimulationScenarioBank_sourceCellUpdatesRevoked.distribute(slotCount = SuitableSlotCount)

    private val slottedStimulationScenarioBank_sourceCellUpdatesCorrected =
        Cell_generic_testUtils.stimulationScenarioBank_sourceCellUpdatesCorrected.distribute(slotCount = SuitableSlotCount)

    @Test
    fun test_step_sourceUpdates() {
        slottedStimulationScenarioBank_sourceCellUpdates.forEach { slottedStimulationScenario ->
            test_step_sourceUpdates(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_step_sourceUpdates(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val helperCell1 = TestInputCell(initialValue = 10)
        val helperCell2 = TestInputCell(initialValue = 20)

        val sourceCell = TestInputCell(
            initialValue = helperCell1.sampling,
        )

        val subjectCell: Cell<Int> = sourceCell.sampleEvery().pullExternally()

        Cell_reaction_testUtils.testReaction(
            subjectCell = subjectCell,
            slottedInputStimulation = sourceCell.updating(
                tag = SourceCellTag,
                newValue = helperCell2.sampling,
            ).bind(slottedStimulationScenario),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                expectedOldValue = 10,
                expectedNewValue = 20,
            ),
        )
    }

    @Test
    fun test_step_sourceUpdatesRevoked() {
        slottedStimulationScenarioBank_sourceCellUpdatesRevoked.forEach { slottedStimulationScenario ->
            test_step_sourceUpdatesRevoked(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_step_sourceUpdatesRevoked(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val helperCell1 = TestInputCell(initialValue = 10)
        val helperCell2 = TestInputCell(initialValue = 20)

        val sourceCell = TestInputCell(
            initialValue = helperCell1.sampling,
        )

        val subjectCell: Cell<Int> = sourceCell.sampleEvery().pullExternally()

        Cell_reaction_testUtils.testReaction(
            subjectCell = subjectCell,
            slottedInputStimulation = sourceCell.revokingUpdate(
                tag = SourceCellTag,
                newValue = helperCell2.sampling,
            ).bind(slottedStimulationScenario),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectNoValueTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedValue = 10,
            ),
        )
    }

    @Test
    fun test_step_sourceUpdatesCorrected() {
        slottedStimulationScenarioBank_sourceCellUpdatesCorrected.forEach { slottedStimulationScenario ->
            test_step_sourceUpdatesCorrected(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_step_sourceUpdatesCorrected(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val helperCell1 = TestInputCell(initialValue = 10)
        val helperCell2 = TestInputCell(initialValue = 20)
        val helperCell3 = TestInputCell(initialValue = 30)

        val sourceCell = TestInputCell(
            initialValue = helperCell1.sampling,
        )

        val subjectCell: Cell<Int> = sourceCell.sampleEvery().pullExternally()

        Cell_reaction_testUtils.testReaction(
            subjectCell = subjectCell,
            slottedInputStimulation = sourceCell.correctingUpdate(
                tag = SourceCellTag,
                intermediateNewValue = helperCell2.sampling,
                correctedNewValue = helperCell3.sampling,
            ).bind(slottedStimulationScenario),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldValue = 10,
                expectedNewValue = 30,
            ),
        )
    }
}
