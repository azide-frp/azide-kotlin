package dev.azide.core.event_stream

import dev.azide.core.Action
import dev.azide.core.Effect
import dev.azide.core.EventStream
import dev.azide.core.executeEach
import dev.azide.core.test_utils.EventStream_expectations_testUtils
import dev.azide.core.test_utils.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.ExpectedImpact
import dev.azide.core.test_utils.TestSlotDispatcher1x4
import dev.azide.core.test_utils.TestSlotDispatcher2x4
import dev.azide.core.test_utils.TestTargetAction
import dev.azide.core.test_utils.bind
import dev.azide.core.test_utils.effect_generic.Effect_generic_start_quickCancelledRevoked_testUtils
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy
import dev.azide.core.test_utils.event_stream.EventStreamTestUtils
import dev.azide.core.test_utils.event_stream.correctingEmission
import dev.azide.core.test_utils.event_stream.revokingEmission
import dev.azide.core.test_utils.expectIsExecutedOnce
import dev.azide.core.test_utils.expectIsNotExecuted
import kotlin.test.Test

@Suppress("ClassName")
class EventStream_executeEach_start_quickCancelledRevoked_tests {
    @Test
    fun test_start_quickCancelledRevoked_subscribed() {
        test_start_quickCancelledRevoked(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
        )
    }

    @Test
    fun test_start_quickCancelledRevoked_nonSubscribed() {
        test_start_quickCancelledRevoked(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
        )
    }

    private fun test_start_quickCancelledRevoked(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
    ) {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Action<Int>>()

        val subjectEffect: Effect<EventStream<Int>> = sourceEventStream.executeEach()

        val subjectEventStream = Effect_generic_start_quickCancelledRevoked_testUtils.executeStartTransaction(
            subjectEffect = subjectEffect,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            expectedSubjectTransition = EventStream_expectations_testUtils.expectNoEmission(),
            expectedTargetImpact = ExpectedImpact.None,
        )

        EventStream_executeEach_testUtils.verifyEffectOngoing(
            sourceEventStream = sourceEventStream,
            subjectEventStream = subjectEventStream,
        )
    }

    @Test
    fun test_start_quickCancelledRevoked_sourceEmitsSimultaneously_subscribed() {
        TestSlotDispatcher1x4.entries.forEach { dispatcher ->
            test_start_quickCancelledRevoked_sourceEmitsSimultaneously(
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                dispatcher = dispatcher,
            )
        }
    }

    @Test
    fun test_start_quickCancelledRevoked_sourceEmitsSimultaneously_nonSubscribed() {
        test_start_quickCancelledRevoked_sourceEmitsSimultaneously(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            dispatcher = TestSlotDispatcher1x4.Case1,
        )
    }

    private fun test_start_quickCancelledRevoked_sourceEmitsSimultaneously(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        dispatcher: TestSlotDispatcher1x4,
    ) {
        val targetAction = TestTargetAction.of(result = 10)

        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Action<Int>>()

        val subjectEffect: Effect<EventStream<Int>> = sourceEventStream.executeEach()

        val subjectEventStream = Effect_generic_start_quickCancelledRevoked_testUtils.executeStartTransaction(
            subjectEffect = subjectEffect,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = sourceEventStream.emit(
                emittedEvent = targetAction,
            ).bind(dispatcher),
            expectedSubjectTransition = EventStream_expectations_testUtils.expectEmission(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedEmittedEvent = 10,
            ),
            expectedTargetImpact = targetAction.expectIsExecutedOnce(),
        )

        EventStream_executeEach_testUtils.verifyEffectOngoing(
            sourceEventStream = sourceEventStream,
            subjectEventStream = subjectEventStream,
        )
    }

    @Test
    fun test_start_quickCancelledRevoked_sourceEmitsRevokedSimultaneously_subscribed() {
        TestSlotDispatcher2x4.entries.forEach { dispatcher ->
            test_start_quickCancelledRevoked_sourceEmitsRevokedSimultaneously(
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                dispatcher = dispatcher,
            )
        }
    }

    @Test
    fun test_start_quickCancelledRevoked_sourceEmitsRevokedSimultaneously_nonSubscribed() {
        test_start_quickCancelledRevoked_sourceEmitsRevokedSimultaneously(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            dispatcher = TestSlotDispatcher2x4.Case11,
        )
    }

    private fun test_start_quickCancelledRevoked_sourceEmitsRevokedSimultaneously(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        dispatcher: TestSlotDispatcher2x4,
    ) {
        val targetAction = TestTargetAction.of(result = 10)

        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Action<Int>>()

        val subjectEffect: Effect<EventStream<Int>> = sourceEventStream.executeEach()

        val subjectEventStream = Effect_generic_start_quickCancelledRevoked_testUtils.executeStartTransaction(
            subjectEffect = subjectEffect,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = sourceEventStream.revokingEmission(
                emittedEvent = targetAction,
            ).bind(dispatcher),
            expectedSubjectTransition = EventStream_expectations_testUtils.expectNoEmission(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
            ),
            expectedTargetImpact = targetAction.expectIsNotExecuted(),
        )

        EventStream_executeEach_testUtils.verifyEffectOngoing(
            sourceEventStream = sourceEventStream,
            subjectEventStream = subjectEventStream,
        )
    }

    @Test
    fun test_start_quickCancelledRevoked_sourceEmitsCorrectedSimultaneously_subscribed() {
        TestSlotDispatcher2x4.entries.forEach { dispatcher ->
            test_start_quickCancelledRevoked_sourceEmitsCorrectedSimultaneously(
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                dispatcher = dispatcher,
            )
        }
    }

    @Test
    fun test_start_quickCancelledRevoked_sourceEmitsCorrectedSimultaneously_nonSubscribed() {
        test_start_quickCancelledRevoked_sourceEmitsCorrectedSimultaneously(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            dispatcher = TestSlotDispatcher2x4.Case11,
        )
    }

    private fun test_start_quickCancelledRevoked_sourceEmitsCorrectedSimultaneously(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        dispatcher: TestSlotDispatcher2x4,
    ) {
        val targetAction1 = TestTargetAction.of(result = 10)
        val targetAction2 = TestTargetAction.of(result = 20)

        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Action<Int>>()

        val subjectEffect: Effect<EventStream<Int>> = sourceEventStream.executeEach()

        val subjectEventStream = Effect_generic_start_quickCancelledRevoked_testUtils.executeStartTransaction(
            subjectEffect = subjectEffect,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = sourceEventStream.correctingEmission(
                intermediateEmittedEvent = targetAction1,
                correctedEmittedEvent = targetAction2,
            ).bind(dispatcher),
            expectedSubjectTransition = EventStream_expectations_testUtils.expectEmission(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedEmittedEvent = 20,
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetAction1.expectIsNotExecuted(),
                targetAction2.expectIsExecutedOnce(),
            ),
        )

        EventStream_executeEach_testUtils.verifyEffectOngoing(
            sourceEventStream = sourceEventStream,
            subjectEventStream = subjectEventStream,
        )
    }
}
