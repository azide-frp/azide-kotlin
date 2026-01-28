package dev.azide.core.event_stream

import dev.azide.core.Action
import dev.azide.core.EventStream
import dev.azide.core.test_utils.EventStream_expectations_testUtils
import dev.azide.core.test_utils.ExpectedTestSubjectTransition
import dev.azide.core.test_utils.TestTargetAction
import dev.azide.core.test_utils.effect_generic.Effect_generic_step_testUtils
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy
import dev.azide.core.test_utils.event_stream.TestInputEventStream
import dev.azide.core.test_utils.expectIsExecutedOnce
import dev.azide.core.test_utils.expectIsNotExecuted

@Suppress("ClassName")
data object EventStream_executeEach_testUtils {
    fun verifyEffectNotOngoing(
        sourceEventStream: TestInputEventStream<Action<Int>>,
        subjectEventStream: EventStream<Int>,
    ) {
        val targetAction = TestTargetAction.Companion.of(result = -1)

        Effect_generic_step_testUtils.executeStepTransaction(
            subject = subjectEventStream,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
            inputStimulation = sourceEventStream.emit(
                emittedEvent = targetAction,
            ),
            expectedSubjectTransition = EventStream_expectations_testUtils.expectNoEmission(),
            expectedTargetImpact = targetAction.expectIsNotExecuted(),
        )
    }

    fun verifyEffectNotOngoing(
        sourceEventStream: TestInputEventStream<Action<Int>>,
    ) {
        val targetAction = TestTargetAction.of(result = -1)

        Effect_generic_step_testUtils.executeStepTransaction(
            subject = Unit,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
            inputStimulation = sourceEventStream.emit(
                emittedEvent = targetAction,
            ),
            expectedSubjectTransition = ExpectedTestSubjectTransition.None,
            expectedTargetImpact = targetAction.expectIsNotExecuted(),
        )
    }

    fun verifyEffectOngoing(
        sourceEventStream: TestInputEventStream<Action<Int>>,
        subjectEventStream: EventStream<Int>,
    ) {
        val targetAction = TestTargetAction.of(result = 0)

        Effect_generic_step_testUtils.executeStepTransaction(
            subject = subjectEventStream,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
            inputStimulation = sourceEventStream.emit(
                emittedEvent = targetAction,
            ),
            expectedSubjectTransition = EventStream_expectations_testUtils.expectEmission(
                expectedEmittedEvent = 0,
            ),
            expectedTargetImpact = targetAction.expectIsExecutedOnce(),
        )
    }
}
