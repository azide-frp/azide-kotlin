package dev.azide.core.event_stream

import dev.azide.core.filter
import dev.azide.core.test_utils.event_stream.EventStream_expectations_testUtils
import dev.azide.core.test_utils.event_stream.EventStream_generic_testUtils
import dev.azide.core.test_utils.event_stream.EventStream_generic_testUtils.SourceEventStreamTag
import dev.azide.core.test_utils.event_stream.EventStream_reaction_testUtils
import dev.azide.core.test_utils.event_stream.TestInputEventStream
import dev.azide.core.test_utils.event_stream.correctingEmission
import dev.azide.core.test_utils.event_stream.emitting
import dev.azide.core.test_utils.event_stream.revokingEmission
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulationScenario
import dev.azide.core.test_utils.stimulation_combinatorics.bind
import kotlin.test.Test

@Suppress("ClassName", "PrivatePropertyName")
// TODO: Switch to new-style unit test suite
class EventStream_filter_tests {
    private typealias SuitableSlotCount = TestSlotCount.Count2

    private typealias SuitableTestSlottedStimulationScenario = TestSlottedStimulationScenario<SuitableSlotCount>

    private val slottedStimulationScenarioBank_sourceEventStreamEmits =
        EventStream_generic_testUtils.stimulationScenarioBank_sourceEventStreamEmits.distribute(slotCount = SuitableSlotCount)

    private val slottedStimulationScenarioBank_sourceEventStreamEmitsRevoked =
        EventStream_generic_testUtils.stimulationScenarioBank_sourceEventStreamEmitsRevoked.distribute(slotCount = SuitableSlotCount)

    private val slottedStimulationScenarioBank_sourceEventStreamEmitsCorrected =
        EventStream_generic_testUtils.stimulationScenarioBank_sourceEventStreamEmitsCorrected.distribute(slotCount = SuitableSlotCount)

    @Test
    fun test_sourceEmits_predicateAccepted() {
        slottedStimulationScenarioBank_sourceEventStreamEmits.forEach { slottedStimulationScenario ->
            test_sourceEmits_predicateAccepted(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_sourceEmits_predicateAccepted(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { true }

        EventStream_reaction_testUtils.testReaction(
            subjectEventStream = subjectEventStream,
            slottedInputStimulation = sourceEventStream.emitting(
                tag = SourceEventStreamTag,
                emittedEvent = 11,
            ).bind(slottedStimulationScenario),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectEmission(
                expectedEmittedEvent = 11,
            ),
        )
    }

    @Test
    fun test_sourceEmits_predicateRejected() {
        slottedStimulationScenarioBank_sourceEventStreamEmits.forEach { slottedStimulationScenario ->
            test_sourceEmits_predicateRejected(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_sourceEmits_predicateRejected(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { false }

        EventStream_reaction_testUtils.testReaction(
            subjectEventStream = subjectEventStream,
            slottedInputStimulation = sourceEventStream.emitting(
                tag = SourceEventStreamTag,
                emittedEvent = 11,
            ).bind(slottedStimulationScenario),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectNoEmission(),
        )
    }

    @Test
    fun test_sourceEmitsRevoked_predicateAccepted() {
        slottedStimulationScenarioBank_sourceEventStreamEmitsRevoked.forEach { slottedStimulationScenario ->
            test_sourceEmitsRevoked_predicateAccepted(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_sourceEmitsRevoked_predicateAccepted(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { true }

        EventStream_reaction_testUtils.testReaction(
            subjectEventStream = subjectEventStream,
            slottedInputStimulation = sourceEventStream.revokingEmission(
                tag = SourceEventStreamTag,
                emittedEvent = 11,
            ).bind(slottedStimulationScenario),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectNoEmission(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
            ),
        )
    }

    @Test
    fun test_sourceEmitsRevoked_predicateRejected() {
        slottedStimulationScenarioBank_sourceEventStreamEmitsRevoked.forEach { slottedStimulationScenario ->
            test_sourceEmitsRevoked_predicateRejected(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_sourceEmitsRevoked_predicateRejected(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { false }

        EventStream_reaction_testUtils.testReaction(
            subjectEventStream = subjectEventStream,
            slottedInputStimulation = sourceEventStream.revokingEmission(
                tag = SourceEventStreamTag,
                emittedEvent = 11,
            ).bind(slottedStimulationScenario),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectNoEmission(),
        )
    }

    @Test
    fun test_sourceEmitsCorrected_predicateAcceptedBoth() {
        slottedStimulationScenarioBank_sourceEventStreamEmitsCorrected.forEach { slottedStimulationScenario ->
            test_sourceEmitsCorrected_predicateAcceptedBoth(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_sourceEmitsCorrected_predicateAcceptedBoth(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { true }

        EventStream_reaction_testUtils.testReaction(
            subjectEventStream = subjectEventStream,
            slottedInputStimulation = sourceEventStream.correctingEmission(
                tag = SourceEventStreamTag,
                intermediateEmittedEvent = 11,
                correctedEmittedEvent = 12,
            ).bind(slottedStimulationScenario),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectEmission(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedEmittedEvent = 12,
            ),
        )
    }

    @Test
    fun test_sourceEmitsCorrected_predicateRejectedBoth() {
        slottedStimulationScenarioBank_sourceEventStreamEmitsCorrected.forEach { slottedStimulationScenario ->
            test_sourceEmitsCorrected_predicateRejectedBoth(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_sourceEmitsCorrected_predicateRejectedBoth(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { false }

        EventStream_reaction_testUtils.testReaction(
            subjectEventStream = subjectEventStream,
            slottedInputStimulation = sourceEventStream.correctingEmission(
                tag = SourceEventStreamTag,
                intermediateEmittedEvent = 11,
                correctedEmittedEvent = 12,
            ).bind(slottedStimulationScenario),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectNoEmission(),
        )
    }

    @Test
    fun test_sourceEmitsCorrected_predicateAcceptedFirst() {
        slottedStimulationScenarioBank_sourceEventStreamEmitsCorrected.forEach { slottedStimulationScenario ->
            test_sourceEmitsCorrected_predicateAcceptedFirst(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_sourceEmitsCorrected_predicateAcceptedFirst(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { it > 0 }

        EventStream_reaction_testUtils.testReaction(
            subjectEventStream = subjectEventStream,
            slottedInputStimulation = sourceEventStream.correctingEmission(
                tag = SourceEventStreamTag,
                intermediateEmittedEvent = 11,
                correctedEmittedEvent = -12,
            ).bind(slottedStimulationScenario),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectNoEmission(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
            ),
        )
    }

    @Test
    fun test_sourceEmitsCorrected_predicateAcceptedSecond() {
        slottedStimulationScenarioBank_sourceEventStreamEmitsCorrected.forEach { slottedStimulationScenario ->
            test_sourceEmitsCorrected_predicateAcceptedSecond(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_sourceEmitsCorrected_predicateAcceptedSecond(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { it > 0 }

        EventStream_reaction_testUtils.testReaction(
            subjectEventStream = subjectEventStream,
            slottedInputStimulation = sourceEventStream.correctingEmission(
                tag = SourceEventStreamTag,
                intermediateEmittedEvent = -11,
                correctedEmittedEvent = 12,
            ).bind(slottedStimulationScenario),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectEmission(
                expectedEmittedEvent = 12,
            ),
        )
    }
}
