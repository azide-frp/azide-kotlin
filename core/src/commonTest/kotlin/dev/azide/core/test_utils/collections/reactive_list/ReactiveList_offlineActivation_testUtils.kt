package dev.azide.core.test_utils.collections.reactive_list

import dev.azide.core.collections.ReactiveList
import dev.azide.core.test_utils.generic.ReactiveListObservationTrait
import dev.azide.core.test_utils.generic.generic_offlineActivation_testUtils
import dev.azide.core.test_utils.generic.generic_reaction_testUtils

@Suppress("ClassName")
data object ReactiveList_offlineActivation_testUtils {
    fun <ElementT> testOfflineActivation(
        subjectReactiveList: ReactiveList<ElementT>,
        subjectHealthChecker: generic_reaction_testUtils.ReactiveListHealthChecker<ElementT>,
    ) {
        generic_offlineActivation_testUtils.testOfflineActivation(
            trait = ReactiveListObservationTrait(),
            subject = subjectReactiveList,
            subjectHealthChecker = subjectHealthChecker,
        )
    }
}
