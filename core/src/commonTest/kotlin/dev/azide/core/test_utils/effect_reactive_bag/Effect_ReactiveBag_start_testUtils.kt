package dev.azide.core.test_utils.effect_reactive_bag

import dev.azide.core.Effect
import dev.azide.core.collections.ReactiveBag
import dev.azide.core.test_utils.ExpectedImpact
import dev.azide.core.test_utils.ExpectedReactiveBagContentTransition
import dev.azide.core.test_utils.TestSlottedStimulation2
import dev.azide.core.test_utils.effect_generic.Effect_generic_start_testUtils
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy

@Suppress("ClassName")
data object Effect_ReactiveBag_start_testUtils {
    fun <ElementT> executeStartTransaction(
        subjectReactiveBagEffect: Effect<ReactiveBag<ElementT>>,
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedInputStimulation: TestSlottedStimulation2? = null,
        expectedSubjectContentTransition: ExpectedReactiveBagContentTransition<ElementT>,
        expectedTargetImpact: ExpectedImpact,
    ): ReactiveBag<ElementT> = Effect_generic_start_testUtils.executeStartTransaction(
        subjectEffect = subjectReactiveBagEffect,
        subjectPerceptionStrategy = subjectPerceptionStrategy,
        slottedInputStimulation = slottedInputStimulation,
        expectedSubjectTransition = expectedSubjectContentTransition,
        expectedTargetImpact = expectedTargetImpact,
    )
}
