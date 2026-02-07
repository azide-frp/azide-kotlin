package dev.azide.core.test_utils.effect_generic

import dev.azide.core.Effect
import dev.azide.core.executeInternallyWrappedUpUnpacked
import dev.azide.core.impl.Revocable
import dev.azide.core.test_utils.ExpectedTestSubjectReaction.TestSubjectReactionVerifier
import dev.azide.core.test_utils.ExpectedTestSubjectTransition
import dev.azide.core.test_utils.ExpectedImpact
import dev.azide.core.test_utils.TestSlottedStimulation3
import dev.azide.core.test_utils.TestStimulationSlot3
import dev.azide.core.test_utils.prepareReactionVerifierWithStrategyInstalled
import dev.azide.core.test_utils.verifyReactionUninstalling

@Suppress("ClassName")
data object Effect_generic_start_quickCancelled_testUtils {
    fun <SubjectT> executeStartTransaction(
        subjectEffect: Effect<SubjectT>,
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedInputStimulation: TestSlottedStimulation3? = null,
        expectedSubjectTransition: ExpectedTestSubjectTransition<SubjectT>,
        expectedTargetImpact: ExpectedImpact,
        cancelCount: Int = 1,
    ): SubjectT = Effect_generic_testUtils.executeTransactionWithImpactAndNewStateVerification(
        expectedTargetImpact = expectedTargetImpact,
        expectedNewState = expectedSubjectTransition.expectedNewState,
    ) { propagationContext ->
        // 0. Pre-stimulation

        slottedInputStimulation?.stimulate(
            propagationContext = propagationContext,
            slot = TestStimulationSlot3.Slot0,
        )

        // 1. Start the effect
        val (effectOutcome, _: Revocable) = subjectEffect.start.executeInternallyWrappedUpUnpacked(
            propagationContext = propagationContext,
        )

        val subject = effectOutcome.result
        val subjectEffectHandle = effectOutcome.handle

        slottedInputStimulation?.stimulate(
            propagationContext = propagationContext,
            slot = TestStimulationSlot3.Slot1,
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
        repeat(cancelCount) {
            val (_: Unit, _: Revocable) = subjectEffectHandle.cancel.executeInternallyWrappedUpUnpacked(
                propagationContext = propagationContext,
            )
        }

        slottedInputStimulation?.stimulate(
            propagationContext = propagationContext,
            slot = TestStimulationSlot3.Slot2,
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
