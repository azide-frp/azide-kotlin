package dev.azide.core.test_utils

import dev.azide.core.impl.Transactions
import dev.azide.core.test_utils.ExpectedTestSubjectReaction.TestSubjectReactionVerifier
import dev.azide.core.test_utils.effects.TestSubjectPerceptionStrategy

interface ExpectedTestSubjectReaction<in SubjectT> {
    data object Noop : ExpectedTestSubjectReaction<Any> {
        override fun prepareReactionVerifier(
            propagationContext: Transactions.PropagationContext,
            subject: Any,
        ): TestSubjectReactionVerifier = object : TestSubjectReactionVerifier {
            override fun verifyReaction() {
            }
        }
    }

    interface TestSubjectReactionVerifier {
        fun verifyReaction()
    }

    enum class IntermediatePropagationTolerance {
        DoNotTolerate, Tolerate,
    }

    fun prepareReactionVerifier(
        propagationContext: Transactions.PropagationContext,
        subject: SubjectT,
    ): TestSubjectReactionVerifier
}

fun <SubjectT> ExpectedTestSubjectReaction<SubjectT>.prepareReactionVerifierWithStrategy(
    propagationContext: Transactions.PropagationContext,
    subject: SubjectT,
    strategy: TestSubjectPerceptionStrategy,
): TestSubjectReactionVerifier? = when (strategy) {
    TestSubjectPerceptionStrategy.NonPerceived -> null

    TestSubjectPerceptionStrategy.Perceived -> prepareReactionVerifier(
        propagationContext = propagationContext,
        subject = subject,
    )
}
