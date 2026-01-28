package dev.azide.core.test_utils.effect_event_stream

import dev.azide.core.Effect
import dev.azide.core.EventStream
import dev.azide.core.test_utils.ExpectedImpact
import dev.azide.core.test_utils.TestSlottedStimulation4
import dev.azide.core.test_utils.effect_generic.Effect_generic_startRevoked_quickCancelled_testUtils

@Suppress("ClassName")
data object Effect_EventStream_startRevoked_quickCancelled_testUtils {
    fun <EventT> executeStartTransaction(
        subjectEventStreamEffect: Effect<EventStream<EventT>>,
        slottedInputStimulation: TestSlottedStimulation4? = null,
        expectedTargetImpact: ExpectedImpact,
    ) {
        Effect_generic_startRevoked_quickCancelled_testUtils.executeStartTransaction(
            subjectEffect = subjectEventStreamEffect,
            slottedInputStimulation = slottedInputStimulation,
            expectedTargetImpact = expectedTargetImpact,
        )
    }
}
