package dev.azide.core.test_utils.schedule

import dev.azide.core.Schedule
import dev.azide.core.executeInternallyWrappedUp
import dev.azide.core.test_utils.TestSlottedStimulation4
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.generic.generic_testUtils
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation0
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation1
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation2
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation3

@Suppress("ClassName")
data object Schedule_start_quickCancelledRevoked_testUtils {
    fun testStart(
        subjectSchedule: Schedule,
        slottedInputStimulation: TestSlottedStimulation4? = null,
        expectedTargetImpact: ExpectedImpact,
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

            val scheduleHandle = scheduleOutcome.handle

            slottedInputStimulation?.slotStimulation1?.stimulate(
                propagationContext = propagationContext,
            )

            // 2. Cancel the schedule
            val cancelRevocable = scheduleHandle.cancel.executeInternallyWrappedUp(
                propagationContext = propagationContext,
            ).revocable

            slottedInputStimulation?.slotStimulation2?.stimulate(
                propagationContext = propagationContext,
            )

            // 3. Revoke the schedule's cancellation
            cancelRevocable.revoke()

            slottedInputStimulation?.slotStimulation3?.stimulate(
                propagationContext = propagationContext,
            )
        }
    }
}
