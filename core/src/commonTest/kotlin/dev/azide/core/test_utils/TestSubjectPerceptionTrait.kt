package dev.azide.core.test_utils

import dev.azide.core.impl.Transactions

interface TestSubjectPerceptionTrait<SubjectT, SubjectProxyT> {
    interface OldStateStabilityVerifier {
        fun verifyOldStateDidNotChange()
    }

    fun prepareOldStateStabilityVerifier(
        propagationContext: Transactions.PropagationContext,
        subjectProxy: SubjectProxyT,
    ): OldStateStabilityVerifier

    fun perceive(
        propagationContext: Transactions.PropagationContext,
        subject: SubjectT,
    ): SubjectProxyT
}
