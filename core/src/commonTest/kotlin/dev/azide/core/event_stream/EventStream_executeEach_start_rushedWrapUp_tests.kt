package dev.azide.core.event_stream

import dev.azide.core.Action
import dev.azide.core.Effect
import dev.azide.core.EventStream
import dev.azide.core.event_stream.EventStream_executeEach_testUtils.SourceActionEventStreamTag
import dev.azide.core.executeEach
import dev.azide.core.test_utils.TestTargetActionRecorder
import dev.azide.core.test_utils.effect_event_stream.Effect_EventStream_start_rushedWrapUp_testUtils
import dev.azide.core.test_utils.event_stream.EventStream_expectations_testUtils
import dev.azide.core.test_utils.event_stream.TestInputEventStream
import dev.azide.core.test_utils.event_stream.emitting
import dev.azide.core.test_utils.expectIsExecutedOnce
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulationScenario
import dev.azide.core.test_utils.stimulation_combinatorics.bind
import kotlin.test.Test

@Suppress("ClassName", "PrivatePropertyName")
class EventStream_executeEach_start_rushedWrapUp_tests {
    private typealias SuitableSlotCount = TestSlotCount.Count3

    private typealias SuitableTestSlottedStimulationScenario = TestSlottedStimulationScenario<SuitableSlotCount>

    private val slottedStimulationBank_sourceActionEventStreamEmits =
        EventStream_executeEach_testUtils.stimulationBank_sourceActionEventStreamEmits.distribute(slotCount = SuitableSlotCount)

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
        slottedStimulationBank_sourceActionEventStreamEmits.forEach { slottedStimulationScenario ->
            test_start_rushedWrapUp_sourceEmitsSimultaneously(slottedStimulationScenario = slottedStimulationScenario)
        }
    }

    private fun test_start_rushedWrapUp_sourceEmitsSimultaneously(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetActionRecorder = TestTargetActionRecorder.of(result = 10)

        val sourceEventStream = TestInputEventStream<Action<Int>>()

        val subjectEffect: Effect<EventStream<Int>> = sourceEventStream.executeEach()

        Effect_EventStream_start_rushedWrapUp_testUtils.executeStartTransaction(
            subjectEventStreamEffect = subjectEffect,
            slottedInputStimulation = sourceEventStream.emitting(
                tag = SourceActionEventStreamTag,
                emittedEvent = targetActionRecorder.recordedAction,
            ).bind(slottedStimulationScenario),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectEmission(
                expectedEmittedEvent = 10,
            ),
            expectedTargetImpact = targetActionRecorder.expectIsExecutedOnce(),
        )
    }
}
