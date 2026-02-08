package dev.azide.core.test_utils.generic

import dev.azide.core.test_utils.TestSlottedStimulation2
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation0
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation1

@Suppress("ClassName")
data object generic_reaction_testUtils {
    fun <SubjectT> executeReactionTransaction(
        subject: SubjectT,
        slottedInputStimulation: TestSlottedStimulation2,
        expectedSubjectTransition: ExpectedTestSubjectTransition<SubjectT>,
    ) {
        generic_testUtils.executeTransactionWithNewStateVerification(
            expectedNewState = expectedSubjectTransition.expectedNewState,
        ) { propagationContext ->
            // 0. Pre-stimulation
            slottedInputStimulation.slotStimulation0.stimulate(
                propagationContext = propagationContext,
            )

            // 1. Perceive the subject
            val subjectReactionVerifier = expectedSubjectTransition.expectedReaction.prepareReactionVerifierInstalled(
                propagationContext = propagationContext,
                subject = subject,
            )

            slottedInputStimulation.slotStimulation1.stimulate(
                propagationContext = propagationContext,
            )

            expectedSubjectTransition.expectedOldState.verifyStableState(
                propagationContext = propagationContext,
                subject = subject,
            )

            subjectReactionVerifier.verifyReactionUninstalling()

            subject
        }
    }
}
