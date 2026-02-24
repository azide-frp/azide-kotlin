package dev.azide.core.event_stream

import dev.azide.core.EventStream
import dev.azide.core.filter
import dev.azide.core.impl.event_stream.EventStreamVertex
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.event_stream.EventStream_expectations_testUtils
import dev.azide.core.test_utils.event_stream.EventStream_reaction_testUtils
import dev.azide.core.test_utils.event_stream.TestInputEventStream
import dev.azide.core.test_utils.event_stream.asTransition
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.generic.TestSubjectHealthChecker
import dev.azide.core.test_utils.generic.generic_reaction_testUtils
import kotlin.test.Test

@Suppress("ClassName")
class EventStream_filter_tests {

    // region Source emits

    @Test
    fun test_sourceEmits_predicateAccepted() {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { true }

        EventStream_reaction_testUtils.testReaction(
            subjectEventStream = subjectEventStream,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                unobservedInputStimulation = TestStimulation.Noop,
                observedInputStimulation = sourceEventStream.emit(11),
            ),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectEmission(
                expectedEmittedEvent = 11,
            ),
            subjectHealthChecker = FilterHealthChecker(input = sourceEventStream, predicateAccepts = true),
        )
    }

    @Test
    fun test_sourceEmits_predicateRejected() {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { false }

        EventStream_reaction_testUtils.testReaction(
            subjectEventStream = subjectEventStream,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                unobservedInputStimulation = TestStimulation.Noop,
                observedInputStimulation = sourceEventStream.emit(11),
            ),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectNoEmission(),
            subjectHealthChecker = FilterHealthChecker(input = sourceEventStream, predicateAccepts = false),
        )
    }

    // endregion

    // region Source emits revoked

    @Test
    fun test_sourceEmitsRevoked_predicateAccepted() {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { true }

        EventStream_reaction_testUtils.testReaction(
            subjectEventStream = subjectEventStream,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                unobservedInputStimulation = TestStimulation.Noop,
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    sourceEventStream.emit(11),
                    sourceEventStream.revokeEmission(),
                ),
            ),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectNoEmission(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
            ),
            subjectHealthChecker = FilterHealthChecker(input = sourceEventStream, predicateAccepts = true),
        )
    }

    @Test
    fun test_sourceEmitsRevoked_predicateRejected() {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { false }

        EventStream_reaction_testUtils.testReaction(
            subjectEventStream = subjectEventStream,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                unobservedInputStimulation = TestStimulation.Noop,
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    sourceEventStream.emit(11),
                    sourceEventStream.revokeEmission(),
                ),
            ),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectNoEmission(),
            subjectHealthChecker = FilterHealthChecker(input = sourceEventStream, predicateAccepts = false),
        )
    }

    // endregion

    // region Source emits corrected

    @Test
    fun test_sourceEmitsCorrected_predicateAcceptedBoth() {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { true }

        EventStream_reaction_testUtils.testReaction(
            subjectEventStream = subjectEventStream,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                unobservedInputStimulation = TestStimulation.Noop,
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    sourceEventStream.emit(11),
                    sourceEventStream.correctEmission(12),
                ),
            ),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectEmission(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedEmittedEvent = 12,
            ),
            subjectHealthChecker = FilterHealthChecker(input = sourceEventStream, predicateAccepts = true),
        )
    }

    @Test
    fun test_sourceEmitsCorrected_predicateRejectedBoth() {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { false }

        EventStream_reaction_testUtils.testReaction(
            subjectEventStream = subjectEventStream,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                unobservedInputStimulation = TestStimulation.Noop,
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    sourceEventStream.emit(11),
                    sourceEventStream.correctEmission(12),
                ),
            ),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectNoEmission(),
            subjectHealthChecker = FilterHealthChecker(input = sourceEventStream, predicateAccepts = false),
        )
    }

    @Test
    fun test_sourceEmitsCorrected_predicateAcceptedFirst() {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { it > 0 }

        EventStream_reaction_testUtils.testReaction(
            subjectEventStream = subjectEventStream,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                unobservedInputStimulation = TestStimulation.Noop,
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    sourceEventStream.emit(11),
                    sourceEventStream.correctEmission(-12),
                ),
            ),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectNoEmission(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
            ),
            // 999 > 0, so the health-check emit is accepted
            subjectHealthChecker = FilterHealthChecker(input = sourceEventStream, predicateAccepts = true),
        )
    }

    @Test
    fun test_sourceEmitsCorrected_predicateAcceptedSecond() {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectEventStream = sourceEventStream.filter { it > 0 }

        EventStream_reaction_testUtils.testReaction(
            subjectEventStream = subjectEventStream,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                unobservedInputStimulation = TestStimulation.Noop,
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    sourceEventStream.emit(-11),
                    sourceEventStream.correctEmission(12),
                ),
            ),
            expectedSubjectEmission = EventStream_expectations_testUtils.expectEmission(
                expectedEmittedEvent = 12,
            ),
            // 999 > 0, so the health-check emit is accepted
            subjectHealthChecker = FilterHealthChecker(input = sourceEventStream, predicateAccepts = true),
        )
    }

    // endregion

}

