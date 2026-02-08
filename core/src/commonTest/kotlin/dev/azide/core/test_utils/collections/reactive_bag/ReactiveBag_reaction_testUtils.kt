package dev.azide.core.test_utils.collections.reactive_bag

import dev.azide.core.collections.ReactiveBag
import dev.azide.core.test_utils.TestSlottedStimulation2
import dev.azide.core.test_utils.generic.generic_reaction_testUtils

@Suppress("ClassName")
data object ReactiveBag_reaction_testUtils {
    fun <ElementT> executeReactionTransaction(
        subjectReactiveBag: ReactiveBag<ElementT>,
        slottedInputStimulation: TestSlottedStimulation2,
        expectedSubjectElementTransition: ExpectedReactiveBagContentTransition<ElementT>,
    ) {
        generic_reaction_testUtils.executeReactionTransaction(
            subject = subjectReactiveBag,
            slottedInputStimulation = slottedInputStimulation,
            expectedSubjectTransition = expectedSubjectElementTransition,
        )
    }
}
