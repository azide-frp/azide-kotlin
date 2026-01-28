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

interface ExpectedTestSubjectTransition<in SubjectT> {
    companion object {
        val None: ExpectedTestSubjectTransition<Any> = ExpectedTestSubjectTransitionImpl(
            expectedOldState = ExpectedTestSubjectState.None,
            expectedReaction = ExpectedTestSubjectReaction.None,
            expectedNewState = ExpectedTestSubjectState.None,
        )
    }

    val expectedOldState: ExpectedTestSubjectState<SubjectT>
    val expectedReaction: ExpectedTestSubjectReaction<SubjectT>
    val expectedNewState: ExpectedTestSubjectState<SubjectT>
}

data class ExpectedTestSubjectTransitionImpl<in SubjectT>(
    override val expectedOldState: ExpectedTestSubjectState<SubjectT>,
    override val expectedReaction: ExpectedTestSubjectReaction<SubjectT>,
    override val expectedNewState: ExpectedTestSubjectState<SubjectT>,
) : ExpectedTestSubjectTransition<SubjectT>
