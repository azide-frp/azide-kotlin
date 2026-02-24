package dev.azide.core.event_stream

import dev.azide.core.Cell
import dev.azide.core.Moment
import dev.azide.core.holding
import dev.azide.core.pullExternally
import dev.azide.core.test_utils.cell.Cell_expectations_testUtils
import dev.azide.core.test_utils.cell.Cell_reaction_nonPerceived_testUtils
import dev.azide.core.test_utils.event_stream.TestInputEventStream
import kotlin.test.Test

@Suppress("ClassName")
class EventStream_hold_reaction_nonObserved_tests {
    @Test
    fun test_reaction_sourceUpdates() {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectMoment: Moment<Cell<Int>> = sourceEventStream.holding(initialValue = 0)

        val subjectCell = subjectMoment.pullExternally()

        Cell_reaction_nonPerceived_testUtils.testReaction(
            subjectCell = subjectCell,
            inputStimulation = sourceEventStream.emit(
                emittedEvent = 10,
            ),
            expectedOldSubjectValue = Cell_expectations_testUtils.expectStableValue(
                expectedValue = 0,
            ),
            expectedNewSubjectValue = Cell_expectations_testUtils.expectStableValue(
                expectedValue = 10,
            ),
        )
    }
}
