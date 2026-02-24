package dev.azide.core.test_utils.effect_reactive_bag

import dev.azide.core.Effect
import dev.azide.core.collections.ReactiveBag
import dev.azide.core.test_utils.TestSlottedStimulation3
import dev.azide.core.test_utils.collections.reactive_bag.ExpectedReactiveBagContentTransition
import dev.azide.core.test_utils.effect_generic.Effect_generic_start_rushedWrapUp_testUtils
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.generic.ReactiveBagObservationTrait

@Suppress("ClassName")
data object Effect_ReactiveBag_start_rushedWrapUp_testUtils {
    fun <ElementT> testStart(
        subjectReactiveBagEffect: Effect<ReactiveBag<ElementT>>,
        slottedInputStimulation: TestSlottedStimulation3? = null,
        expectedSubjectContentTransition: ExpectedReactiveBagContentTransition<ElementT>,
        expectedTargetImpact: ExpectedImpact,
    ): ReactiveBag<ElementT> = Effect_generic_start_rushedWrapUp_testUtils.testStart(
        trait = ReactiveBagObservationTrait(),
        subjectEffect = subjectReactiveBagEffect,
        slottedInputStimulation = slottedInputStimulation,
        expectedSubjectTransition = expectedSubjectContentTransition,
        expectedTargetImpact = expectedTargetImpact,
    )
}
