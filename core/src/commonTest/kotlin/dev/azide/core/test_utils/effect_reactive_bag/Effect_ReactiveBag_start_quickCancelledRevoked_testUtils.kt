package dev.azide.core.test_utils.effect_reactive_bag

import dev.azide.core.Effect
import dev.azide.core.collections.ReactiveBag
import dev.azide.core.test_utils.TestSlottedStimulation4
import dev.azide.core.test_utils.collections.reactive_bag.ExpectedReactiveBagContentTransition
import dev.azide.core.test_utils.effect_generic.Effect_generic_start_quickCancelledRevoked_testUtils
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy
import dev.azide.core.test_utils.generic.ExpectedImpact

@Suppress("ClassName")
data object Effect_ReactiveBag_start_quickCancelledRevoked_testUtils {
    fun <ElementT> executeStartTransaction(
        subjectReactiveBagEffect: Effect<ReactiveBag<ElementT>>,
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedInputStimulation: TestSlottedStimulation4? = null,
        expectedSubjectContentTransition: ExpectedReactiveBagContentTransition<ElementT>,
        expectedTargetImpact: ExpectedImpact,
    ): ReactiveBag<ElementT> = Effect_generic_start_quickCancelledRevoked_testUtils.executeStartTransaction(
        subjectEffect = subjectReactiveBagEffect,
        subjectPerceptionStrategy = subjectPerceptionStrategy,
        slottedInputStimulation = slottedInputStimulation,
        expectedSubjectTransition = expectedSubjectContentTransition,
        expectedTargetImpact = expectedTargetImpact,
    )
}
