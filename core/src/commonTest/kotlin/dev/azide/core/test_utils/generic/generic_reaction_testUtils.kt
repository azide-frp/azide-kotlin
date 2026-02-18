package dev.azide.core.test_utils.generic

import dev.azide.core.test_utils.TestSlottedStimulation2
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation0
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation1

@Suppress("ClassName")
data object generic_reaction_testUtils {
    fun <SubjectT, NotificationT : Any> executeReactionTransaction(
        trait: TestSubjectObservationTrait<SubjectT, NotificationT>,
        subject: SubjectT,
        slottedInputStimulation: TestSlottedStimulation2,
        expectedSubjectTransition: ExpectedTestSubjectTransition<SubjectT, NotificationT>,
    ) {
        generic_testUtils.executeTransactionWithNewStateVerification(
            expectedNewState = expectedSubjectTransition.expectedNewState,
        ) { propagationContext ->
            // 0. Pre-stimulation
            slottedInputStimulation.slotStimulation0.stimulate(
                propagationContext = propagationContext,
            )

            // 1. Observe the subject
            val subjectObserver = TestSubjectObserver.observe(
                trait = trait,
                subject = subject,
                propagationContext = propagationContext,
            )

            slottedInputStimulation.slotStimulation1.stimulate(
                propagationContext = propagationContext,
            )

            expectedSubjectTransition.expectedOldState.verifyStableState(
                propagationContext = propagationContext,
                subject = subject,
            )

            expectedSubjectTransition.expectedReaction.verifyReaction(
                trait = trait,
                subject = subject,
                subjectObserver = subjectObserver,
            )

            // TODO: Test _without_ unobserving (other paths are triggered for stateless vertices!)
            subjectObserver.unobserve()

            subject
        }
    }
}
