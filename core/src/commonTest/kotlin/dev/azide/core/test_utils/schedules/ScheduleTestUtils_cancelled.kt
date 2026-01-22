package dev.azide.core.test_utils.schedules

import dev.azide.core.Effect
import dev.azide.core.test_utils.ExpectedImpact
import dev.azide.core.test_utils.ExpectedTestSubjectReaction
import dev.azide.core.test_utils.ExpectedTestSubjectState
import dev.azide.core.test_utils.ExpectedTestSubjectTransition
import dev.azide.core.test_utils.TestSlottedStimulation2
import dev.azide.core.test_utils.effects.EffectTestUtils_cancelled
import dev.azide.core.test_utils.effects.TestSubjectPerceptionStrategy

@Suppress("ClassName")
data object ScheduleTestUtils_cancelled {
    fun executeCancelTransaction(
        subjectOutcome: Effect.Outcome<Unit>,
        slottedInputStimulation: TestSlottedStimulation2? = null,
        expectedTargetImpact: ExpectedImpact,
        cancelCount: Int = 1,
    ) {
        EffectTestUtils_cancelled.executeCancelTransaction(
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
