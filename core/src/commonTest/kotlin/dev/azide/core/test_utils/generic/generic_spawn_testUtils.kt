package dev.azide.core.test_utils.generic

import dev.azide.core.Moment
import dev.azide.core.pullInternallyWrappedUp
import dev.azide.core.test_utils.TestSlottedStimulation3
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation0
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation1
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation2

@Suppress("ClassName")
data object generic_spawn_testUtils {
    fun <SubjectT, NotificationT : Any> executeSpawnTransaction(
        trait: TestSubjectObservationTrait<SubjectT, NotificationT>,
        subjectSpawnMoment: Moment<SubjectT>,
        slottedInputStimulation: TestSlottedStimulation3? = null,
        expectedSubjectTransition: ExpectedTestSubjectTransition<SubjectT, NotificationT>,
    ) {
        generic_testUtils.executeTransactionWithNewStateVerification(
            expectedNewState = expectedSubjectTransition.expectedNewState,
        ) { propagationContext ->
            // 0. Pre-stimulation
            slottedInputStimulation?.slotStimulation0?.stimulate(
                propagationContext = propagationContext,
            )

            // 1. Spawn the subject
            val subject = subjectSpawnMoment.pullInternallyWrappedUp(
                propagationContext = propagationContext,
            )

            slottedInputStimulation?.slotStimulation1?.stimulate(
                propagationContext = propagationContext,
            )

            // 2. Observe the subject
            val subjectObserver = TestSubjectObserver.observe(
                trait = trait,
                subject = subject,
                propagationContext = propagationContext,
            )

            slottedInputStimulation?.slotStimulation2?.stimulate(
                propagationContext = propagationContext,
            )

            expectedSubjectTransition.expectedOldState.verifyStableState(
                propagationContext = propagationContext,
                subject = subject,
            )

            expectedSubjectTransition.expectedReaction.verifyReaction(
                trait = trait,
                subject = subject,
                subjectObserver = subjectObserver,
            )

            // TODO: Test _without_ unobserving (other paths are triggered for stateless vertices!)
            subjectObserver.unobserve()

            subject
        }
    }
}
