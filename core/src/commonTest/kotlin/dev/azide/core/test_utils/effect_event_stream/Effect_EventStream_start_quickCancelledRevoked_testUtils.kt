package dev.azide.core.test_utils.effect_event_stream

import dev.azide.core.Effect
import dev.azide.core.EventStream
import dev.azide.core.test_utils.ExpectedEventStreamEmission
import dev.azide.core.test_utils.ExpectedImpact
import dev.azide.core.test_utils.TestSlottedStimulation4
import dev.azide.core.test_utils.effect_generic.Effect_generic_start_quickCancelledRevoked_testUtils
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy

@Suppress("ClassName")
data object Effect_EventStream_start_quickCancelledRevoked_testUtils {
    fun <EventT> executeStartTransaction(
        subjectEventStreamEffect: Effect<EventStream<EventT>>,
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedInputStimulation: TestSlottedStimulation4? = null,
        expectedSubjectEmission: ExpectedEventStreamEmission<EventT>,
        expectedTargetImpact: ExpectedImpact,
    ): EventStream<EventT> = Effect_generic_start_quickCancelledRevoked_testUtils.executeStartTransaction(
        subjectEffect = subjectEventStreamEffect,
        subjectPerceptionStrategy = subjectPerceptionStrategy,
        slottedInputStimulation = slottedInputStimulation,
        expectedSubjectTransition = expectedSubjectEmission,
        expectedTargetImpact = expectedTargetImpact,
    )
}
