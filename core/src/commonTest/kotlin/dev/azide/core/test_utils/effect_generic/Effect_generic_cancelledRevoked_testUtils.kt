package dev.azide.core.test_utils.effect_generic

import dev.azide.core.Effect
import dev.azide.core.executeInternallyWrappedUpUnpacked
import dev.azide.core.impl.Revocable
import dev.azide.core.test_utils.TestSlottedStimulation3
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.TestSubjectReactionVerifier
import dev.azide.core.test_utils.generic.ExpectedTestSubjectTransition
import dev.azide.core.test_utils.generic.prepareReactionVerifierWithStrategyInstalled
import dev.azide.core.test_utils.generic.verifyReactionUninstalling
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation0
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation1
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation2

@Suppress("ClassName")
data object Effect_generic_cancelledRevoked_testUtils {
    fun <SubjectT, NotificationT : Any> executeCancelTransaction(
        subjectOutcome: Effect.Outcome<SubjectT>,
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedInputStimulation: TestSlottedStimulation3? = null,
        expectedSubjectTransition: ExpectedTestSubjectTransition<SubjectT, NotificationT>,
        expectedTargetImpact: ExpectedImpact,
    ) {
        val subject = subjectOutcome.result
        val subjectHandle = subjectOutcome.handle

        Effect_generic_testUtils.executeTransactionWithImpactAndNewStateVerification(
            expectedTargetImpact = expectedTargetImpact,
            expectedNewState = expectedSubjectTransition.expectedNewState,
        ) { propagationContext ->
            val subjectReactionVerifier: TestSubjectReactionVerifier<SubjectT, NotificationT>? =
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
            slottedInputStimulation?.slotStimulation0?.stimulate(
                propagationContext = propagationContext,
            )

            // 1. Cancel the effect
            val (_: Unit, cancelRevocable: Revocable) = subjectHandle.cancel.executeInternallyWrappedUpUnpacked(
                propagationContext = propagationContext,
            )

            slottedInputStimulation?.slotStimulation1?.stimulate(
                propagationContext = propagationContext,
            )

            // 2. Revoke the effect's cancellation
            cancelRevocable.revoke()

            slottedInputStimulation?.slotStimulation2?.stimulate(
                propagationContext = propagationContext,
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
