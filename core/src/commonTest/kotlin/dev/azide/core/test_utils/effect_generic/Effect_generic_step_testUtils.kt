package dev.azide.core.test_utils.effect_generic

import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.generic.ExpectedTestSubjectTransition
import dev.azide.core.test_utils.generic.TestSubjectObservationTrait
import dev.azide.core.test_utils.generic.TestSubjectObserver

@Suppress("ClassName")
data object Effect_generic_step_testUtils {
    fun <SubjectT, NotificationT : Any> executeStepTransaction(
        trait: TestSubjectObservationTrait<SubjectT, NotificationT>,
        subject: SubjectT,
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        inputStimulation: TestStimulation,
        expectedSubjectTransition: ExpectedTestSubjectTransition<SubjectT, NotificationT>,
        expectedTargetImpact: ExpectedImpact,
    ) {
        Effect_generic_testUtils.executeTransactionWithImpactAndNewStateVerification(
            expectedTargetImpact = expectedTargetImpact,
            expectedNewState = expectedSubjectTransition.expectedNewState,
        ) { propagationContext ->
            val subjectObserver = TestSubjectObserver.observeWithStrategy(
                trait = trait,
                subject = subject,
                propagationContext = propagationContext,
                subjectPerceptionStrategy = subjectPerceptionStrategy,
            )

            // Verify the old state for the first time
            expectedSubjectTransition.expectedOldState.verifyStableState(
                propagationContext = propagationContext,
                subject = subject,
            )

            inputStimulation.stimulate(
                propagationContext = propagationContext,
            )

            // Verify the old state again (to ensure its stability)
            expectedSubjectTransition.expectedOldState.verifyStableState(
                propagationContext = propagationContext,
                subject = subject,
            )

            subjectObserver?.let {
                expectedSubjectTransition.expectedReaction.verifyReaction(
                    trait = trait,
                    subject = subject,
                    subjectObserver = it,
                )
                it.unobserve()
            }

            subject
        }
    }
}
