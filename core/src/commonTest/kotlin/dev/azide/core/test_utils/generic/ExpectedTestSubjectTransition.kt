package dev.azide.core.test_utils.generic

interface ExpectedTestSubjectTransition<in SubjectT> {
    data object None : ExpectedTestSubjectTransition<Any> {
        override val expectedOldState: ExpectedTestSubjectState<Any> = ExpectedTestSubjectState.None
        override val expectedReaction: ExpectedTestSubjectReaction<Any> = ExpectedTestSubjectReaction.None
        override val expectedNewState: ExpectedTestSubjectState<Any> = ExpectedTestSubjectState.None
    }

    val expectedOldState: ExpectedTestSubjectState<SubjectT>
    val expectedReaction: ExpectedTestSubjectReaction<SubjectT>
    val expectedNewState: ExpectedTestSubjectState<SubjectT>
}