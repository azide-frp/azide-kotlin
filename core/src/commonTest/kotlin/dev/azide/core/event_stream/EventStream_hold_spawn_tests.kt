package dev.azide.core.event_stream

import dev.azide.core.Cell
import dev.azide.core.Moment
import dev.azide.core.holding
import dev.azide.core.test_utils.Cell_expectations_testUtils
import dev.azide.core.test_utils.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.TestSlotDispatcher1x3
import dev.azide.core.test_utils.TestSlotDispatcher2x3
import dev.azide.core.test_utils.bind
import dev.azide.core.test_utils.event_stream.EventStreamTestUtils
import dev.azide.core.test_utils.event_stream.correctingEmission
import dev.azide.core.test_utils.event_stream.revokingEmission
import dev.azide.core.test_utils.stateful.StatefulTestUtils_spawn
import kotlin.test.Test

@Suppress("ClassName")
class EventStream_hold_spawn_tests {
    @Test
    fun test_spawn() {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val subjectMoment: Moment<Cell<Int>> = sourceEventStream.holding(initialValue = 0)

        StatefulTestUtils_spawn.executeSpawnTransaction(
            subjectSpawnMoment = subjectMoment,
            expectedSubjectTransition = Cell_expectations_testUtils.expectNoTransition(
                expectedUnaffectedValue = 0,
            ),
        )
    }

    @Test
    fun test_spawn_sourceEmitsSimultaneously() {
        TestSlotDispatcher1x3.entries.forEach { dispatcher ->
            test_spawn_sourceEmitsSimultaneously(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_spawn_sourceEmitsSimultaneously(
        dispatcher: TestSlotDispatcher1x3,
    ) {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val subjectMoment: Moment<Cell<Int>> = sourceEventStream.holding(initialValue = 0)

        StatefulTestUtils_spawn.executeSpawnTransaction(
            subjectSpawnMoment = subjectMoment,
            slottedInputStimulation = sourceEventStream.emit(
                emittedEvent = 10,
            ).bind(dispatcher),
            expectedSubjectTransition = Cell_expectations_testUtils.expectTransition(
                expectedOldValue = 0,
                expectedNewValue = 10,
            ),
        )
    }

    @Test
    fun test_spawn_sourceEmitsRevokedSimultaneously() {
        TestSlotDispatcher2x3.entries.forEach { dispatcher ->
            test_spawn_sourceEmitsRevokedSimultaneously(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_spawn_sourceEmitsRevokedSimultaneously(
        dispatcher: TestSlotDispatcher2x3,
    ) {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val subjectMoment: Moment<Cell<Int>> = sourceEventStream.holding(initialValue = 0)

        StatefulTestUtils_spawn.executeSpawnTransaction(
            subjectSpawnMoment = subjectMoment,
            slottedInputStimulation = sourceEventStream.revokingEmission(
                emittedEvent = 10,
            ).bind(dispatcher),
            expectedSubjectTransition = Cell_expectations_testUtils.expectNoTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedValue = 0,
            ),
        )
    }

    @Test
    fun test_spawn_sourceEmitsCorrectedSimultaneously() {
        TestSlotDispatcher2x3.entries.forEach { dispatcher ->
            test_spawn_sourceEmitsCorrectedSimultaneously(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_spawn_sourceEmitsCorrectedSimultaneously(
        dispatcher: TestSlotDispatcher2x3,
    ) {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val subjectMoment: Moment<Cell<Int>> = sourceEventStream.holding(initialValue = 0)

        StatefulTestUtils_spawn.executeSpawnTransaction(
            subjectSpawnMoment = subjectMoment,
            slottedInputStimulation = sourceEventStream.correctingEmission(
                intermediateEmittedEvent = 10,
                correctedEmittedEvent = 20,
            ).bind(dispatcher),
            expectedSubjectTransition = Cell_expectations_testUtils.expectTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldValue = 0,
                expectedNewValue = 20,
            ),
        )
    }
}
