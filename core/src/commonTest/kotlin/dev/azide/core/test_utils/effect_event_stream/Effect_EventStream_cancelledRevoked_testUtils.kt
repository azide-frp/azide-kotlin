package dev.azide.core.test_utils.effect_event_stream

import dev.azide.core.Effect
import dev.azide.core.EventStream
import dev.azide.core.test_utils.TestSlottedStimulation3
import dev.azide.core.test_utils.effect_generic.Effect_generic_cancelledRevoked_testUtils
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy
import dev.azide.core.test_utils.event_stream.ExpectedEventStreamEmission
import dev.azide.core.test_utils.event_stream.asTransition
import dev.azide.core.test_utils.generic.CellObservationTrait
import dev.azide.core.test_utils.generic.EventStreamObservationTrait
import dev.azide.core.test_utils.generic.ExpectedImpact

@Suppress("ClassName")
data object Effect_EventStream_cancelledRevoked_testUtils {
    fun <EventT> testCancel(
        subjectEffectOutcome: Effect.Outcome<EventStream<EventT>>,
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedInputStimulation: TestSlottedStimulation3? = null,
        expectedSubjectEmission: ExpectedEventStreamEmission<EventT>,
        expectedTargetImpact: ExpectedImpact,
    ) {
        Effect_generic_cancelledRevoked_testUtils.testCancel(
            trait = EventStreamObservationTrait(),
            subjectOutcome = subjectEffectOutcome,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = slottedInputStimulation,
            expectedSubjectTransition = expectedSubjectEmission.asTransition(),
            expectedTargetImpact = expectedTargetImpact,
        )
    }
}
