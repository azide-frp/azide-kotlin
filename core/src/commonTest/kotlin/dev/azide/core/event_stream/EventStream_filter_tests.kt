package dev.azide.core.event_stream

import dev.azide.core.EventStream
import dev.azide.core.filter
import dev.azide.core.test_utils.TestInputStimulation
import dev.azide.core.test_utils.event_stream.EventStreamTestUtils
import kotlin.test.Test

@Suppress("ClassName")
class EventStream_filter_tests {
    @Test
    fun test_sourceNever() {
        val subjectEventStream = EventStream.Never.filter {
            throw UnsupportedOperationException()
        }

        _root_ide_package_.dev.azide.core.test_utils.event_stream.EventStreamTestUtils.verifyTerminated(
            subjectEventStream = subjectEventStream,
        )
    }

    @Test
    fun test_emission_predicateAccepted() {
        val sourceEventStream = _root_ide_package_.dev.azide.core.test_utils.event_stream.EventStreamTestUtils.createInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { true }

        _root_ide_package_.dev.azide.core.test_utils.event_stream.EventStreamTestUtils.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = sourceEventStream.emit(
                emittedEvent = 11,
            ),
            expectedEmittedEvent = 11,
        )
    }

    @Test
    fun test_emission_predicateRejected() {
        val sourceEventStream = _root_ide_package_.dev.azide.core.test_utils.event_stream.EventStreamTestUtils.createInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { false }

        _root_ide_package_.dev.azide.core.test_utils.event_stream.EventStreamTestUtils.verifyDoesNotEmitAtAll(
            subjectEventStream = subjectEventStream,
            inputStimulation = sourceEventStream.emit(
                emittedEvent = 11,
            ),
        )
    }

    @Test
    fun test_emission_revoked_predicateAccepted() {
        val sourceEventStream = _root_ide_package_.dev.azide.core.test_utils.event_stream.EventStreamTestUtils.createInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { true }

        _root_ide_package_.dev.azide.core.test_utils.event_stream.EventStreamTestUtils.verifyDoesNotEmitEffectively(
            subjectEventStream = subjectEventStream,
            inputStimulation = _root_ide_package_.dev.azide.core.test_utils.TestInputStimulation.Companion.combine(
                sourceEventStream.emit(
                    emittedEvent = 11,
                ),
                sourceEventStream.revokeEmission(),
            ),
        )
    }

    @Test
    fun test_emission_revoked_predicateRejected() {
        val sourceEventStream = _root_ide_package_.dev.azide.core.test_utils.event_stream.EventStreamTestUtils.createInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { false }

        _root_ide_package_.dev.azide.core.test_utils.event_stream.EventStreamTestUtils.verifyDoesNotEmitEffectively(
            subjectEventStream = subjectEventStream,
            inputStimulation = _root_ide_package_.dev.azide.core.test_utils.TestInputStimulation.Companion.combine(
                sourceEventStream.emit(
                    emittedEvent = 11,
                ),
                sourceEventStream.revokeEmission(),
            ),
        )
    }

    @Test
    fun test_emission_corrected_predicateAcceptedBoth() {
        val sourceEventStream = _root_ide_package_.dev.azide.core.test_utils.event_stream.EventStreamTestUtils.createInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { true }

        _root_ide_package_.dev.azide.core.test_utils.event_stream.EventStreamTestUtils.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = _root_ide_package_.dev.azide.core.test_utils.TestInputStimulation.Companion.combine(
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
        val sourceEventStream = _root_ide_package_.dev.azide.core.test_utils.event_stream.EventStreamTestUtils.createInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { false }

        _root_ide_package_.dev.azide.core.test_utils.event_stream.EventStreamTestUtils.verifyDoesNotEmitAtAll(
            subjectEventStream = subjectEventStream,
            inputStimulation = _root_ide_package_.dev.azide.core.test_utils.TestInputStimulation.Companion.combine(
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
        val sourceEventStream = _root_ide_package_.dev.azide.core.test_utils.event_stream.EventStreamTestUtils.createInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { it > 0 }

        _root_ide_package_.dev.azide.core.test_utils.event_stream.EventStreamTestUtils.verifyDoesNotEmitEffectively(
            subjectEventStream = subjectEventStream,
            inputStimulation = _root_ide_package_.dev.azide.core.test_utils.TestInputStimulation.Companion.combine(
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
        val sourceEventStream = _root_ide_package_.dev.azide.core.test_utils.event_stream.EventStreamTestUtils.createInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { it > 0 }

        _root_ide_package_.dev.azide.core.test_utils.event_stream.EventStreamTestUtils.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = _root_ide_package_.dev.azide.core.test_utils.TestInputStimulation.Companion.combine(
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
