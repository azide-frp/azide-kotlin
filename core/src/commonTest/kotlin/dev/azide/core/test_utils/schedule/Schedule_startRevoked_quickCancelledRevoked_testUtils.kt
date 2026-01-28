package dev.azide.core.test_utils.schedule

import dev.azide.core.Schedule
import dev.azide.core.test_utils.ExpectedImpact
import dev.azide.core.test_utils.TestSlottedStimulation5
import dev.azide.core.test_utils.effect_generic.Effect_generic_startRevoked_quickCancelledRevoked_testUtils

@Suppress("ClassName")
data object Schedule_startRevoked_quickCancelledRevoked_testUtils {
    fun executeStartTransaction(
        subjectSchedule: Schedule,
        slottedInputStimulation: TestSlottedStimulation5? = null,
        expectedTargetImpact: ExpectedImpact,
    ) {
        Effect_generic_startRevoked_quickCancelledRevoked_testUtils.executeStartTransaction(
            subjectEffect = subjectSchedule,
            slottedInputStimulation = slottedInputStimulation,
            expectedTargetImpact = expectedTargetImpact,
        )
    }
}
