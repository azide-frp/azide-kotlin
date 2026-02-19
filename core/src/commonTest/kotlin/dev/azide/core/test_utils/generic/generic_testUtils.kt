package dev.azide.core.test_utils.generic

import dev.azide.core.impl.Transactions
import dev.azide.core.test_utils.TestStimulation

@Suppress("ClassName")
data object generic_testUtils {
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

    fun executeTransactionWithImpactVerification(
        inputStimulation: TestStimulation,
        expectedTargetImpact: ExpectedImpact,
    ) {
        executeTransactionWithImpactVerification(
            expectedTargetImpact,
        ) { propagationContext ->
            inputStimulation.stimulate(
                propagationContext = propagationContext,
            )
        }
    }

    fun <SubjectT> executeTransactionWithNewStateVerification(
        expectedNewState: ExpectedTestSubjectState<SubjectT>?,
        propagate: (Transactions.PropagationContext) -> SubjectT,
    ) {
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
    }
}
