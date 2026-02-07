package dev.azide.core.event_stream

import dev.azide.core.Cell
import dev.azide.core.Moment
import dev.azide.core.holding
import dev.azide.core.test_utils.TestSlotDispatcher1x3
import dev.azide.core.test_utils.TestSlotDispatcher2x3
import dev.azide.core.test_utils.bind
import dev.azide.core.test_utils.cell.Cell_expectations_testUtils
import dev.azide.core.test_utils.cell.Cell_spawn_testUtils
import dev.azide.core.test_utils.event_stream.TestInputEventStream
import dev.azide.core.test_utils.event_stream.correctingEmission
import dev.azide.core.test_utils.event_stream.revokingEmission
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import kotlin.test.Test

@Suppress("ClassName")
class EventStream_hold_spawn_tests {
    @Test
    fun test_spawn() {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectMoment: Moment<Cell<Int>> = sourceEventStream.holding(initialValue = 0)

        Cell_spawn_testUtils.executeSpawnTransaction(
            subjectSpawnMoment = subjectMoment,
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectNoValueTransition(
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
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectMoment: Moment<Cell<Int>> = sourceEventStream.holding(initialValue = 0)

        Cell_spawn_testUtils.executeSpawnTransaction(
            subjectSpawnMoment = subjectMoment,
            slottedInputStimulation = sourceEventStream.emit(
                emittedEvent = 10,
            ).bind(dispatcher),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
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
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectMoment: Moment<Cell<Int>> = sourceEventStream.holding(initialValue = 0)

        Cell_spawn_testUtils.executeSpawnTransaction(
            subjectSpawnMoment = subjectMoment,
            slottedInputStimulation = sourceEventStream.revokingEmission(
                emittedEvent = 10,
            ).bind(dispatcher),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectNoValueTransition(
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
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectMoment: Moment<Cell<Int>> = sourceEventStream.holding(initialValue = 0)

        Cell_spawn_testUtils.executeSpawnTransaction(
            subjectSpawnMoment = subjectMoment,
            slottedInputStimulation = sourceEventStream.correctingEmission(
                intermediateEmittedEvent = 10,
                correctedEmittedEvent = 20,
            ).bind(dispatcher),
            expectedSubjectValueTransition = Cell_expectations_testUtils.expectValueTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldValue = 0,
                expectedNewValue = 20,
            ),
        )
    }
}
