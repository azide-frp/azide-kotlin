package dev.azide.core.cell

import dev.azide.core.Cell
import dev.azide.core.Moment
import dev.azide.core.pullExternally
import dev.azide.core.sampleEvery
import dev.azide.core.test_utils.RandomValueGenerator
import dev.azide.core.test_utils.TestSequentialStimulation
import dev.azide.core.test_utils.TestSlottedStimulation2
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.cell.Cell_fuzzyTestUtils
import dev.azide.core.test_utils.cell.Cell_reaction_testUtils
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.moment.TestInputMoment
import dev.azide.core.test_utils.semantic.AnySemanticCell
import dev.azide.core.test_utils.semantic.HoldSemanticCell
import dev.azide.core.test_utils.semantic.SemanticCell
import dev.azide.core.test_utils.semantic.SemanticMoment
import dev.azide.core.test_utils.semantic.Timestamp
import dev.azide.core.test_utils.semantic.evaluateTransition
import dev.azide.core.test_utils.semantic.realize
import kotlin.jvm.JvmInline
import kotlin.random.Random
import kotlin.test.Test

@Suppress("ClassName")
class Cell_sampleEvery_fuzzyTests {
    @JvmInline
    private value class InputMomentLabel(val index: Int) : SemanticMoment.Label

    private data object InputCellLabel : SemanticCell.Label

    private typealias SemanticInputMoment = SemanticMoment<InputMomentLabel, Int>

    companion object {
        private const val momentPoolSize = 8
        private const val iterationCount = 1_000
        private const val maxMomentPayload = 100
    }

    @Test
    fun test_fuzzy() {
        val random = Random(0)

        val momentValueGenerator = object : RandomValueGenerator<Int> {
            override operator fun next(): Int = random.nextInt(maxMomentPayload)
        }

        // Create a pool of semantic moments
        val semanticMoments: List<SemanticInputMoment> = List(momentPoolSize) { index ->
            SemanticMoment.generateRandom(
                label = InputMomentLabel(index),
                random = random,
                randomValueGenerator = momentValueGenerator,
            )
        }

        // Generator that picks a random semantic moment from the pool
        val semanticMomentGenerator = object : RandomValueGenerator<SemanticInputMoment> {
            override operator fun next(): SemanticInputMoment = semanticMoments.random(random = random)
        }

        // Semantic input cell whose values are moments
        val semanticInputCell: AnySemanticCell<SemanticInputMoment> = HoldSemanticCell.generateRandom(
            label = InputCellLabel,
            random = random,
            randomValueGenerator = semanticMomentGenerator,
        )

        // Realize semantic moments as TestInputMoment instances (indexed by label)
        val testInputMoments: List<TestInputMoment<Int>> = semanticMoments.map { semanticMoment ->
            TestInputMoment(initialValue = semanticMoment.evaluate(timestamp = Timestamp.zero))
        }

        val momentRealizer = object : TestInputCell.ValueRealizer<SemanticInputMoment, Moment<Int>> {
            override fun realize(semanticValue: SemanticInputMoment): Moment<Int> =
                testInputMoments[semanticValue.label.index]
        }

        // Create input cell realized initially
        val inputCell: TestInputCell<Moment<Int>> = TestInputCell.realizeInitially(
            semanticCell = semanticInputCell,
            valueRealizer = momentRealizer,
        )

        val subjectCell: Cell<Int> = inputCell.sampleEvery().pullExternally()

        // Build a semantic view of the sampled subject using SemanticCell.sampleEvery
        val semanticSubjectMoment = SemanticCell.sampleEvery(outerCell = semanticInputCell)
        // Evaluate the moment once to obtain the semantic subject cell (stable object across timestamps).
        val semanticSubjectCell: AnySemanticCell<Int> = semanticSubjectMoment.evaluate(timestamp = Timestamp.zero)

        Timestamp.generate(iterationCount).forEach { newTimestamp ->
            // Before each transaction, set all TestInputMoments to their semantic values at newTimestamp,
            // so that when sampleEvery samples a moment during a source-cell update it reads the right value.
            semanticMoments.forEachIndexed { index, semanticMoment ->
                testInputMoments[index].setCurrentValue(semanticMoment.evaluate(timestamp = newTimestamp))
            }

            val semanticInputTransition = semanticInputCell.evaluateTransition(newTimestamp = newTimestamp)

            val realizedInputTransition = semanticInputTransition.realize(valueRealValueT = momentRealizer)

            val inputCellSequentialStimulation: TestSequentialStimulation? = Cell_fuzzyTestUtils.buildRandomInputCellStimulationSequence(
                random = random,
                noiseValueGenerator = object : RandomValueGenerator<Moment<Int>> {
                    override operator fun next(): Moment<Int> = momentRealizer.realize(semanticMomentGenerator.next())
                },
                inputCell = inputCell,
                semanticInputTransition = realizedInputTransition,
            )

            val combinedInputStimulation = inputCellSequentialStimulation ?: TestStimulation.Noop

            // Derive the expected semantic subject transition from the semantic subject cell.
            val semanticSubjectTransition: SemanticCell.Transition<Int> = semanticSubjectCell.evaluateTransition(newTimestamp = newTimestamp)

            val expectedSubjectValueTransition = Cell_fuzzyTestUtils.buildExpectedSubjectCellValueTransition(
                semanticSemanticTransition = semanticSubjectTransition,
            )

            Cell_reaction_testUtils.testReaction(
                subjectCell = subjectCell,
                slottedInputStimulation = TestSlottedStimulation2(listOf(TestStimulation.Noop, combinedInputStimulation)),
                expectedSubjectValueTransition = expectedSubjectValueTransition,
            )
        }
    }
}
