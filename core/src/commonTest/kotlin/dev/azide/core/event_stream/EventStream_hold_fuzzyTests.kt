package dev.azide.core.event_stream

import dev.azide.core.holding
import dev.azide.core.pullExternally
import dev.azide.core.test_utils.RandomValueGenerator
import dev.azide.core.test_utils.TestSequentialStimulation
import dev.azide.core.test_utils.TestSlottedStimulation2
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.cell.Cell_fuzzyTestUtils
import dev.azide.core.test_utils.cell.Cell_reaction_testUtils
import dev.azide.core.test_utils.event_stream.EventStream_fuzzyTestUtils
import dev.azide.core.test_utils.event_stream.TestInputEventStream
import dev.azide.core.test_utils.semantic.AnySemanticEventStream
import dev.azide.core.test_utils.semantic.HoldSemanticCell
import dev.azide.core.test_utils.semantic.SemanticCell
import dev.azide.core.test_utils.semantic.SemanticEventStream
import dev.azide.core.test_utils.semantic.Timestamp
import dev.azide.core.test_utils.semantic.evaluateTransition
import kotlin.random.Random
import kotlin.test.Test

@Suppress("ClassName")
class EventStream_hold_fuzzyTests {
    companion object {
        private const val iterationCount = 1_000

        private const val initialHeldValue = 0
        private const val maxEventPayloadValue = 100
    }

    data object InputEventStreamLabel : SemanticEventStream.Label

    @Test
    fun test_fuzzy() {
        val random = Random(0)

        val semanticEventValueGenerator = object : RandomValueGenerator<Int> {
            override operator fun next(): Int = random.nextInt(maxEventPayloadValue)
        }

        val semanticInputEventStream: AnySemanticEventStream<Int> =
            SemanticEventStream.generateRandom(
                label = InputEventStreamLabel,
                random = random,
                randomValueGenerator = semanticEventValueGenerator,
            )

        // Realize the semantic event stream into a technical test input and a provider that mirrors the semantic stream
        val (source, semanticStimulationProvider) = TestInputEventStream.realizeIndirectly(semanticInputEventStream)

        val semanticSubjectCell = HoldSemanticCell.fromEventStream(
            label = SemanticCell.Label.Dependent,
            eventStream = semanticStimulationProvider,
            initialValue = initialHeldValue,
        )

        val subjectMoment = source.holding(initialValue = initialHeldValue)
        val subjectCell = subjectMoment.pullExternally()

        Timestamp.generate(iterationCount).forEach { newTimestamp ->
            val semanticEmission = semanticInputEventStream.evaluate(timestamp = newTimestamp)

            val semanticSubjectTransition = semanticSubjectCell.evaluateTransition(newTimestamp = newTimestamp)

            // Build a randomized stimulation sequence that realizes the semantic emission (or lack thereof)
            val combinedInputStimulation: TestSequentialStimulation? = EventStream_fuzzyTestUtils.buildRandomInputEventStreamStimulationSequence(
                random = random,
                noiseValueGenerator = object : RandomValueGenerator<Int> {
                    override operator fun next(): Int = semanticEventValueGenerator.next()
                },
                inputEventStream = source,
                semanticEmission = semanticEmission,
            )

            // Convert semantic subject transition into ExpectedCellValueTransition
            val expectedSubjectValueTransition = Cell_fuzzyTestUtils.buildExpectedSubjectCellValueTransition(
                semanticSemanticTransition = semanticSubjectTransition,
            )

            // Run the reaction test
            Cell_reaction_testUtils.testReaction(
                subjectCell = subjectCell,
                slottedInputStimulation = TestSlottedStimulation2(
                    listOf(
                        TestStimulation.Noop,
                        combinedInputStimulation ?: TestStimulation.Noop,
                    ),
                ),
                expectedSubjectValueTransition = expectedSubjectValueTransition,
            )
        }
    }
}
