package dev.azide.core.event_stream

import dev.azide.core.EventStream
import dev.azide.core.impl.event_stream.EventStreamVertex
import dev.azide.core.test_utils.event_stream.EventStream_expectations_testUtils
import dev.azide.core.test_utils.event_stream.TestInputEventStream
import dev.azide.core.test_utils.generic.TestSubjectHealthChecker
import dev.azide.core.test_utils.generic.generic_reaction_testUtils
import dev.azide.core.test_utils.event_stream.asTransition

/**
 * Health checker for tests of the `filter` operator.
 *
 * Kept minimal and operator-scoped (OPERATOR_testUtils style).
 */
class FilterHealthChecker(
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
