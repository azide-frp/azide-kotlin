package dev.azide.core.event_stream

import dev.azide.core.mapAt
import dev.azide.core.sample
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.event_stream.EventStreamTestUtils
import kotlin.test.Test

@Suppress("ClassName")
class EventStream_mapAt_tests {
    @Test
    fun test_emission() {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()
        val externalCell = TestInputCell(initialValue = 'A')

        val subjectEventStream = sourceEventStream.mapAt {
            val externalValue: Char = externalCell.sample()
            "$it:$externalValue"
        }

        EventStreamTestUtils.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = sourceEventStream.emit(
                emittedEvent = 11,
            ),
            expectedEmittedEvent = "11:A",
        )
    }

    @Test
    fun test_emission_revoked() {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()
        val externalCell = TestInputCell(initialValue = 'A')

        val subjectEventStream = sourceEventStream.mapAt {
            val externalValue: Char = externalCell.sample()
            "$it:$externalValue"
        }

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
    fun test_emission_corrected() {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()
        val externalCell = TestInputCell(initialValue = 'A')

        val subjectEventStream = sourceEventStream.mapAt {
            val externalValue: Char = externalCell.sample()
            "$it:$externalValue"
        }

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
            expectedEmittedEvent = "12:A",
        )
    }
}
