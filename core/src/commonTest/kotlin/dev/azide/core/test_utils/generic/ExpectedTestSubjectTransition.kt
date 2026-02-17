package dev.azide.core.test_utils.generic

interface ExpectedTestSubjectTransition<SubjectT, NotificationT : Any> {
    data object None : ExpectedTestSubjectTransition<Any, Nothing> {
        override val expectedOldState: ExpectedTestSubjectState<Any> = ExpectedTestSubjectState.None
        override val expectedReaction: ExpectedTestSubjectReaction<Any, Nothing> = ExpectedTestSubjectReaction.None
        override val expectedNewState: ExpectedTestSubjectState<Any> = ExpectedTestSubjectState.None
    }

    val expectedOldState: ExpectedTestSubjectState<SubjectT>
    val expectedReaction: ExpectedTestSubjectReaction<SubjectT, NotificationT>
    val expectedNewState: ExpectedTestSubjectState<SubjectT>
}
