package dev.azide.core.test_utils.effect_generic

import dev.azide.core.Effect
import dev.azide.core.executeInternallyWrappedUpUnpacked
import dev.azide.core.test_utils.TestSlottedStimulation3
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation0
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation1
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation2

@Suppress("ClassName")
data object Effect_generic_startRevoked_testUtils {
    data class InputStimulationPlan(
        /**
         * Input stimulation before the test subject effect was started.
         */
        val preStartStimulation: TestStimulation,
        /**
         * Input stimulation after the test subject effect was started, but before it was revoked.
         */
        val postStartStimulation: TestStimulation,
        /**
         * Input stimulation after the test subject effect's start was revoked.
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

            inputStimulationPlan?.postStartStimulation?.stimulate(
                propagationContext = propagationContext,
            )

            // 2. Revoke the effect's start
            startRevocable.revoke()

            inputStimulationPlan?.postStartRevocationStimulation?.stimulate(
                propagationContext = propagationContext,
            )

            subject
        }
    }

    fun <SubjectT> testStart(
        subjectEffect: Effect<SubjectT>,
        slottedInputStimulation: TestSlottedStimulation3? = null,
        expectedTargetImpact: ExpectedImpact,
    ) {
        testStart(
            subjectEffect = subjectEffect,
            inputStimulationPlan = slottedInputStimulation?.let {
                InputStimulationPlan(
                    preStartStimulation = it.slotStimulation0,
                    postStartStimulation = it.slotStimulation1,
                    postStartRevocationStimulation = it.slotStimulation2,
                )
            },
            expectedTargetImpact = expectedTargetImpact,
        )
    }
}
