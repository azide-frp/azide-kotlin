package dev.azide.core.test_utils

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

data class ExpectedTestSubjectTransition<in SubjectT>(
    val expectedOldState: ExpectedTestSubjectState<SubjectT>,
    val expectedReaction: ExpectedTestSubjectReaction<SubjectT>,
    val expectedNewState: ExpectedTestSubjectState<SubjectT>,
) {
    companion object {
        val None: ExpectedTestSubjectTransition<Any> = ExpectedTestSubjectTransition(
            expectedOldState = ExpectedTestSubjectState.None,
            expectedReaction = ExpectedTestSubjectReaction.None,
            expectedNewState = ExpectedTestSubjectState.None,
        )
    }
}
