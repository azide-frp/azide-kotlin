package dev.azide.core.test_utils.cell

import dev.azide.core.Cell
import dev.azide.core.Moment
import dev.azide.core.test_utils.TestSlottedStimulation3
import dev.azide.core.test_utils.generic.CellObservationTrait
import dev.azide.core.test_utils.generic.generic_spawn_testUtils

@Suppress("ClassName")
data object Cell_spawn_testUtils {
    fun <ValueT> executeSpawnTransaction(
        subjectSpawnMoment: Moment<Cell<ValueT>>,
        slottedInputStimulation: TestSlottedStimulation3? = null,
        expectedSubjectValueTransition: ExpectedCellValueTransition<ValueT>,
    ) {
        generic_spawn_testUtils.executeSpawnTransaction(
            trait = CellObservationTrait(),
            subjectSpawnMoment = subjectSpawnMoment,
            slottedInputStimulation = slottedInputStimulation,
            expectedSubjectTransition = expectedSubjectValueTransition,
        )
    }
}
