package dev.azide.core.test_utils.collections.reactive_list

import dev.azide.core.collections.ReactiveList
import dev.azide.core.impl.Transactions
import dev.azide.core.test_utils.TestStimulation

@Suppress("ClassName")
data object ReactiveList_sampling_testUtils {
    fun <ElementT> testPassiveSampling(
        subjectReactiveList: ReactiveList<ElementT>,
        inputStimulation: TestStimulation? = null,
        expectedSubjectContent: ExpectedReactiveListContent<ElementT>,
    ) {
        Transactions.execute { propagationContext ->
            inputStimulation?.stimulate(
                propagationContext = propagationContext,
            )

            expectedSubjectContent.verifyStableState(
                propagationContext = propagationContext,
                subject = subjectReactiveList,
            )
        }
    }
}
