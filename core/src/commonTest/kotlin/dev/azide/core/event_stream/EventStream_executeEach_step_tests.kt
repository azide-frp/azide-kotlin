package dev.azide.core.event_stream

import dev.azide.core.Action
import dev.azide.core.Effect
import dev.azide.core.EventStream
import dev.azide.core.executeEach
import dev.azide.core.startExternally
import dev.azide.core.test_utils.ExpectedEventStreamReactionTestUtils
import dev.azide.core.test_utils.ExpectedTestSubjectReaction
import dev.azide.core.test_utils.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.ExpectedTestTargetImpact
import dev.azide.core.test_utils.TestSlotDispatcher2x2
import dev.azide.core.test_utils.TestTargetAction
import dev.azide.core.test_utils.effects.EffectTestUtils_step
import dev.azide.core.test_utils.effects.TestSubjectPerceptionStrategy
import dev.azide.core.test_utils.event_stream.EventStreamTestUtils
import dev.azide.core.test_utils.event_stream.correctingEmission
import dev.azide.core.test_utils.event_stream.revokingEmission
import dev.azide.core.test_utils.expectIsExecutedOnce
import dev.azide.core.test_utils.expectIsNotExecuted
import kotlin.test.Test

@Suppress("ClassName")
class EventStream_executeEach_step_tests {
    @Test
    fun test_step_sourceEmitsSimultaneously_subscribed() {
        test_step_sourceEmitsSimultaneously(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
        )
    }

    @Test
    fun test_step_sourceEmitsSimultaneously_nonSubscribed() {
        test_step_sourceEmitsSimultaneously(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
        )
    }

    private fun test_step_sourceEmitsSimultaneously(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
    ) {
        val targetAction = TestTargetAction.of(result = 10)

        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Action<Int>>()

        val subjectEffect: Effect<EventStream<Int>> = sourceEventStream.executeEach()

        val subjectEventStream = subjectEffect.startExternally().result

        EffectTestUtils_step.executeStepTransaction(
            subject = subjectEventStream,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            inputStimulation = sourceEventStream.emit(
                emittedEvent = targetAction,
            ),
            expectedSubjectTransition = ExpectedEventStreamReactionTestUtils.expectEmission(
                expectedEmittedEvent = 10,
            ),
            expectedTargetImpact = targetAction.expectIsExecutedOnce(),
        )
    }

    @Test
    fun test_step_sourceEmitsRevokedSimultaneously_subscribed() {
        test_step_sourceEmitsRevokedSimultaneously(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
        )
    }

    @Test
    fun test_step_sourceEmitsRevokedSimultaneously_nonSubscribed() {
        test_step_sourceEmitsRevokedSimultaneously(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
        )
    }

    private fun test_step_sourceEmitsRevokedSimultaneously(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
    ) {
        val targetAction = TestTargetAction.of(result = 10)

        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Action<Int>>()

        val subjectEffect: Effect<EventStream<Int>> = sourceEventStream.executeEach()

        val subjectEventStream = subjectEffect.startExternally().result

        EffectTestUtils_step.executeStepTransaction(
            subject = subjectEventStream,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            inputStimulation = sourceEventStream.revokingEmission(
                emittedEvent = targetAction,
            ).joint(),
            expectedSubjectTransition = ExpectedEventStreamReactionTestUtils.expectNoEmission(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
            ),
            expectedTargetImpact = targetAction.expectIsNotExecuted(),
        )
    }

    @Test
    fun test_step_sourceEmitsCorrectedSimultaneously_subscribed() {
        test_step_sourceEmitsCorrectedSimultaneously(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
        )
    }

    @Test
    fun test_step_sourceEmitsCorrectedSimultaneously_nonSubscribed() {
        test_step_sourceEmitsCorrectedSimultaneously(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
        )
    }

    private fun test_step_sourceEmitsCorrectedSimultaneously(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
    ) {
        val targetAction1 = TestTargetAction.of(result = 10)
        val targetAction2 = TestTargetAction.of(result = 20)

        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Action<Int>>()

        val subjectEffect: Effect<EventStream<Int>> = sourceEventStream.executeEach()

        val subjectEventStream = subjectEffect.startExternally().result

        EffectTestUtils_step.executeStepTransaction(
            subject = subjectEventStream,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            inputStimulation = sourceEventStream.correctingEmission(
                intermediateEmittedEvent = targetAction1,
                correctedEmittedEvent = targetAction2,
            ).joint(),
            expectedSubjectTransition = ExpectedEventStreamReactionTestUtils.expectEmission(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedEmittedEvent = 20,
            ),
            expectedTargetImpact = ExpectedTestTargetImpact.combine(
                targetAction1.expectIsNotExecuted(),
                targetAction2.expectIsExecutedOnce(),
            ),
        )
    }
}
