package dev.azide.core.event_stream

import dev.azide.core.Cell
import dev.azide.core.EventStream
import dev.azide.core.hold
import dev.azide.core.holding
import dev.azide.core.joinOf
import dev.azide.core.map
import dev.azide.core.mapAt
import dev.azide.core.sample
import dev.azide.core.sampling
import dev.azide.core.test_utils.TestUtils
import dev.azide.core.test_utils.cell.CellTestUtils
import dev.azide.core.test_utils.event_stream.EventStreamTestUtils
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("ClassName")
class EventStream_hold_tests {
    @Test
    fun test_passiveSample_sourceNever() {
        val subjectCell = CellTestUtils.spawnStatefulCell {
            EventStream.Never.hold(initialValue = 10)
        }

        CellTestUtils.verifyAtRest(
            subjectCell = subjectCell,
            expectedValue = 10,
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
    fun test_delayed() {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val subjectCell = TestUtils.pullSeparately {
            sourceEventStream.hold(initialValue = 10)
        }

        // Emit some source events, while the subject cell wasn't even "touched"
        TestUtils.stimulateSeparately(
            sourceEventStream.emit(20),
        )

        CellTestUtils.verifyAtRest(
            subjectCell = subjectCell,
            expectedValue = 20,
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

    @Test
    fun test_looped() {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val memoryCell = TestUtils.pullSeparately(
            EventStream.loopedInMoment { loopedMultiplicationStream: EventStream<Int> ->
                loopedMultiplicationStream.holding(initialValue = 0).map { memoryCell: Cell<Int> ->
                    val multiplicationStream: EventStream<Int> = sourceEventStream.mapAt { multiplier ->
                        memoryCell.sample() * multiplier
                    }

                    Pair(
                        memoryCell,
                        multiplicationStream,
                    )
                }
            },
        )

        CellTestUtils.verifyAtRest(
            subjectCell = memoryCell,
            expectedValue = 0,
        )
    }

    @Test
    fun test_looped_initialSample() {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val memoryCellInitialValue: Int = TestUtils.pullSeparately(
            EventStream.loopedInMoment { loopedMultiplicationStream: EventStream<Int> ->
                loopedMultiplicationStream.holding(initialValue = 0).joinOf { memoryCell: Cell<Int> ->
                    val multiplicationStream: EventStream<Int> = sourceEventStream.mapAt { multiplier ->
                        memoryCell.sample() * multiplier
                    }

                    memoryCell.sampling.map { initialValue ->
                        Pair(
                            initialValue,
                            multiplicationStream,
                        )
                    }
                }
            },
        )

        assertEquals(
            expected = 0,
            actual = memoryCellInitialValue,
        )
    }
}
