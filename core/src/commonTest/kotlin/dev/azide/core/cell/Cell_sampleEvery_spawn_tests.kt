package dev.azide.core.cell

import dev.azide.core.Cell
import dev.azide.core.Moment
import dev.azide.core.sampleEvery
import dev.azide.core.sampling
import dev.azide.core.test_utils.cell.Cell_expectations_testUtils
import dev.azide.core.test_utils.cell.Cell_generic_testUtils
import dev.azide.core.test_utils.cell.Cell_generic_testUtils.SourceCellTag
import dev.azide.core.test_utils.cell.Cell_spawn_testUtils
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
class Cell_sampleEvery_spawn_tests {
    private typealias SuitableSlotCount = TestSlotCount.Count3

    private typealias SuitableTestSlottedStimulationScenario = TestSlottedStimulationScenario<SuitableSlotCount>

    private val slottedStimulationBank_sourceCellUpdates =
        Cell_generic_testUtils.stimulationBank_sourceCellUpdates.distribute(slotCount = SuitableSlotCount)

    private val slottedStimulationBank_sourceCellUpdatesRevoked =
        Cell_generic_testUtils.stimulationBank_sourceCellUpdatesRevoked.distribute(slotCount = SuitableSlotCount)

    private val slottedStimulationBank_sourceCellUpdatesCorrected =
        Cell_generic_testUtils.stimulationBank_sourceCellUpdatesCorrected.distribute(slotCount = SuitableSlotCount)

    @Test
    fun test_spawn() {
        val helperCell1 = TestInputCell(initialValue = 10)

        val sourceCell = TestInputCell(
            initialValue = helperCell1.sampling,
        )

        val subjectSpawnMoment: Moment<Cell<Int>> = sourceCell.sampleEvery()

        Cell_spawn_testUtils.executeSpawnTransaction(
            subjectSpawnMoment = subjectSpawnMoment,
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectNoValueTransition(
                expectedUnaffectedValue = 10,
            ),
        )
    }

    @Test
    fun test_spawn_sourceEmitsSimultaneously() {
        slottedStimulationBank_sourceCellUpdates.forEach { slottedStimulationScenario ->
            test_spawn_sourceEmitsSimultaneously(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_spawn_sourceEmitsSimultaneously(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val helperCell1 = TestInputCell(initialValue = 10)
        val helperCell2 = TestInputCell(initialValue = 20)

        val sourceCell = TestInputCell(
            initialValue = helperCell1.sampling,
        )

        val subjectSpawnMoment: Moment<Cell<Int>> = sourceCell.sampleEvery()

        Cell_spawn_testUtils.executeSpawnTransaction(
            subjectSpawnMoment = subjectSpawnMoment,
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
    fun test_spawn_sourceEmitsRevokedSimultaneously() {
        slottedStimulationBank_sourceCellUpdatesRevoked.forEach { slottedStimulationScenario ->
            test_spawn_sourceEmitsRevokedSimultaneously(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_spawn_sourceEmitsRevokedSimultaneously(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val helperCell1 = TestInputCell(initialValue = 10)
        val helperCell2 = TestInputCell(initialValue = 20)

        val sourceCell = TestInputCell(
            initialValue = helperCell1.sampling,
        )

        val subjectSpawnMoment: Moment<Cell<Int>> = sourceCell.sampleEvery()

        Cell_spawn_testUtils.executeSpawnTransaction(
            subjectSpawnMoment = subjectSpawnMoment,
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
    fun test_spawn_sourceEmitsCorrectedSimultaneously() {
        slottedStimulationBank_sourceCellUpdatesCorrected.forEach { slottedStimulationScenario ->
            test_spawn_sourceEmitsCorrectedSimultaneously(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_spawn_sourceEmitsCorrectedSimultaneously(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val helperCell1 = TestInputCell(initialValue = 10)
        val helperCell2 = TestInputCell(initialValue = 20)
        val helperCell3 = TestInputCell(initialValue = 30)

        val sourceCell = TestInputCell(
            initialValue = helperCell1.sampling,
        )

        val subjectSpawnMoment: Moment<Cell<Int>> = sourceCell.sampleEvery()

        Cell_spawn_testUtils.executeSpawnTransaction(
            subjectSpawnMoment = subjectSpawnMoment,
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
