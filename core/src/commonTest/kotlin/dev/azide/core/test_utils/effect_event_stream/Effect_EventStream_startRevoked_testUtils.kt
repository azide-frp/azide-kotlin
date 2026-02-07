package dev.azide.core.test_utils.effect_event_stream

import dev.azide.core.Effect
import dev.azide.core.EventStream
import dev.azide.core.test_utils.TestSlottedStimulation3
import dev.azide.core.test_utils.effect_generic.Effect_generic_startRevoked_testUtils
import dev.azide.core.test_utils.generic.ExpectedImpact

@Suppress("ClassName")
data object Effect_EventStream_startRevoked_testUtils {
    fun <EventT> executeStartTransaction(
        subjectEventStreamEffect: Effect<EventStream<EventT>>,
        slottedInputStimulation: TestSlottedStimulation3? = null,
        expectedTargetImpact: ExpectedImpact,
    ) {
        Effect_generic_startRevoked_testUtils.executeStartTransaction(
            subjectEffect = subjectEventStreamEffect,
            slottedInputStimulation = slottedInputStimulation,
            expectedTargetImpact = expectedTargetImpact,
        )
    }
}
