package dev.azide.core.test_utils.effect_generic

import dev.azide.core.Effect
import dev.azide.core.executeInternallyWrappedUpUnpacked
import dev.azide.core.test_utils.TestSlottedStimulation4
import dev.azide.core.test_utils.TestStimulationSlot4
import dev.azide.core.test_utils.generic.ExpectedImpact

@Suppress("ClassName")
data object Effect_generic_startRevoked_quickCancelled_testUtils {
    fun <SubjectT> executeStartTransaction(
        subjectEffect: Effect<SubjectT>,
        slottedInputStimulation: TestSlottedStimulation4? = null,
        expectedTargetImpact: ExpectedImpact,
    ) {
        Effect_generic_testUtils.executeTransactionWithImpactAndNewStateVerification(
            expectedTargetImpact = expectedTargetImpact,
            expectedNewState = null,
        ) { propagationContext ->
            // 0. Pre-stimulation
            slottedInputStimulation?.stimulate(
                propagationContext = propagationContext,
                slot = TestStimulationSlot4.Slot0,
            )

            // 1. Start the effect
            val (effectOutcome, startRevocable) = subjectEffect.start.executeInternallyWrappedUpUnpacked(
                propagationContext = propagationContext,
            )

            val subject = effectOutcome.result
            val effectHandle = effectOutcome.handle

            slottedInputStimulation?.stimulate(
                propagationContext = propagationContext,
                slot = TestStimulationSlot4.Slot1,
            )

            // 2. Cancel the effect
            val (_, cancelRevocable) = effectHandle.cancel.executeInternallyWrappedUpUnpacked(
                propagationContext = propagationContext,
            )

            slottedInputStimulation?.stimulate(
                propagationContext = propagationContext,
                slot = TestStimulationSlot4.Slot2,
            )

            // 3. Revoke the effect's start
            startRevocable.revoke()

            slottedInputStimulation?.stimulate(
                propagationContext = propagationContext,
                slot = TestStimulationSlot4.Slot3,
            )

            // Extra: Revoke the effect's cancellation
            cancelRevocable.revoke()

            subject
        }
    }
}
