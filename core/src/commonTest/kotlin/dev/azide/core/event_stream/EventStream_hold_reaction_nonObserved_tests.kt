package dev.azide.core.event_stream

import dev.azide.core.Cell
import dev.azide.core.Moment
import dev.azide.core.holding
import dev.azide.core.pullExternally
import dev.azide.core.test_utils.ExpectedCellReactionTestUtils
import dev.azide.core.test_utils.event_stream.EventStreamTestUtils
import dev.azide.core.test_utils.stateful.StatefulTestUtils_reaction_nonPerceived
import kotlin.test.Test

@Suppress("ClassName")
class EventStream_hold_reaction_nonObserved_tests {
    @Test
    fun test_reaction_sourceUpdates() {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val subjectMoment: Moment<Cell<Int>> = sourceEventStream.holding(initialValue = 0)

        val subjectCell = subjectMoment.pullExternally()

        StatefulTestUtils_reaction_nonPerceived.executeReactionTransaction(
            subject = subjectCell,
            inputStimulation = sourceEventStream.emit(
                emittedEvent = 10,
            ),
            expectedOldState = ExpectedCellReactionTestUtils.expectStableValue(
                expectedStableValue = 0,
            ),
            expectedNewState = ExpectedCellReactionTestUtils.expectStableValue(
                expectedStableValue = 10,
            ),
        )
    }
}
