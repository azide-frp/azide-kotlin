package dev.azide.core.test_utils.effect_generic

import dev.azide.core.impl.Transactions
import dev.azide.core.test_utils.generic.ExpectedTestSubjectState
import dev.azide.core.test_utils.generic.ExpectedImpact

@Suppress("ClassName")
data object Effect_generic_testUtils {
    fun <SubjectT> executeTransactionWithImpactAndNewStateVerification(
        expectedTargetImpact: ExpectedImpact,
        expectedNewState: ExpectedTestSubjectState<SubjectT>?,
        propagate: (Transactions.PropagationContext) -> SubjectT,
    ): SubjectT {
        val targetImpactVerifier = expectedTargetImpact.prepareImpactVerifier()

        val subject = Transactions.executeWithResult { propagationContext ->
            val subject = propagate(propagationContext)

            targetImpactVerifier.verifyPostPropagation()

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
