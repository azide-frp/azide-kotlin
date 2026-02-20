package dev.azide.core.test_utils.schedule

import dev.azide.core.ScheduleOutcome
import dev.azide.core.executeInternallyWrappedUpUnpacked
import dev.azide.core.impl.Revocable
import dev.azide.core.test_utils.TestSlottedStimulation2
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.generic.generic_testUtils
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation0
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation1

@Suppress("ClassName")
data object Schedule_cancelled_testUtils {
    fun executeCancelTransaction(
        subjectOutcome: ScheduleOutcome,
        slottedInputStimulation: TestSlottedStimulation2? = null,
        expectedTargetImpact: ExpectedImpact,
        cancelCount: Int = 1,
    ) {
        val subjectScheduleHandle = subjectOutcome.handle

        generic_testUtils.executeTransactionWithImpactVerification(
            expectedTargetImpact = expectedTargetImpact,
        ) { propagationContext ->
            // 0. Pre-stimulation
            slottedInputStimulation?.slotStimulation0?.stimulate(
                propagationContext = propagationContext,
            )

            // 1. Cancel the schedule
            repeat(cancelCount) {
                val (_: Unit, _: Revocable) = subjectScheduleHandle.cancel.executeInternallyWrappedUpUnpacked(
                    propagationContext = propagationContext,
                )
            }

            slottedInputStimulation?.slotStimulation1?.stimulate(
                propagationContext = propagationContext,
            )

        }
    }
}
