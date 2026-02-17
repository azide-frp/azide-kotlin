package dev.azide.core.test_utils.generic

import dev.azide.core.impl.Transactions
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy

interface ExpectedTestSubjectReaction<SubjectT, NotificationT : Any> {
    data object None : ExpectedBasicTestSubjectReaction<Any, Nothing>() {
        override val expectedSubjectNotification: Nothing? = null

        override fun prepareReactionVerifier(
            propagationContext: Transactions.PropagationContext,
            subjectLazy: Lazy<Any>,
        ): TestSubjectReactionVerifier<Any, Any> = object : TestSubjectReactionVerifier<Any, Any> {
            override fun install() {
            }

            override fun verifyReaction() {
            }

            override fun uninstall() {
            }
        }
    }

    interface TestSubjectReactionVerifier<out SubjectT, in NotificationT : Any> {
        fun install()

        fun verifyReaction()

        fun uninstall()
    }

    enum class IntermediatePropagationTolerance {
        DoNotTolerate, Tolerate,
    }

    fun verifyReaction(
        subjectObserver: TestSubjectObserver<SubjectT, NotificationT>,
    )

    fun prepareReactionVerifier(
        propagationContext: Transactions.PropagationContext,
        subjectLazy: Lazy<SubjectT>,
    ): TestSubjectReactionVerifier<SubjectT, NotificationT>
}

abstract class ExpectedBasicTestSubjectReaction<SubjectT, NotificationT : Any> :
    ExpectedTestSubjectReaction<SubjectT, NotificationT> {

    final override fun verifyReaction(
        subjectObserver: TestSubjectObserver<SubjectT, NotificationT>,
    ) {
        TODO("Not yet implemented")
    }

    abstract val expectedSubjectNotification: NotificationT?
}

fun <SubjectT, NotificationT : Any> ExpectedTestSubjectReaction<SubjectT, NotificationT>.prepareReactionVerifierInstalled(
    propagationContext: Transactions.PropagationContext,
    subject: SubjectT,
): ExpectedTestSubjectReaction.TestSubjectReactionVerifier<SubjectT, NotificationT> = prepareReactionVerifier(
    propagationContext = propagationContext,
    subjectLazy = lazyOf(subject),
).apply {
    install()
}

fun <SubjectT, NotificationT : Any> ExpectedTestSubjectReaction<SubjectT, NotificationT>.prepareReactionVerifierWithStrategyInstalled(
    propagationContext: Transactions.PropagationContext,
    subject: SubjectT,
    strategy: TestSubjectPerceptionStrategy,
): ExpectedTestSubjectReaction.TestSubjectReactionVerifier<SubjectT, NotificationT>? = when (strategy) {
    TestSubjectPerceptionStrategy.NonPerceived -> null

    TestSubjectPerceptionStrategy.Perceived -> prepareReactionVerifierInstalled(
        propagationContext = propagationContext,
        subject = subject,
    )
}

fun <SubjectT, NotificationT : Any> ExpectedTestSubjectReaction.TestSubjectReactionVerifier<SubjectT, NotificationT>.verifyReactionUninstalling() {
    verifyReaction()
    uninstall()
}

fun <SubjectT, NotificationT : Any> ExpectedTestSubjectReaction.TestSubjectReactionVerifier<SubjectT, NotificationT>.installLater(
    wrapUpContext: Transactions.WrapUpContext,
) {
    wrapUpContext.enqueueForWrapUp {
        install()
    }
}
