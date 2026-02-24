package dev.azide.core.test_utils.event_stream

import dev.azide.core.EventStream
import dev.azide.core.test_utils.TestSlottedStimulation2
import dev.azide.core.test_utils.generic.EventStreamObservationTrait
import dev.azide.core.test_utils.generic.generic_reaction_testUtils

@Suppress("ClassName")
data object EventStream_reaction_testUtils {
    fun <EventT> testReaction(
        subjectEventStream: EventStream<EventT>,
        slottedInputStimulation: TestSlottedStimulation2,
        expectedSubjectEmission: ExpectedEventStreamEmission<EventT>,
    ) {
        generic_reaction_testUtils.testReaction(
            trait = EventStreamObservationTrait(),
            subject = subjectEventStream,
            slottedInputStimulation = slottedInputStimulation,
            expectedSubjectTransition = expectedSubjectEmission.asTransition(),
        )
    }
}
