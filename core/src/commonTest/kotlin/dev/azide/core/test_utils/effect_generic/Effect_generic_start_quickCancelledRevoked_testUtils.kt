package dev.azide.core.test_utils.effect_generic

import dev.azide.core.Effect
import dev.azide.core.executeInternallyWrappedUp
import dev.azide.core.impl.Revocable
import dev.azide.core.test_utils.ExpectedTestSubjectReaction.TestSubjectReactionVerifier
import dev.azide.core.test_utils.ExpectedTestSubjectTransition
import dev.azide.core.test_utils.ExpectedImpact
import dev.azide.core.test_utils.TestSlottedStimulation4
import dev.azide.core.test_utils.TestStimulationSlot4
import dev.azide.core.test_utils.prepareReactionVerifierWithStrategyInstalled
import dev.azide.core.test_utils.verifyReactionUninstalling

@Suppress("ClassName")
data object Effect_generic_start_quickCancelledRevoked_testUtils {
    fun <SubjectT> executeStartTransaction(
        subjectEffect: Effect<SubjectT>,
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedInputStimulation: TestSlottedStimulation4? = null,
        expectedSubjectTransition: ExpectedTestSubjectTransition<SubjectT>,
        expectedTargetImpact: ExpectedImpact,
    ): SubjectT = Effect_generic_testUtils.executeTransactionWithImpactAndNewStateVerification(
           expectedTargetImpact = expectedTargetImpact,
           expectedNewState = expectedSubjectTransition.expectedNewState,
       ) { propagationContext ->
           // 0. Pre-stimulation
           slottedInputStimulation?.stimulate(
               propagationContext = propagationContext,
               slot = TestStimulationSlot4.Slot0,
           )

           // 1. Start the effect
           val (effectOutcome, _: Revocable) = subjectEffect.start.executeInternallyWrappedUp(
               propagationContext = propagationContext,
           )

           val subject = effectOutcome.result
           val effectHandle = effectOutcome.handle

           slottedInputStimulation?.stimulate(
               propagationContext = propagationContext,
               slot = TestStimulationSlot4.Slot1,
           )

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

           // 2. Cancel the effect
           val (_: Unit, cancelRevocable) = effectHandle.cancel.executeInternallyWrappedUp(
               propagationContext = propagationContext,
           )

           slottedInputStimulation?.stimulate(
               propagationContext = propagationContext,
               slot = TestStimulationSlot4.Slot2,
           )

           // 3. Revoke the effect's cancellation
           cancelRevocable.revoke()

           slottedInputStimulation?.stimulate(
               propagationContext = propagationContext,
               slot = TestStimulationSlot4.Slot3,
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
