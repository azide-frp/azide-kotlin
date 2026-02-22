package dev.azide.core.test_utils.generic

/**
 * Strategy that defines how the test subject is checked for being "in good health" after the reaction transaction.
 */
enum class TestSubjectHealthCheckStrategy {
    /**
     * Strategy which deactivates the subject after the stimulation aiming to prove that it correctly unlistens its
     * dependencies and doesn't invalidate its internal state during deactivation.
     */
    TestSubjectDeactivated,

    /**
     * Strategy which keeps the subject active after the stimulation aiming to prove that it correctly maintains the
     * observation of the inputs after the stimulation and correctly commits its internal state.
     */
    TestSubjectKeptActive,
}
