package dev.azide.core.test_utils.collections.reactive_bag

import dev.azide.core.collections.ReactiveBag
import dev.azide.core.test_utils.generic.ReactiveBagObservationTrait
import dev.azide.core.test_utils.generic.generic_offlineActivation_testUtils
import dev.azide.core.test_utils.generic.generic_reaction_testUtils

@Suppress("ClassName")
data object ReactiveBag_offlineActivation_testUtils {
    fun <ElementT> testOfflineActivation(
        subjectReactiveBag: ReactiveBag<ElementT>,
        subjectHealthChecker: generic_reaction_testUtils.ReactiveBagHealthChecker<ElementT>,
    ) {
        generic_offlineActivation_testUtils.testOfflineActivation(
            trait = ReactiveBagObservationTrait(),
            subject = subjectReactiveBag,
            subjectHealthChecker = subjectHealthChecker,
        )
    }
}
