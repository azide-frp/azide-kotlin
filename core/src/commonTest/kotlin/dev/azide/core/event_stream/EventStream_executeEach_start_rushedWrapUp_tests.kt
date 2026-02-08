package dev.azide.core.event_stream

import dev.azide.core.Action
import dev.azide.core.Effect
import dev.azide.core.EventStream
import dev.azide.core.executeEach
import dev.azide.core.test_utils.TestSlottedStimulationScenario1x3
import dev.azide.core.test_utils.TestTargetActionRecorder
import dev.azide.core.test_utils.bind
import dev.azide.core.test_utils.effect_event_stream.Effect_EventStream_start_rushedWrapUp_testUtils
import dev.azide.core.test_utils.event_stream.EventStream_expectations_testUtils
import dev.azide.core.test_utils.event_stream.TestInputEventStream
import dev.azide.core.test_utils.expectIsExecutedOnce
import dev.azide.core.test_utils.generic.ExpectedImpact
import kotlin.test.Test

@Suppress("ClassName")
class EventStream_executeEach_start_rushedWrapUp_tests {
    @Test
    fun test_start_rushedWrapUp() {
        val sourceEventStream = TestInputEventStream<Action<Int>>()

        val subjectEffect: Effect<EventStream<Int>> = sourceEventStream.executeEach()

        Effect_EventStream_start_rushedWrapUp_testUtils.executeStartTransaction(
            subjectEventStreamEffect = subjectEffect,
            expectedSubjectEmission = EventStream_expectations_testUtils.expectNoEmission(),
            expectedTargetImpact = ExpectedImpact.None,
        )
    }

    @Test
    fun test_start_rushedWrapUp_sourceEmitsSimultaneously() {
        TestSlottedStimulationScenario1x3.entries.forEach { slottedStimulationScenario ->
            test_start_rushedWrapUp_sourceEmitsSimultaneously(slottedStimulationScenario = slottedStimulationScenario)
        }
    }

    private fun test_start_rushedWrapUp_sourceEmitsSimultaneously(
        slottedStimulationScenario: TestSlottedStimulationScenario1x3,
    ) {
        val targetActionRecorder = TestTargetActionRecorder.of(result = 10)

        val sourceEventStream = TestInputEventStream<Action<Int>>()

        val subjectEffect: Effect<EventStream<Int>> = sourceEventStream.executeEach()

        Effect_EventStream_start_rushedWrapUp_testUtils.executeStartTransaction(
            subjectEventStreamEffect = subjectEffect,
            slottedInputStimulation = sourceEventStream.emit(
                emittedEvent = targetActionRecorder.recordedAction,
            ).bind(slottedStimulationScenario),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectEmission(
                expectedEmittedEvent = 10,
            ),
            expectedTargetImpact = targetActionRecorder.expectIsExecutedOnce(),
        )
    }
}
