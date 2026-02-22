package dev.azide.core.test_utils.effect_reactive_bag

import dev.azide.core.Effect
import dev.azide.core.collections.ReactiveBag
import dev.azide.core.test_utils.TestSlottedStimulation3
import dev.azide.core.test_utils.collections.reactive_bag.ExpectedReactiveBagContentTransition
import dev.azide.core.test_utils.effect_generic.Effect_generic_start_quickCancelled_testUtils
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.generic.ReactiveBagObservationTrait

@Suppress("ClassName")
data object Effect_ReactiveBag_start_quickCancelled_testUtils {
    fun <ElementT> testStart(
        subjectReactiveBagEffect: Effect<ReactiveBag<ElementT>>,
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedInputStimulation: TestSlottedStimulation3? = null,
        expectedSubjectContentTransition: ExpectedReactiveBagContentTransition<ElementT>,
        expectedTargetImpact: ExpectedImpact,
        cancelCount: Int = 1,
    ): ReactiveBag<ElementT> = Effect_generic_start_quickCancelled_testUtils.testStart(
        trait = ReactiveBagObservationTrait(),
        subjectEffect = subjectReactiveBagEffect,
        subjectPerceptionStrategy = subjectPerceptionStrategy,
        slottedInputStimulation = slottedInputStimulation,
        expectedSubjectTransition = expectedSubjectContentTransition,
        expectedTargetImpact = expectedTargetImpact,
        cancelCount = cancelCount,
    )
}
