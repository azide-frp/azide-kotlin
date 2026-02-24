package dev.azide.core.test_utils.cell

import dev.azide.core.Cell
import dev.azide.core.test_utils.TestSlottedStimulation2
import dev.azide.core.test_utils.generic.CellObservationTrait
import dev.azide.core.test_utils.generic.generic_reaction_testUtils

@Suppress("ClassName")
data object Cell_reaction_testUtils {
    fun <ValueT> testReaction(
        subjectCell: Cell<ValueT>,
        slottedInputStimulation: TestSlottedStimulation2,
        expectedSubjectValueTransition: ExpectedCellValueTransition<ValueT>,
    ) {
        generic_reaction_testUtils.testReaction(
            trait = CellObservationTrait(),
            subject = subjectCell,
            slottedInputStimulation = slottedInputStimulation,
            expectedSubjectTransition = expectedSubjectValueTransition,
        )
    }
}
