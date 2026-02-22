package dev.azide.core.test_utils.effect_generic

import dev.azide.core.Effect
import dev.azide.core.executeInternallyWrappedUpUnpacked
import dev.azide.core.impl.Revocable
import dev.azide.core.test_utils.TestSlottedStimulation4
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.generic.ExpectedTestSubjectTransition
import dev.azide.core.test_utils.generic.TestSubjectObservationTrait
import dev.azide.core.test_utils.generic.TestSubjectObserver
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation0
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation1
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation2
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation3

@Suppress("ClassName")
data object Effect_generic_start_quickCancelledRevoked_testUtils {
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
         * Input stimulation after the test subject effect was canceled, but before the cancellation was revoked.
         */
        val postCancelStimulation: TestStimulation,
        /**
         * Input stimulation after the test subject effect cancellation was revoked.
         */
        val postCancelRevocationStimulation: TestStimulation,
    )

    fun <SubjectT, NotificationT : Any> testStart(
        trait: TestSubjectObservationTrait<SubjectT, NotificationT>,
        subjectEffect: Effect<SubjectT>,
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        inputStimulationPlan: InputStimulationPlan? = null,
        expectedSubjectTransition: ExpectedTestSubjectTransition<SubjectT, NotificationT>,
        expectedTargetImpact: ExpectedImpact,
    ): SubjectT = Effect_generic_testUtils.executeTransactionWithImpactAndNewStateVerification(
           expectedTargetImpact = expectedTargetImpact,
           expectedNewState = expectedSubjectTransition.expectedNewState,
       ) { propagationContext ->
           // 0. Pre-stimulation
           inputStimulationPlan?.preStartStimulation?.stimulate(
               propagationContext = propagationContext,
           )

           // 1. Start the effect
           val (effectOutcome, _: Revocable) = subjectEffect.start.executeInternallyWrappedUpUnpacked(
               propagationContext = propagationContext,
           )

           val subject = effectOutcome.result
           val effectHandle = effectOutcome.handle

           inputStimulationPlan?.preCancelStimulation?.stimulate(
               propagationContext = propagationContext,
           )

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

           // 2. Cancel the effect
           val (_: Unit, cancelRevocable) = effectHandle.cancel.executeInternallyWrappedUpUnpacked(
               propagationContext = propagationContext,
           )

           inputStimulationPlan?.postCancelStimulation?.stimulate(
               propagationContext = propagationContext,
           )

           // 3. Revoke the effect's cancellation
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

    fun <SubjectT, NotificationT : Any> testStart(
        trait: TestSubjectObservationTrait<SubjectT, NotificationT>,
        subjectEffect: Effect<SubjectT>,
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedInputStimulation: TestSlottedStimulation4? = null,
        expectedSubjectTransition: ExpectedTestSubjectTransition<SubjectT, NotificationT>,
        expectedTargetImpact: ExpectedImpact,
    ): SubjectT = testStart(
        trait = trait,
        subjectEffect = subjectEffect,
        subjectPerceptionStrategy = subjectPerceptionStrategy,
        inputStimulationPlan = slottedInputStimulation?.let {
            InputStimulationPlan(
                preStartStimulation = it.slotStimulation0,
                preCancelStimulation = it.slotStimulation1,
                postCancelStimulation = it.slotStimulation2,
                postCancelRevocationStimulation = it.slotStimulation3,
            )
        },
        expectedSubjectTransition = expectedSubjectTransition,
        expectedTargetImpact = expectedTargetImpact,
    )
}
