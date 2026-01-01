package dev.azide.core.cell

import dev.azide.core.Action
import dev.azide.core.Cell
import dev.azide.core.EventStream
import dev.azide.core.Schedule
import dev.azide.core.actuate
import dev.azide.core.executeEach
import dev.azide.core.filter
import dev.azide.core.hold
import dev.azide.core.map
import dev.azide.core.test_utils.MockSideEffect
import dev.azide.core.test_utils.TestInputStimulation
import dev.azide.core.test_utils.TestUtils
import dev.azide.core.test_utils.cell.CellTestUtils
import dev.azide.core.test_utils.event_stream.EventStreamTestUtils
import dev.azide.core.triggerEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("ClassName")
class Cell_actuate_tests {
    private fun buildLoggingSchedule(
        ticker: EventStream<Int>,
        tag: Char,
    ): Pair<Schedule, List<String>> {
        val log = mutableListOf<String>()

        val schedule: Schedule = ticker.map { tickId: Int ->
            Action.wrap { log.add("$tag$tickId") }
        }.triggerEach()

        return Pair(schedule, log)
    }

    @Test
    fun test_actuate_initial() {
        val ticker = EventStreamTestUtils.createInputEventStream<Int>()

        val (schedule, log) = buildLoggingSchedule(ticker, 'A')

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = schedule,
        )

        TestUtils.executeSeparately(
            sourceCell.actuate().start,
        )

        TestUtils.stimulateSeparately(
            ticker.emit(emittedEvent = 1)
        )

