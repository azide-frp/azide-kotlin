package dev.azide.core.test_utils.effect_generic

import dev.azide.core.Cell
import dev.azide.core.Effect
import dev.azide.core.EventStream
import dev.azide.core.executeInternallyWrappedUp
import dev.azide.core.impl.Revocable
import dev.azide.core.test_utils.ExpectedTestSubjectReaction.TestSubjectReactionVerifier
import dev.azide.core.test_utils.ExpectedTestSubjectTransition
import dev.azide.core.test_utils.ExpectedImpact
import dev.azide.core.test_utils.TestSlottedStimulation3
import dev.azide.core.test_utils.TestStimulationSlot3
import dev.azide.core.test_utils.prepareReactionVerifierWithStrategyInstalled
import dev.azide.core.test_utils.verifyReactionUninstalling

@Suppress("ClassName")
data object Effect_generic_cancelledRevoked_testUtils {
    fun <SubjectT> executeCancelTransaction(
        subjectOutcome: Effect.Outcome<SubjectT>,
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedInputStimulation: TestSlottedStimulation3? = null,
        expectedSubjectTransition: ExpectedTestSubjectTransition<SubjectT>,
        expectedTargetImpact: ExpectedImpact,
    ) {
        val subject = subjectOutcome.result
        val subjectHandle = subjectOutcome.handle

        Effect_generic_testUtils.executeTransactionWithImpactAndNewStateVerification(
            expectedTargetImpact = expectedTargetImpact,
            expectedNewState = expectedSubjectTransition.expectedNewState,
        ) { propagationContext ->
            val subjectReactionVerifier: TestSubjectReactionVerifier? =
                expectedSubjectTransition.expectedReaction.prepareReactionVerifierWithStrategyInstalled(
                    propagationContext = propagationContext,
                    subject = subject,
                    strategy = subjectPerceptionStrategy,
                )

            // Verify the old state for the first time
            expectedSubjectTransition.expectedOldState.verifyStableState(
                propagationContext = propagationContext,
                subject = subject,
            )

            // 0. Pre-stimulation
            slottedInputStimulation?.stimulate(
                propagationContext = propagationContext,
                slot = TestStimulationSlot3.Slot0,
            )

            // 1. Cancel the effect
            val (_: Unit, cancelRevocable: Revocable) = subjectHandle.cancel.executeInternallyWrappedUp(
                propagationContext = propagationContext,
            )

            slottedInputStimulation?.stimulate(
                propagationContext = propagationContext,
                slot = TestStimulationSlot3.Slot1,
            )

            // 2. Revoke the effect's cancellation
            cancelRevocable.revoke()

            slottedInputStimulation?.stimulate(
                propagationContext = propagationContext,
                slot = TestStimulationSlot3.Slot2,
            )

            // Verify the old state again (to ensure its stability)
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
data object Effect_EventStream_cancelledRevoked_testUtils {
    fun <EventT> executeCancelTransaction(
        subjectOutcome: Effect.Outcome<EventStream<EventT>>,
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedInputStimulation: TestSlottedStimulation3? = null,
        expectedSubjectTransition: ExpectedTestSubjectTransition<EventStream<EventT>>,
        expectedTargetImpact: ExpectedImpact,
    ) {
        Effect_generic_cancelledRevoked_testUtils.executeCancelTransaction(
            subjectOutcome = subjectOutcome,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = slottedInputStimulation,
            expectedSubjectTransition = expectedSubjectTransition,
            expectedTargetImpact = expectedTargetImpact,
        )
    }
}

@Suppress("ClassName")
data object Effect_Cell_cancelledRevoked_testUtils {
    fun <ValueT> executeCancelTransaction(
        subjectOutcome: Effect.Outcome<Cell<ValueT>>,
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedInputStimulation: TestSlottedStimulation3? = null,
        expectedSubjectTransition: ExpectedTestSubjectTransition<Cell<ValueT>>,
        expectedTargetImpact: ExpectedImpact,
    ) {
        Effect_generic_cancelledRevoked_testUtils.executeCancelTransaction(
            subjectOutcome = subjectOutcome,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = slottedInputStimulation,
            expectedSubjectTransition = expectedSubjectTransition,
            expectedTargetImpact = expectedTargetImpact,
        )
    }
}
