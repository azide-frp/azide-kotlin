package dev.azide.core.cell

import dev.azide.core.map
import dev.azide.core.test_utils.RandomValueGenerator
import dev.azide.core.test_utils.TestSlottedStimulation2
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.cell.Cell_expectations_testUtils
import dev.azide.core.test_utils.cell.Cell_fuzzyTestUtils
import dev.azide.core.test_utils.cell.Cell_reaction_testUtils
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import kotlin.random.Random
import kotlin.test.Ignore
import kotlin.test.Test

@Suppress("ClassName")
class Cell_map_fuzzyTests {
    companion object {
        private const val iterationCount = 1_000

        private const val initialCellValue = 0
        private const val maxCellPayloadValue = 100
    }

    @Test
    @Ignore // TODO: Figure out how a cell should behave when its input updates from X to X
    fun test_fuzzy() {
        val random = Random(0)

        val inputCell = TestInputCell(
            initialValue = initialCellValue,
        )

        val subjectCell = inputCell.map { "#$it" }

        var currentCellValue = initialCellValue

        repeat(iterationCount) {
            val oldInputCellValue = currentCellValue
            val newInputCellValue = random.nextInt(maxCellPayloadValue)

            // Build the stimulation sequence
            val cellStimulationSequence = Cell_fuzzyTestUtils.buildAppropriateInputCellStimulationSequence(
                random = random,
                intermediateValueGenerator = object : RandomValueGenerator<Int> {
                    override fun next(): Int = 0xBAADF00D.toInt()
                },
                inputCell = inputCell,
                oldValue = oldInputCellValue,
                newValue = newInputCellValue,
            )

            val combinedInputStimulation =
                cellStimulationSequence?.consecutiveStimulations?.let { consecutiveStimulations ->
                    TestStimulation.combineInProvidedOrder(
                        stimulations = consecutiveStimulations,
                    )
                } ?: TestStimulation.Noop

            // Execute the reaction transaction
            Cell_reaction_testUtils.testReaction(
                subjectCell = subjectCell,
                slottedInputStimulation = TestSlottedStimulation2(
                    listOf(
                        TestStimulation.Noop,
                        combinedInputStimulation,
                    ),
                ),
                expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                    intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                    expectedOldValue = "#$oldInputCellValue",
                    expectedNewValue = "#$newInputCellValue",
                ),
            )

            // Update state for next iteration
            currentCellValue = newInputCellValue
        }
    }
}
