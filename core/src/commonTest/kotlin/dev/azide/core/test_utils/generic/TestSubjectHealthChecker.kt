package dev.azide.core.test_utils.generic

import dev.azide.core.impl.Transactions
import dev.azide.core.test_utils.TestStimulation
import kotlin.test.assertTrue

/**
 * Operator-dependent helper that verifies whether the test subject is in good health after the reaction transaction.
 */
interface TestSubjectHealthChecker<SubjectT, NotificationT : Any> {
    data class HealthCheckDescription<SubjectT, NotificationT : Any>(
        /**
         * The stimulation of the subject's input(s).
         */
        val inputStimulation: TestStimulation,
        /**
         * The expected transition of the subject's state in response to the provided stimulation.
         */
        val expectedSubjectTransition: ExpectedTestSubjectTransition<SubjectT, NotificationT>,
    )

    companion object {
        fun <SubjectT, NotificationT : Any> checkHealthPassively(
            trait: TestSubjectObservationTrait<SubjectT, NotificationT>,
            subject: SubjectT,
            subjectObserver: TestSubjectObserver<SubjectT, NotificationT>,
            healthCheckDescription: HealthCheckDescription<SubjectT, NotificationT>,
        ) {
            val expectedSubjectTransition = healthCheckDescription.expectedSubjectTransition

            // Execute a health check transaction after unobserving the test subject
            Transactions.execute { propagationContext ->
                // Stimulate the subject input(s) to check its health
                healthCheckDescription.inputStimulation.stimulate(
                    propagationContext = propagationContext,
                )

                // Verify that the stable new state is as expected when the subject is expected to be inactive
                // (the subject might be forced to recompute the new state on demand)
                expectedSubjectTransition.expectedOldState.verifyStableState(
                    propagationContext = propagationContext,
                    subject = subject,
                )

                val observedNotifications = subjectObserver.retrieveObservedNotifications()

                // Verify that the subject doesn't react to the provided follow-up stimulation, which proves that it
                // properly deactivates after being unobserved.
                assertTrue(
                    actual = observedNotifications.isEmpty(),
                    message = "Expected no notifications to be observed after the subject is unobserved, but some were observed: $observedNotifications",
                )
            }

            // Execute a post-(health check) transaction
            Transactions.execute { propagationContext ->
                // Verify that the stable state is computed properly after the health check stimulation
                expectedSubjectTransition.expectedNewState.verifyStableState(
                    propagationContext = propagationContext,
                    subject = subject,
                )
            }
        }

        fun <SubjectT, NotificationT : Any> checkHealthActively(
            trait: TestSubjectObservationTrait<SubjectT, NotificationT>,
            subject: SubjectT,
            subjectObserver: TestSubjectObserver<SubjectT, NotificationT>,
            healthCheckDescription: HealthCheckDescription<SubjectT, NotificationT>,
        ) {
            val expectedSubjectTransition = healthCheckDescription.expectedSubjectTransition

            // Execute a health check transaction, keeping the test subject observed (active)
            Transactions.execute { propagationContext ->
                // Stimulate the subject input(s) to check its health
                healthCheckDescription.inputStimulation.stimulate(
                    propagationContext = propagationContext,
                )

                // Verify that the subject correctly reacts to the provided follow-up stimulation, which proves
                // that it keeps observing its input(s) after the original reaction.
                expectedSubjectTransition.expectedReaction.verifyReaction(
                    trait = trait,
                    subject = subject,
                    subjectObserver = subjectObserver,
                )

                // Verify that the stable new state is as expected when the subject is expected to be active
                // (the subject might be forced to share its internally maintained state, which might've been
                // corrupted by some specific reaction processing).
                expectedSubjectTransition.expectedOldState.verifyStableState(
                    propagationContext = propagationContext,
                    subject = subject,
                )
            }

            // Execute a post-(health check) transaction
            Transactions.execute { propagationContext ->
                // Verify that the stable state is computed properly after the health check stimulation
                expectedSubjectTransition.expectedNewState.verifyStableState(
                    propagationContext = propagationContext,
                    subject = subject,
                )
            }
        }
    }

    /**
     * Verify that the test subject's inputs are inactive.
     */
    fun verifyInputsInactive()

    /**
     * Prepare a check that aims to prove that the test subject is "in good health", i.e. that it is still working
     * correctly. It's not the goal to tests all possible subject's behaviors or aspects.
     */
    fun prepareHealthCheck(
        subject: SubjectT,
    ): HealthCheckDescription<SubjectT, NotificationT>
}
