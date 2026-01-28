package dev.azide.core.event_stream

import dev.azide.core.EventStream
import dev.azide.core.filter
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.event_stream.EventStreamTestUtils
import dev.azide.core.test_utils.event_stream.TestInputEventStream
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
        val sourceEventStream = TestInputEventStream<Int>()

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
        val sourceEventStream = TestInputEventStream<Int>()

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
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { true }

        EventStreamTestUtils.verifyDoesNotEmitEffectively(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestStimulation.combine(
                sourceEventStream.emit(
                    emittedEvent = 11,
                ),
                sourceEventStream.revokeEmission(),
            ),
        )
    }

    @Test
    fun test_emission_revoked_predicateRejected() {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { false }

        EventStreamTestUtils.verifyDoesNotEmitEffectively(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestStimulation.combine(
                sourceEventStream.emit(
                    emittedEvent = 11,
                ),
                sourceEventStream.revokeEmission(),
            ),
        )
    }

    @Test
    fun test_emission_corrected_predicateAcceptedBoth() {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { true }

        EventStreamTestUtils.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestStimulation.combine(
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
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { false }

        EventStreamTestUtils.verifyDoesNotEmitAtAll(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestStimulation.combine(
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
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { it > 0 }

        EventStreamTestUtils.verifyDoesNotEmitEffectively(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestStimulation.combine(
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
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { it > 0 }

        EventStreamTestUtils.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestStimulation.combine(
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
