package dev.azide.core.test_utils

import dev.azide.core.impl.Transactions
import dev.azide.core.test_utils.ExpectedTestSubjectReaction.TestSubjectReactionVerifier
import dev.azide.core.test_utils.effects.TestSubjectPerceptionStrategy

interface ExpectedTestSubjectReaction<in SubjectT> {
    data object None : ExpectedTestSubjectReaction<Any> {
        override fun prepareReactionVerifier(
            propagationContext: Transactions.PropagationContext,
            subjectLazy: Lazy<Any>,
        ): TestSubjectReactionVerifier = object : TestSubjectReactionVerifier {
            override fun install() {
            }

            override fun verifyReaction() {
            }

            override fun uninstall() {
            }
        }
    }

    interface TestSubjectReactionVerifier {
        fun install()

        fun verifyReaction()

        fun uninstall()
    }

    enum class IntermediatePropagationTolerance {
        DoNotTolerate, Tolerate,
    }

    fun prepareReactionVerifier(
        propagationContext: Transactions.PropagationContext,
        subjectLazy: Lazy<SubjectT>,
    ): TestSubjectReactionVerifier
}

fun <SubjectT> ExpectedTestSubjectReaction<SubjectT>.prepareReactionVerifierInstalled(
    propagationContext: Transactions.PropagationContext,
    subject: SubjectT,
): TestSubjectReactionVerifier = prepareReactionVerifier(
    propagationContext = propagationContext,
    subjectLazy = lazyOf(subject),
).apply {
    install()
}

fun <SubjectT> ExpectedTestSubjectReaction<SubjectT>.prepareReactionVerifierWithStrategyInstalled(
    propagationContext: Transactions.PropagationContext,
    subject: SubjectT,
    strategy: TestSubjectPerceptionStrategy,
): TestSubjectReactionVerifier? = when (strategy) {
    TestSubjectPerceptionStrategy.NonPerceived -> null

    TestSubjectPerceptionStrategy.Perceived -> prepareReactionVerifierInstalled(
        propagationContext = propagationContext,
        subject = subject,
    )
}

fun TestSubjectReactionVerifier.verifyReactionUninstalling() {
    verifyReaction()
    uninstall()
}

fun TestSubjectReactionVerifier.installLater(
    wrapUpContext: Transactions.WrapUpContext,
) {
    wrapUpContext.enqueueForWrapUp {
        install()
    }
}
