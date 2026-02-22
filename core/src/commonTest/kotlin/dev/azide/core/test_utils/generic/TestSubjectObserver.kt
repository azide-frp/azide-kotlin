package dev.azide.core.test_utils.generic

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.ListenableVertex
import dev.azide.core.impl.enqueueCallbackForCommitment
import dev.azide.core.impl.registerBoundListenerOnline
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy

class TestSubjectObserver<in SubjectT, out NotificationT : Any> private constructor(
    private val trait: TestSubjectObservationTrait<SubjectT, NotificationT>,
    private val subjectLazy: Lazy<SubjectT>,
) : ListenableVertex.BoundListener {
    companion object {
        fun <SubjectT, NotificationT : Any> prepare(
            trait: TestSubjectObservationTrait<SubjectT, NotificationT>,
            subjectLazy: Lazy<SubjectT>,
        ): TestSubjectObserver<SubjectT, NotificationT> = TestSubjectObserver(
            trait = trait,
            subjectLazy = subjectLazy,
        )

        fun <SubjectT, NotificationT : Any> prepare(
            trait: TestSubjectObservationTrait<SubjectT, NotificationT>,
            subject: SubjectT,
        ): TestSubjectObserver<SubjectT, NotificationT> = TestSubjectObserver(
            trait = trait,
            subjectLazy = lazyOf(subject),
        )

        fun <SubjectT, NotificationT : Any> observe(
            trait: TestSubjectObservationTrait<SubjectT, NotificationT>,
            subject: SubjectT,
            propagationContext: Transactions.PropagationContext,
        ): TestSubjectObserver<SubjectT, NotificationT> = TestSubjectObserver.prepare(
            trait = trait,
            subjectLazy = lazyOf(subject),
        ).also {
            it.observe(
                propagationContext = propagationContext,
            )
        }

        fun <SubjectT, NotificationT : Any> observeWithStrategy(
            trait: TestSubjectObservationTrait<SubjectT, NotificationT>,
            subject: SubjectT,
            propagationContext: Transactions.PropagationContext,
            subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
        ): TestSubjectObserver<SubjectT, NotificationT>? = when (subjectPerceptionStrategy) {
            TestSubjectPerceptionStrategy.NonPerceived -> null
            TestSubjectPerceptionStrategy.Perceived -> observe(
                trait = trait,
                subject = subject,
                propagationContext = propagationContext,
            )
        }
    }

    private var listenerHandle: ListenableVertex.ListenerHandle? = null

    private var observedNotifications: MutableList<NotificationT?> = mutableListOf()

    private var isEnqueuedForCommitment = false

    override fun handle(
        propagationContext: Transactions.PropagationContext,
    ) {
        val subject = subjectLazy.value

        observedNotifications.add(
            trait.extractOngoingNotification(subject),
        )

        ensureEnqueuedForCommitment(
            propagationContext = propagationContext,
        )
    }

    fun retrieveObservedNotifications(): List<NotificationT?> {
        val copiedObservedNotifications = observedNotifications.toList()

        observedNotifications.clear()

        return copiedObservedNotifications
    }

    private fun ensureEnqueuedForCommitment(
        propagationContext: Transactions.PropagationContext,
    ) {
        if (!isEnqueuedForCommitment) {
            propagationContext.enqueueCallbackForCommitment {
                observedNotifications.clear()

                isEnqueuedForCommitment = false
            }

            isEnqueuedForCommitment = true
        }
    }

    fun observe(
        propagationContext: Transactions.PropagationContext,
    ) {
        if (listenerHandle != null) {
            throw IllegalStateException("The subject is already being observed")
        }

        val subject = subjectLazy.value

        listenerHandle = trait.extractVertex(subject).registerBoundListenerOnline(
            propagationContext = propagationContext,
            listener = this,
        )

        observedNotifications = listOfNotNull(
            trait.extractOngoingNotification(subject),
        ).toMutableList()

        ensureEnqueuedForCommitment(
            propagationContext = propagationContext,
        )
    }

    fun observeLater(
        wrapUpContext: Transactions.WrapUpContext,
    ) {
        wrapUpContext.enqueueForWrapUp { propagationContext ->
            this.observe(
                propagationContext = propagationContext,
            )
        }
    }

    fun unobserve() {
        val listenerHandle = this.listenerHandle ?: throw IllegalStateException("The subject is already unobserved")

        val subject = subjectLazy.value

        trait.extractVertex(subject).unregisterListener(
            handle = listenerHandle,
        )

        this.listenerHandle = null
    }
}
