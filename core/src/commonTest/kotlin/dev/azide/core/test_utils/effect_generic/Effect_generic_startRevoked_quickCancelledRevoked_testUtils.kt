package dev.azide.core.test_utils.effect_generic

import dev.azide.core.Effect
import dev.azide.core.executeInternallyWrappedUpUnpacked
import dev.azide.core.test_utils.TestSlottedStimulation5
import dev.azide.core.test_utils.TestStimulationSlot5
import dev.azide.core.test_utils.generic.ExpectedImpact

@Suppress("ClassName")
data object Effect_generic_startRevoked_quickCancelledRevoked_testUtils {
    fun <SubjectT> executeStartTransaction(
        subjectEffect: Effect<SubjectT>,
        slottedInputStimulation: TestSlottedStimulation5? = null,
        expectedTargetImpact: ExpectedImpact,
    ) {
        Effect_generic_testUtils.executeTransactionWithImpactAndNewStateVerification(
            expectedTargetImpact = expectedTargetImpact,
            expectedNewState = null,
        ) { propagationContext ->
            // 0. Pre-stimulation

            slottedInputStimulation?.stimulate(
                propagationContext = propagationContext,
                slot = TestStimulationSlot5.Slot0,
            )

            // 1. Start the effect
            val (effectOutcome, startRevocable) = subjectEffect.start.executeInternallyWrappedUpUnpacked(
                propagationContext = propagationContext,
            )

            val subject = effectOutcome.result
            val effectHandle = effectOutcome.handle

            slottedInputStimulation?.stimulate(
                propagationContext = propagationContext,
                slot = TestStimulationSlot5.Slot1,
            )

            // 2. Cancel the effect
            val (_: Unit, cancelRevocable) = effectHandle.cancel.executeInternallyWrappedUpUnpacked(
                propagationContext = propagationContext,
            )

            slottedInputStimulation?.stimulate(
                propagationContext = propagationContext,
                slot = TestStimulationSlot5.Slot2,
            )

            // 3. Revoke the effect's cancellation
            cancelRevocable.revoke()

            slottedInputStimulation?.stimulate(
                propagationContext = propagationContext,
                slot = TestStimulationSlot5.Slot3,
            )

            // 4. Revoke the effect's start
            startRevocable.revoke()

            slottedInputStimulation?.stimulate(
                propagationContext = propagationContext,
                slot = TestStimulationSlot5.Slot4,
            )

            subject
        }
    }
}
