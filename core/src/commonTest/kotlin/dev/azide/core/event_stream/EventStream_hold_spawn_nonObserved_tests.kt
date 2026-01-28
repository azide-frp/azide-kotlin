package dev.azide.core.event_stream

import dev.azide.core.Cell
import dev.azide.core.Moment
import dev.azide.core.holding
import dev.azide.core.test_utils.Cell_expectations_testUtils
import dev.azide.core.test_utils.TestSlotDispatcher1x2
import dev.azide.core.test_utils.bind
import dev.azide.core.test_utils.event_stream.EventStreamTestUtils
import dev.azide.core.test_utils.stateful.StatefulTestUtils_spawn_nonPerceived
import kotlin.test.Test

@Suppress("ClassName")
class EventStream_hold_spawn_nonObserved_tests {
    @Test
    fun test_spawn() {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val subjectMoment: Moment<Cell<Int>> = sourceEventStream.holding(initialValue = 0)

        val expectedUnaffectedState = Cell_expectations_testUtils.expectStableValue(
            expectedStableValue = 0,
        )

        StatefulTestUtils_spawn_nonPerceived.executeSpawnTransaction(
            subjectMoment = subjectMoment,
            expectedOldState = expectedUnaffectedState,
            expectedNewState = expectedUnaffectedState,
        )
    }

    @Test
    fun test_spawn_sourceEmitsSimultaneously() {
        TestSlotDispatcher1x2.entries.forEach { dispatcher ->
            test_spawn_sourceEmitsSimultaneously(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_spawn_sourceEmitsSimultaneously(
        dispatcher: TestSlotDispatcher1x2,
    ) {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val subjectMoment: Moment<Cell<Int>> = sourceEventStream.holding(initialValue = 0)

        StatefulTestUtils_spawn_nonPerceived.executeSpawnTransaction(
            subjectMoment = subjectMoment,
            slottedInputStimulation = sourceEventStream.emit(
                emittedEvent = 10,
            ).bind(dispatcher),
            expectedOldState = Cell_expectations_testUtils.expectStableValue(
                expectedStableValue = 0,
            ),
            expectedNewState = Cell_expectations_testUtils.expectStableValue(
                expectedStableValue = 10,
            ),
        )
    }
}
