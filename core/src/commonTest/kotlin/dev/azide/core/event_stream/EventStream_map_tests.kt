package dev.azide.core.event_stream

import dev.azide.core.map
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.event_stream.EventStreamTestUtils_deprecated
import dev.azide.core.test_utils.event_stream.TestInputEventStream
import kotlin.test.Test

@Suppress("ClassName")
class EventStream_map_tests {
    @Test
    fun test_sourceEmits() {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.map { it.toString() }

        EventStreamTestUtils_deprecated.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = sourceEventStream.emit(
                emittedEvent = 11,
            ),
            expectedEmittedEvent = "11",
        )
    }

    @Test
    fun test_sourceEmits_revoked() {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.map { it.toString() }

        EventStreamTestUtils_deprecated.verifyDoesNotEmitEffectively(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestStimulation.combineInProvidedOrder(
                sourceEventStream.emit(
                    emittedEvent = 11,
                ),
                sourceEventStream.revokeEmission(),
            ),
        )
    }

    @Test
    fun test_sourceEmits_corrected() {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.map { it.toString() }

        EventStreamTestUtils_deprecated.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestStimulation.combineInProvidedOrder(
                sourceEventStream.emit(
                    emittedEvent = 11,
                ),
                sourceEventStream.correctEmission(
                    correctedEmittedEvent = 12,
                ),
            ),
            expectedEmittedEvent = "12",
        )
    }
}
