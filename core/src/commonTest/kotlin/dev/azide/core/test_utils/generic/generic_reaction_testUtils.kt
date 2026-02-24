package dev.azide.core.test_utils.generic

import dev.azide.core.Cell
import dev.azide.core.EventStream
import dev.azide.core.collections.ReactiveBag
import dev.azide.core.collections.ReactiveList
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.collections.reactive_bag.TaggedBagChange
import dev.azide.core.impl.collections.reactive_list.ListChange
import dev.azide.core.impl.event_stream.EventStreamVertex
import dev.azide.core.test_utils.TestSlottedStimulation2
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation0
import dev.azide.core.test_utils.stimulation_combinatorics.slotStimulation1

@Suppress("ClassName")
data object generic_reaction_testUtils {
    data class InputStimulationPlan(
        val unobservedInputStimulation: TestStimulation = TestStimulation.Noop,
        val observedInputStimulation: TestStimulation,
    )

    typealias CellHealthChecker<ValueT> = TestSubjectHealthChecker<Cell<ValueT>, CellVertex.Update<ValueT>>

    typealias EventStreamHealthChecker<EventT> = TestSubjectHealthChecker<EventStream<EventT>, EventStreamVertex.Emission<EventT>>

    typealias ReactiveBagHealthChecker<ElementT> = TestSubjectHealthChecker<ReactiveBag<ElementT>, TaggedBagChange<ElementT>>

    typealias ReactiveListHealthChecker<ElementT> = TestSubjectHealthChecker<ReactiveList<ElementT>, ListChange<ElementT>>

    /**
     * Execute a transaction with the given stimulation, verifying whether the test subject reacts in the expected way.
     * The test subject is observed mid-transaction. After the reaction transaction, it's tested whether the subject
     * is in good health via [subjectHealthChecker] (using the given [subjectHealthCheckStrategy]).
     */
    fun <SubjectT, NotificationT : Any> testReaction(
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
                processingContext = propagationContext,
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

        val healthCheckDescription = subjectHealthChecker.prepareHealthCheck(
            subject = subject,
        )

        when (subjectHealthCheckStrategy) {
            TestSubjectHealthCheckStrategy.TestSubjectDeactivated -> {
                // Unobserve the subject, assuming it deactivates
                subjectObserver.unobserve()

                // Verify that the subject is properly deactivated (e.g. that all dependencies are unlistened)
                subjectHealthChecker.verifyInputsInactive()

                TestSubjectHealthChecker.checkHealthPassively(
                    trait = trait,
                    subject = subject,
                    subjectObserver = subjectObserver,
                    healthCheckDescription = healthCheckDescription,
                )

                // Verify that stimulating the subject after deactivation doesn't cause it to "self-activate" (e.g.
                // listen to some of its dependencies again).
                subjectHealthChecker.verifyInputsInactive()
            }

            TestSubjectHealthCheckStrategy.TestSubjectKeptActive -> {
                TestSubjectHealthChecker.checkHealthActively(
                    trait = trait,
                    subject = subject,
                    subjectObserver = subjectObserver,
                    healthCheckDescription = healthCheckDescription,
                )
            }
        }
    }

    fun <SubjectT, NotificationT : Any> testReaction(
        trait: TestSubjectObservationTrait<SubjectT, NotificationT>,
        subject: SubjectT,
        slottedInputStimulation: TestSlottedStimulation2,
        expectedSubjectTransition: ExpectedTestSubjectTransition<SubjectT, NotificationT>,
    ) {
        testReaction(
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
