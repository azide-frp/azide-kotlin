package dev.azide.core.test_utils.effect_event_stream

import dev.azide.core.Effect
import dev.azide.core.EventStream
import dev.azide.core.test_utils.TestSlottedStimulation3
import dev.azide.core.test_utils.effect_generic.Effect_generic_start_quickCancelled_testUtils
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy
import dev.azide.core.test_utils.event_stream.ExpectedEventStreamEmission
import dev.azide.core.test_utils.event_stream.asTransition
import dev.azide.core.test_utils.generic.EventStreamObservationTrait
import dev.azide.core.test_utils.generic.ExpectedImpact

@Suppress("ClassName")
data object Effect_EventStream_start_quickCancelled_testUtils {
    fun <EventT> executeStartTransaction(
        subjectEventStreamEffect: Effect<EventStream<EventT>>,
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedInputStimulation: TestSlottedStimulation3? = null,
        expectedSubjectEmission: ExpectedEventStreamEmission<EventT>,
        expectedTargetImpact: ExpectedImpact,
        cancelCount: Int = 1,
    ): EventStream<EventT> = Effect_generic_start_quickCancelled_testUtils.executeStartTransaction(
        trait = EventStreamObservationTrait(),
        subjectEffect = subjectEventStreamEffect,
        subjectPerceptionStrategy = subjectPerceptionStrategy,
        slottedInputStimulation = slottedInputStimulation,
        expectedSubjectTransition = expectedSubjectEmission.asTransition(),
        expectedTargetImpact = expectedTargetImpact,
        cancelCount = cancelCount,
    )
}
