package dev.azide.core.test_utils.cell

import dev.azide.core.Cell
import dev.azide.core.test_utils.generic.CellObservationTrait
import dev.azide.core.test_utils.generic.generic_offlineActivation_testUtils
import dev.azide.core.test_utils.generic.generic_reaction_testUtils

@Suppress("ClassName")
data object Cell_offlineActivation_testUtils {
    fun <ValueT> executeOfflineActivationTransaction(
        subjectCell: Cell<ValueT>,
        subjectHealthChecker: generic_reaction_testUtils.CellHealthChecker<ValueT>,
    ) {
        generic_offlineActivation_testUtils.executeOfflineActivationTransaction(
            trait = CellObservationTrait(),
            subject = subjectCell,
            subjectHealthChecker = subjectHealthChecker,
        )
    }
}
