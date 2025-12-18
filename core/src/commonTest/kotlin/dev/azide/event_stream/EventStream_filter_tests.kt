package dev.azide.event_stream

import dev.azide.EventStream
import dev.azide.filter
import dev.azide.test_utils.TestInputStimulation
import dev.azide.test_utils.event_stream.EventStreamTestUtils
import kotlin.test.Test

@Suppress("ClassName")
class EventStream_filter_tests {
    @Test
    fun test_sourceNever() {
        val subjectEventStream = EventStream.Never.filter {
            throw UnsupportedOperationException()
        }

        EventStreamTestUtils.verifyTerminated(
            subjectEventStream = subjectEventStream,
        )
    }

    @Test
    fun test_emission_predicateAccepted() {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { true }

        EventStreamTestUtils.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = sourceEventStream.emit(
                emittedEvent = 11,
            ),
            expectedEmittedEvent = 11,
        )
    }

    @Test
    fun test_emission_predicateRejected() {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { false }

        EventStreamTestUtils.verifyDoesNotEmitAtAll(
            subjectEventStream = subjectEventStream,
            inputStimulation = sourceEventStream.emit(
                emittedEvent = 11,
            ),
        )
    }

    @Test
    fun test_emission_revoked_predicateAccepted() {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { true }

        EventStreamTestUtils.verifyDoesNotEmitEffectively(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestInputStimulation.combine(
                sourceEventStream.emit(
                    emittedEvent = 11,
                ),
                sourceEventStream.revokeEmission(),
            ),
        )
    }

    @Test
    fun test_emission_revoked_predicateRejected() {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { false }

        EventStreamTestUtils.verifyDoesNotEmitEffectively(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestInputStimulation.combine(
                sourceEventStream.emit(
                    emittedEvent = 11,
                ),
                sourceEventStream.revokeEmission(),
            ),
        )
    }

    @Test
    fun test_emission_corrected_predicateAcceptedBoth() {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { true }

        EventStreamTestUtils.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestInputStimulation.combine(
                sourceEventStream.emit(
                    emittedEvent = 11,
                ),
                sourceEventStream.correctEmission(
                    correctedEmittedEvent = 12,
                ),
            ),
            expectedEmittedEvent = 12,
        )
    }

    @Test
    fun test_emission_corrected_predicateRejectedBoth() {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { false }

        EventStreamTestUtils.verifyDoesNotEmitAtAll(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestInputStimulation.combine(
                sourceEventStream.emit(
                    emittedEvent = 11,
                ),
                sourceEventStream.correctEmission(
                    correctedEmittedEvent = 12,
                ),
            ),
        )
    }

    @Test
    fun test_emission_corrected_predicateAcceptedFirst() {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { it > 0 }

        EventStreamTestUtils.verifyDoesNotEmitEffectively(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestInputStimulation.combine(
                sourceEventStream.emit(
                    emittedEvent = 11,
                ),
                sourceEventStream.correctEmission(
                    correctedEmittedEvent = -12,
                ),
            ),
        )
    }

    @Test
    fun test_emission_corrected_predicateAcceptedSecond() {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { it > 0 }

        EventStreamTestUtils.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestInputStimulation.combine(
                sourceEventStream.emit(
                    emittedEvent = -11,
                ),
                sourceEventStream.correctEmission(
                    correctedEmittedEvent = 12,
                ),
            ),
            expectedEmittedEvent = 12,
        )
    }
}
