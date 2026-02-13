package dev.azide.core.event_stream

import dev.azide.core.Action
import dev.azide.core.Effect
import dev.azide.core.EventStream
import dev.azide.core.event_stream.EventStream_executeEach_testUtils.SourceActionEventStreamTag
import dev.azide.core.executeEach
import dev.azide.core.test_utils.TestTargetActionRecorder
import dev.azide.core.test_utils.effect_event_stream.Effect_EventStream_start_quickCancelled_testUtils
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy
import dev.azide.core.test_utils.event_stream.EventStream_expectations_testUtils
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
class EventStream_executeEach_start_quickCancelled_tests {
    private typealias SuitableSlotCount = TestSlotCount.Count3

    private typealias SuitableTestSlottedStimulationScenario = TestSlottedStimulationScenario<SuitableSlotCount>

    private val slottedStimulationScenarioBank_sourceActionEventStreamEmits =
        EventStream_executeEach_testUtils.stimulationScenarioBank_sourceActionEventStreamEmits.distribute(slotCount = SuitableSlotCount)

    private val arbitrarySlottedStimulationScenario_sourceActionEventStreamEmits =
        slottedStimulationScenarioBank_sourceActionEventStreamEmits.get(0)

    private val slottedStimulationScenarioBank_sourceActionEventStreamEmitsRevoked =
        EventStream_executeEach_testUtils.stimulationScenarioBank_sourceActionEventStreamEmitsRevoked.distribute(
            slotCount = SuitableSlotCount
        )

    private val arbitrarySlottedStimulationScenario_sourceActionEventStreamEmitsRevoked =
        slottedStimulationScenarioBank_sourceActionEventStreamEmitsRevoked.get(0)

    private val slottedStimulationScenarioBank_sourceActionEventStreamEmitsCorrected =
        EventStream_executeEach_testUtils.stimulationScenarioBank_sourceActionEventStreamEmitsCorrected.distribute(
            slotCount = SuitableSlotCount
        )

    private val arbitrarySlottedStimulationScenario_sourceActionEventStreamEmitsCorrected =
        slottedStimulationScenarioBank_sourceActionEventStreamEmitsCorrected.get(0)

    @Test
    fun test_start_quickCancelled_subscribed() {
        test_start_quickCancelled(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
        )
    }

    @Test
    fun test_start_quickCancelled_nonSubscribed() {
        test_start_quickCancelled(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
        )
    }

    @Test
    fun test_start_quickCancelled_twice() {
        test_start_quickCancelled(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
            cancelCount = 2,
        )
    }

    private fun test_start_quickCancelled(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        cancelCount: Int = 1,
    ) {
        val sourceEventStream = TestInputEventStream<Action<Int>>()

        val subjectEffect: Effect<EventStream<Int>> = sourceEventStream.executeEach()

        val subjectEventStream = Effect_EventStream_start_quickCancelled_testUtils.executeStartTransaction(
            subjectEventStreamEffect = subjectEffect,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            expectedSubjectEmission = EventStream_expectations_testUtils.expectNoEmission(),
            expectedTargetImpact = ExpectedImpact.None,
            cancelCount = cancelCount,
        )

        EventStream_executeEach_testUtils.verifyEffectNotOngoing(
            sourceEventStream = sourceEventStream,
            subjectEventStream = subjectEventStream,
        )
    }

