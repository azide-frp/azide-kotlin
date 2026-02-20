package dev.azide.core.test_utils.collections.reactive_list

import dev.azide.core.collections.ReactiveBag
import dev.azide.core.impl.Transactions
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.collections.reactive_bag.ExpectedReactiveBagContent

@Suppress("ClassName")
data object ReactiveBag_sampling_testUtils {
    fun <ElementT> executeSamplingTransaction(
        subjectReactiveBag: ReactiveBag<ElementT>,
        inputStimulation: TestStimulation? = null,
        expectedSubjectContent: ExpectedReactiveBagContent<ElementT>,
    ) {
        Transactions.execute { propagationContext ->
            inputStimulation?.stimulate(
                propagationContext = propagationContext,
            )

            expectedSubjectContent.verifyStableState(
                propagationContext = propagationContext,
                subject = subjectReactiveBag,
            )
        }
    }
}
