package dev.azide.core.event_stream

import dev.azide.core.EventStream
import dev.azide.core.hold
import dev.azide.core.test_utils.TestUtils
import dev.azide.core.test_utils.cell.CellTestUtils
import dev.azide.core.test_utils.event_stream.EventStreamTestUtils
import kotlin.test.Test

@Suppress("ClassName")
class EventStream_hold_tests {
    @Test
    fun test_passiveSample_sourceNever() {
        val subjectCell = CellTestUtils.spawnStatefulCell {
            EventStream.Never.hold(initialValue = 10)
        }

        CellTestUtils.verifyFrozen(
            subjectCell = subjectCell,
            expectedFrozenValue = 10,
        )
    }

    @Test
    fun test_passiveSample_initial() {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val subjectCell = CellTestUtils.spawnStatefulCell {
            sourceEventStream.hold(initialValue = 10)
        }

        CellTestUtils.verifyAtRest(
            subjectCell = subjectCell,
            expectedValue = 10,
        )
    }

    @Test
    fun test_passiveSample_afterSourceEmits() {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val subjectCell = CellTestUtils.spawnStatefulCell {
            sourceEventStream.hold(initialValue = 10)
        }

        TestUtils.stimulateSeparately(
            inputStimulation = sourceEventStream.emit(
                emittedEvent = 11,
            ),
        )

        CellTestUtils.verifyAtRest(
            subjectCell = subjectCell,
            expectedValue = 11,
        )
    }

    @Test
    fun test_update() {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val subjectCell = CellTestUtils.spawnStatefulCell {
            sourceEventStream.hold(initialValue = 10)
        }

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = sourceEventStream.emit(
                emittedEvent = 11,
            ),
            expectedOldValue = 10,
            expectedNewValue = 11,
        )
    }

    @Test
    fun test_update_atSpawn() {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        CellTestUtils.spawnStatefulCellExpectingUpdate(
            inputStimulation = sourceEventStream.emit(emittedEvent = 11),
            expectedOldValue = 10,
            expectedUpdatedValue = 11,
        ) {
            sourceEventStream.hold(initialValue = 10)
        }
    }
}
