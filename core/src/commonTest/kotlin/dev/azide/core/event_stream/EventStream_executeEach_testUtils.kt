package dev.azide.core.event_stream

import dev.azide.core.Action
import dev.azide.core.EventStream
import dev.azide.core.test_utils.TestTargetActionRecorder
import dev.azide.core.test_utils.effect_event_stream.Effect_EventStream_step_testUtils
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy
import dev.azide.core.test_utils.event_stream.EventStream_expectations_testUtils
import dev.azide.core.test_utils.event_stream.TestInputEventStream
import dev.azide.core.test_utils.event_stream.TestInputEventStreamTag
import dev.azide.core.test_utils.expectIsExecutedOnce
import dev.azide.core.test_utils.expectIsNotExecuted
import dev.azide.core.test_utils.generic.generic_testUtils
import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationScenarioBank_deprecated

@Suppress("ClassName")
data object EventStream_executeEach_testUtils {
    data object SourceActionEventStreamTag : TestInputEventStreamTag

    val stimulationScenarioBank_sourceActionEventStreamEmits = TestStimulationScenarioBank_deprecated.mixAll(
        TestInputEventStreamTag.emissionScenario(
            inputEventStreamTag = SourceActionEventStreamTag,
        ),
    )

    val stimulationScenarioBank_sourceActionEventStreamEmitsRevoked = TestStimulationScenarioBank_deprecated.mixAll(
        TestInputEventStreamTag.revokedEmissionScenario(
            inputEventStreamTag = SourceActionEventStreamTag,
        ),
    )

    val stimulationScenarioBank_sourceActionEventStreamEmitsCorrected = TestStimulationScenarioBank_deprecated.mixAll(
        TestInputEventStreamTag.correctedEmissionScenario(
            inputEventStreamTag = SourceActionEventStreamTag,
        ),
    )

    fun verifyEffectNotOngoing(
        sourceEventStream: TestInputEventStream<Action<Int>>,
        subjectEventStream: EventStream<Int>,
    ) {
        val targetActionRecorder = TestTargetActionRecorder.pure(result = -1)

        Effect_EventStream_step_testUtils.testStep(
            subjectEventStream = subjectEventStream,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
            inputStimulation = sourceEventStream.emit(
                emittedEvent = targetActionRecorder.recordedAction,
            ),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectNoEmission(),
            expectedTargetImpact = targetActionRecorder.expectIsNotExecuted(),
        )
    }

    fun verifyEffectNotOngoing(
        sourceEventStream: TestInputEventStream<Action<Int>>,
    ) {
        val targetActionRecorder = TestTargetActionRecorder.pure(result = -1)

        generic_testUtils.executeTransactionWithImpactVerification(
            inputStimulation = sourceEventStream.emit(
                emittedEvent = targetActionRecorder.recordedAction,
            ),
            expectedTargetImpact = targetActionRecorder.expectIsNotExecuted(),
        )
    }

    fun verifyEffectOngoing(
        sourceEventStream: TestInputEventStream<Action<Int>>,
        subjectEventStream: EventStream<Int>,
    ) {
        val targetActionRecorder = TestTargetActionRecorder.pure(result = 0)

        Effect_EventStream_step_testUtils.testStep(
            subjectEventStream = subjectEventStream,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
            inputStimulation = sourceEventStream.emit(
                emittedEvent = targetActionRecorder.recordedAction,
            ),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectEmission(
                expectedEmittedEvent = 0,
            ),
            expectedTargetImpact = targetActionRecorder.expectIsExecutedOnce(),
        )
    }
}
