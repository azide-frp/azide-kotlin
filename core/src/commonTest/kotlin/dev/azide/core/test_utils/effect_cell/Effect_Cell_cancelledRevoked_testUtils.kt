package dev.azide.core.test_utils.effect_cell

import dev.azide.core.Cell
import dev.azide.core.Effect
import dev.azide.core.test_utils.TestSlottedStimulation3
import dev.azide.core.test_utils.cell.ExpectedCellValueTransition
import dev.azide.core.test_utils.effect_generic.Effect_generic_cancelledRevoked_testUtils
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy
import dev.azide.core.test_utils.generic.CellObservationTrait
import dev.azide.core.test_utils.generic.ExpectedImpact

@Suppress("ClassName")
data object Effect_Cell_cancelledRevoked_testUtils {
    fun <ValueT> testCancel(
        subjectEffectOutcome: Effect.Outcome<Cell<ValueT>>,
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        slottedInputStimulation: TestSlottedStimulation3? = null,
        expectedSubjectValueTransition: ExpectedCellValueTransition<ValueT>,
        expectedTargetImpact: ExpectedImpact,
    ) {
        Effect_generic_cancelledRevoked_testUtils.testCancel(
            trait = CellObservationTrait(),
            subjectOutcome = subjectEffectOutcome,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            slottedInputStimulation = slottedInputStimulation,
            expectedSubjectTransition = expectedSubjectValueTransition,
            expectedTargetImpact = expectedTargetImpact,
        )
    }
}
