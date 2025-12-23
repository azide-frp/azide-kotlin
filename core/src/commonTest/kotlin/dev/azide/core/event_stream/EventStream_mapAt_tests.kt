package dev.azide.core.event_stream

import dev.azide.core.EventStream
import dev.azide.core.mapAt
import dev.azide.core.sample
import dev.azide.core.test_utils.TestInputStimulation
import dev.azide.core.test_utils.cell.CellTestUtils
import dev.azide.core.test_utils.event_stream.EventStreamTestUtils
import kotlin.test.Test

@Suppress("ClassName")
class EventStream_mapAt_tests {
    @Test
    fun test_sourceNever() {
        val subjectEventStream = EventStream.Never.mapAt {
            throw UnsupportedOperationException()
        }

        EventStreamTestUtils.verifyTerminated(
            subjectEventStream = subjectEventStream,
        )
    }

    @Test
    fun test_emission() {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()
        val externalCell = CellTestUtils.createInputCell(initialValue = 'A')

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
        val externalCell = CellTestUtils.createInputCell(initialValue = 'A')

        val subjectEventStream = sourceEventStream.mapAt {
            val externalValue: Char = externalCell.sample()
            "$it:$externalValue"
        }

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
    fun test_emission_corrected() {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()
        val externalCell = CellTestUtils.createInputCell(initialValue = 'A')

        val subjectEventStream = sourceEventStream.mapAt {
            val externalValue: Char = externalCell.sample()
            "$it:$externalValue"
        }

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
            expectedEmittedEvent = "12:A",
        )
    }
}
