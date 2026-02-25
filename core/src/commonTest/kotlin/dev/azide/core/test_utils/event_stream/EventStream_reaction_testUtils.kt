package dev.azide.core.test_utils.event_stream

import dev.azide.core.EventStream
import dev.azide.core.impl.event_stream.EventStreamVertex
import dev.azide.core.test_utils.TestSlottedStimulation2
import dev.azide.core.test_utils.generic.EventStreamObservationTrait
import dev.azide.core.test_utils.generic.TestSubjectHealthCheckStrategy
import dev.azide.core.test_utils.generic.TestSubjectHealthChecker
import dev.azide.core.test_utils.generic.generic_reaction_testUtils

@Suppress("ClassName")
data object EventStream_reaction_testUtils {
    fun <EventT> testReaction(
        subjectEventStream: EventStream<EventT>,
        inputStimulationPlan: generic_reaction_testUtils.InputStimulationPlan,
        expectedSubjectEmission: ExpectedEventStreamEmission<EventT>,
        subjectHealthChecker: TestSubjectHealthChecker<EventStream<EventT>, EventStreamVertex.Emission<EventT>>? = null,
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
    ) {
        val expectedTransition = expectedSubjectEmission.asTransition()

        generic_reaction_testUtils.testReaction(
            trait = EventStreamObservationTrait(),
            subject = subjectEventStream,
            inputStimulationPlan = inputStimulationPlan,
            expectedSubjectTransition = expectedTransition,
            subjectHealthChecker = subjectHealthChecker,
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    // Backwards-compatible overload that accepts the older slotted stimulation.
    fun <EventT> testReaction(
        subjectEventStream: EventStream<EventT>,
        slottedInputStimulation: TestSlottedStimulation2,
        expectedSubjectEmission: ExpectedEventStreamEmission<EventT>,
        subjectHealthChecker: TestSubjectHealthChecker<EventStream<EventT>, EventStreamVertex.Emission<EventT>>? = null,
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
    ) {
        val inputPlan = generic_reaction_testUtils.InputStimulationPlan(
            unobservedInputStimulation = slottedInputStimulation.slotStimulations[0],
            observedInputStimulation = slottedInputStimulation.slotStimulations[1],
        )

        testReaction(
            subjectEventStream = subjectEventStream,
            inputStimulationPlan = inputPlan,
            expectedSubjectEmission = expectedSubjectEmission,
            subjectHealthChecker = subjectHealthChecker,
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }
}
