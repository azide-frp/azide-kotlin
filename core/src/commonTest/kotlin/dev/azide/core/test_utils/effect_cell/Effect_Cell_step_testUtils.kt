package dev.azide.core.test_utils.effect_cell

import dev.azide.core.Cell
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.cell.ExpectedCellValueTransition
import dev.azide.core.test_utils.effect_generic.Effect_generic_step_testUtils
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy

@Suppress("ClassName")
data object Effect_Cell_step_testUtils {
    fun <ValueT> executeStepTransaction(
        subjectCell: Cell<ValueT>,
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        inputStimulation: TestStimulation,
        expectedSubjectValueTransition: ExpectedCellValueTransition<ValueT>,
        expectedTargetImpact: ExpectedImpact,
    ) {
        Effect_generic_step_testUtils.executeStepTransaction(
            subject = subjectCell,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            inputStimulation = inputStimulation,
            expectedSubjectTransition = expectedSubjectValueTransition,
            expectedTargetImpact = expectedTargetImpact,
        )
    }
}
