package dev.azide.core.test_utils.schedule

import dev.azide.core.Schedule
import dev.azide.core.executeInternallyWrappedUpUnpacked
import dev.azide.core.test_utils.TestSlottedStimulation5
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation0
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation1
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation2
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation3
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation4

@Suppress("ClassName")
data object Schedule_startRevoked_quickCancelledRevoked_testUtils {
    fun executeStartTransaction(
        subjectSchedule: Schedule,
        slottedInputStimulation: TestSlottedStimulation5? = null,
        expectedTargetImpact: ExpectedImpact,
    ) {
        Schedule_testUtils.executeTransactionWithImpactVerification(
            expectedTargetImpact = expectedTargetImpact,
        ) { propagationContext ->
            // 0. Pre-stimulation

            slottedInputStimulation?.slotStimulation0?.stimulate(
                propagationContext = propagationContext,
            )

            // 1. Start the schedule
            val (scheduleOutcome, startRevocable) = subjectSchedule.start.executeInternallyWrappedUpUnpacked(
                propagationContext = propagationContext,
            )

            val subject = scheduleOutcome.result
            val scheduleHandle = scheduleOutcome.handle

            slottedInputStimulation?.slotStimulation1?.stimulate(
                propagationContext = propagationContext,
            )

            // 2. Cancel the schedule
            val (_: Unit, cancelRevocable) = scheduleHandle.cancel.executeInternallyWrappedUpUnpacked(
                propagationContext = propagationContext,
            )

            slottedInputStimulation?.slotStimulation2?.stimulate(
                propagationContext = propagationContext,
            )

            // 3. Revoke the schedule's cancellation
            cancelRevocable.revoke()

            slottedInputStimulation?.slotStimulation3?.stimulate(
                propagationContext = propagationContext,
            )

            // 4. Revoke the schedule's start
            startRevocable.revoke()

            slottedInputStimulation?.slotStimulation4?.stimulate(
                propagationContext = propagationContext,
            )
        }
    }
}
