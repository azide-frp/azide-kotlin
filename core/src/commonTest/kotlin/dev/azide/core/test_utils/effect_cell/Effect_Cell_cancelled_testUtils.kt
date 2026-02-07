package dev.azide.core.test_utils.effect_cell

import dev.azide.core.Cell
import dev.azide.core.Effect
import dev.azide.core.test_utils.ExpectedCellValueTransition
import dev.azide.core.test_utils.ExpectedImpact
import dev.azide.core.test_utils.TestSlottedStimulation2
import dev.azide.core.test_utils.effect_generic.Effect_generic_cancelled_testUtils
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy

@Suppress("ClassName")
data object Effect_Cell_cancelled_testUtils {
    fun <ValueT> executeCancelTransaction(
        subjectEffectOutcome: Effect.Outcome<Cell<ValueT>>,
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedInputStimulation: TestSlottedStimulation2? = null,
        expectedSubjectValueTransition: ExpectedCellValueTransition<ValueT>,
        expectedTargetImpact: ExpectedImpact,
        cancelCount: Int = 1,
    ) {
        Effect_generic_cancelled_testUtils.executeCancelTransaction(
            subjectOutcome = subjectEffectOutcome,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = slottedInputStimulation,
            expectedSubjectTransition = expectedSubjectValueTransition,
            expectedTargetImpact = expectedTargetImpact,
            cancelCount = cancelCount,
        )
    }
}