        assertEquals(
            expected = listOf("A1"),
            actual = log,
        )
    }

    @Test
    fun test_actuate_initial_scheduleRunsOnStart() {
        val ticker = EventStreamTestUtils.createInputEventStream<Int>()

        val (schedule, log) = buildLoggingSchedule(ticker, 'A')

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = schedule,
        )

        TestUtils.executeSeparately(
            action = sourceCell.actuate().start,
            inputStimulation = ticker.emit(
                emittedEvent = 0,
            ),
        )

        assertEquals(
            expected = listOf("A0"),
            actual = log,
        )
    }

    @Test
    fun test_actuate_sourceUpdate_schedulesRunOnUpdate() {
        val ticker = EventStreamTestUtils.createInputEventStream<Int>()

        val (schedule1, log1) = buildLoggingSchedule(ticker, 'A')
        val (schedule2, log2) = buildLoggingSchedule(ticker, 'B')

        val sourceCell = CellTestUtils.spawnStatefulCell {
            Cell.define(
                initialValue = schedule1,
                // Update the schedule tick = 2
                newValues = ticker.filter { it == 2 }.map { schedule2 },
            )
        }

        TestUtils.executeSeparately(
            sourceCell.actuate().start,
        )

        TestUtils.stimulateSeparately(
            ticker.emit(emittedEvent = 1)
        )

        // This tick updates the ćurrent schedule from first to second
        TestUtils.stimulateSeparately(
            ticker.emit(emittedEvent = 2)
        )

        TestUtils.stimulateSeparately(
            ticker.emit(emittedEvent = 3)
        )

        assertEquals(
            expected = listOf("A1"),
            actual = log1,
        )

        assertEquals(
            expected = listOf("B2", "B3"),
            actual = log2,
        )
    }


    @Test
    fun test_actuate_sourceUpdate() {
        val ticker = EventStreamTestUtils.createInputEventStream<Int>()

        val (schedule1, log1) = buildLoggingSchedule(ticker, 'A')
        val (schedule2, log2) = buildLoggingSchedule(ticker, 'B')

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = schedule1,
        )

        TestUtils.executeSeparately(
            sourceCell.actuate().start,
        )

        TestUtils.stimulateSeparately(
            ticker.emit(emittedEvent = 1)
        )

        TestUtils.stimulateSeparately(
            sourceCell.update(
                newValue = schedule2,
            ),
        )

        TestUtils.stimulateSeparately(
            ticker.emit(emittedEvent = 2)
        )

        assertEquals(
            expected = listOf("A1"),
            actual = log1,
        )

        assertEquals(
            expected = listOf("B2"),
            actual = log2,
        )
    }

    @Test
    fun test_actuate_sourceUpdate_revoked() {
        val ticker = EventStreamTestUtils.createInputEventStream<Int>()

        val (schedule1, log1) = buildLoggingSchedule(ticker, 'A')
        val (schedule2, log2) = buildLoggingSchedule(ticker, 'B')

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = schedule1,
        )

        TestUtils.executeSeparately(
            sourceCell.actuate().start,
        )

        TestUtils.stimulateSeparately(
            ticker.emit(emittedEvent = 1)
        )

        TestUtils.stimulateSeparately(
            TestInputStimulation.combine(
                sourceCell.update(
                    newValue = schedule2,
                ),
                sourceCell.revokeUpdate(),
            )
        )

        TestUtils.stimulateSeparately(
            ticker.emit(emittedEvent = 2)
        )

        assertEquals(
            expected = listOf("A1", "A2"),
            actual = log1,
        )

        assertEquals(
            expected = emptyList(),
            actual = log2,
        )
    }

    @Test
    fun test_actuate_sourceUpdate_corrected() {
        val ticker = EventStreamTestUtils.createInputEventStream<Int>()

        val (schedule1, log1) = buildLoggingSchedule(ticker, 'A')
        val (schedule2a, log2a) = buildLoggingSchedule(ticker, 'B')
        val (schedule2b, log2b) = buildLoggingSchedule(ticker, 'C')

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = schedule1,
        )

        TestUtils.executeSeparately(
            sourceCell.actuate().start,
        )

        TestUtils.stimulateSeparately(
            ticker.emit(emittedEvent = 1)
        )

        TestUtils.stimulateSeparately(
            TestInputStimulation.combine(
                sourceCell.update(
                    newValue = schedule2a,
                ),
                sourceCell.correctUpdate(
                    correctedNewValue = schedule2b,
                ),
            )
        )

        TestUtils.stimulateSeparately(
            ticker.emit(emittedEvent = 2)
        )

        assertEquals(
            expected = listOf("A1"),
            actual = log1,
        )

        assertEquals(
            expected = emptyList(),
            actual = log2a,
        )

        assertEquals(
            expected = listOf("C2"),
            actual = log2b,
        )
    }


    @Test
    fun test_actuate_cancel() {
        val ticker = EventStreamTestUtils.createInputEventStream<Int>()

        val (schedule, log) = buildLoggingSchedule(ticker, 'A')

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = schedule,
        )

        val (_, handle) = TestUtils.executeSeparately(
            sourceCell.actuate().start,
        )

        TestUtils.stimulateSeparately(
            ticker.emit(emittedEvent = 1)
        )

        TestUtils.executeSeparately(
            handle.cancel,
        )

        TestUtils.stimulateSeparately(
            ticker.emit(emittedEvent = 2)
        )

        assertEquals(
            expected = listOf("A1"),
            actual = log,
        )
    }

    @Test
    fun test_actuate_cancel_scheduleRunsSimultaneously() {
        val ticker = EventStreamTestUtils.createInputEventStream<Int>()

        val (schedule, log) = buildLoggingSchedule(ticker, 'A')

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = schedule,
        )

        val (_, handle) = TestUtils.executeSeparately(
            sourceCell.actuate().start,
        )

        // Cancel the schedule on tick = 3
        TestUtils.executeSeparately(
            ticker.filter { it == 3 }.map { handle.cancel }.triggerEach().start,
        )

        TestUtils.stimulateSeparately(
            ticker.emit(emittedEvent = 1)
        )

        TestUtils.stimulateSeparately(
            ticker.emit(emittedEvent = 2)
        )

        // This tick should cancel the schedule
        TestUtils.stimulateSeparately(
            ticker.emit(emittedEvent = 3)
        )

        assertEquals(
            expected = listOf("A1", "A2"),
            actual = log,
        )
    }
}
