package dev.azide.core.test_utils.effect_generic

import dev.azide.core.Cell
import dev.azide.core.Effect
import dev.azide.core.EventStream
import dev.azide.core.executeInternallyWrappedUp
import dev.azide.core.impl.Revocable
import dev.azide.core.test_utils.ExpectedTestSubjectReaction
import dev.azide.core.test_utils.ExpectedTestSubjectTransition
import dev.azide.core.test_utils.ExpectedImpact
import dev.azide.core.test_utils.TestSlottedStimulation2
import dev.azide.core.test_utils.TestStimulationSlot2
import dev.azide.core.test_utils.prepareReactionVerifierWithStrategyInstalled
import dev.azide.core.test_utils.verifyReactionUninstalling

@Suppress("ClassName")
data object Effect_generic_start_testUtils {
    fun <SubjectT> executeStartTransaction(
        subjectEffect: Effect<SubjectT>,
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedInputStimulation: TestSlottedStimulation2? = null,
        expectedSubjectTransition: ExpectedTestSubjectTransition<SubjectT>,
        expectedTargetImpact: ExpectedImpact,
    ) {
        Effect_generic_testUtils.executeTransactionWithImpactAndNewStateVerification(
            expectedTargetImpact = expectedTargetImpact,
            expectedNewState = expectedSubjectTransition.expectedNewState,
        ) { propagationContext ->
            // 0. Pre-stimulation
            slottedInputStimulation?.stimulate(
                propagationContext = propagationContext,
                slot = TestStimulationSlot2.Slot0,
            )

            // 1. Start the effect
            val (effectOutcome, _: Revocable) = subjectEffect.start.executeInternallyWrappedUp(
                propagationContext = propagationContext,
            )

            val subject = effectOutcome.result

            slottedInputStimulation?.stimulate(
                propagationContext = propagationContext,
                slot = TestStimulationSlot2.Slot1,
            )

            val subjectReactionVerifier: ExpectedTestSubjectReaction.TestSubjectReactionVerifier? =
                expectedSubjectTransition.expectedReaction.prepareReactionVerifierWithStrategyInstalled(
                    propagationContext = propagationContext,
                    subject = subject,
                    strategy = subjectPerceptionStrategy,
                )

            expectedSubjectTransition.expectedOldState.verifyStableState(
                propagationContext = propagationContext,
                subject = subject,
            )

            subjectReactionVerifier?.verifyReactionUninstalling()

            subject
        }
    }
}

@Suppress("ClassName")
data object Effect_EventStream_start_testUtils {
    fun <EventT> executeStartTransaction(
        subjectEffect: Effect<EventStream<EventT>>,
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedInputStimulation: TestSlottedStimulation2? = null,
        expectedSubjectTransition: ExpectedTestSubjectTransition<EventStream<EventT>>,
        expectedTargetImpact: ExpectedImpact,
    ) {
        Effect_generic_start_testUtils.executeStartTransaction(
            subjectEffect = subjectEffect,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = slottedInputStimulation,
            expectedSubjectTransition = expectedSubjectTransition,
            expectedTargetImpact = expectedTargetImpact,
        )
    }
}

@Suppress("ClassName")
data object Effect_Cell_start_testUtils {
    fun <ValueT> executeStartTransaction(
        subjectEffect: Effect<Cell<ValueT>>,
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedInputStimulation: TestSlottedStimulation2? = null,
        expectedSubjectTransition: ExpectedTestSubjectTransition<Cell<ValueT>>,
        expectedTargetImpact: ExpectedImpact,
    ) {
        Effect_generic_start_testUtils.executeStartTransaction(
            subjectEffect = subjectEffect,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = slottedInputStimulation,
            expectedSubjectTransition = expectedSubjectTransition,
            expectedTargetImpact = expectedTargetImpact,
        )
    }
}
