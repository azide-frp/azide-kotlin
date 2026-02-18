package dev.azide.core.test_utils.generic

import dev.azide.core.impl.Transactions
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import kotlin.test.assertEquals
import kotlin.test.assertTrue

interface ExpectedTestSubjectReaction<SubjectT, NotificationT : Any> {
    data object None : AbstractExplicitExpectedTestSubjectReaction<Any, Nothing>() {
        override val intermediatePropagationTolerance: IntermediatePropagationTolerance =
            IntermediatePropagationTolerance.DoNotTolerate

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

    /**
     * Verifies the reaction of a subject observer to some stimulation. This function should be called at the end of the
     * transaction.
     */
    fun verifyReaction(
        trait: TestSubjectObservationTrait<SubjectT, NotificationT>,
        subject: SubjectT,
        subjectObserver: TestSubjectObserver<SubjectT, NotificationT>,
    )

    fun prepareReactionVerifier(
        propagationContext: Transactions.PropagationContext,
        subjectLazy: Lazy<SubjectT>,
    ): TestSubjectReactionVerifier<SubjectT, NotificationT>
}

abstract class AbstractBasicExpectedTestSubjectReaction<SubjectT, NotificationT : Any> :
    ExpectedTestSubjectReaction<SubjectT, NotificationT> {

    final override fun verifyReaction(
        trait: TestSubjectObservationTrait<SubjectT, NotificationT>,
        subject: SubjectT,
        subjectObserver: TestSubjectObserver<SubjectT, NotificationT>,
    ) {
        val observedNotifications: List<NotificationT?> = subjectObserver.retrieveObservedNotifications()

        // This will be null both when no notification was observed (which means that the subject didn't have an ongoing
        // notification when the observation started) and when the last observed notification was `null` (which means
        // revocation).
        val lastObservedNotification: NotificationT? = observedNotifications.lastOrNull()

        // The final exposed ongoing notifications (after all potential revocations and corrections) should be the
        // same as the last observed one.
        val finalOngoingNotification: NotificationT? = trait.extractOngoingNotification(subject)

        assertEquals(
            expected = lastObservedNotification,
            actual = finalOngoingNotification,
            message = "Final ongoing notification did not match the expected one.",
        )

        // The last observed notification (including the potentially ongoing one when the observation started) should be
        // as expected.
        verifyEffectiveNotification(
            effectiveNotification = lastObservedNotification,
        )

        when (intermediatePropagationTolerance) {
            IntermediatePropagationTolerance.DoNotTolerate -> {
                val observedNotificationCount = observedNotifications.size

                assertTrue(
                    actual = observedNotificationCount <= 2,
                    message = "Expected at most one notification to be observed, but received $observedNotificationCount changes (intermediate propagation is not tolerated).",
                )
            }

            IntermediatePropagationTolerance.Tolerate -> {}
        }
    }

    abstract fun verifyEffectiveNotification(
        effectiveNotification: NotificationT?,
    )

    abstract val intermediatePropagationTolerance: IntermediatePropagationTolerance
}

abstract class AbstractExplicitExpectedTestSubjectReaction<SubjectT, NotificationT : Any> :
    AbstractBasicExpectedTestSubjectReaction<SubjectT, NotificationT>() {

    final override fun verifyEffectiveNotification(
        effectiveNotification: NotificationT?,
    ) {
        assertEquals(
            expected = expectedSubjectNotification,
            actual = effectiveNotification,
            message = "The effective notification did not match the expected one.",
        )
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
