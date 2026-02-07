package dev.azide.core.test_utils.cell

import dev.azide.core.Cell
import dev.azide.core.Moment
import dev.azide.core.test_utils.ExpectedCellValue
import dev.azide.core.test_utils.TestSlottedStimulation2
import dev.azide.core.test_utils.generic.generic_spawn_nonPerceived_testUtils

@Suppress("ClassName")
data object Cell_spawn_nonPerceived_testUtils {
    fun <ValueT> executeSpawnTransaction(
        subjectCellSpawnMoment: Moment<Cell<ValueT>>,
        slottedInputStimulation: TestSlottedStimulation2? = null,
        expectedOldSubjectValue: ExpectedCellValue<ValueT>,
        expectedNewSubjectValue: ExpectedCellValue<ValueT>,
    ) {
        generic_spawn_nonPerceived_testUtils.executeSpawnTransaction(
            subjectSpawnMoment = subjectCellSpawnMoment,
            slottedInputStimulation = slottedInputStimulation,
            expectedOldState = expectedOldSubjectValue,
            expectedNewState = expectedNewSubjectValue,
        )
    }
}
