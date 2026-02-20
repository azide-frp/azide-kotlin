package dev.azide.core.test_utils.schedule

import dev.azide.core.ScheduleOutcome
import dev.azide.core.executeInternallyWrappedUp
import dev.azide.core.test_utils.TestSlottedStimulation3
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.generic.generic_testUtils
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation0
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation1
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation2

@Suppress("ClassName")
data object Schedule_cancelledRevoked_testUtils {
    fun executeCancelTransaction(
        subjectOutcome: ScheduleOutcome,
        slottedInputStimulation: TestSlottedStimulation3? = null,
        expectedTargetImpact: ExpectedImpact,
    ) {
        val subjectHandle = subjectOutcome.handle

        generic_testUtils.executeTransactionWithImpactVerification(
            expectedTargetImpact = expectedTargetImpact,
        ) { propagationContext ->
            // 0. Pre-stimulation
            slottedInputStimulation?.slotStimulation0?.stimulate(
                propagationContext = propagationContext,
            )

            // 1. Cancel the schedule
            val cancelRevocable = subjectHandle.cancel.executeInternallyWrappedUp(
                propagationContext = propagationContext,
            ).revocable

            slottedInputStimulation?.slotStimulation1?.stimulate(
                propagationContext = propagationContext,
            )

            // 2. Revoke the schedule's cancellation
            cancelRevocable.revoke()

            slottedInputStimulation?.slotStimulation2?.stimulate(
                propagationContext = propagationContext,
            )
        }
    }
}
