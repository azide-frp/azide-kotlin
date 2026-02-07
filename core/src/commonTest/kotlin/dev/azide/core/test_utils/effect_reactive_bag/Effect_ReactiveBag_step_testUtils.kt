package dev.azide.core.test_utils.effect_reactive_bag

import dev.azide.core.collections.ReactiveBag
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.collections.reactive_bag.ExpectedReactiveBagContentTransition
import dev.azide.core.test_utils.effect_generic.Effect_generic_step_testUtils
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy

@Suppress("ClassName")
data object Effect_ReactiveBag_step_testUtils {
    fun <ElementT> executeStepTransaction(
        subjectReactiveBag: ReactiveBag<ElementT>,
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedInputStimulation: TestStimulation,
        expectedSubjectContentTransition: ExpectedReactiveBagContentTransition<ElementT>,
        expectedTargetImpact: ExpectedImpact,
    ) {
        Effect_generic_step_testUtils.executeStepTransaction(
            subject = subjectReactiveBag,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            inputStimulation = slottedInputStimulation,
            expectedSubjectTransition = expectedSubjectContentTransition,
            expectedTargetImpact = expectedTargetImpact,
        )
    }
}
