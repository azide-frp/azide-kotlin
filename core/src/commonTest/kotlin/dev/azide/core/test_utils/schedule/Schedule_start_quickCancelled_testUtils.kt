package dev.azide.core.test_utils.schedule

import dev.azide.core.Schedule
import dev.azide.core.test_utils.TestSlottedStimulation3
import dev.azide.core.test_utils.effect_generic.Effect_generic_start_quickCancelled_testUtils
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.generic.ExpectedTestSubjectTransition

@Suppress("ClassName")
data object Schedule_start_quickCancelled_testUtils {
    fun executeStartTransaction(
        subjectSchedule: Schedule,
        slottedInputStimulation: TestSlottedStimulation3? = null,
        expectedTargetImpact: ExpectedImpact,
        cancelCount: Int = 1,
    ) {
        Effect_generic_start_quickCancelled_testUtils.executeStartTransaction(
            subjectEffect = subjectSchedule,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            slottedInputStimulation = slottedInputStimulation,
            expectedSubjectTransition = ExpectedTestSubjectTransition.None,
            expectedTargetImpact = expectedTargetImpact,
            cancelCount = cancelCount,
        )
    }
}
