package dev.azide.core.test_utils.effect_generic

import dev.azide.core.Effect
import dev.azide.core.executeInternallyWrappedUpUnpacked
import dev.azide.core.impl.Revocable
import dev.azide.core.test_utils.TestSlottedStimulation2
import dev.azide.core.test_utils.TestStimulationSlot2
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.TestSubjectReactionVerifier
import dev.azide.core.test_utils.generic.ExpectedTestSubjectTransition
import dev.azide.core.test_utils.generic.prepareReactionVerifierWithStrategyInstalled
import dev.azide.core.test_utils.generic.verifyReactionUninstalling

@Suppress("ClassName")
data object Effect_generic_cancelled_testUtils {
    fun <SubjectT> executeCancelTransaction(
        subjectOutcome: Effect.Outcome<SubjectT>,
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedInputStimulation: TestSlottedStimulation2? = null,
        expectedSubjectTransition: ExpectedTestSubjectTransition<SubjectT>,
        expectedTargetImpact: ExpectedImpact,
        cancelCount: Int = 1,
    ) {
        val subject = subjectOutcome.result
        val subjectEffectHandle = subjectOutcome.handle

        Effect_generic_testUtils.executeTransactionWithImpactAndNewStateVerification(
            expectedTargetImpact = expectedTargetImpact,
            expectedNewState = expectedSubjectTransition.expectedNewState,
        ) { propagationContext ->
            val subjectReactionVerifier: TestSubjectReactionVerifier? =
                expectedSubjectTransition.expectedReaction.prepareReactionVerifierWithStrategyInstalled(
                    propagationContext = propagationContext,
                    subject = subject,
                    strategy = subjectPerceptionStrategy,
                )

            // Verify the old state for the first time
            expectedSubjectTransition.expectedOldState.verifyStableState(
                propagationContext = propagationContext,
                subject = subject,
            )

            // 0. Pre-stimulation
            slottedInputStimulation?.stimulate(
                propagationContext = propagationContext,
                slot = TestStimulationSlot2.Slot0,
            )

            // 1. Cancel the effect
            repeat(cancelCount) {
                val (_: Unit, _: Revocable) = subjectEffectHandle.cancel.executeInternallyWrappedUpUnpacked(
                    propagationContext = propagationContext,
                )
            }

            slottedInputStimulation?.stimulate(
                propagationContext = propagationContext,
                slot = TestStimulationSlot2.Slot1,
            )

            // Verify the old state again (to ensure its stability)
            expectedSubjectTransition.expectedOldState.verifyStableState(
                propagationContext = propagationContext,
                subject = subject,
            )

            subjectReactionVerifier?.verifyReactionUninstalling()

            subject
        }
    }
}
