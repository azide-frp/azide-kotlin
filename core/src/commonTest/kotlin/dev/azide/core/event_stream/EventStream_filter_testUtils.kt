package dev.azide.core.event_stream

import dev.azide.core.EventStream
import dev.azide.core.impl.event_stream.EventStreamVertex
import dev.azide.core.test_utils.event_stream.EventStream_expectations_testUtils
import dev.azide.core.test_utils.event_stream.EventStream_offlineActivation_testUtils
import dev.azide.core.test_utils.event_stream.TestInputEventStream
import dev.azide.core.test_utils.generic.EventStreamObservationTrait
import dev.azide.core.test_utils.generic.TestSubjectHealthCheckStrategy
import dev.azide.core.test_utils.generic.TestSubjectHealthChecker
import dev.azide.core.test_utils.generic.generic_reaction_testUtils
import dev.azide.core.test_utils.event_stream.asTransition

/**
 * Health checker and test wrappers for the `filter` operator.
 *
 * Modeled 1:1 after `Cell_map_testUtils`.
 */
@Suppress("ClassName")
object EventStream_filter_testUtils {
    private class FilterHealthChecker(
        private val input: TestInputEventStream<Int>,
        private val predicateAccepts: Boolean,
    ) : generic_reaction_testUtils.EventStreamHealthChecker<Int> {
        override fun verifyInputsInactive() {
            // TestInputEventStream does not expose its internal vertex for listener-count assertions; no-op
        }

        override fun prepareHealthCheck(
            subject: EventStream<Int>,
        ): TestSubjectHealthChecker.HealthCheckDescription<EventStream<Int>, EventStreamVertex.Emission<Int>> {
            val expectedEmission = if (predicateAccepts) {
                EventStream_expectations_testUtils.expectEmission(expectedEmittedEvent = 999)
            } else {
                EventStream_expectations_testUtils.expectNoEmission()
            }

            return TestSubjectHealthChecker.HealthCheckDescription(
                inputStimulation = input.emit(999),
                expectedSubjectTransition = expectedEmission.asTransition(),
            )
        }
    }

    fun testOfflineActivation(
        input: TestInputEventStream<Int>,
        subjectEventStream: EventStream<Int>,
        predicateAccepts: Boolean,
    ) {
        EventStream_offlineActivation_testUtils.testOfflineActivation(
            subjectEventStream = subjectEventStream,
            subjectHealthChecker = FilterHealthChecker(input = input, predicateAccepts = predicateAccepts),
        )
    }

    fun testReaction(
        input: TestInputEventStream<Int>,
        inputStimulationPlan: generic_reaction_testUtils.InputStimulationPlan,
        subjectEventStream: EventStream<Int>,
        expectedSubjectEmission: dev.azide.core.test_utils.event_stream.ExpectedEventStreamEmission<Int>,
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
        predicateAccepts: Boolean,
    ) {
        val expectedTransition = expectedSubjectEmission.asTransition()

        generic_reaction_testUtils.testReaction(
            trait = EventStreamObservationTrait(),
            subject = subjectEventStream,
            inputStimulationPlan = inputStimulationPlan,
            expectedSubjectTransition = expectedTransition,
            subjectHealthChecker = FilterHealthChecker(input = input, predicateAccepts = predicateAccepts),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }
}
