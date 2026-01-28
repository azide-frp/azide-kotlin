package dev.azide.core.test_utils.generic

import dev.azide.core.Moment
import dev.azide.core.pullInternallyWrappedUp
import dev.azide.core.test_utils.ExpectedTestSubjectState
import dev.azide.core.test_utils.TestSlottedStimulation2
import dev.azide.core.test_utils.TestStimulationSlot2

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
            slottedInputStimulation?.stimulate(
                propagationContext = propagationContext,
                slot = TestStimulationSlot2.Slot0,
            )

            // 1. Spawn the subject
            val subject = subjectSpawnMoment.pullInternallyWrappedUp(
                propagationContext = propagationContext,
            )

            expectedOldState.verifyStableState(
                propagationContext = propagationContext,
                subject = subject,
            )

            slottedInputStimulation?.stimulate(
                propagationContext = propagationContext,
                slot = TestStimulationSlot2.Slot1,
            )

            expectedOldState.verifyStableState(
                propagationContext = propagationContext,
                subject = subject,
            )

            subject
        }
    }
}
