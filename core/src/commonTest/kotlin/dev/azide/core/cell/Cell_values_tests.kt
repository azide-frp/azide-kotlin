package dev.azide.core.cell

import dev.azide.core.test_utils.TestInputStimulation
import dev.azide.core.test_utils.TestUtils
import dev.azide.core.test_utils.cell.CellTestUtils
import dev.azide.core.test_utils.event_stream.EventStreamTestUtils
import dev.azide.core.values
import kotlin.test.Test

@Suppress("ClassName")
class Cell_values_tests {
    @Test
    fun test_spawn() {
        val sourceCell = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        EventStreamTestUtils.spawnStatefulEventStreamExpectingEmission(
            expectedEmittedEvent = 10,
            spawn = sourceCell.values,
        )
    }

    @Test
    fun test_spawn_sourceUpdatesSimultaneously() {
        val sourceCell = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        EventStreamTestUtils.spawnStatefulEventStreamExpectingEmission(
            inputStimulation = sourceCell.update(newValue = 11),
            expectedEmittedEvent = 11,
            spawn = sourceCell.values,
        )
    }

    @Test
    fun test_sourceUpdates() {
        val sourceCell = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val subjectEventStream = TestUtils.pullSeparately(
            sourceCell.values,
        )

        EventStreamTestUtils.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = sourceCell.update(newValue = 20),
            expectedEmittedEvent = 20,
        )
    }

    @Test
    fun test_sourceUpdates_revoked() {
        val sourceCell = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val subjectEventStream = TestUtils.pullSeparately(
            sourceCell.values,
        )

        EventStreamTestUtils.verifyDoesNotEmitEffectively(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestInputStimulation.combine(
                sourceCell.update(newValue = 20),
                sourceCell.revokeUpdate(),
            ),
        )
    }

    @Test
    fun test_sourceUpdates_corrected() {
        val sourceCell = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val subjectEventStream = TestUtils.pullSeparately(
            sourceCell.values,
        )

        EventStreamTestUtils.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestInputStimulation.combine(
                sourceCell.update(newValue = 20),
                sourceCell.correctUpdate(correctedNewValue = 21)
            ),
            expectedEmittedEvent = 21,
        )
    }
}
