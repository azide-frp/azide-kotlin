package dev.azide.core.test_utils.schedule

import dev.azide.core.Schedule
import dev.azide.core.test_utils.TestSlottedStimulation4
import dev.azide.core.test_utils.effect_generic.Effect_generic_startRevoked_quickCancelled_testUtils
import dev.azide.core.test_utils.generic.ExpectedImpact

@Suppress("ClassName")
data object Schedule_startRevoked_quickCancelled_testUtils {
    fun executeStartTransaction(
        subjectSchedule: Schedule,
        slottedInputStimulation: TestSlottedStimulation4? = null,
        expectedTargetImpact: ExpectedImpact,
    ) {
        Effect_generic_startRevoked_quickCancelled_testUtils.executeStartTransaction(
            subjectEffect = subjectSchedule,
            slottedInputStimulation = slottedInputStimulation,
            expectedTargetImpact = expectedTargetImpact,
        )
    }
}
