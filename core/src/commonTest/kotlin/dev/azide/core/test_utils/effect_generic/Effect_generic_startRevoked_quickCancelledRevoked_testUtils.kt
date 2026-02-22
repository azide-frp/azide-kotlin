package dev.azide.core.test_utils.effect_generic

import dev.azide.core.Effect
import dev.azide.core.executeInternallyWrappedUpUnpacked
import dev.azide.core.test_utils.TestSlottedStimulation5
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation0
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation1
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation2
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation3
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation4

@Suppress("ClassName")
data object Effect_generic_startRevoked_quickCancelledRevoked_testUtils {
    fun <SubjectT> testStart(
        subjectEffect: Effect<SubjectT>,
        slottedInputStimulation: TestSlottedStimulation5? = null,
        expectedTargetImpact: ExpectedImpact,
    ) {
        Effect_generic_testUtils.executeTransactionWithImpactAndNewStateVerification(
            expectedTargetImpact = expectedTargetImpact,
            expectedNewState = null,
        ) { propagationContext ->
            // 0. Pre-stimulation

            slottedInputStimulation?.slotStimulation0?.stimulate(
                propagationContext = propagationContext,
            )

            // 1. Start the effect
            val (effectOutcome, startRevocable) = subjectEffect.start.executeInternallyWrappedUpUnpacked(
                propagationContext = propagationContext,
            )

            val subject = effectOutcome.result
            val effectHandle = effectOutcome.handle

            slottedInputStimulation?.slotStimulation1?.stimulate(
                propagationContext = propagationContext,
            )

            // 2. Cancel the effect
            val (_: Unit, cancelRevocable) = effectHandle.cancel.executeInternallyWrappedUpUnpacked(
                propagationContext = propagationContext,
            )

            slottedInputStimulation?.slotStimulation2?.stimulate(
                propagationContext = propagationContext,
            )

            // 3. Revoke the effect's cancellation
            cancelRevocable.revoke()

            slottedInputStimulation?.slotStimulation3?.stimulate(
                propagationContext = propagationContext,
            )

            // 4. Revoke the effect's start
            startRevocable.revoke()

            slottedInputStimulation?.slotStimulation4?.stimulate(
                propagationContext = propagationContext,
            )

            subject
        }
    }
}
