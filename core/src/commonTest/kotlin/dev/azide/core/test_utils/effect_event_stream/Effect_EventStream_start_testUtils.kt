package dev.azide.core.test_utils.effect_event_stream

import dev.azide.core.Effect
import dev.azide.core.EventStream
import dev.azide.core.test_utils.TestSlottedStimulation2
import dev.azide.core.test_utils.effect_generic.Effect_generic_start_testUtils
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy
import dev.azide.core.test_utils.event_stream.ExpectedEventStreamEmission
import dev.azide.core.test_utils.generic.ExpectedImpact

@Suppress("ClassName")
data object Effect_EventStream_start_testUtils {
    fun <EventT> executeStartTransaction(
        subjectEventStreamEffect: Effect<EventStream<EventT>>,
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedInputStimulation: TestSlottedStimulation2? = null,
        expectedSubjectEmission: ExpectedEventStreamEmission<EventT>,
        expectedTargetImpact: ExpectedImpact,
    ) {
        Effect_generic_start_testUtils.executeStartTransaction(
            subjectEffect = subjectEventStreamEffect,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = slottedInputStimulation,
            expectedSubjectTransition = expectedSubjectEmission,
            expectedTargetImpact = expectedTargetImpact,
        )
    }
}
