package dev.azide.core.test_utils.effect_cell

import dev.azide.core.Cell
import dev.azide.core.Effect
import dev.azide.core.test_utils.ExpectedCellValueTransition
import dev.azide.core.test_utils.ExpectedImpact
import dev.azide.core.test_utils.TestSlottedStimulation4
import dev.azide.core.test_utils.effect_generic.Effect_generic_start_quickCancelledRevoked_testUtils
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy

@Suppress("ClassName")
data object Effect_Cell_start_quickCancelledRevoked_testUtils {
    fun <ValueT> executeStartTransaction(
        subjectCellEffect: Effect<Cell<ValueT>>,
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedInputStimulation: TestSlottedStimulation4? = null,
        expectedSubjectValueTransition: ExpectedCellValueTransition<ValueT>,
        expectedTargetImpact: ExpectedImpact,
    ): Cell<ValueT> = Effect_generic_start_quickCancelledRevoked_testUtils.executeStartTransaction(
        subjectEffect = subjectCellEffect,
        subjectPerceptionStrategy = subjectPerceptionStrategy,
        slottedInputStimulation = slottedInputStimulation,
        expectedSubjectTransition = expectedSubjectValueTransition,
        expectedTargetImpact = expectedTargetImpact,
    )
}
