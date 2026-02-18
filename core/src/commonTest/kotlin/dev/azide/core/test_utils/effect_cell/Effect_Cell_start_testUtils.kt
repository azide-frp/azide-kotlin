package dev.azide.core.test_utils.effect_cell

import dev.azide.core.Cell
import dev.azide.core.Effect
import dev.azide.core.test_utils.TestSlottedStimulation2
import dev.azide.core.test_utils.cell.ExpectedCellValueTransition
import dev.azide.core.test_utils.effect_generic.Effect_generic_start_testUtils
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy
import dev.azide.core.test_utils.generic.CellObservationTrait
import dev.azide.core.test_utils.generic.ExpectedImpact

@Suppress("ClassName")
data object Effect_Cell_start_testUtils {
    fun <ValueT> executeStartTransaction(
        subjectCellEffect: Effect<Cell<ValueT>>,
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedInputStimulation: TestSlottedStimulation2? = null,
        expectedSubjectValueTransition: ExpectedCellValueTransition<ValueT>,
        expectedTargetImpact: ExpectedImpact,
    ) {
        Effect_generic_start_testUtils.executeStartTransaction(
            trait = CellObservationTrait(),
            subjectEffect = subjectCellEffect,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = slottedInputStimulation,
            expectedSubjectTransition = expectedSubjectValueTransition,
            expectedTargetImpact = expectedTargetImpact,
        )
    }
}
