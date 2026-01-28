package dev.azide.core.test_utils.schedule_generic

import dev.azide.core.Schedule
import dev.azide.core.test_utils.ExpectedTestSubjectReaction
import dev.azide.core.test_utils.ExpectedTestSubjectState
import dev.azide.core.test_utils.ExpectedTestSubjectTransition
import dev.azide.core.test_utils.ExpectedImpact
import dev.azide.core.test_utils.TestSlottedStimulation4
import dev.azide.core.test_utils.effect_generic.Effect_generic_start_quickCancelledRevoked_testUtils
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy

@Suppress("ClassName")
data object Schedule_generic_start_quickCancelledRevoked_testUtils {
    fun executeStartTransaction(
        subjectSchedule: Schedule,
        slottedInputStimulation: TestSlottedStimulation4? = null,
        expectedTargetImpact: ExpectedImpact,
    ) {
        Effect_generic_start_quickCancelledRevoked_testUtils.executeStartTransaction(
            subjectEffect = subjectSchedule,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            slottedInputStimulation = slottedInputStimulation,
            expectedSubjectTransition = ExpectedTestSubjectTransition(
                expectedOldState = ExpectedTestSubjectState.None,
                expectedNewState = ExpectedTestSubjectState.None,
                expectedReaction = ExpectedTestSubjectReaction.None,
            ),
            expectedTargetImpact = expectedTargetImpact,
        )
    }
}
