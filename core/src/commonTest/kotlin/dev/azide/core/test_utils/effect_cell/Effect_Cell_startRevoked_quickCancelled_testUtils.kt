package dev.azide.core.test_utils.effect_cell

import dev.azide.core.Cell
import dev.azide.core.Effect
import dev.azide.core.test_utils.ExpectedImpact
import dev.azide.core.test_utils.TestSlottedStimulation4
import dev.azide.core.test_utils.effect_generic.Effect_generic_startRevoked_quickCancelled_testUtils

@Suppress("ClassName")
data object Effect_Cell_startRevoked_quickCancelled_testUtils {
    fun <ValueT> executeStartTransaction(
        subjectCellEffect: Effect<Cell<ValueT>>,
        slottedInputStimulation: TestSlottedStimulation4? = null,
        expectedTargetImpact: ExpectedImpact,
    ) {
        Effect_generic_startRevoked_quickCancelled_testUtils.executeStartTransaction(
            subjectEffect = subjectCellEffect,
            slottedInputStimulation = slottedInputStimulation,
            expectedTargetImpact = expectedTargetImpact,
        )
    }
}
