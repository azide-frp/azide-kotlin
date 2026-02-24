package dev.azide.core.cell

import dev.azide.core.Cell
import dev.azide.core.test_utils.RandomValueGenerator
import dev.azide.core.test_utils.TestSequentialStimulation
import dev.azide.core.test_utils.TestSequentialStimulationSet
import dev.azide.core.test_utils.TestSlottedStimulation2
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.cell.Cell_fuzzyTestUtils
import dev.azide.core.test_utils.cell.Cell_reaction_testUtils
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.semantic.HoldSemanticCell
import dev.azide.core.test_utils.semantic.SemanticCell
import dev.azide.core.test_utils.semantic.Timestamp
import dev.azide.core.test_utils.semantic.evaluateTransition
import dev.azide.core.test_utils.semantic.realize
import kotlin.jvm.JvmInline
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.test.Test

@Suppress("ClassName")
class Cell_switch_fuzzyTests {
    @JvmInline
    private value class InnerCellLabel(
        val index: Int,
    ) : SemanticCell.Label {
        init {
            require(index < innerCellCount) {
                "InnerCellLabel id must be less than $innerCellCount, but was $index."
            }
        }
    }

    private data object OuterCellLabel : SemanticCell.Label

    private typealias SemanticInputInnerCell = SemanticCell<InnerCellLabel, Int>

    companion object {
        private const val innerCellCount = 10
        private const val iterationCount = 1000

        private const val maxInnerValue = 50
    }

    @Test
    fun test_fuzzy() {
        val random = Random(0)

        val innerValueGenerator = object : RandomValueGenerator<Int> {
            override operator fun next(): Int = random.nextInt(maxInnerValue)
        }

        val semanticInputInnerCells: List<SemanticInputInnerCell> = List(innerCellCount) { index ->
            HoldSemanticCell.generateRandom(
                label = InnerCellLabel(index = index),
                random = random,
                randomValueGenerator = innerValueGenerator,
            )
        }

        val semanticInnerCellGenerator = object : RandomValueGenerator<SemanticInputInnerCell> {
            override operator fun next(): SemanticInputInnerCell = semanticInputInnerCells.random(random = random)
        }

        val semanticInputOuterCell: SemanticCell<*, SemanticInputInnerCell> = HoldSemanticCell.generateRandom(
            label = OuterCellLabel,
            random = random,
            randomValueGenerator = semanticInnerCellGenerator,
        )

        val semanticSubjectCell = SemanticCell.switch(
            outerCell = semanticInputOuterCell,
        )

        val inputInnerCells: List<TestInputCell<Int>> = semanticInputInnerCells.map { semanticInputInnerCell ->
            TestInputCell.realizeInitially(
                semanticCell = semanticInputInnerCell,
            )
        }

        val innerInputCellGenerator = object : RandomValueGenerator<Cell<Int>> {
            override operator fun next(): Cell<Int> = inputInnerCells.random(random = random)
        }

        val innerCellRealizer = object : TestInputCell.ValueRealizer<SemanticInputInnerCell, Cell<Int>> {
            override fun realize(
                semanticValue: SemanticInputInnerCell,
            ): Cell<Int> = inputInnerCells[semanticValue.label.index]
        }

        val inputOuterCell = TestInputCell.realizeInitially(
            semanticCell = semanticInputOuterCell,
            valueRealizer = innerCellRealizer,
        )

        val subjectCell: Cell<Int> = Cell.switch(inputOuterCell)

        val noiseInnerValueGenerator = object : RandomValueGenerator<Int> {
            override operator fun next(): Int = random.nextInt(-10..-1)
        }

        Timestamp.generate(iterationCount).forEach { newTimestamp ->
            val semanticInputInnerCellTransitions: List<SemanticCell.Transition<Int>> =
                semanticInputInnerCells.map { semanticInputInnerCell ->
                    semanticInputInnerCell.evaluateTransition(
                        newTimestamp = newTimestamp,
                    )
                }

            val semanticInputOuterCellTransition: SemanticCell.Transition<SemanticInputInnerCell> =
                semanticInputOuterCell.evaluateTransition(
                    newTimestamp = newTimestamp,
                )

            val realizedSemanticInputOuterCellTransition: SemanticCell.Transition<Cell<Int>> =
                semanticInputOuterCellTransition.realize(innerCellRealizer)

            val inputInnerCellSequentialStimulations: Set<TestSequentialStimulation> =
                semanticInputInnerCellTransitions.mapIndexedNotNull { index, semanticInnerCellTransition ->
                    Cell_fuzzyTestUtils.buildRandomInputCellStimulationSequence(
                        random = random,
                        noiseValueGenerator = noiseInnerValueGenerator,
                        inputCell = inputInnerCells[index],
                        semanticInputTransition = semanticInnerCellTransition,
                    )
                }.toSet()

            val inputOuterCellSequentialStimulation: TestSequentialStimulation? =
                Cell_fuzzyTestUtils.buildRandomInputCellStimulationSequence(
                    random = random,
                    noiseValueGenerator = innerInputCellGenerator,
                    inputCell = inputOuterCell,
                    semanticInputTransition = realizedSemanticInputOuterCellTransition,
                )

            val inputSequentialStimulation = TestSequentialStimulationSet(
                inputInnerCellSequentialStimulations + setOfNotNull(inputOuterCellSequentialStimulation),
            ).determinizeRandomly(
                random = random,
            )

            val semanticSubjectCellTransition = semanticSubjectCell.evaluateTransition(
                newTimestamp = newTimestamp,
            )

            val expectedSubjectValueTransition = Cell_fuzzyTestUtils.buildExpectedSubjectCellValueTransition(
                semanticSemanticTransition = semanticSubjectCellTransition,
            )

            Cell_reaction_testUtils.testReaction(
                subjectCell = subjectCell,
                slottedInputStimulation = TestSlottedStimulation2(
                    listOf(
                        TestStimulation.Noop,
                        inputSequentialStimulation,
                    ),
                ),
                expectedSubjectValueTransition = expectedSubjectValueTransition,
            )
        }
    }
}
