package dev.azide.core.event_stream

import dev.azide.core.Action
import dev.azide.core.Effect
import dev.azide.core.EventStream
import dev.azide.core.executeEach
import dev.azide.core.startExternally
import dev.azide.core.test_utils.TestTargetActionRecorder
import dev.azide.core.test_utils.effect_event_stream.Effect_EventStream_step_testUtils
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy
import dev.azide.core.test_utils.event_stream.EventStream_expectations_testUtils
import dev.azide.core.test_utils.event_stream.TestInputEventStream
import dev.azide.core.test_utils.event_stream.correctingEmission_deprecated
import dev.azide.core.test_utils.event_stream.revokingEmission_deprecated
import dev.azide.core.test_utils.expectIsExecutedOnce
import dev.azide.core.test_utils.expectIsNotExecuted
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import kotlin.test.Test

@Suppress("ClassName")
class EventStream_executeEach_step_tests {
    @Test
    fun test_step_sourceEmits_subscribed() {
        test_step_sourceEmits(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
        )
    }

    @Test
    fun test_step_sourceEmits_nonSubscribed() {
        test_step_sourceEmits(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
        )
    }

    private fun test_step_sourceEmits(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
    ) {
        val targetActionRecorder = TestTargetActionRecorder.pure(result = 10)

        val sourceEventStream = TestInputEventStream<Action<Int>>()

        val subjectEffect: Effect<EventStream<Int>> = sourceEventStream.executeEach()

        val subjectEventStream = subjectEffect.startExternally().result

        Effect_EventStream_step_testUtils.testStep(
            subjectEventStream = subjectEventStream,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            inputStimulation = sourceEventStream.emit(
                emittedEvent = targetActionRecorder.recordedAction,
            ),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectEmission(
                expectedEmittedEvent = 10,
            ),
            expectedTargetImpact = targetActionRecorder.expectIsExecutedOnce(),
        )
    }

    @Test
    fun test_step_sourceEmitsRevoked_subscribed() {
        test_step_sourceEmitsRevoked(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
        )
    }

    @Test
    fun test_step_sourceEmitsRevoked_nonSubscribed() {
        test_step_sourceEmitsRevoked(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
        )
    }

    private fun test_step_sourceEmitsRevoked(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
    ) {
        val targetActionRecorder = TestTargetActionRecorder.pure(result = 10)

        val sourceEventStream = TestInputEventStream<Action<Int>>()

        val subjectEffect: Effect<EventStream<Int>> = sourceEventStream.executeEach()

        val subjectEventStream = subjectEffect.startExternally().result

        Effect_EventStream_step_testUtils.testStep(
            subjectEventStream = subjectEventStream,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            inputStimulation = sourceEventStream.revokingEmission_deprecated(
                emittedEvent = targetActionRecorder.recordedAction,
            ).joint(),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectNoEmission(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
            ),
            expectedTargetImpact = targetActionRecorder.expectIsNotExecuted(),
        )
    }

    @Test
    fun test_step_sourceEmitsCorrected_subscribed() {
        test_step_sourceEmitsCorrected(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
        )
    }

    @Test
    fun test_step_sourceEmitsCorrected_nonSubscribed() {
        test_step_sourceEmitsCorrected(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
        )
    }

    private fun test_step_sourceEmitsCorrected(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
    ) {
        val targetActionRecorder1 = TestTargetActionRecorder.pure(result = 10)
        val targetActionRecorder2 = TestTargetActionRecorder.pure(result = 20)

        val sourceEventStream = TestInputEventStream<Action<Int>>()

        val subjectEffect: Effect<EventStream<Int>> = sourceEventStream.executeEach()

        val subjectEventStream = subjectEffect.startExternally().result

        Effect_EventStream_step_testUtils.testStep(
            subjectEventStream = subjectEventStream,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            inputStimulation = sourceEventStream.correctingEmission_deprecated(
                intermediateEmittedEvent = targetActionRecorder1.recordedAction,
                correctedEmittedEvent = targetActionRecorder2.recordedAction,
            ).joint(),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectEmission(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedEmittedEvent = 20,
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetActionRecorder1.expectIsNotExecuted(),
                targetActionRecorder2.expectIsExecutedOnce(),
            ),
        )
    }
}
