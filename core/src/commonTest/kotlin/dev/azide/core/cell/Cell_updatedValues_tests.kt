package dev.azide.core.cell

import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.event_stream.EventStreamTestUtils
import dev.azide.core.updatedValues
import kotlin.test.Test

@Suppress("ClassName")
class Cell_updatedValues_tests {
    @Test
    fun test_sourceUpdates() {
        val sourceCell = TestInputCell(
            initialValue = 10,
        )

        val subjectEventStream = sourceCell.updatedValues

        EventStreamTestUtils.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = sourceCell.update(newValue = 20),
            expectedEmittedEvent = 20,
        )
    }

    @Test
    fun test_sourceUpdates_revoked() {
        val sourceCell = TestInputCell(
            initialValue = 10,
        )

        val subjectEventStream = sourceCell.updatedValues

        EventStreamTestUtils.verifyDoesNotEmitEffectively(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestStimulation.combine(
                sourceCell.update(newValue = 20),
                sourceCell.revokeUpdate(),
            ),
        )
    }

    @Test
    fun test_sourceUpdates_corrected() {
        val sourceCell = TestInputCell(
            initialValue = 10,
        )

        val subjectEventStream = sourceCell.updatedValues

        EventStreamTestUtils.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestStimulation.combine(
                sourceCell.update(newValue = 20),
                sourceCell.correctUpdate(correctedNewValue = 21),
            ),
            expectedEmittedEvent = 21,
        )
    }
}
