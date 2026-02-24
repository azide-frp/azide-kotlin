package dev.azide.core.event_stream

import dev.azide.core.EventStream
import dev.azide.core.filter
import dev.azide.core.impl.event_stream.EventStreamVertex
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.event_stream.EventStream_expectations_testUtils
import dev.azide.core.test_utils.event_stream.TestInputEventStream
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.generic.TestSubjectHealthCheckStrategy
import dev.azide.core.test_utils.generic.generic_reaction_testUtils
import kotlin.test.Test

@Suppress("ClassName")
class EventStream_filter_tests {

    // region Source emits

    @Test
    fun test_sourceEmits_predicateAccepted() {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { true }

        EventStream_filter_testUtils.testReaction(
            input = sourceEventStream,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                unobservedInputStimulation = TestStimulation.Noop,
                observedInputStimulation = sourceEventStream.emit(11),
            ),
            subjectEventStream = subjectEventStream,
            expectedSubjectEmission = EventStream_expectations_testUtils.expectEmission<Int>(
                expectedEmittedEvent = 11,
            ),
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
            predicateAccepts = true,
        )
    }

    @Test
    fun test_sourceEmits_predicateRejected() {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { false }

        EventStream_filter_testUtils.testReaction(
            input = sourceEventStream,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                unobservedInputStimulation = TestStimulation.Noop,
                observedInputStimulation = sourceEventStream.emit(11),
            ),
            subjectEventStream = subjectEventStream,
            expectedSubjectEmission = EventStream_expectations_testUtils.expectNoEmission<Int>(),
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
            predicateAccepts = false,
        )
    }

    // endregion

    // region Source emits revoked

    @Test
    fun test_sourceEmitsRevoked_predicateAccepted() {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { true }

        EventStream_filter_testUtils.testReaction(
            input = sourceEventStream,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                unobservedInputStimulation = TestStimulation.Noop,
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    sourceEventStream.emit(11),
                    sourceEventStream.revokeEmission(),
                ),
            ),
            subjectEventStream = subjectEventStream,
            expectedSubjectEmission = EventStream_expectations_testUtils.expectNoEmission<Int>(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
            ),
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
            predicateAccepts = true,
        )
    }

    @Test
    fun test_sourceEmitsRevoked_predicateRejected() {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { false }

        EventStream_filter_testUtils.testReaction(
            input = sourceEventStream,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                unobservedInputStimulation = TestStimulation.Noop,
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    sourceEventStream.emit(11),
                    sourceEventStream.revokeEmission(),
                ),
            ),
            subjectEventStream = subjectEventStream,
            expectedSubjectEmission = EventStream_expectations_testUtils.expectNoEmission<Int>(),
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
            predicateAccepts = false,
        )
    }

    // endregion

    // region Source emits corrected

    @Test
    fun test_sourceEmitsCorrected_predicateAcceptedBoth() {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { true }

        EventStream_filter_testUtils.testReaction(
            input = sourceEventStream,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                unobservedInputStimulation = TestStimulation.Noop,
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    sourceEventStream.emit(11),
                    sourceEventStream.correctEmission(12),
                ),
            ),
            subjectEventStream = subjectEventStream,
            expectedSubjectEmission = EventStream_expectations_testUtils.expectEmission<Int>(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedEmittedEvent = 12,
            ),
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
            predicateAccepts = true,
        )
    }

    @Test
    fun test_sourceEmitsCorrected_predicateRejectedBoth() {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { false }

        EventStream_filter_testUtils.testReaction(
            input = sourceEventStream,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                unobservedInputStimulation = TestStimulation.Noop,
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    sourceEventStream.emit(11),
                    sourceEventStream.correctEmission(12),
                ),
            ),
            subjectEventStream = subjectEventStream,
            expectedSubjectEmission = EventStream_expectations_testUtils.expectNoEmission<Int>(),
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
            predicateAccepts = false,
        )
    }

    @Test
    fun test_sourceEmitsCorrected_predicateAcceptedFirst() {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { it > 0 }

        EventStream_filter_testUtils.testReaction(
            input = sourceEventStream,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                unobservedInputStimulation = TestStimulation.Noop,
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    sourceEventStream.emit(11),
                    sourceEventStream.correctEmission(-12),
                ),
            ),
            subjectEventStream = subjectEventStream,
            expectedSubjectEmission = EventStream_expectations_testUtils.expectNoEmission<Int>(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
            ),
            // 999 > 0, so the health-check emit is accepted
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
            predicateAccepts = true,
        )
    }

    @Test
    fun test_sourceEmitsCorrected_predicateAcceptedSecond() {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { it > 0 }

        EventStream_filter_testUtils.testReaction(
            input = sourceEventStream,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                unobservedInputStimulation = TestStimulation.Noop,
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    sourceEventStream.emit(-11),
                    sourceEventStream.correctEmission(12),
                ),
            ),
            subjectEventStream = subjectEventStream,
            expectedSubjectEmission = EventStream_expectations_testUtils.expectEmission<Int>(
                expectedEmittedEvent = 12,
            ),
            // 999 > 0, so the health-check emit is accepted
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
            predicateAccepts = true,
        )
    }

    // endregion

}

