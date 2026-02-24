package dev.azide.core.test_utils.effect_generic

import dev.azide.core.Effect
import dev.azide.core.executeInternallyWrappedUpUnpacked
import dev.azide.core.test_utils.TestSlottedStimulation4
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation0
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation1
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation2
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation3

@Suppress("ClassName")
data object Effect_generic_startRevoked_quickCancelled_testUtils {
    data class InputStimulationPlan(
        /**
         * Input stimulation before the test subject effect was started.
         */
        val preStartStimulation: TestStimulation,
        /**
         * Input stimulation after the test subject effect was started, but before it was canceled.
         */
        val preCancelStimulation: TestStimulation,
        /**
         * Input stimulation after the test subject effect was canceled, but before the start was revoked.
         */
        val postCancelStimulation: TestStimulation,
        /**
         * Input stimulation after the test subject effect start was revoked.
         */
        val postStartRevocationStimulation: TestStimulation,
    )

    fun <SubjectT> testStart(
        subjectEffect: Effect<SubjectT>,
        inputStimulationPlan: InputStimulationPlan? = null,
        expectedTargetImpact: ExpectedImpact,
    ) {
        Effect_generic_testUtils.executeTransactionWithImpactAndNewStateVerification(
            expectedTargetImpact = expectedTargetImpact,
            expectedNewState = null,
        ) { propagationContext ->
            // 0. Pre-stimulation
            inputStimulationPlan?.preStartStimulation?.stimulate(
                propagationContext = propagationContext,
            )

            // 1. Start the effect
            val (effectOutcome, startRevocable) = subjectEffect.start.executeInternallyWrappedUpUnpacked(
                propagationContext = propagationContext,
            )

            val subject = effectOutcome.result
            val effectHandle = effectOutcome.handle

            inputStimulationPlan?.preCancelStimulation?.stimulate(
                propagationContext = propagationContext,
            )

            // 2. Cancel the effect
            val (_, cancelRevocable) = effectHandle.cancel.executeInternallyWrappedUpUnpacked(
                propagationContext = propagationContext,
            )

            inputStimulationPlan?.postCancelStimulation?.stimulate(
                propagationContext = propagationContext,
            )

            // 3. Revoke the effect's start
            startRevocable.revoke()

            inputStimulationPlan?.postStartRevocationStimulation?.stimulate(
                propagationContext = propagationContext,
            )

            // Extra: Revoke the effect's cancellation
            cancelRevocable.revoke()

            subject
        }
    }

    fun <SubjectT> testStart(
        subjectEffect: Effect<SubjectT>,
        slottedInputStimulation: TestSlottedStimulation4? = null,
        expectedTargetImpact: ExpectedImpact,
    ) {
        testStart(
            subjectEffect = subjectEffect,
            inputStimulationPlan = slottedInputStimulation?.let {
                InputStimulationPlan(
                    preStartStimulation = it.slotStimulation0,
                    preCancelStimulation = it.slotStimulation1,
                    postCancelStimulation = it.slotStimulation2,
                    postStartRevocationStimulation = it.slotStimulation3,
                )
            },
            expectedTargetImpact = expectedTargetImpact,
        )
    }
}
