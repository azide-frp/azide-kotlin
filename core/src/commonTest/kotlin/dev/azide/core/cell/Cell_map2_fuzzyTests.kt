package dev.azide.core.cell

import dev.azide.core.Cell
import dev.azide.core.test_utils.RandomValueGenerator
import dev.azide.core.test_utils.TestSequentialStimulationSet
import dev.azide.core.test_utils.TestSlottedStimulation2
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.cell.Cell_fuzzyTestUtils
import dev.azide.core.test_utils.cell.Cell_reaction_testUtils
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.semantic.AnySemanticCell
import dev.azide.core.test_utils.semantic.HoldSemanticCell
import dev.azide.core.test_utils.semantic.SemanticCell
import dev.azide.core.test_utils.semantic.Timestamp
import dev.azide.core.test_utils.semantic.evaluateTransition
import kotlin.random.Random
import kotlin.test.Test

@Suppress("ClassName")
class Cell_map2_fuzzyTests {
    companion object {
        private const val iterationCount = 1_000
    }

    @Test
    fun test_fuzzy() {
        val random = Random(0)

        val semanticValueGenerator1 = object : RandomValueGenerator<Int> {
            override operator fun next(): Int = random.nextInt(0, 100)
        }

        val semanticValueGenerator2 = object : RandomValueGenerator<Char> {
            override operator fun next(): Char = random.nextInt('A'.code, 'Z'.code + 1).toChar()
        }

        val semanticInputCell1: AnySemanticCell<Int> = HoldSemanticCell.generateRandom(
            label = SemanticCell.Label.Dependent,
            random = random,
            randomValueGenerator = semanticValueGenerator1,
        )

        val semanticInputCell2: AnySemanticCell<Char> = HoldSemanticCell.generateRandom(
            label = SemanticCell.Label.Dependent,
            random = random,
            randomValueGenerator = semanticValueGenerator2,
        )

        val inputCell1: TestInputCell<Int> = TestInputCell.realizeInitially(
            semanticCell = semanticInputCell1,
        )

        val inputCell2: TestInputCell<Char> = TestInputCell.realizeInitially(
            semanticCell = semanticInputCell2,
        )

        val subjectCell: Cell<String> = Cell.map2(
            cell1 = inputCell1,
            cell2 = inputCell2,
        ) { v1, v2 -> "$v1:$v2" }

        val noiseGenerator1 = object : RandomValueGenerator<Int> {
            override operator fun next(): Int = listOf(
                0xBAADF00D.toInt(),
                0xDEADBEEF.toInt(),
                0xFEEDFACE.toInt(),
            ).random()
        }

        val noiseGenerator2 = object : RandomValueGenerator<Char> {
            override operator fun next(): Char = listOf(
                '\u0000',
                '\uFFFF',
            ).random()
        }

        Timestamp.generate(iterationCount).forEach { newTimestamp ->
            val semanticTransition1 = semanticInputCell1.evaluateTransition(newTimestamp = newTimestamp)
            val semanticTransition2 = semanticInputCell2.evaluateTransition(newTimestamp = newTimestamp)

            val seq1 = Cell_fuzzyTestUtils.buildRandomInputCellStimulationSequence(
                random = random,
                noiseValueGenerator = noiseGenerator1,
                inputCell = inputCell1,
                semanticInputTransition = semanticTransition1,
            )

            val seq2 = Cell_fuzzyTestUtils.buildRandomInputCellStimulationSequence(
                random = random,
                noiseValueGenerator = noiseGenerator2,
                inputCell = inputCell2,
                semanticInputTransition = semanticTransition2,
            )

            val inputSequentialStimulation = TestSequentialStimulationSet(
                setOfNotNull(seq1, seq2),
            ).determinizeRandomly(random = random)

            // Build expected semantic subject transition by combining old/new snapshots
            val oldSnap1 = semanticInputCell1.evaluate(timestamp = newTimestamp.previous)
            val newSnap1 = semanticInputCell1.evaluate(timestamp = newTimestamp)
            val oldSnap2 = semanticInputCell2.evaluate(timestamp = newTimestamp.previous)
            val newSnap2 = semanticInputCell2.evaluate(timestamp = newTimestamp)

            val semanticSubjectTransition = when {
                oldSnap1 == newSnap1 && oldSnap2 == newSnap2 -> SemanticCell.Transition.Pass(
                    unaffectedValue = "${oldSnap1.value}:${oldSnap2.value}",
                )

                else -> SemanticCell.Transition.Update(
                    oldValue = "${oldSnap1.value}:${oldSnap2.value}",
                    updatedValue = "${newSnap1.value}:${newSnap2.value}",
                )
            }

            Cell_reaction_testUtils.testReaction(
                subjectCell = subjectCell,
                slottedInputStimulation = TestSlottedStimulation2(
                    listOf(
                        TestStimulation.Noop,
                        inputSequentialStimulation,
                    ),
                ),
                expectedSubjectValueTransition = Cell_fuzzyTestUtils.buildExpectedSubjectCellValueTransition(
                    semanticSubjectTransition,
                ),
            )
        }
    }
}
