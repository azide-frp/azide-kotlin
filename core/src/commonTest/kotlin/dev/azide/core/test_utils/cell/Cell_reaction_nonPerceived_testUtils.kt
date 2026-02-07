package dev.azide.core.test_utils.cell

import dev.azide.core.Cell
import dev.azide.core.test_utils.ExpectedCellValue
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.generic.generic_reaction_nonPerceived_testUtils

@Suppress("ClassName")
data object Cell_reaction_nonPerceived_testUtils {
    fun <ValueT> executeReactionTransaction(
        subjectCell: Cell<ValueT>,
        inputStimulation: TestStimulation,
        expectedOldSubjectValue: ExpectedCellValue<ValueT>,
        expectedNewSubjectValue: ExpectedCellValue<ValueT>,
    ) {
        generic_reaction_nonPerceived_testUtils.executeReactionTransaction(
            subject = subjectCell,
            inputStimulation = inputStimulation,
            expectedOldState = expectedOldSubjectValue,
            expectedNewState = expectedNewSubjectValue,
        )
    }
}
