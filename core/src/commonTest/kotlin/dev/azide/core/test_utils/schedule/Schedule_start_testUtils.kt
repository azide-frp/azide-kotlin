package dev.azide.core.test_utils.schedule

import dev.azide.core.Schedule
import dev.azide.core.executeInternallyWrappedUp
import dev.azide.core.test_utils.TestSlottedStimulation2
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.generic.generic_testUtils
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation0
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation1

@Suppress("ClassName")
data object Schedule_start_testUtils {
    fun executeStartTransaction(
        subjectSchedule: Schedule,
        slottedInputStimulation: TestSlottedStimulation2? = null,
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
            subjectSchedule.start.executeInternallyWrappedUp(
                propagationContext = propagationContext,
            )

            slottedInputStimulation?.slotStimulation1?.stimulate(
                propagationContext = propagationContext,
            )
        }
    }
}
