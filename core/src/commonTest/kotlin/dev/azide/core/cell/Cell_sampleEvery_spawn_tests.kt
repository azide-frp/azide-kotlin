package dev.azide.core.cell

import dev.azide.core.Cell
import dev.azide.core.Moment
import dev.azide.core.sampleEvery
import dev.azide.core.sampling
import dev.azide.core.test_utils.TestSlottedStimulationScenario1x3
import dev.azide.core.test_utils.TestSlottedStimulationScenario2x3
import dev.azide.core.test_utils.bind
import dev.azide.core.test_utils.cell.Cell_expectations_testUtils
import dev.azide.core.test_utils.cell.Cell_spawn_testUtils
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.cell.correctingUpdate
import dev.azide.core.test_utils.cell.revokingUpdate
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import kotlin.test.Test

@Suppress("ClassName")
class Cell_sampleEvery_spawn_tests {
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
        TestSlottedStimulationScenario1x3.entries.forEach { slottedStimulationScenario ->
            test_spawn_sourceEmitsSimultaneously(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_spawn_sourceEmitsSimultaneously(
        slottedStimulationScenario: TestSlottedStimulationScenario1x3,
    ) {
        val helperCell1 = TestInputCell(initialValue = 10)
        val helperCell2 = TestInputCell(initialValue = 20)

        val sourceCell = TestInputCell(
            initialValue = helperCell1.sampling,
        )

        val subjectSpawnMoment: Moment<Cell<Int>> = sourceCell.sampleEvery()

        Cell_spawn_testUtils.executeSpawnTransaction(
            subjectSpawnMoment = subjectSpawnMoment,
            slottedInputStimulation = sourceCell.update(
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
        TestSlottedStimulationScenario2x3.entries.forEach { slottedStimulationScenario ->
            test_spawn_sourceEmitsRevokedSimultaneously(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_spawn_sourceEmitsRevokedSimultaneously(
        slottedStimulationScenario: TestSlottedStimulationScenario2x3,
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
        TestSlottedStimulationScenario2x3.entries.forEach { slottedStimulationScenario ->
            test_spawn_sourceEmitsCorrectedSimultaneously(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_spawn_sourceEmitsCorrectedSimultaneously(
        slottedStimulationScenario: TestSlottedStimulationScenario2x3,
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
