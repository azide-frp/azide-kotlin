package dev.azide.core.test_utils.effect_generic

import dev.azide.core.Cell
import dev.azide.core.Effect
import dev.azide.core.EventStream
import dev.azide.core.executeInternallyWrappedUp
import dev.azide.core.test_utils.ExpectedImpact
import dev.azide.core.test_utils.TestSlottedStimulation5
import dev.azide.core.test_utils.TestStimulationSlot5

@Suppress("ClassName")
data object Effect_generic_startRevoked_quickCancelledRevoked_testUtils {
    fun <SubjectT> executeStartTransaction(
        subjectEffect: Effect<SubjectT>,
        slottedInputStimulation: TestSlottedStimulation5? = null,
        expectedTargetImpact: ExpectedImpact,
    ) {
        Effect_generic_testUtils.executeTransactionWithImpactAndNewStateVerification(
            expectedTargetImpact = expectedTargetImpact,
            expectedNewState = null,
        ) { propagationContext ->
            // 0. Pre-stimulation

            slottedInputStimulation?.stimulate(
                propagationContext = propagationContext,
                slot = TestStimulationSlot5.Slot0,
            )

            // 1. Start the effect
            val (effectOutcome, startRevocable) = subjectEffect.start.executeInternallyWrappedUp(
                propagationContext = propagationContext,
            )

            val subject = effectOutcome.result
            val effectHandle = effectOutcome.handle

            slottedInputStimulation?.stimulate(
                propagationContext = propagationContext,
                slot = TestStimulationSlot5.Slot1,
            )

            // 2. Cancel the effect
            val (_: Unit, cancelRevocable) = effectHandle.cancel.executeInternallyWrappedUp(
                propagationContext = propagationContext,
            )

            slottedInputStimulation?.stimulate(
                propagationContext = propagationContext,
                slot = TestStimulationSlot5.Slot2,
            )

            // 3. Revoke the effect's cancellation
            cancelRevocable.revoke()

            slottedInputStimulation?.stimulate(
                propagationContext = propagationContext,
                slot = TestStimulationSlot5.Slot3,
            )

            // 4. Revoke the effect's start
            startRevocable.revoke()

            slottedInputStimulation?.stimulate(
                propagationContext = propagationContext,
                slot = TestStimulationSlot5.Slot4,
            )

            subject
        }
    }
}

@Suppress("ClassName")
data object Effect_EventStream_startRevoked_quickCancelledRevoked_testUtils {
    fun <EventT> executeStartTransaction(
        subjectEffect: Effect<EventStream<EventT>>,
        slottedInputStimulation: TestSlottedStimulation5? = null,
        expectedTargetImpact: ExpectedImpact,
    ) {
        Effect_generic_startRevoked_quickCancelledRevoked_testUtils.executeStartTransaction(
            subjectEffect = subjectEffect,
            slottedInputStimulation = slottedInputStimulation,
            expectedTargetImpact = expectedTargetImpact,
        )
    }
}

@Suppress("ClassName")
data object Effect_Cell_startRevoked_quickCancelledRevoked_testUtils {
    fun <ValueT> executeStartTransaction(
        subjectEffect: Effect<Cell<ValueT>>,
        slottedInputStimulation: TestSlottedStimulation5? = null,
        expectedTargetImpact: ExpectedImpact,
    ) {
        Effect_generic_startRevoked_quickCancelledRevoked_testUtils.executeStartTransaction(
            subjectEffect = subjectEffect,
            slottedInputStimulation = slottedInputStimulation,
            expectedTargetImpact = expectedTargetImpact,
        )
    }
}
