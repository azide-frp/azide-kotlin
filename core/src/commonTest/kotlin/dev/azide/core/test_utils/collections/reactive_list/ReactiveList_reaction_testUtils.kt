package dev.azide.core.test_utils.collections.reactive_list

import dev.azide.core.collections.ReactiveList
import dev.azide.core.test_utils.TestSlottedStimulation2
import dev.azide.core.test_utils.generic.ReactiveListObservationTrait
import dev.azide.core.test_utils.generic.generic_reaction_testUtils

@Suppress("ClassName")
data object ReactiveList_reaction_testUtils {
    fun <ElementT> testReaction(
        subjectReactiveList: ReactiveList<ElementT>,
        slottedInputStimulation: TestSlottedStimulation2,
        expectedSubjectElementTransition: ExpectedReactiveListContentTransition<ElementT>,
    ) {
        generic_reaction_testUtils.testReaction(
            trait = ReactiveListObservationTrait(),
            subject = subjectReactiveList,
            slottedInputStimulation = slottedInputStimulation,
            expectedSubjectTransition = expectedSubjectElementTransition,
        )
    }
}
