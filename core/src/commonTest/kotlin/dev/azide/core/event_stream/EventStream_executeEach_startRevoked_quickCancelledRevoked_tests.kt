package dev.azide.core.event_stream

import dev.azide.core.Action
import dev.azide.core.Effect
import dev.azide.core.EventStream
import dev.azide.core.executeEach
import dev.azide.core.test_utils.ExpectedTestSubjectTransition
import dev.azide.core.test_utils.ExpectedTestTargetImpact
import dev.azide.core.test_utils.TestSlotDispatcher1x5
import dev.azide.core.test_utils.TestSlotDispatcher2x5
import dev.azide.core.test_utils.TestTargetAction
import dev.azide.core.test_utils.bind
import dev.azide.core.test_utils.effects.EffectTestUtils_startRevoked_quickCancelledRevoked
import dev.azide.core.test_utils.effects.EffectTestUtils_step
import dev.azide.core.test_utils.effects.TestSubjectPerceptionStrategy
import dev.azide.core.test_utils.event_stream.EventStreamTestUtils
import dev.azide.core.test_utils.event_stream.TestInputEventStream
import dev.azide.core.test_utils.event_stream.correctingEmission
import dev.azide.core.test_utils.event_stream.revokingEmission
import dev.azide.core.test_utils.expectIsNotExecuted
import kotlin.test.Test

@Suppress("ClassName")
class EventStream_executeEach_startRevoked_quickCancelledRevoked_quickCancelledRevoked_tests {
    @Test
    fun test_startRevoked_quickCancelledRevoked() {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Action<Int>>()

        val subjectEffect: Effect<EventStream<Int>> = sourceEventStream.executeEach()

        EffectTestUtils_startRevoked_quickCancelledRevoked.executeStartTransaction(
            subjectEffect = subjectEffect,
            expectedTargetImpact = ExpectedTestTargetImpact.None,
        )

        EventStream_executeEach_testUtils.verifyEffectNotOngoing(
            sourceEventStream = sourceEventStream,
        )
    }

    @Test
    fun test_startRevoked_quickCancelledRevoked_sourceEmitsSimultaneously() {
        TestSlotDispatcher1x5.entries.forEach { dispatcher ->
            test_startRevoked_quickCancelledRevoked_sourceEmitsSimultaneously(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_startRevoked_quickCancelledRevoked_sourceEmitsSimultaneously(
        dispatcher: TestSlotDispatcher1x5,
    ) {
        val targetAction = TestTargetAction.of(result = 10)

        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Action<Int>>()

        val subjectEffect: Effect<EventStream<Int>> = sourceEventStream.executeEach()

        EffectTestUtils_startRevoked_quickCancelledRevoked.executeStartTransaction(
            subjectEffect = subjectEffect,
            slottedInputStimulation = sourceEventStream.emit(
                emittedEvent = targetAction,
            ).bind(dispatcher),
            expectedTargetImpact = ExpectedTestTargetImpact.None,
        )

        EventStream_executeEach_testUtils.verifyEffectNotOngoing(
            sourceEventStream = sourceEventStream,
        )
    }

    @Test
    fun test_startRevoked_quickCancelledRevoked_sourceEmitsRevokedSimultaneously() {
        TestSlotDispatcher2x5.entries.forEach { dispatcher ->
            test_startRevoked_quickCancelledRevoked_sourceEmitsRevokedSimultaneously(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_startRevoked_quickCancelledRevoked_sourceEmitsRevokedSimultaneously(
        dispatcher: TestSlotDispatcher2x5,
    ) {
        val targetAction = TestTargetAction.of(result = 10)

        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Action<Int>>()

        val subjectEffect: Effect<EventStream<Int>> = sourceEventStream.executeEach()

        EffectTestUtils_startRevoked_quickCancelledRevoked.executeStartTransaction(
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
    fun test_startRevoked_quickCancelledRevoked_sourceEmitsCorrectedSimultaneously() {
        TestSlotDispatcher2x5.entries.forEach { dispatcher ->
            test_startRevoked_quickCancelledRevoked_sourceEmitsCorrectedSimultaneously(
                dispatcher = dispatcher,
            )
        }
    }

    private fun test_startRevoked_quickCancelledRevoked_sourceEmitsCorrectedSimultaneously(
        dispatcher: TestSlotDispatcher2x5,
    ) {
        val targetAction1 = TestTargetAction.of(result = 10)
        val targetAction2 = TestTargetAction.of(result = 10)

        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Action<Int>>()

        val subjectEffect: Effect<EventStream<Int>> = sourceEventStream.executeEach()

        EffectTestUtils_startRevoked_quickCancelledRevoked.executeStartTransaction(
            subjectEffect = subjectEffect,
            slottedInputStimulation = sourceEventStream.correctingEmission(
                intermediateEmittedEvent = targetAction1,
                correctedEmittedEvent = targetAction2,
            ).bind(dispatcher),
            expectedTargetImpact = ExpectedTestTargetImpact.combine(
                targetAction1.expectIsNotExecuted(),
                targetAction2.expectIsNotExecuted(),
            ),
        )

        EventStream_executeEach_testUtils.verifyEffectNotOngoing(
            sourceEventStream = sourceEventStream,
        )
    }
}
