package dev.azide.core.test_utils.event_stream

import dev.azide.core.EventStream
import dev.azide.core.test_utils.generic.EventStreamObservationTrait
import dev.azide.core.test_utils.generic.generic_offlineActivation_testUtils
import dev.azide.core.test_utils.generic.generic_reaction_testUtils

@Suppress("ClassName")
data object EventStream_offlineActivation_testUtils {
    fun <EventT> testOfflineActivation(
        subjectEventStream: EventStream<EventT>,
        subjectHealthChecker: generic_reaction_testUtils.EventStreamHealthChecker<EventT>,
    ) {
        generic_offlineActivation_testUtils.testOfflineActivation(
            trait = EventStreamObservationTrait(),
            subject = subjectEventStream,
            subjectHealthChecker = subjectHealthChecker,
        )
    }
}
