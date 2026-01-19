package dev.azide.core.test_utils.stateful

import dev.azide.core.test_utils.ExpectedTestSubjectTransition
import dev.azide.core.test_utils.TestSlottedStimulation2
import dev.azide.core.test_utils.TestStimulationSlot2
import dev.azide.core.test_utils.prepareReactionVerifierInstalled
import dev.azide.core.test_utils.verifyReactionUninstalling

@Suppress("ClassName")
data object StatefulTestUtils_reaction {
    fun <SubjectT> executeReactionTransaction(
        subject: SubjectT,
        slottedInputStimulation: TestSlottedStimulation2,
        expectedSubjectTransition: ExpectedTestSubjectTransition<SubjectT>,
    ) {
        StatefulTestUtils.executeTransactionWithNewStateVerification(
            expectedNewState = expectedSubjectTransition.expectedNewState,
        ) { propagationContext ->
            // 0. Pre-stimulation
            slottedInputStimulation.stimulate(
                propagationContext = propagationContext,
                slot = TestStimulationSlot2.Slot0,
            )

            // 1. Perceive the subject
            val subjectReactionVerifier = expectedSubjectTransition.expectedReaction.prepareReactionVerifierInstalled(
                propagationContext = propagationContext,
                subject = subject,
            )

            slottedInputStimulation.stimulate(
                propagationContext = propagationContext,
                slot = TestStimulationSlot2.Slot1,
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
