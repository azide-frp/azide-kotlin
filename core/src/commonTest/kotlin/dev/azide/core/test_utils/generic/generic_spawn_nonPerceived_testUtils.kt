package dev.azide.core.test_utils.generic

import dev.azide.core.Moment
import dev.azide.core.pullInternallyWrappedUp
import dev.azide.core.test_utils.TestSlottedStimulation2
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation0
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation1

@Suppress("ClassName")
data object generic_spawn_nonPerceived_testUtils {
    fun <SubjectT> executeSpawnTransaction(
        subjectSpawnMoment: Moment<SubjectT>,
        slottedInputStimulation: TestSlottedStimulation2? = null,
        expectedOldState: ExpectedTestSubjectState<SubjectT>,
        expectedNewState: ExpectedTestSubjectState<SubjectT>,
    ) {
        generic_testUtils.executeTransactionWithNewStateVerification(
            expectedNewState = expectedNewState,
        ) { propagationContext ->
            // 0. Pre-stimulation
            slottedInputStimulation?.slotStimulation0?.stimulate(
                propagationContext = propagationContext,
            )

            // 1. Spawn the subject
            val subject = subjectSpawnMoment.pullInternallyWrappedUp(
                propagationContext = propagationContext,
            )

            expectedOldState.verifyStableState(
                propagationContext = propagationContext,
                subject = subject,
            )

            slottedInputStimulation?.slotStimulation1?.stimulate(
                propagationContext = propagationContext,
            )

            expectedOldState.verifyStableState(
                propagationContext = propagationContext,
                subject = subject,
            )

            subject
        }
    }
}
