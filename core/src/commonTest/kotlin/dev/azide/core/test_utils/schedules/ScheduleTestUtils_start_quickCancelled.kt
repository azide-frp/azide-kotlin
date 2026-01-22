package dev.azide.core.test_utils.schedules

import dev.azide.core.Schedule
import dev.azide.core.test_utils.ExpectedTestSubjectReaction
import dev.azide.core.test_utils.ExpectedTestSubjectState
import dev.azide.core.test_utils.ExpectedTestSubjectTransition
import dev.azide.core.test_utils.ExpectedImpact
import dev.azide.core.test_utils.TestSlottedStimulation3
import dev.azide.core.test_utils.effects.EffectTestUtils_start_quickCancelled
import dev.azide.core.test_utils.effects.TestSubjectPerceptionStrategy

@Suppress("ClassName")
data object ScheduleTestUtils_start_quickCancelled {
    fun executeStartTransaction(
        subjectSchedule: Schedule,
        slottedInputStimulation: TestSlottedStimulation3? = null,
        expectedTargetImpact: ExpectedImpact,
        cancelCount: Int = 1,
    ) {
        EffectTestUtils_start_quickCancelled.executeStartTransaction(
            subjectEffect = subjectSchedule,
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
