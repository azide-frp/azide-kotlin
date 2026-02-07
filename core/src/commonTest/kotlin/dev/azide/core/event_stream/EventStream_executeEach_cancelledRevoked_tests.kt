package dev.azide.core.event_stream

import dev.azide.core.Action
import dev.azide.core.Effect
import dev.azide.core.EventStream
import dev.azide.core.executeEach
import dev.azide.core.startExternally
import dev.azide.core.test_utils.EventStream_expectations_testUtils
import dev.azide.core.test_utils.ExpectedImpact
import dev.azide.core.test_utils.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.TestSlotDispatcher1x3
import dev.azide.core.test_utils.TestSlotDispatcher2x3
import dev.azide.core.test_utils.TestTargetActionRecorder
import dev.azide.core.test_utils.bind
import dev.azide.core.test_utils.effect_event_stream.Effect_EventStream_cancelledRevoked_testUtils
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy
import dev.azide.core.test_utils.event_stream.TestInputEventStream
import dev.azide.core.test_utils.event_stream.correctingEmission
import dev.azide.core.test_utils.event_stream.revokingEmission
import dev.azide.core.test_utils.expectIsExecutedOnce
import dev.azide.core.test_utils.expectIsNotExecuted
import kotlin.test.Test

@Suppress("ClassName")
class EventStream_executeEach_cancelledRevoked_tests {
    @Test
    fun test_cancelledRevoked_subscribed() {
        test_cancelledRevoked(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
        )
    }

    @Test
    fun test_cancelledRevoked_nonSubscribed() {
        test_cancelledRevoked(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
        )
    }

    private fun test_cancelledRevoked(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
    ) {
        val sourceEventStream = TestInputEventStream<Action<Int>>()

        val subjectEffect: Effect<EventStream<Int>> = sourceEventStream.executeEach()

        val subjectOutcome = subjectEffect.startExternally()

        Effect_EventStream_cancelledRevoked_testUtils.executeCancelTransaction(
            subjectEffectOutcome = subjectOutcome,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            expectedSubjectEmission = EventStream_expectations_testUtils.expectNoEmission(),
            expectedTargetImpact = ExpectedImpact.None,
        )

        EventStream_executeEach_testUtils.verifyEffectOngoing(
            sourceEventStream = sourceEventStream,
            subjectEventStream = subjectOutcome.result,
        )
    }

    @Test
    fun test_cancelledRevoked_sourceEmitsSimultaneously_subscribed() {
        TestSlotDispatcher1x3.entries.forEach { dispatcher ->
            test_cancelledRevoked_sourceEmitsSimultaneously(
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                dispatcher = dispatcher,
            )
        }
    }

    @Test
    fun test_cancelledRevoked_sourceEmitsSimultaneously_nonSubscribed() {
        test_cancelledRevoked_sourceEmitsSimultaneously(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            dispatcher = TestSlotDispatcher1x3.Case2,
        )
    }

    private fun test_cancelledRevoked_sourceEmitsSimultaneously(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        dispatcher: TestSlotDispatcher1x3,
    ) {
        val targetActionRecorder = TestTargetActionRecorder.of(result = 10)

        val sourceEventStream = TestInputEventStream<Action<Int>>()

        val subjectEffect: Effect<EventStream<Int>> = sourceEventStream.executeEach()

        val subjectOutcome = subjectEffect.startExternally()

        Effect_EventStream_cancelledRevoked_testUtils.executeCancelTransaction(
            subjectEffectOutcome = subjectOutcome,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = sourceEventStream.emit(
                emittedEvent = targetActionRecorder.recordedAction,
            ).bind(dispatcher),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectEmission(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedEmittedEvent = 10,
            ),
            expectedTargetImpact = targetActionRecorder.expectIsExecutedOnce(),
        )

        EventStream_executeEach_testUtils.verifyEffectOngoing(
            sourceEventStream = sourceEventStream,
            subjectEventStream = subjectOutcome.result,
        )
    }

    @Test
    fun test_cancelledRevoked_sourceEmitsRevokedSimultaneously_subscribed() {
        TestSlotDispatcher2x3.entries.forEach { dispatcher ->
            test_cancelledRevoked_sourceEmitsRevokedSimultaneously(
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                dispatcher = dispatcher,
            )
        }
    }

    @Test
    fun test_cancelledRevoked_sourceEmitsRevokedSimultaneously_nonSubscribed() {
        test_cancelledRevoked_sourceEmitsRevokedSimultaneously(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            dispatcher = TestSlotDispatcher2x3.Case22,
        )
    }

    private fun test_cancelledRevoked_sourceEmitsRevokedSimultaneously(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        dispatcher: TestSlotDispatcher2x3,
    ) {
        val targetActionRecorder = TestTargetActionRecorder.of(result = 10)

        val sourceEventStream = TestInputEventStream<Action<Int>>()

        val subjectEffect: Effect<EventStream<Int>> = sourceEventStream.executeEach()

        val subjectOutcome = subjectEffect.startExternally()

        Effect_EventStream_cancelledRevoked_testUtils.executeCancelTransaction(
            subjectEffectOutcome = subjectOutcome,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = sourceEventStream.revokingEmission(
                emittedEvent = targetActionRecorder.recordedAction,
            ).bind(dispatcher),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectNoEmission(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
            ),
            expectedTargetImpact = targetActionRecorder.expectIsNotExecuted(),
        )

        EventStream_executeEach_testUtils.verifyEffectOngoing(
            sourceEventStream = sourceEventStream,
            subjectEventStream = subjectOutcome.result,
        )
    }

    @Test
    fun test_cancelledRevoked_sourceEmitsCorrectedSimultaneously_subscribed() {
        TestSlotDispatcher2x3.entries.forEach { dispatcher ->
            test_cancelledRevoked_sourceEmitsCorrectedSimultaneously(
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                dispatcher = dispatcher,
            )
        }
    }

    @Test
    fun test_cancelledRevoked_sourceEmitsCorrectedSimultaneously_nonSubscribed() {
        test_cancelledRevoked_sourceEmitsCorrectedSimultaneously(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            dispatcher = TestSlotDispatcher2x3.Case22,
        )
    }

    private fun test_cancelledRevoked_sourceEmitsCorrectedSimultaneously(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        dispatcher: TestSlotDispatcher2x3,
    ) {
        val targetActionRecorder1 = TestTargetActionRecorder.of(result = 10)
        val targetActionRecorder2 = TestTargetActionRecorder.of(result = 20)

        val sourceEventStream = TestInputEventStream<Action<Int>>()

        val subjectEffect: Effect<EventStream<Int>> = sourceEventStream.executeEach()

        val subjectOutcome = subjectEffect.startExternally()

        Effect_EventStream_cancelledRevoked_testUtils.executeCancelTransaction(
            subjectEffectOutcome = subjectOutcome,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = sourceEventStream.correctingEmission(
                intermediateEmittedEvent = targetActionRecorder1.recordedAction,
                correctedEmittedEvent = targetActionRecorder2.recordedAction,
            ).bind(dispatcher),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectEmission(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedEmittedEvent = 20,
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetActionRecorder1.expectIsNotExecuted(),
                targetActionRecorder2.expectIsExecutedOnce(),
            ),
        )

        EventStream_executeEach_testUtils.verifyEffectOngoing(
            sourceEventStream = sourceEventStream,
            subjectEventStream = subjectOutcome.result,
        )
    }
}
