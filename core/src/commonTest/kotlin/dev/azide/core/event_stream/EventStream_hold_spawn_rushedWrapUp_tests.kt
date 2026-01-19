package dev.azide.core.event_stream

import dev.azide.core.Cell
import dev.azide.core.Moment
import dev.azide.core.holding
import dev.azide.core.test_utils.ExpectedCellReactionTestUtils
import dev.azide.core.test_utils.TestSlotDispatcher1x3
import dev.azide.core.test_utils.bind
import dev.azide.core.test_utils.event_stream.EventStreamTestUtils
import dev.azide.core.test_utils.stateful.StatefulTestUtils_spawn_rushedWrapUp
import kotlin.test.Ignore
import kotlin.test.Test

@Suppress("ClassName")
class EventStream_hold_spawn_rushedWrapUp_tests {
    @Test
    fun test_spawn_rushedWrapUp() {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val subjectMoment: Moment<Cell<Int>> = sourceEventStream.holding(initialValue = 0)

        StatefulTestUtils_spawn_rushedWrapUp.executeSpawnTransaction(
            subjectMoment = subjectMoment,
            expectedSubjectTransition = ExpectedCellReactionTestUtils.expectNoTransition(
                expectedUnaffectedValue = 0,
            ),
        )
    }

    @Test
    @Ignore // FIXME: Make this pass
    fun test_spawn_rushedWrapUp_sourceEmitsSimultaneously() {
        TestSlotDispatcher1x3.entries.forEach { dispatcher ->
            test_spawn_rushedWrapUp_sourceEmitsSimultaneously(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_spawn_rushedWrapUp_sourceEmitsSimultaneously(
        dispatcher: TestSlotDispatcher1x3,
    ) {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val subjectMoment: Moment<Cell<Int>> = sourceEventStream.holding(initialValue = 0)

        StatefulTestUtils_spawn_rushedWrapUp.executeSpawnTransaction(
            subjectMoment = subjectMoment,
            slottedInputStimulation = sourceEventStream.emit(
                emittedEvent = 10,
            ).bind(dispatcher),
            expectedSubjectTransition = ExpectedCellReactionTestUtils.expectTransition(
                expectedOldValue = 0,
                expectedNewValue = 10,
            ),
        )
    }
}
