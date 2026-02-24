package dev.azide.core.test_utils.effect_generic

import dev.azide.core.Effect
import dev.azide.core.executeInternallyWrappedUpUnpacked
import dev.azide.core.impl.Revocable
import dev.azide.core.test_utils.TestSlottedStimulation3
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.generic.ExpectedTestSubjectTransition
import dev.azide.core.test_utils.generic.TestSubjectObservationTrait
import dev.azide.core.test_utils.generic.TestSubjectObserver
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation0
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation1
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation2

@Suppress("ClassName")
data object Effect_generic_cancelledRevoked_testUtils {
    data class InputStimulationPlan(
        /**
         * Input stimulation before the test subject effect was canceled.
         */
        val preCancelStimulation: TestStimulation,
        /**
         * Input stimulation after the test subject effect was canceled, but before the cancellation was revoked.
         */
        val postCancelStimulation: TestStimulation,
        /**
         * Input stimulation after the test subject effect cancellation was revoked.
         */
        val postCancelRevocationStimulation: TestStimulation,
    )

    fun <SubjectT, NotificationT : Any> testCancel(
        trait: TestSubjectObservationTrait<SubjectT, NotificationT>,
        subjectOutcome: Effect.Outcome<SubjectT>,
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        inputStimulationPlan: InputStimulationPlan? = null,
        expectedSubjectTransition: ExpectedTestSubjectTransition<SubjectT, NotificationT>,
        expectedTargetImpact: ExpectedImpact,
    ) {
        val subject = subjectOutcome.result
        val subjectHandle = subjectOutcome.handle

        Effect_generic_testUtils.executeTransactionWithImpactAndNewStateVerification(
            expectedTargetImpact = expectedTargetImpact,
            expectedNewState = expectedSubjectTransition.expectedNewState,
        ) { propagationContext ->
            val subjectObserver = TestSubjectObserver.observeWithStrategy(
                trait = trait,
                subject = subject,
                propagationContext = propagationContext,
                subjectPerceptionStrategy = subjectPerceptionStrategy,
            )

            // Verify the old state for the first time
            expectedSubjectTransition.expectedOldState.verifyStableState(
                propagationContext = propagationContext,
                subject = subject,
            )

            // 0. Pre-stimulation
            inputStimulationPlan?.preCancelStimulation?.stimulate(
                propagationContext = propagationContext,
            )

            // 1. Cancel the effect
            val (_: Unit, cancelRevocable: Revocable) = subjectHandle.cancel.executeInternallyWrappedUpUnpacked(
                propagationContext = propagationContext,
            )

            inputStimulationPlan?.postCancelStimulation?.stimulate(
                propagationContext = propagationContext,
            )

            // 2. Revoke the effect's cancellation
            cancelRevocable.revoke()

            inputStimulationPlan?.postCancelRevocationStimulation?.stimulate(
                propagationContext = propagationContext,
            )

            // Verify the old state again (to ensure its stability)
            expectedSubjectTransition.expectedOldState.verifyStableState(
                propagationContext = propagationContext,
                subject = subject,
            )

            subjectObserver?.let {
                expectedSubjectTransition.expectedReaction.verifyReaction(
                    trait = trait,
                    subject = subject,
                    subjectObserver = it,
                )
                it.unobserve()
            }

            subject
        }
    }

    fun <SubjectT, NotificationT : Any> testCancel(
        trait: TestSubjectObservationTrait<SubjectT, NotificationT>,
        subjectOutcome: Effect.Outcome<SubjectT>,
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedInputStimulation: TestSlottedStimulation3? = null,
        expectedSubjectTransition: ExpectedTestSubjectTransition<SubjectT, NotificationT>,
        expectedTargetImpact: ExpectedImpact,
    ) {
        testCancel(
            trait = trait,
            subjectOutcome = subjectOutcome,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            inputStimulationPlan = slottedInputStimulation?.let {
                InputStimulationPlan(
                    preCancelStimulation = it.slotStimulation0,
                    postCancelStimulation = it.slotStimulation1,
                    postCancelRevocationStimulation = it.slotStimulation2,
                )
            },
            expectedSubjectTransition = expectedSubjectTransition,
            expectedTargetImpact = expectedTargetImpact,
        )
    }
}
