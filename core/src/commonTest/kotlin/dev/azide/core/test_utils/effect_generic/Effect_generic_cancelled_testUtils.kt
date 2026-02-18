package dev.azide.core.test_utils.effect_generic

import dev.azide.core.Effect
import dev.azide.core.executeInternallyWrappedUpUnpacked
import dev.azide.core.impl.Revocable
import dev.azide.core.test_utils.TestSlottedStimulation2
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.generic.ExpectedTestSubjectTransition
import dev.azide.core.test_utils.generic.TestSubjectObservationTrait
import dev.azide.core.test_utils.generic.TestSubjectObserver
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation0
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation1

@Suppress("ClassName")
data object Effect_generic_cancelled_testUtils {
    fun <SubjectT, NotificationT : Any> executeCancelTransaction(
        trait: TestSubjectObservationTrait<SubjectT, NotificationT>,
        subjectOutcome: Effect.Outcome<SubjectT>,
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedInputStimulation: TestSlottedStimulation2? = null,
        expectedSubjectTransition: ExpectedTestSubjectTransition<SubjectT, NotificationT>,
        expectedTargetImpact: ExpectedImpact,
        cancelCount: Int = 1,
    ) {
        val subject = subjectOutcome.result
        val subjectEffectHandle = subjectOutcome.handle

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

            // 0. Pre-stimulation
            slottedInputStimulation?.slotStimulation0?.stimulate(
                propagationContext = propagationContext,
            )

            // 1. Cancel the effect
            repeat(cancelCount) {
                val (_: Unit, _: Revocable) = subjectEffectHandle.cancel.executeInternallyWrappedUpUnpacked(
                    propagationContext = propagationContext,
                )
            }

            slottedInputStimulation?.slotStimulation1?.stimulate(
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
