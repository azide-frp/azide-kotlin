package dev.azide.core.event_stream

import dev.azide.core.filter
import dev.azide.core.test_utils.RandomValueGenerator
import dev.azide.core.test_utils.TestSequentialStimulation
import dev.azide.core.test_utils.TestSlottedStimulation2
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.event_stream.EventStream_fuzzyTestUtils
import dev.azide.core.test_utils.event_stream.EventStream_expectations_testUtils
import dev.azide.core.test_utils.event_stream.EventStream_reaction_testUtils
import dev.azide.core.test_utils.event_stream.TestInputEventStream
import dev.azide.core.test_utils.semantic.AnySemanticEventStream
import dev.azide.core.test_utils.semantic.SemanticEventStream
import dev.azide.core.test_utils.semantic.Timestamp
import kotlin.random.Random
import kotlin.test.Test

@Suppress("ClassName")
class EventStream_filter_fuzzyTests {
    companion object {
        private const val iterationCount = 1_000

        private const val maxEventPayloadValue = 100
    }

    data object InputEventStreamLabel : SemanticEventStream.Label

    @Test
    fun test_fuzzy() {
        val random = Random(0)

        val semanticEventValueGenerator = object : RandomValueGenerator<Int> {
            override operator fun next(): Int = random.nextInt(maxEventPayloadValue)
        }

        val semanticInputEventStream: AnySemanticEventStream<Int> = SemanticEventStream.generateRandom(
            label = InputEventStreamLabel,
            random = random,
            randomValueGenerator = semanticEventValueGenerator,
        )

        // Realize semantic stream into technical source and a mirror provider
        val (source, semanticProvider) = TestInputEventStream.realizeIndirectly(semanticInputEventStream)

        // Predicate under test: accept even numbers
        val predicate: (Int) -> Boolean = { it % 2 == 0 }

        val subjectEventStream = source.filter(predicate = predicate)

        Timestamp.generate(iterationCount).forEach { newTimestamp ->
            // Semantic emitted event at this timestamp (may be null)
            val semanticEmission = semanticInputEventStream.evaluate(timestamp = newTimestamp)

            // Semantic subject emission: apply predicate to semantic emission
            val semanticSubjectEmission: Int? = semanticEmission?.takeIf { predicate(it) }

            // Build randomized technical stimulation sequence that realizes the semantic emission
            val seq: TestSequentialStimulation? = EventStream_fuzzyTestUtils.buildRandomInputEventStreamStimulationSequence(
                random = random,
                noiseValueGenerator = object : RandomValueGenerator<Int> {
                    override operator fun next(): Int = semanticEventValueGenerator.next()
                },
                inputEventStream = source,
                semanticEmission = semanticEmission,
            )

            val combinedTestStimulation = seq?.consecutiveStimulations?.let { consecutive ->
                @Suppress("UNCHECKED_CAST")
                TestStimulation.combineInProvidedOrder(stimulations = (consecutive as List<TestStimulation>))
            } ?: TestStimulation.Noop

            val expected = if (semanticSubjectEmission != null) {
                EventStream_expectations_testUtils.expectEmission(
                    intermediatePropagationTolerance = dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance.Tolerate,
                    expectedEmittedEvent = semanticSubjectEmission,
                )
            } else {
                EventStream_expectations_testUtils.expectNoEmission(
                    intermediatePropagationTolerance = dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance.Tolerate,
                )
            }

            EventStream_reaction_testUtils.testReaction(
                subjectEventStream = subjectEventStream,
                slottedInputStimulation = TestSlottedStimulation2(
                    listOf(
                        TestStimulation.Noop,
                        combinedTestStimulation,
                    ),
                ),
                expectedSubjectEmission = expected,
            )
        }
    }
}
