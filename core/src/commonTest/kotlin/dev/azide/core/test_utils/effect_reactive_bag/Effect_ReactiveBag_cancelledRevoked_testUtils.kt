package dev.azide.core.test_utils.effect_reactive_bag

import dev.azide.core.Effect
import dev.azide.core.collections.ReactiveBag
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.TestSlottedStimulation3
import dev.azide.core.test_utils.collections.reactive_bag.ExpectedReactiveBagContentTransition
import dev.azide.core.test_utils.effect_generic.Effect_generic_cancelledRevoked_testUtils
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy

@Suppress("ClassName")
data object Effect_ReactiveBag_cancelledRevoked_testUtils {
    fun <ElementT> executeCancelTransaction(
        subjectEffectOutcome: Effect.Outcome<ReactiveBag<ElementT>>,
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedInputStimulation: TestSlottedStimulation3? = null,
        expectedSubjectContentTransition: ExpectedReactiveBagContentTransition<ElementT>,
        expectedTargetImpact: ExpectedImpact,
    ) {
        Effect_generic_cancelledRevoked_testUtils.executeCancelTransaction(
            subjectOutcome = subjectEffectOutcome,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = slottedInputStimulation,
            expectedSubjectTransition = expectedSubjectContentTransition,
            expectedTargetImpact = expectedTargetImpact,
        )
    }
}
