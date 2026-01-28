package dev.azide.core.test_utils.stateful

import dev.azide.core.test_utils.ExpectedTestSubjectState
import dev.azide.core.test_utils.TestStimulation

@Suppress("ClassName")
data object StatefulTestUtils_reaction_nonPerceived {
    fun <SubjectT> executeReactionTransaction(
        subject: SubjectT,
        inputStimulation: TestStimulation,
        expectedOldState: ExpectedTestSubjectState<SubjectT>,
        expectedNewState: ExpectedTestSubjectState<SubjectT>,
    ) {
        StatefulTestUtils.executeTransactionWithNewStateVerification(
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
