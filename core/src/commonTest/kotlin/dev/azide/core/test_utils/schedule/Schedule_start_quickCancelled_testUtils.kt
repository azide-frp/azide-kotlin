package dev.azide.core.test_utils.schedule

import dev.azide.core.Schedule
import dev.azide.core.executeInternallyWrappedUp
import dev.azide.core.test_utils.TestSlottedStimulation3
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.generic.generic_testUtils
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation0
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation1
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation2

@Suppress("ClassName")
data object Schedule_start_quickCancelled_testUtils {
    fun testStart(
        subjectSchedule: Schedule,
        slottedInputStimulation: TestSlottedStimulation3? = null,
        expectedTargetImpact: ExpectedImpact,
        cancelCount: Int = 1,
    ) {
        generic_testUtils.executeTransactionWithImpactVerification(
            expectedTargetImpact = expectedTargetImpact,
        ) { propagationContext ->
            // 0. Pre-stimulation

            slottedInputStimulation?.slotStimulation0?.stimulate(
                propagationContext = propagationContext,
            )

            // 1. Start the schedule
            val scheduleOutcome = subjectSchedule.start.executeInternallyWrappedUp(
                propagationContext = propagationContext,
            ).result

            val subjectScheduleHandle = scheduleOutcome.handle

            slottedInputStimulation?.slotStimulation1?.stimulate(
                propagationContext = propagationContext,
            )

            // 2. Cancel the schedule
            repeat(cancelCount) {
                subjectScheduleHandle.cancel.executeInternallyWrappedUp(
                    propagationContext = propagationContext,
                )
            }

            slottedInputStimulation?.slotStimulation2?.stimulate(
                propagationContext = propagationContext,
            )
        }
    }
}
