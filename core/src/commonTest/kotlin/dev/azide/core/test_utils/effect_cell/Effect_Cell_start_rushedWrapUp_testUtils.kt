package dev.azide.core.test_utils.effect_cell

import dev.azide.core.Cell
import dev.azide.core.Effect
import dev.azide.core.test_utils.cell.ExpectedCellValueTransition
import dev.azide.core.test_utils.ExpectedImpact
import dev.azide.core.test_utils.TestSlottedStimulation3
import dev.azide.core.test_utils.effect_generic.Effect_generic_start_rushedWrapUp_testUtils

@Suppress("ClassName")
data object Effect_Cell_start_rushedWrapUp_testUtils {
    fun <ValueT> executeStartTransaction(
        subjectCellEffect: Effect<Cell<ValueT>>,
        slottedInputStimulation: TestSlottedStimulation3? = null,
        expectedSubjectValueTransition: ExpectedCellValueTransition<ValueT>,
        expectedTargetImpact: ExpectedImpact,
    ) {
        Effect_generic_start_rushedWrapUp_testUtils.executeStartTransaction(
            subjectEffect = subjectCellEffect,
            slottedInputStimulation = slottedInputStimulation,
            expectedSubjectTransition = expectedSubjectValueTransition,
            expectedTargetImpact = expectedTargetImpact,
        )
    }
}
