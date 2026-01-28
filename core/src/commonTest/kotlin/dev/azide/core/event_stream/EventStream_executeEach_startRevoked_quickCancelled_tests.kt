package dev.azide.core.event_stream

import dev.azide.core.Action
import dev.azide.core.Effect
import dev.azide.core.EventStream
import dev.azide.core.executeEach
import dev.azide.core.test_utils.ExpectedImpact
import dev.azide.core.test_utils.TestSlotDispatcher1x4
import dev.azide.core.test_utils.TestSlotDispatcher2x4
import dev.azide.core.test_utils.TestTargetAction
import dev.azide.core.test_utils.bind
import dev.azide.core.test_utils.effect_generic.Effect_generic_startRevoked_quickCancelled_testUtils
import dev.azide.core.test_utils.event_stream.EventStreamTestUtils
import dev.azide.core.test_utils.event_stream.correctingEmission
import dev.azide.core.test_utils.event_stream.revokingEmission
import dev.azide.core.test_utils.expectIsNotExecuted
import kotlin.test.Test

@Suppress("ClassName")
class EventStream_executeEach_startRevoked_quickCancelled_quickCancelled_tests {
    @Test
    fun test_startRevoked_quickCancelled() {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Action<Int>>()

        val subjectEffect: Effect<EventStream<Int>> = sourceEventStream.executeEach()

        Effect_generic_startRevoked_quickCancelled_testUtils.executeStartTransaction(
            subjectEffect = subjectEffect,
            expectedTargetImpact = ExpectedImpact.None,
        )

        EventStream_executeEach_testUtils.verifyEffectNotOngoing(
            sourceEventStream = sourceEventStream,
        )
    }

    @Test
    fun test_startRevoked_quickCancelled_sourceEmitsSimultaneously() {
        TestSlotDispatcher1x4.entries.forEach { dispatcher ->
            test_startRevoked_quickCancelled_sourceEmitsSimultaneously(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_startRevoked_quickCancelled_sourceEmitsSimultaneously(
        dispatcher: TestSlotDispatcher1x4,
    ) {
        val targetAction = TestTargetAction.of(result = 10)

        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Action<Int>>()

        val subjectEffect: Effect<EventStream<Int>> = sourceEventStream.executeEach()

        Effect_generic_startRevoked_quickCancelled_testUtils.executeStartTransaction(
            subjectEffect = subjectEffect,
            slottedInputStimulation = sourceEventStream.emit(
                emittedEvent = targetAction,
            ).bind(dispatcher),
            expectedTargetImpact = ExpectedImpact.None,
        )

        EventStream_executeEach_testUtils.verifyEffectNotOngoing(
            sourceEventStream = sourceEventStream,
        )
    }

    @Test
    fun test_startRevoked_quickCancelled_sourceEmitsRevokedSimultaneously() {
        TestSlotDispatcher2x4.entries.forEach { dispatcher ->
            test_startRevoked_quickCancelled_sourceEmitsRevokedSimultaneously(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_startRevoked_quickCancelled_sourceEmitsRevokedSimultaneously(
        dispatcher: TestSlotDispatcher2x4,
    ) {
        val targetAction = TestTargetAction.of(result = 10)

        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Action<Int>>()

        val subjectEffect: Effect<EventStream<Int>> = sourceEventStream.executeEach()

        Effect_generic_startRevoked_quickCancelled_testUtils.executeStartTransaction(
            subjectEffect = subjectEffect,
            slottedInputStimulation = sourceEventStream.revokingEmission(
                emittedEvent = targetAction,
            ).bind(dispatcher),
            expectedTargetImpact = targetAction.expectIsNotExecuted(),
        )

        EventStream_executeEach_testUtils.verifyEffectNotOngoing(
            sourceEventStream = sourceEventStream,
        )
    }

    @Test
    fun test_startRevoked_quickCancelled_sourceEmitsCorrectedSimultaneously() {
        TestSlotDispatcher2x4.entries.forEach { dispatcher ->
            test_startRevoked_quickCancelled_sourceEmitsCorrectedSimultaneously(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_startRevoked_quickCancelled_sourceEmitsCorrectedSimultaneously(
        dispatcher: TestSlotDispatcher2x4,
    ) {
        val targetAction1 = TestTargetAction.of(result = 10)
        val targetAction2 = TestTargetAction.of(result = 10)

        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Action<Int>>()

        val subjectEffect: Effect<EventStream<Int>> = sourceEventStream.executeEach()

        Effect_generic_startRevoked_quickCancelled_testUtils.executeStartTransaction(
            subjectEffect = subjectEffect,
            slottedInputStimulation = sourceEventStream.correctingEmission(
                intermediateEmittedEvent = targetAction1,
                correctedEmittedEvent = targetAction2,
            ).bind(dispatcher),
            expectedTargetImpact = ExpectedImpact.combine(
                targetAction1.expectIsNotExecuted(),
                targetAction2.expectIsNotExecuted(),
            ),
        )

        EventStream_executeEach_testUtils.verifyEffectNotOngoing(
            sourceEventStream = sourceEventStream,
        )
    }

}
