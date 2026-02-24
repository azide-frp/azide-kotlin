package dev.azide.core.test_utils.effect_event_stream

import dev.azide.core.EventStream
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.effect_generic.Effect_generic_step_testUtils
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy
import dev.azide.core.test_utils.event_stream.ExpectedEventStreamEmission
import dev.azide.core.test_utils.event_stream.asTransition
import dev.azide.core.test_utils.generic.EventStreamObservationTrait
import dev.azide.core.test_utils.generic.ExpectedImpact

@Suppress("ClassName")
data object Effect_EventStream_step_testUtils {
    fun <EventT> testStep(
        subjectEventStream: EventStream<EventT>,
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        inputStimulation: TestStimulation,
        expectedSubjectEmission: ExpectedEventStreamEmission<EventT>,
        expectedTargetImpact: ExpectedImpact,
    ) {
        Effect_generic_step_testUtils.testStep(
            trait = EventStreamObservationTrait(),
            subject = subjectEventStream,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            inputStimulation = inputStimulation,
            expectedSubjectTransition = expectedSubjectEmission.asTransition(),
            expectedTargetImpact = expectedTargetImpact,
        )
    }
}
