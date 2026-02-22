package dev.azide.core.event_stream

import dev.azide.core.Action
import dev.azide.core.Effect
import dev.azide.core.EventStream
import dev.azide.core.event_stream.EventStream_executeEach_testUtils.SourceActionEventStreamTag
import dev.azide.core.executeEach
import dev.azide.core.test_utils.TestTargetActionRecorder
import dev.azide.core.test_utils.effect_event_stream.Effect_EventStream_startRevoked_quickCancelledRevoked_testUtils
import dev.azide.core.test_utils.event_stream.TestInputEventStream
import dev.azide.core.test_utils.event_stream.correctingEmission
import dev.azide.core.test_utils.event_stream.emitting
import dev.azide.core.test_utils.event_stream.revokingEmission
import dev.azide.core.test_utils.expectIsNotExecuted
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulationScenario
import dev.azide.core.test_utils.stimulation_combinatorics.bind
import kotlin.test.Test

@Suppress("ClassName", "PrivatePropertyName")
class EventStream_executeEach_startRevoked_quickCancelledRevoked_quickCancelledRevoked_tests {
    private typealias SuitableSlotCount = TestSlotCount.Count5

    private typealias SuitableTestSlottedStimulationScenario = TestSlottedStimulationScenario<SuitableSlotCount>

    private val slottedStimulationScenarioBank_sourceActionEventStreamEmits =
        EventStream_executeEach_testUtils.stimulationScenarioBank_sourceActionEventStreamEmits.distribute(slotCount = SuitableSlotCount)

    private val slottedStimulationScenarioBank_sourceActionEventStreamEmitsRevoked =
        EventStream_executeEach_testUtils.stimulationScenarioBank_sourceActionEventStreamEmitsRevoked.distribute(
            slotCount = SuitableSlotCount
        )

    private val slottedStimulationScenarioBank_sourceActionEventStreamEmitsCorrected =
        EventStream_executeEach_testUtils.stimulationScenarioBank_sourceActionEventStreamEmitsCorrected.distribute(
            slotCount = SuitableSlotCount
        )

    @Test
    fun test_startRevoked_quickCancelledRevoked() {
        val sourceEventStream = TestInputEventStream<Action<Int>>()

        val subjectEffect: Effect<EventStream<Int>> = sourceEventStream.executeEach()

        Effect_EventStream_startRevoked_quickCancelledRevoked_testUtils.testStart(
            subjectEventStreamEffect = subjectEffect,
            expectedTargetImpact = ExpectedImpact.None,
        )

        EventStream_executeEach_testUtils.verifyEffectNotOngoing(
            sourceEventStream = sourceEventStream,
        )
    }

    @Test
    fun test_startRevoked_quickCancelledRevoked_sourceEmitsSimultaneously() {
        slottedStimulationScenarioBank_sourceActionEventStreamEmits.forEach { slottedStimulationScenario ->
            test_startRevoked_quickCancelledRevoked_sourceEmitsSimultaneously(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_startRevoked_quickCancelledRevoked_sourceEmitsSimultaneously(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetActionRecorder = TestTargetActionRecorder.pure(result = 10)

        val sourceEventStream = TestInputEventStream<Action<Int>>()

        val subjectEffect: Effect<EventStream<Int>> = sourceEventStream.executeEach()

        Effect_EventStream_startRevoked_quickCancelledRevoked_testUtils.testStart(
            subjectEventStreamEffect = subjectEffect,
            slottedInputStimulation = sourceEventStream.emitting(
                tag = SourceActionEventStreamTag,
                emittedEvent = targetActionRecorder.recordedAction,
            ).bind(slottedStimulationScenario),
            expectedTargetImpact = ExpectedImpact.None,
        )

        EventStream_executeEach_testUtils.verifyEffectNotOngoing(
            sourceEventStream = sourceEventStream,
        )
    }

    @Test
    fun test_startRevoked_quickCancelledRevoked_sourceEmitsRevokedSimultaneously() {
        slottedStimulationScenarioBank_sourceActionEventStreamEmitsRevoked.forEach { slottedStimulationScenario ->
            test_startRevoked_quickCancelledRevoked_sourceEmitsRevokedSimultaneously(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_startRevoked_quickCancelledRevoked_sourceEmitsRevokedSimultaneously(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetActionRecorder = TestTargetActionRecorder.pure(result = 10)

        val sourceEventStream = TestInputEventStream<Action<Int>>()

        val subjectEffect: Effect<EventStream<Int>> = sourceEventStream.executeEach()

        Effect_EventStream_startRevoked_quickCancelledRevoked_testUtils.testStart(
            subjectEventStreamEffect = subjectEffect,
            slottedInputStimulation = sourceEventStream.revokingEmission(
                tag = SourceActionEventStreamTag,
                emittedEvent = targetActionRecorder.recordedAction,
            ).bind(slottedStimulationScenario),
            expectedTargetImpact = targetActionRecorder.expectIsNotExecuted(),
        )

        EventStream_executeEach_testUtils.verifyEffectNotOngoing(
            sourceEventStream = sourceEventStream,
        )
    }

    @Test
    fun test_startRevoked_quickCancelledRevoked_sourceEmitsCorrectedSimultaneously() {
        slottedStimulationScenarioBank_sourceActionEventStreamEmitsCorrected.forEach { slottedStimulationScenario ->
            test_startRevoked_quickCancelledRevoked_sourceEmitsCorrectedSimultaneously(
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    private fun test_startRevoked_quickCancelledRevoked_sourceEmitsCorrectedSimultaneously(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetActionRecorder1 = TestTargetActionRecorder.pure(result = 10)
        val targetActionRecorder2 = TestTargetActionRecorder.pure(result = 10)

        val sourceEventStream = TestInputEventStream<Action<Int>>()

        val subjectEffect: Effect<EventStream<Int>> = sourceEventStream.executeEach()

        Effect_EventStream_startRevoked_quickCancelledRevoked_testUtils.testStart(
            subjectEventStreamEffect = subjectEffect,
            slottedInputStimulation = sourceEventStream.correctingEmission(
                tag = SourceActionEventStreamTag,
                intermediateEmittedEvent = targetActionRecorder1.recordedAction,
                correctedEmittedEvent = targetActionRecorder2.recordedAction,
            ).bind(slottedStimulationScenario),
            expectedTargetImpact = ExpectedImpact.combine(
                targetActionRecorder1.expectIsNotExecuted(),
                targetActionRecorder2.expectIsNotExecuted(),
            ),
        )

        EventStream_executeEach_testUtils.verifyEffectNotOngoing(
            sourceEventStream = sourceEventStream,
        )
    }
}
