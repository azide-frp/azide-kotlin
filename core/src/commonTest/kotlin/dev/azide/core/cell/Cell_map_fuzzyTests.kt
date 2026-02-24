package dev.azide.core.cell

import dev.azide.core.map
import dev.azide.core.test_utils.RandomValueGenerator
import dev.azide.core.test_utils.TestSlottedStimulation2
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.cell.Cell_fuzzyTestUtils
import dev.azide.core.test_utils.cell.Cell_reaction_testUtils
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.semantic.HoldSemanticCell
import dev.azide.core.test_utils.semantic.SemanticCell
import dev.azide.core.test_utils.semantic.Timestamp
import dev.azide.core.test_utils.semantic.evaluateTransition
import dev.azide.core.test_utils.semantic.map
import kotlin.random.Random
import kotlin.test.Test

@Suppress("ClassName")
class Cell_map_fuzzyTests {
    companion object {
        private const val iterationCount = 1_000

        private const val maxCellPayloadValue = 100
    }

    data object InputCellLabel : SemanticCell.Label

    @Test
    fun test_fuzzy() {
        val random = Random(0)

        val semanticValueGenerator = object : RandomValueGenerator<Int> {
            override operator fun next(): Int = random.nextInt(maxCellPayloadValue)
        }

        val semanticInputCell: dev.azide.core.test_utils.semantic.AnySemanticCell<Int> = HoldSemanticCell.generateRandom(
            label = InputCellLabel,
            random = random,
            randomValueGenerator = semanticValueGenerator,
        )

        val inputCell: TestInputCell<Int> = TestInputCell.realizeInitially(
            semanticCell = semanticInputCell,
        )

        val subjectCell = inputCell.map { "#$it" }

        Timestamp.generate(iterationCount).forEach { newTimestamp ->
            val semanticInputTransition = semanticInputCell.evaluateTransition(newTimestamp = newTimestamp)

            val semanticSubjectTransition = semanticInputTransition.map { "#$it" }

            val cellStimulationSequence = Cell_fuzzyTestUtils.buildRandomInputCellStimulationSequence(
                random = random,
                noiseValueGenerator = object : RandomValueGenerator<Int> {
                    override fun next(): Int = 0xBAADF00D.toInt()
                },
                inputCell = inputCell,
                semanticInputTransition = semanticInputTransition,
            )

            val combinedInputStimulation =
                cellStimulationSequence?.consecutiveStimulations?.let { consecutiveStimulations ->
                    TestStimulation.combineInProvidedOrder(
                        stimulations = consecutiveStimulations,
                    )
                } ?: TestStimulation.Noop

            Cell_reaction_testUtils.testReaction(
                subjectCell = subjectCell,
                slottedInputStimulation = TestSlottedStimulation2(
                    listOf(
                        TestStimulation.Noop,
                        combinedInputStimulation,
                    ),
                ),
                expectedSubjectValueTransition = Cell_fuzzyTestUtils.buildExpectedSubjectCellValueTransition(
                    semanticSubjectTransition,
                ),
            )
        }
    }
}
