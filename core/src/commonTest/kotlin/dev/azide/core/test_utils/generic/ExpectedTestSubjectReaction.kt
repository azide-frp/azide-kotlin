package dev.azide.core.test_utils.generic

import dev.azide.core.impl.Transactions
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy

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
): ExpectedTestSubjectReaction.TestSubjectReactionVerifier = prepareReactionVerifier(
    propagationContext = propagationContext,
    subjectLazy = lazyOf(subject),
).apply {
    install()
}

fun <SubjectT> ExpectedTestSubjectReaction<SubjectT>.prepareReactionVerifierWithStrategyInstalled(
    propagationContext: Transactions.PropagationContext,
    subject: SubjectT,
    strategy: TestSubjectPerceptionStrategy,
): ExpectedTestSubjectReaction.TestSubjectReactionVerifier? = when (strategy) {
    TestSubjectPerceptionStrategy.NonPerceived -> null

    TestSubjectPerceptionStrategy.Perceived -> prepareReactionVerifierInstalled(
        propagationContext = propagationContext,
        subject = subject,
    )
}

fun ExpectedTestSubjectReaction.TestSubjectReactionVerifier.verifyReactionUninstalling() {
    verifyReaction()
    uninstall()
}

fun ExpectedTestSubjectReaction.TestSubjectReactionVerifier.installLater(
    wrapUpContext: Transactions.WrapUpContext,
) {
    wrapUpContext.enqueueForWrapUp {
        install()
    }
}
