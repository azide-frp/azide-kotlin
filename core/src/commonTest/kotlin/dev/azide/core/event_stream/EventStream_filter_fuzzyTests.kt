package dev.azide.core.event_stream

import dev.azide.core.filter
import dev.azide.core.test_utils.RandomValueGenerator
import dev.azide.core.test_utils.TestSlottedStimulation2
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.event_stream.EventStream_fuzzyTestUtils
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

        val semanticValueGenerator = object : RandomValueGenerator<Int> {
            override operator fun next(): Int = random.nextInt(maxEventPayloadValue)
        }

        val semanticInputEventStream: AnySemanticEventStream<Int> = SemanticEventStream.generateRandom(
            label = InputEventStreamLabel,
            random = random,
            randomValueGenerator = semanticValueGenerator,
        )

        // Realize semantic stream into technical input and a mirror provider
        val inputEventStream = TestInputEventStream<Int>()

        // Predicate under test: accept even numbers
        val predicate: (Int) -> Boolean = { it % 2 == 0 }

        val subjectEventStream = inputEventStream.filter(predicate = predicate)

        Timestamp.generate(iterationCount).forEach { newTimestamp ->
            // Semantic emitted event at this timestamp (may be null)
            val semanticEmission = semanticInputEventStream.evaluate(timestamp = newTimestamp)

            // Semantic subject emission: apply predicate to semantic emission
            val semanticSubjectEmission: Int? = semanticEmission?.takeIf { predicate(it) }

            // Build randomized technical stimulation sequence that realizes the semantic emission
            val eventStimulationSequence = EventStream_fuzzyTestUtils.buildRandomInputEventStreamStimulationSequence(
                random = random,
                noiseValueGenerator = object : RandomValueGenerator<Int> {
                    override operator fun next(): Int = 0xBAADF00D.toInt()
                },
                inputEventStream = inputEventStream,
                semanticEmission = semanticEmission,
            ) ?: TestStimulation.Noop

            val expected = EventStream_fuzzyTestUtils.buildExpectedSubjectEventStreamEmission(semanticSubjectEmission)

            EventStream_reaction_testUtils.testReaction(
                subjectEventStream = subjectEventStream,
                slottedInputStimulation = TestSlottedStimulation2(
                    listOf(
                        TestStimulation.Noop,
                        eventStimulationSequence,
                    ),
                ),
                expectedSubjectEmission = expected,
            )
        }
    }
}
