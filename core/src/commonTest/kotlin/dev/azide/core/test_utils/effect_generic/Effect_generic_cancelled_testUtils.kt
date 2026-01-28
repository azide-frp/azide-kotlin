package dev.azide.core.test_utils.effect_generic

import dev.azide.core.Effect
import dev.azide.core.executeInternallyWrappedUp
import dev.azide.core.impl.Revocable
import dev.azide.core.test_utils.ExpectedTestSubjectReaction.TestSubjectReactionVerifier
import dev.azide.core.test_utils.ExpectedTestSubjectTransition
import dev.azide.core.test_utils.ExpectedImpact
import dev.azide.core.test_utils.TestSlottedStimulation2
import dev.azide.core.test_utils.TestStimulationSlot2
import dev.azide.core.test_utils.prepareReactionVerifierWithStrategyInstalled
import dev.azide.core.test_utils.verifyReactionUninstalling

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
                val (_: Unit, _: Revocable) = subjectEffectHandle.cancel.executeInternallyWrappedUp(
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
