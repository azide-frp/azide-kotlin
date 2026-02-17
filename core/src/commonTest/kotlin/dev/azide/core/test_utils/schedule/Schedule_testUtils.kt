package dev.azide.core.test_utils.schedule

import dev.azide.core.impl.Transactions
import dev.azide.core.test_utils.generic.ExpectedImpact

@Suppress("ClassName")
data object Schedule_testUtils {
    fun executeTransactionWithImpactVerification(
        expectedTargetImpact: ExpectedImpact,
        propagate: (Transactions.PropagationContext) -> Unit,
    ) {
        val targetImpactVerifier = expectedTargetImpact.prepareImpactVerifier()

        return Transactions.execute { propagationContext ->
            propagate(propagationContext)

            targetImpactVerifier.verifyPostPropagation()
        }
    }
}
