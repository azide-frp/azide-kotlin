package dev.azide.core.event_stream

import dev.azide.core.Action
import dev.azide.core.Effect
import dev.azide.core.EventStream
import dev.azide.core.event_stream.EventStream_executeEach_testUtils.SourceActionEventStreamTag
import dev.azide.core.executeEach
import dev.azide.core.startExternally
import dev.azide.core.test_utils.TestTargetActionRecorder
import dev.azide.core.test_utils.effect_event_stream.Effect_EventStream_cancelledRevoked_testUtils
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy
import dev.azide.core.test_utils.event_stream.EventStream_expectations_testUtils
import dev.azide.core.test_utils.event_stream.TestInputEventStream
import dev.azide.core.test_utils.event_stream.correctingEmission
import dev.azide.core.test_utils.event_stream.emitting
import dev.azide.core.test_utils.event_stream.revokingEmission
import dev.azide.core.test_utils.expectIsExecutedOnce
import dev.azide.core.test_utils.expectIsNotExecuted
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulationScenario
import dev.azide.core.test_utils.stimulation_combinatorics.bind
import kotlin.test.Test

@Suppress("ClassName", "PrivatePropertyName")
class EventStream_executeEach_cancelledRevoked_tests {
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
    fun test_cancelledRevoked_subscribed() {
        test_cancelledRevoked(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
        )
    }

    @Test
    fun test_cancelledRevoked_nonSubscribed() {
        test_cancelledRevoked(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
        )
    }

    private fun test_cancelledRevoked(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
    ) {
        val sourceEventStream = TestInputEventStream<Action<Int>>()

        val subjectEffect: Effect<EventStream<Int>> = sourceEventStream.executeEach()

        val subjectOutcome = subjectEffect.startExternally()

        Effect_EventStream_cancelledRevoked_testUtils.executeCancelTransaction(
            subjectEffectOutcome = subjectOutcome,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            expectedSubjectEmission = EventStream_expectations_testUtils.expectNoEmission(),
            expectedTargetImpact = ExpectedImpact.None,
        )

        EventStream_executeEach_testUtils.verifyEffectOngoing(
            sourceEventStream = sourceEventStream,
            subjectEventStream = subjectOutcome.result,
        )
    }

    @Test
    fun test_cancelledRevoked_sourceEmitsSimultaneously_subscribed() {
        slottedStimulationScenarioBank_sourceActionEventStreamEmits.forEach { slottedStimulationScenario ->
            test_cancelledRevoked_sourceEmitsSimultaneously(
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    @Test
    fun test_cancelledRevoked_sourceEmitsSimultaneously_nonSubscribed() {
        test_cancelledRevoked_sourceEmitsSimultaneously(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            slottedStimulationScenario = arbitrarySlottedStimulationScenario_sourceActionEventStreamEmits,
        )
    }

    private fun test_cancelledRevoked_sourceEmitsSimultaneously(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetActionRecorder = TestTargetActionRecorder.pure(result = 10)

        val sourceEventStream = TestInputEventStream<Action<Int>>()

        val subjectEffect: Effect<EventStream<Int>> = sourceEventStream.executeEach()

        val subjectOutcome = subjectEffect.startExternally()

        Effect_EventStream_cancelledRevoked_testUtils.executeCancelTransaction(
            subjectEffectOutcome = subjectOutcome,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = sourceEventStream.emitting(
                tag = SourceActionEventStreamTag,
                emittedEvent = targetActionRecorder.recordedAction,
            ).bind(slottedStimulationScenario),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectEmission(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedEmittedEvent = 10,
            ),
            expectedTargetImpact = targetActionRecorder.expectIsExecutedOnce(),
        )

        EventStream_executeEach_testUtils.verifyEffectOngoing(
            sourceEventStream = sourceEventStream,
            subjectEventStream = subjectOutcome.result,
        )
    }

    @Test
    fun test_cancelledRevoked_sourceEmitsRevokedSimultaneously_subscribed() {
        slottedStimulationScenarioBank_sourceActionEventStreamEmitsRevoked.forEach { slottedStimulationScenario ->
            test_cancelledRevoked_sourceEmitsRevokedSimultaneously(
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    @Test
    fun test_cancelledRevoked_sourceEmitsRevokedSimultaneously_nonSubscribed() {
        test_cancelledRevoked_sourceEmitsRevokedSimultaneously(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            slottedStimulationScenario = arbitrarySlottedStimulationScenario_sourceActionEventStreamEmitsRevoked,
        )
    }

    private fun test_cancelledRevoked_sourceEmitsRevokedSimultaneously(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetActionRecorder = TestTargetActionRecorder.pure(result = 10)

        val sourceEventStream = TestInputEventStream<Action<Int>>()

        val subjectEffect: Effect<EventStream<Int>> = sourceEventStream.executeEach()

        val subjectOutcome = subjectEffect.startExternally()

        Effect_EventStream_cancelledRevoked_testUtils.executeCancelTransaction(
            subjectEffectOutcome = subjectOutcome,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = sourceEventStream.revokingEmission(
                tag = SourceActionEventStreamTag,
                emittedEvent = targetActionRecorder.recordedAction,
            ).bind(slottedStimulationScenario),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectNoEmission(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
            ),
            expectedTargetImpact = targetActionRecorder.expectIsNotExecuted(),
        )

        EventStream_executeEach_testUtils.verifyEffectOngoing(
            sourceEventStream = sourceEventStream,
            subjectEventStream = subjectOutcome.result,
        )
    }

    @Test
    fun test_cancelledRevoked_sourceEmitsCorrectedSimultaneously_subscribed() {
        slottedStimulationScenarioBank_sourceActionEventStreamEmitsCorrected.forEach { slottedStimulationScenario ->
            test_cancelledRevoked_sourceEmitsCorrectedSimultaneously(
                subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    @Test
    fun test_cancelledRevoked_sourceEmitsCorrectedSimultaneously_nonSubscribed() {
        test_cancelledRevoked_sourceEmitsCorrectedSimultaneously(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            slottedStimulationScenario = arbitrarySlottedStimulationScenario_sourceActionEventStreamEmitsCorrected,
        )
    }

    private fun test_cancelledRevoked_sourceEmitsCorrectedSimultaneously(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val targetActionRecorder1 = TestTargetActionRecorder.pure(result = 10)
        val targetActionRecorder2 = TestTargetActionRecorder.pure(result = 20)

        val sourceEventStream = TestInputEventStream<Action<Int>>()

        val subjectEffect: Effect<EventStream<Int>> = sourceEventStream.executeEach()

        val subjectOutcome = subjectEffect.startExternally()

        Effect_EventStream_cancelledRevoked_testUtils.executeCancelTransaction(
            subjectEffectOutcome = subjectOutcome,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = sourceEventStream.correctingEmission(
                tag = SourceActionEventStreamTag,
                intermediateEmittedEvent = targetActionRecorder1.recordedAction,
                correctedEmittedEvent = targetActionRecorder2.recordedAction,
            ).bind(slottedStimulationScenario),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectEmission(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedEmittedEvent = 20,
            ),
            expectedTargetImpact = ExpectedImpact.combine(
                targetActionRecorder1.expectIsNotExecuted(),
                targetActionRecorder2.expectIsExecutedOnce(),
            ),
        )

        EventStream_executeEach_testUtils.verifyEffectOngoing(
            sourceEventStream = sourceEventStream,
            subjectEventStream = subjectOutcome.result,
        )
    }
}
