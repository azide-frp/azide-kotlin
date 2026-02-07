package dev.azide.core.test_utils.schedule

import dev.azide.core.Schedule
import dev.azide.core.test_utils.TestSlottedStimulation3
import dev.azide.core.test_utils.effect_generic.Effect_generic_startRevoked_testUtils
import dev.azide.core.test_utils.generic.ExpectedImpact

@Suppress("ClassName")
data object Schedule_startRevoked_testUtils {
    fun executeStartTransaction(
        subjectSchedule: Schedule,
        slottedInputStimulation: TestSlottedStimulation3? = null,
        expectedTargetImpact: ExpectedImpact,
    ) {
        Effect_generic_startRevoked_testUtils.executeStartTransaction(
            subjectEffect = subjectSchedule,
            slottedInputStimulation = slottedInputStimulation,
            expectedTargetImpact = expectedTargetImpact,
        )
    }
}
