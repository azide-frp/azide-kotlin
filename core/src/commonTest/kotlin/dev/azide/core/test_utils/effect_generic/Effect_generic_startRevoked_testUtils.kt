package dev.azide.core.test_utils.effect_generic

import dev.azide.core.Cell
import dev.azide.core.Effect
import dev.azide.core.EventStream
import dev.azide.core.executeInternallyWrappedUp
import dev.azide.core.test_utils.ExpectedImpact
import dev.azide.core.test_utils.TestSlottedStimulation3
import dev.azide.core.test_utils.TestStimulationSlot3

@Suppress("ClassName")
data object Effect_generic_startRevoked_testUtils {
    fun <SubjectT> executeStartTransaction(
        subjectEffect: Effect<SubjectT>,
        slottedInputStimulation: TestSlottedStimulation3? = null,
        expectedTargetImpact: ExpectedImpact,
    ) {
        Effect_generic_testUtils.executeTransactionWithImpactAndNewStateVerification(
            expectedTargetImpact = expectedTargetImpact,
            expectedNewState = null,
        ) { propagationContext ->
            // 0. Pre-stimulation
            slottedInputStimulation?.stimulate(
                propagationContext = propagationContext,
                slot = TestStimulationSlot3.Slot0,
            )

            // 1. Start the effect
            val (effectOutcome, startRevocable) = subjectEffect.start.executeInternallyWrappedUp(
                propagationContext = propagationContext,
            )

            val subject = effectOutcome.result

            slottedInputStimulation?.stimulate(
                propagationContext = propagationContext,
                slot = TestStimulationSlot3.Slot1,
            )

            // 2. Revoke the effect's start
            startRevocable.revoke()

            slottedInputStimulation?.stimulate(
                propagationContext = propagationContext,
                slot = TestStimulationSlot3.Slot2,
            )

            subject
        }
    }
}

@Suppress("ClassName")
data object Effect_EventStream_startRevoked_testUtils {
    fun <EventT> executeStartTransaction(
        subjectEffect: Effect<EventStream<EventT>>,
        slottedInputStimulation: TestSlottedStimulation3? = null,
        expectedTargetImpact: ExpectedImpact,
    ) {
        Effect_generic_startRevoked_testUtils.executeStartTransaction(
            subjectEffect = subjectEffect,
            slottedInputStimulation = slottedInputStimulation,
            expectedTargetImpact = expectedTargetImpact,
        )
    }
}

@Suppress("ClassName")
data object Effect_Cell_startRevoked_testUtils {
    fun <ValueT> executeStartTransaction(
        subjectEffect: Effect<Cell<ValueT>>,
        slottedInputStimulation: TestSlottedStimulation3? = null,
        expectedTargetImpact: ExpectedImpact,
    ) {
        Effect_generic_startRevoked_testUtils.executeStartTransaction(
            subjectEffect = subjectEffect,
            slottedInputStimulation = slottedInputStimulation,
            expectedTargetImpact = expectedTargetImpact,
        )
    }
}
