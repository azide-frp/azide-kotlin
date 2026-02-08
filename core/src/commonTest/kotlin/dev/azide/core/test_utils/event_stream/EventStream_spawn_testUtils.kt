package dev.azide.core.test_utils.event_stream

import dev.azide.core.EventStream
import dev.azide.core.Moment
import dev.azide.core.test_utils.TestSlottedStimulation3
import dev.azide.core.test_utils.generic.generic_spawn_testUtils

@Suppress("ClassName")
data object EventStream_spawn_testUtils {
    fun <EventT> executeSpawnTransaction(
        subjectEventStreamSpawnMoment: Moment<EventStream<EventT>>,
        slottedInputStimulation: TestSlottedStimulation3? = null,
        expectedSubjectEmission: ExpectedEventStreamEmission<EventT>,
    ) {
        generic_spawn_testUtils.executeSpawnTransaction(
            subjectSpawnMoment = subjectEventStreamSpawnMoment,
            slottedInputStimulation = slottedInputStimulation,
            expectedSubjectTransition = expectedSubjectEmission,
        )
    }
}
