package dev.azide.core.test_utils.stateful

import dev.azide.core.impl.Transactions
import dev.azide.core.test_utils.ExpectedTestSubjectState

data object StatefulTestUtils {
    fun <SubjectT> executeTransactionWithNewStateVerification(
        expectedNewState: ExpectedTestSubjectState<SubjectT>?,
        propagate: (Transactions.PropagationContext) -> SubjectT,
    ): SubjectT {
        val subject = Transactions.executeWithResult { propagationContext ->
            val subject = propagate(propagationContext)

            return@executeWithResult subject
        }

        if (expectedNewState != null) {
            Transactions.execute { propagationContext ->
                // Verify the new state in a separate helper transaction
                expectedNewState.verifyStableState(
                    propagationContext = propagationContext,
                    subject = subject,
                )
            }
        }

        return subject
    }
}