    @Test
    fun test_start_quickCancelled_sourceEmitsSimultaneously_subscribed() {
        slottedStimulationScenarioBank_sourceActionEventStreamEmits.forEach { slottedStimulationScenario ->
            test_start_quickCancelled_sourceEmitsSimultaneously(
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    @Test
    fun test_start_quickCancelled_sourceEmitsSimultaneously_nonSubscribed() {
        test_start_quickCancelled_sourceEmitsSimultaneously(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            slottedStimulationScenario = arbitrarySlottedStimulationScenario_sourceActionEventStreamEmits,
        )
    }

    private fun test_start_quickCancelled_sourceEmitsSimultaneously(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetActionRecorder = TestTargetActionRecorder.pure(result = 10)

        val sourceEventStream = TestInputEventStream<Action<Int>>()

        val subjectEffect: Effect<EventStream<Int>> = sourceEventStream.executeEach()

        val subjectEventStream = Effect_EventStream_start_quickCancelled_testUtils.executeStartTransaction(
            subjectEventStreamEffect = subjectEffect,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = sourceEventStream.emitting(
                tag = SourceActionEventStreamTag,
                emittedEvent = targetActionRecorder.recordedAction,
            ).bind(slottedStimulationScenario),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectNoEmission(),
            expectedTargetImpact = targetActionRecorder.expectIsNotExecuted(),
        )

        EventStream_executeEach_testUtils.verifyEffectNotOngoing(
            sourceEventStream = sourceEventStream,
            subjectEventStream = subjectEventStream,
        )
    }

    @Test
    fun test_start_quickCancelled_sourceEmitsRevokedSimultaneously_subscribed() {
        slottedStimulationScenarioBank_sourceActionEventStreamEmits.forEach { slottedStimulationScenario ->
            test_start_quickCancelled_sourceEmitsRevokedSimultaneously(
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    @Test
    fun test_start_quickCancelled_sourceEmitsRevokedSimultaneously_nonSubscribed() {
        test_start_quickCancelled_sourceEmitsRevokedSimultaneously(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            slottedStimulationScenario = arbitrarySlottedStimulationScenario_sourceActionEventStreamEmitsRevoked,
        )
    }

    private fun test_start_quickCancelled_sourceEmitsRevokedSimultaneously(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetActionRecorder = TestTargetActionRecorder.pure(result = 10)

        val sourceEventStream = TestInputEventStream<Action<Int>>()

        val subjectEffect: Effect<EventStream<Int>> = sourceEventStream.executeEach()

        val subjectEventStream = Effect_EventStream_start_quickCancelled_testUtils.executeStartTransaction(
            subjectEventStreamEffect = subjectEffect,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = sourceEventStream.revokingEmission(
                tag = SourceActionEventStreamTag,
                emittedEvent = targetActionRecorder.recordedAction,
            ).bind(slottedStimulationScenario),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectNoEmission(),
            expectedTargetImpact = targetActionRecorder.expectIsNotExecuted(),
        )

        EventStream_executeEach_testUtils.verifyEffectNotOngoing(
            sourceEventStream = sourceEventStream,
            subjectEventStream = subjectEventStream,
        )
    }

    @Test
    fun test_start_quickCancelled_sourceEmitsCorrectedSimultaneously_subscribed() {
        slottedStimulationScenarioBank_sourceActionEventStreamEmits.forEach { slottedStimulationScenario ->
            test_start_quickCancelled_sourceEmitsCorrectedSimultaneously(
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    @Test
    fun test_start_quickCancelled_sourceEmitsCorrectedSimultaneously_nonSubscribed() {
        test_start_quickCancelled_sourceEmitsCorrectedSimultaneously(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            slottedStimulationScenario = arbitrarySlottedStimulationScenario_sourceActionEventStreamEmitsCorrected,
        )
    }

    private fun test_start_quickCancelled_sourceEmitsCorrectedSimultaneously(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetActionRecorder1 = TestTargetActionRecorder.pure(result = 10)
        val targetActionRecorder2 = TestTargetActionRecorder.pure(result = 20)

        val sourceEventStream = TestInputEventStream<Action<Int>>()

        val subjectEffect: Effect<EventStream<Int>> = sourceEventStream.executeEach()

        val subjectEventStream = Effect_EventStream_start_quickCancelled_testUtils.executeStartTransaction(
            subjectEventStreamEffect = subjectEffect,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = sourceEventStream.correctingEmission(
                tag = SourceActionEventStreamTag,
                intermediateEmittedEvent = targetActionRecorder1.recordedAction,
                correctedEmittedEvent = targetActionRecorder2.recordedAction,
            ).bind(slottedStimulationScenario),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectNoEmission(),
            expectedTargetImpact = ExpectedImpact.combine(
                targetActionRecorder1.expectIsNotExecuted(),
                targetActionRecorder2.expectIsNotExecuted(),
            ),
        )

        EventStream_executeEach_testUtils.verifyEffectNotOngoing(
            sourceEventStream = sourceEventStream,
            subjectEventStream = subjectEventStream,
        )
    }
}
