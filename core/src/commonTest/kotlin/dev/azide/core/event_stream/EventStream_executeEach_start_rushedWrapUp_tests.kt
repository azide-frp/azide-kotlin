package dev.azide.core.event_stream

import dev.azide.core.Action
import dev.azide.core.Effect
import dev.azide.core.EventStream
import dev.azide.core.executeEach
import dev.azide.core.test_utils.EventStream_expectations_testUtils
import dev.azide.core.test_utils.ExpectedImpact
import dev.azide.core.test_utils.TestSlotDispatcher1x3
import dev.azide.core.test_utils.TestTargetActionRecorder
import dev.azide.core.test_utils.bind
import dev.azide.core.test_utils.effect_event_stream.Effect_EventStream_start_rushedWrapUp_testUtils
import dev.azide.core.test_utils.event_stream.TestInputEventStream
import dev.azide.core.test_utils.expectIsExecutedOnce
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
        TestSlotDispatcher1x3.entries.forEach { dispatcher ->
            test_start_rushedWrapUp_sourceEmitsSimultaneously(dispatcher = dispatcher)
        }
    }

    private fun test_start_rushedWrapUp_sourceEmitsSimultaneously(
        dispatcher: TestSlotDispatcher1x3,
    ) {
        val targetActionRecorder = TestTargetActionRecorder.of(result = 10)

        val sourceEventStream = TestInputEventStream<Action<Int>>()

        val subjectEffect: Effect<EventStream<Int>> = sourceEventStream.executeEach()

        Effect_EventStream_start_rushedWrapUp_testUtils.executeStartTransaction(
            subjectEventStreamEffect = subjectEffect,
            slottedInputStimulation = sourceEventStream.emit(
                emittedEvent = targetActionRecorder.recordedAction,
            ).bind(dispatcher),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectEmission(
                expectedEmittedEvent = 10,
            ),
            expectedTargetImpact = targetActionRecorder.expectIsExecutedOnce(),
        )
    }
}
