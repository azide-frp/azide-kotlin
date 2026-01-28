package dev.azide.core.test_utils.schedule_generic

import dev.azide.core.Effect
import dev.azide.core.test_utils.ExpectedImpact
import dev.azide.core.test_utils.ExpectedTestSubjectReaction
import dev.azide.core.test_utils.ExpectedTestSubjectState
import dev.azide.core.test_utils.ExpectedTestSubjectTransition
import dev.azide.core.test_utils.TestSlottedStimulation2
import dev.azide.core.test_utils.effect_generic.Effect_generic_cancelled_testUtils
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy

@Suppress("ClassName")
data object Schedule_generic_cancelled_testUtils {
    fun executeCancelTransaction(
        subjectOutcome: Effect.Outcome<Unit>,
        slottedInputStimulation: TestSlottedStimulation2? = null,
        expectedTargetImpact: ExpectedImpact,
        cancelCount: Int = 1,
    ) {
        Effect_generic_cancelled_testUtils.executeCancelTransaction(
            subjectOutcome = subjectOutcome,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            slottedInputStimulation = slottedInputStimulation,
            expectedSubjectTransition = ExpectedTestSubjectTransition(
                expectedOldState = ExpectedTestSubjectState.None,
                expectedNewState = ExpectedTestSubjectState.None,
                expectedReaction = ExpectedTestSubjectReaction.None,
            ),
            expectedTargetImpact = expectedTargetImpact,
            cancelCount = cancelCount,
        )
    }
}
