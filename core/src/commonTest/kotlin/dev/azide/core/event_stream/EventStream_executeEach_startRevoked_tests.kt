package dev.azide.core.event_stream

import dev.azide.core.Action
import dev.azide.core.Effect
import dev.azide.core.EventStream
import dev.azide.core.executeEach
import dev.azide.core.test_utils.TestSlottedStimulationScenario1x3
import dev.azide.core.test_utils.TestSlottedStimulationScenario2x3
import dev.azide.core.test_utils.TestTargetActionRecorder
import dev.azide.core.test_utils.bind
import dev.azide.core.test_utils.effect_event_stream.Effect_EventStream_startRevoked_testUtils
import dev.azide.core.test_utils.event_stream.TestInputEventStream
import dev.azide.core.test_utils.event_stream.correctingEmission
import dev.azide.core.test_utils.event_stream.revokingEmission
import dev.azide.core.test_utils.expectIsNotExecuted
import dev.azide.core.test_utils.generic.ExpectedImpact
import kotlin.test.Test

@Suppress("ClassName")
class EventStream_executeEach_startRevoked_tests {
    @Test
    fun test_startRevoked() {
        val sourceEventStream = TestInputEventStream<Action<Int>>()

        val subjectEffect: Effect<EventStream<Int>> = sourceEventStream.executeEach()

        Effect_EventStream_startRevoked_testUtils.executeStartTransaction(
            subjectEventStreamEffect = subjectEffect,
            expectedTargetImpact = ExpectedImpact.None,
        )
    }

    @Test
    fun test_startRevoked_sourceEmitsSimultaneously() {
        TestSlottedStimulationScenario1x3.entries.forEach { slottedStimulationScenario ->
            test_startRevoked_sourceEmitsSimultaneously(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_startRevoked_sourceEmitsSimultaneously(
        slottedStimulationScenario: TestSlottedStimulationScenario1x3,
    ) {
        val targetActionRecorder = TestTargetActionRecorder.of(result = 10)

        val sourceEventStream = TestInputEventStream<Action<Int>>()

        val subjectEffect: Effect<EventStream<Int>> = sourceEventStream.executeEach()

        Effect_EventStream_startRevoked_testUtils.executeStartTransaction(
            subjectEventStreamEffect = subjectEffect,
            slottedInputStimulation = sourceEventStream.emit(
                emittedEvent = targetActionRecorder.recordedAction,
            ).bind(slottedStimulationScenario),
            expectedTargetImpact = ExpectedImpact.None,
        )
    }

    @Test
    fun test_startRevoked_sourceEmitsRevokedSimultaneously() {
        TestSlottedStimulationScenario2x3.entries.forEach { slottedStimulationScenario ->
            test_startRevoked_sourceEmitsRevokedSimultaneously(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_startRevoked_sourceEmitsRevokedSimultaneously(
        slottedStimulationScenario: TestSlottedStimulationScenario2x3,
    ) {
        val targetActionRecorder = TestTargetActionRecorder.of(result = 10)

        val sourceEventStream = TestInputEventStream<Action<Int>>()

        val subjectEffect: Effect<EventStream<Int>> = sourceEventStream.executeEach()

        Effect_EventStream_startRevoked_testUtils.executeStartTransaction(
            subjectEventStreamEffect = subjectEffect,
            slottedInputStimulation = sourceEventStream.revokingEmission(
                emittedEvent = targetActionRecorder.recordedAction,
            ).bind(slottedStimulationScenario),
            expectedTargetImpact = targetActionRecorder.expectIsNotExecuted(),
        )
    }

    @Test
    fun test_startRevoked_sourceEmitsCorrectedSimultaneously() {
        TestSlottedStimulationScenario2x3.entries.forEach { slottedStimulationScenario ->
            test_startRevoked_sourceEmitsCorrectedSimultaneously(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_startRevoked_sourceEmitsCorrectedSimultaneously(
        slottedStimulationScenario: TestSlottedStimulationScenario2x3,
    ) {
        val targetActionRecorder1 = TestTargetActionRecorder.of(result = 10)
        val targetActionRecorder2 = TestTargetActionRecorder.of(result = 10)

        val sourceEventStream = TestInputEventStream<Action<Int>>()

        val subjectEffect: Effect<EventStream<Int>> = sourceEventStream.executeEach()

        Effect_EventStream_startRevoked_testUtils.executeStartTransaction(
            subjectEventStreamEffect = subjectEffect,
            slottedInputStimulation = sourceEventStream.correctingEmission(
                intermediateEmittedEvent = targetActionRecorder1.recordedAction,
                correctedEmittedEvent = targetActionRecorder2.recordedAction,
            ).bind(slottedStimulationScenario),
            expectedTargetImpact = ExpectedImpact.combine(
                targetActionRecorder1.expectIsNotExecuted(),
                targetActionRecorder2.expectIsNotExecuted(),
            ),
        )
    }
}
