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
import dev.azide.core.test_utils.cell.correctingUpdate_deprecated
import dev.azide.core.test_utils.cell.revokingUpdate_deprecated
import dev.azide.core.test_utils.cell.updating_deprecated
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulationScenario
import dev.azide.core.test_utils.stimulation_combinatorics.bind
import kotlin.test.Test

@Suppress("ClassName", "PrivatePropertyName")
class Cell_sampleEvery_reaction_tests {
    private typealias SuitableSlotCount = TestSlotCount.Count2

    private typealias SuitableTestSlottedStimulationScenario = TestSlottedStimulationScenario<SuitableSlotCount>

    private val slottedStimulationScenarioBank_inputCellUpdates =
        Cell_generic_testUtils.stimulationScenarioBank_sourceCellUpdates.distribute(slotCount = SuitableSlotCount)

    private val slottedStimulationScenarioBank_inputCellUpdatesRevoked =
        Cell_generic_testUtils.stimulationScenarioBank_sourceCellUpdatesRevoked.distribute(slotCount = SuitableSlotCount)

    private val slottedStimulationScenarioBank_inputCellUpdatesCorrected =
        Cell_generic_testUtils.stimulationScenarioBank_sourceCellUpdatesCorrected.distribute(slotCount = SuitableSlotCount)

    @Test
    fun test_reaction_inputCellUpdates() {
        slottedStimulationScenarioBank_inputCellUpdates.forEach { slottedStimulationScenario ->
            test_reaction_inputCellUpdates(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_reaction_inputCellUpdates(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val helperCell1 = TestInputCell(initialValue = 10)
        val helperCell2 = TestInputCell(initialValue = 20)

        val inputCell = TestInputCell(
            initialValue = helperCell1.sampling,
        )

        val subjectCell: Cell<Int> = inputCell.sampleEvery().pullExternally()

        Cell_reaction_testUtils.testReaction(
            subjectCell = subjectCell,
            slottedInputStimulation = inputCell.updating_deprecated(
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
    fun test_reaction_inputCellUpdatesRevoked() {
        slottedStimulationScenarioBank_inputCellUpdatesRevoked.forEach { slottedStimulationScenario ->
            test_reaction_inputCellUpdatesRevoked(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_reaction_inputCellUpdatesRevoked(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val helperCell1 = TestInputCell(initialValue = 10)
        val helperCell2 = TestInputCell(initialValue = 20)

        val inputCell = TestInputCell(
            initialValue = helperCell1.sampling,
        )

        val subjectCell: Cell<Int> = inputCell.sampleEvery().pullExternally()

        Cell_reaction_testUtils.testReaction(
            subjectCell = subjectCell,
            slottedInputStimulation = inputCell.revokingUpdate_deprecated(
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
    fun test_reaction_inputCellUpdatesCorrected() {
        slottedStimulationScenarioBank_inputCellUpdatesCorrected.forEach { slottedStimulationScenario ->
            test_reaction_inputCellUpdatesCorrected(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_reaction_inputCellUpdatesCorrected(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val helperCell1 = TestInputCell(initialValue = 10)
        val helperCell2 = TestInputCell(initialValue = 20)
        val helperCell3 = TestInputCell(initialValue = 30)

        val inputCell = TestInputCell(
            initialValue = helperCell1.sampling,
        )

        val subjectCell: Cell<Int> = inputCell.sampleEvery().pullExternally()

        Cell_reaction_testUtils.testReaction(
            subjectCell = subjectCell,
            slottedInputStimulation = inputCell.correctingUpdate_deprecated(
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
