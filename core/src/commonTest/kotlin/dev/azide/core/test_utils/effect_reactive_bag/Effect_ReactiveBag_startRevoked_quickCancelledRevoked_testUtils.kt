package dev.azide.core.test_utils.effect_reactive_bag

import dev.azide.core.Effect
import dev.azide.core.collections.ReactiveBag
import dev.azide.core.test_utils.TestSlottedStimulation5
import dev.azide.core.test_utils.effect_generic.Effect_generic_startRevoked_quickCancelledRevoked_testUtils
import dev.azide.core.test_utils.generic.ExpectedImpact

@Suppress("ClassName")
data object Effect_ReactiveBag_startRevoked_quickCancelledRevoked_testUtils {
    fun <ValueT> executeStartTransaction(
        subjectReactiveBagEffect: Effect<ReactiveBag<ValueT>>,
        slottedInputStimulation: TestSlottedStimulation5? = null,
        expectedTargetImpact: ExpectedImpact,
    ) {
        Effect_generic_startRevoked_quickCancelledRevoked_testUtils.executeStartTransaction(
            subjectEffect = subjectReactiveBagEffect,
            slottedInputStimulation = slottedInputStimulation,
            expectedTargetImpact = expectedTargetImpact,
        )
    }
}
