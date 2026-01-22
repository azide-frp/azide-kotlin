package dev.azide.core.test_utils.schedules

import dev.azide.core.Schedule
import dev.azide.core.test_utils.ExpectedImpact
import dev.azide.core.test_utils.TestSlottedStimulation5
import dev.azide.core.test_utils.effects.EffectTestUtils_startRevoked_quickCancelledRevoked

@Suppress("ClassName")
data object ScheduleTestUtils_startRevoked_quickCancelledRevoked {
    fun executeStartTransaction(
        subjectSchedule: Schedule,
        slottedInputStimulation: TestSlottedStimulation5? = null,
        expectedTargetImpact: ExpectedImpact,
    ) {
        EffectTestUtils_startRevoked_quickCancelledRevoked.executeStartTransaction(
            subjectEffect = subjectSchedule,
            slottedInputStimulation = slottedInputStimulation,
            expectedTargetImpact = expectedTargetImpact,
        )
    }
}
