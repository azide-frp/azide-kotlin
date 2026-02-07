package dev.azide.core.test_utils.generic

import dev.azide.core.impl.Transactions

interface ExpectedTestSubjectState<in SubjectT> {
    data object None : ExpectedTestSubjectState<Any> {
        override fun verifyStableState(
            propagationContext: Transactions.PropagationContext,
            subject: Any,
        ) {
        }
    }

    fun verifyStableState(
        propagationContext: Transactions.PropagationContext,
        subject: SubjectT,
    )
}