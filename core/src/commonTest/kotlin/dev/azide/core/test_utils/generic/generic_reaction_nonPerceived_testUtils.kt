package dev.azide.core.test_utils.generic

import dev.azide.core.test_utils.ExpectedTestSubjectState
import dev.azide.core.test_utils.TestStimulation

@Suppress("ClassName")
data object generic_reaction_nonPerceived_testUtils {
    fun <SubjectT> executeReactionTransaction(
        subject: SubjectT,
        inputStimulation: TestStimulation,
        expectedOldState: ExpectedTestSubjectState<SubjectT>,
        expectedNewState: ExpectedTestSubjectState<SubjectT>,
    ) {
        generic_testUtils.executeTransactionWithNewStateVerification(
            expectedNewState = expectedNewState,
        ) { propagationContext ->
            expectedOldState.verifyStableState(
                propagationContext = propagationContext,
                subject = subject,
            )

            inputStimulation.stimulate(
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
