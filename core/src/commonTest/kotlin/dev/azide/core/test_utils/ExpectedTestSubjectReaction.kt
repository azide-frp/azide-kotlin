package dev.azide.core.test_utils

import dev.azide.core.impl.Transactions

interface ExpectedTestSubjectReaction<SubjectProxyT> {
    interface DeltaVerifier {
        fun verifyExposedCorrectly()

        fun verifyPropagatedCorrectly()
    }

    interface NewStateVerifier<SubjectProxyT> {
        fun verifyNewState(
            propagationContext: Transactions.PropagationContext,
        )
    }

    enum class IntermediatePropagationTolerance {
        DoNotTolerate, Tolerate,
    }

    fun prepareDeltaVerifier(
        propagationContext: Transactions.PropagationContext,
        subjectProxy: SubjectProxyT,
    ): DeltaVerifier

    fun prepareNewStateVerifier(
        propagationContext: Transactions.PropagationContext,
        subjectProxy: SubjectProxyT,
    ): NewStateVerifier<SubjectProxyT>
}
