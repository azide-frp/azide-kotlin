package dev.azide.core.event_stream

import dev.azide.core.EventStream
import dev.azide.core.Moment
import dev.azide.core.sampleEach
import dev.azide.core.sampling
import dev.azide.core.test_utils.event_stream.EventStream_expectations_testUtils
import dev.azide.core.test_utils.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.TestSlotDispatcher1x2
import dev.azide.core.test_utils.TestSlotDispatcher2x2
import dev.azide.core.test_utils.bind
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.event_stream.EventStream_reaction_testUtils
import dev.azide.core.test_utils.event_stream.TestInputEventStream
import dev.azide.core.test_utils.event_stream.correctingEmission
import dev.azide.core.test_utils.event_stream.revokingEmission
import kotlin.test.Test

@Suppress("ClassName")
class EventStream_sampleEach_reaction_tests {
    @Test
    fun test_step_sourceEmits() {
        TestSlotDispatcher1x2.entries.forEach { dispatcher ->
            test_step_sourceEmits(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_step_sourceEmits(
        dispatcher: TestSlotDispatcher1x2,
    ) {
        val helperCell = TestInputCell(initialValue = 10)

        val sourceEventStream = TestInputEventStream<Moment<Int>>()

        val subjectEventStream: EventStream<Int> = sourceEventStream.sampleEach()

        EventStream_reaction_testUtils.executeReactionTransaction(
            subjectEventStream = subjectEventStream,
            slottedInputStimulation = sourceEventStream.emit(
                emittedEvent = helperCell.sampling,
            ).bind(dispatcher),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectEmission(
                expectedEmittedEvent = 10,
            ),
        )
    }

    @Test
    fun test_step_sourceEmitsRevoked() {
        TestSlotDispatcher2x2.entries.forEach { dispatcher ->
            test_step_sourceEmitsRevoked(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_step_sourceEmitsRevoked(
        dispatcher: TestSlotDispatcher2x2,
    ) {
        val helperCell = TestInputCell(initialValue = 10)

        val sourceEventStream = TestInputEventStream<Moment<Int>>()

        val subjectEventStream: EventStream<Int> = sourceEventStream.sampleEach()

        EventStream_reaction_testUtils.executeReactionTransaction(
            subjectEventStream = subjectEventStream,
            slottedInputStimulation = sourceEventStream.revokingEmission(
                emittedEvent = helperCell.sampling,
            ).bind(dispatcher),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectNoEmission(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
            ),
        )
    }

    @Test
    fun test_step_sourceEmitsCorrected() {
        TestSlotDispatcher2x2.entries.forEach { dispatcher ->
            test_step_sourceEmitsCorrected(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_step_sourceEmitsCorrected(
        dispatcher: TestSlotDispatcher2x2,
    ) {
        val helperCell1 = TestInputCell(initialValue = 10)
        val helperCell2 = TestInputCell(initialValue = 20)

        val sourceEventStream = TestInputEventStream<Moment<Int>>()

        val subjectEventStream: EventStream<Int> = sourceEventStream.sampleEach()

        EventStream_reaction_testUtils.executeReactionTransaction(
            subjectEventStream = subjectEventStream,
            slottedInputStimulation = sourceEventStream.correctingEmission(
                intermediateEmittedEvent = helperCell1.sampling,
                correctedEmittedEvent = helperCell2.sampling,
            ).bind(dispatcher),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectEmission(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedEmittedEvent = 20,
            ),
        )
    }
}
