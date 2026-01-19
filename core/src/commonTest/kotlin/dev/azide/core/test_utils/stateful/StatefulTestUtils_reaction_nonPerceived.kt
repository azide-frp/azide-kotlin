package dev.azide.core.test_utils.stateful

import dev.azide.core.test_utils.ExpectedTestSubjectState
import dev.azide.core.test_utils.ExpectedTestSubjectTransition
import dev.azide.core.test_utils.TestInputStimulation
import dev.azide.core.test_utils.TestSlottedStimulation2
import dev.azide.core.test_utils.TestStimulationSlot2
import dev.azide.core.test_utils.prepareReactionVerifierInstalled
import dev.azide.core.test_utils.verifyReactionUninstalling

@Suppress("ClassName")
data object StatefulTestUtils_reaction_nonPerceived {
    fun <SubjectT> executeReactionTransaction(
        subject: SubjectT,
        inputStimulation: TestInputStimulation,
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
