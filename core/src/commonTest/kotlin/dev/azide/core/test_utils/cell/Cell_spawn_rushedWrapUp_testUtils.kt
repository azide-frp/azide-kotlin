package dev.azide.core.test_utils.cell

import dev.azide.core.Cell
import dev.azide.core.Moment
import dev.azide.core.test_utils.TestSlottedStimulation3
import dev.azide.core.test_utils.generic.CellObservationTrait
import dev.azide.core.test_utils.generic.generic_spawn_rushedWrapUp_testUtils

@Suppress("ClassName")
data object Cell_spawn_rushedWrapUp_testUtils {
    fun <ValueT : Any> testSpawn(
        subjectCellSpawnMoment: Moment<Cell<ValueT>>,
        slottedInputStimulation: TestSlottedStimulation3? = null,
        expectedSubjectValueTransition: ExpectedCellValueTransition<ValueT>,
    ) {
        generic_spawn_rushedWrapUp_testUtils.testSpawn(
            trait = CellObservationTrait(),
            subjectSpawnMoment = subjectCellSpawnMoment,
            slottedInputStimulation = slottedInputStimulation,
            expectedSubjectTransition = expectedSubjectValueTransition,
        )
    }
}
