package dev.azide.core.test_utils.generic

import dev.azide.core.collections.ReactiveBag
import dev.azide.core.collections.ReactiveList
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.collections.reactive_bag.TaggedBagChange
import dev.azide.core.impl.collections.reactive_list.ListChange
import dev.azide.core.test_utils.TestSlottedStimulation2
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation0
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation1
import kotlin.test.assertTrue

@Suppress("ClassName")
data object generic_reaction_testUtils {
    data class InputStimulationPlan(
        val unobservedInputStimulation: TestStimulation = TestStimulation.Noop,
        val observedInputStimulation: TestStimulation,
    )

    /**
     * Strategy that defines how the test subject is checked for being "in good health" after the reaction transaction.
     */
    enum class TestSubjectHealthCheckStrategy {
        /**
         * Strategy which deactivates the subject after the stimulation aiming to prove that it correctly unlistens its
         * dependencies and doesn't invalidate its internal state during deactivation.
         */
        TestSubjectDeactivated,

        /**
         * Strategy which keeps the subject active after the stimulation aiming to prove that it correctly maintains the
         * observation of the inputs after the stimulation and correctly commits its internal state.
         */
        TestSubjectKeptActive,
    }

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

    typealias ReactiveBagHealthChecker<ElementT> = TestSubjectHealthChecker<ReactiveBag<ElementT>, TaggedBagChange<ElementT>>

    typealias ReactiveListHealthChecker<ElementT> = TestSubjectHealthChecker<ReactiveList<ElementT>, ListChange<ElementT>>

    /**
     * Execute a transaction with the given stimulation, verifying whether the test subject reacts in the expected way.
     * The test subject is observed mid-transaction. After the reaction transaction, it's tested whether the subject
     * is in good health via [subjectHealthChecker] (using the given [subjectHealthCheckStrategy]).
     */
    fun <SubjectT, NotificationT : Any> executeReactionTransaction(
        trait: TestSubjectObservationTrait<SubjectT, NotificationT>,
        subject: SubjectT,
        inputStimulationPlan: InputStimulationPlan,
        expectedSubjectTransition: ExpectedTestSubjectTransition<SubjectT, NotificationT>,
        subjectHealthChecker: TestSubjectHealthChecker<SubjectT, NotificationT>? = null,
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
    ) {
        val subjectObserver = TestSubjectObserver.prepare(
            trait = trait,
            subject = subject,
        )

        Transactions.execute { propagationContext ->
            // Stimulate the subject's input(s) at the point when the subject can' be aware of it
            inputStimulationPlan.unobservedInputStimulation.stimulate(
                propagationContext = propagationContext,
            )

            // Observe the subject
            subjectObserver.observe(
                propagationContext = propagationContext,
            )

            // Stimulate the subject's input(s) at the point when the subject can observe it
            inputStimulationPlan.observedInputStimulation.stimulate(
                propagationContext = propagationContext,
            )

            // Verify that the stable state wasn't updated pre-maturely
            expectedSubjectTransition.expectedOldState.verifyStableState(
                propagationContext = propagationContext,
                subject = subject,
            )

            // Verify that the observed subject's reaction is as expected
            expectedSubjectTransition.expectedReaction.verifyReaction(
                trait = trait,
                subject = subject,
                subjectObserver = subjectObserver,
            )
        }

        if (subjectHealthChecker == null) {
            return
        }

        val healthCheck = subjectHealthChecker.prepareHealthCheck(
            subject = subject,
        )

        when (subjectHealthCheckStrategy) {
            TestSubjectHealthCheckStrategy.TestSubjectDeactivated -> {
                // Unobserve the subject, assuming it deactivates
                subjectObserver.unobserve()

                // Verify that the subject is properly deactivated (e.g. that all dependencies are unlistened)
                subjectHealthChecker.verifyInputsInactive()

                // Execute a follow-up transaction after unobserving the test subject
                Transactions.execute { propagationContext ->
                    // Stimulate the subject input(s) to check its health
                    healthCheck.inputStimulation.stimulate(
                        propagationContext = propagationContext,
                    )

                    // Verify that the stable new state is as expected when the subject is expected to be inactive
                    // (the subject might be forced to recompute the new state on demand)
                    expectedSubjectTransition.expectedNewState.verifyStableState(
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

                // Execute a post-follow-up transaction
                Transactions.execute { propagationContext ->
                    // Verify that the stable state is computed properly after the health check stimulation
                    healthCheck.expectedSubjectTransition.expectedNewState.verifyStableState(
                        propagationContext = propagationContext,
                        subject = subject,
                    )
                }

                // Verify that stimulating the subject after deactivation doesn't cause it to "self-activate" (e.g.
                // listen to some of its dependencies again).
                subjectHealthChecker.verifyInputsInactive()
            }

            TestSubjectHealthCheckStrategy.TestSubjectKeptActive -> {
                // Execute a follow-up transaction, keeping the test subject observed (active)
                Transactions.execute { propagationContext ->
                    // Stimulate the subject input(s) to check its health
                    healthCheck.inputStimulation.stimulate(
                        propagationContext = propagationContext,
                    )

                    // Verify that the subject correctly reacts to the provided follow-up stimulation, which proves
                    // that it keeps observing its input(s) after the original reaction.
                    healthCheck.expectedSubjectTransition.expectedReaction.verifyReaction(
                        trait = trait,
                        subject = subject,
                        subjectObserver = subjectObserver,
                    )

                    // Verify that the stable new state is as expected when the subject is expected to be active
                    // (the subject might be forced to share its internally maintained state, which might've been
                    // corrupted by some specific reaction processing).
                    expectedSubjectTransition.expectedNewState.verifyStableState(
                        propagationContext = propagationContext,
                        subject = subject,
                    )
                }

                // Execute a post-follow-up transaction
                Transactions.execute { propagationContext ->
                    // Verify that the stable state is computed properly after the health check stimulation
                    healthCheck.expectedSubjectTransition.expectedNewState.verifyStableState(
                        propagationContext = propagationContext,
                        subject = subject,
                    )
                }
            }
        }
    }

    fun <SubjectT, NotificationT : Any> executeReactionTransaction(
        trait: TestSubjectObservationTrait<SubjectT, NotificationT>,
        subject: SubjectT,
        slottedInputStimulation: TestSlottedStimulation2,
        expectedSubjectTransition: ExpectedTestSubjectTransition<SubjectT, NotificationT>,
    ) {
        executeReactionTransaction(
            trait = trait,
            subject = subject,
            inputStimulationPlan = InputStimulationPlan(
                unobservedInputStimulation = slottedInputStimulation.slotStimulation0,
                observedInputStimulation = slottedInputStimulation.slotStimulation1,
            ),
            expectedSubjectTransition = expectedSubjectTransition,
        )
    }
}
