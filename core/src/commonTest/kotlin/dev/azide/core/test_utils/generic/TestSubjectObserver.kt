package dev.azide.core.test_utils.generic

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.registerBoundListenerOnline
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy

class TestSubjectObserver<in SubjectT, out NotificationT : Any> private constructor(
    private val trait: TestSubjectObservationTrait<SubjectT, NotificationT>,
    private val subject: SubjectT,
    propagationContext: Transactions.PropagationContext,
) : Vertex.BoundListener {
    companion object {
        fun <SubjectT, NotificationT : Any> observe(
            trait: TestSubjectObservationTrait<SubjectT, NotificationT>,
            subject: SubjectT,
            propagationContext: Transactions.PropagationContext,
        ): TestSubjectObserver<SubjectT, NotificationT> = TestSubjectObserver(
            trait = trait,
            subject = subject,
            propagationContext = propagationContext,
        )

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

    private var listenerHandle: Vertex.ListenerHandle? = trait.extractVertex(subject).registerBoundListenerOnline(
        propagationContext = propagationContext,
        listener = this,
    )

    private var observedNotifications: MutableList<NotificationT?> = listOfNotNull(
        trait.extractOngoingNotification(subject),
    ).toMutableList()

    private var isEnqueuedForCommitment = false

    override fun handle(
        propagationContext: Transactions.PropagationContext,
    ) {
        observedNotifications.add(
            trait.extractOngoingNotification(subject),
        )

        ensureEnqueuedForCommitment(
            propagationContext = propagationContext,
        )
    }

    fun retrieveObservedNotifications(): List<NotificationT?> {
        val observedNotifications = this.observedNotifications.toList()

        this.observedNotifications.clear()

        return observedNotifications
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

    fun unobserve() {
        val listenerHandle = this.listenerHandle ?: throw IllegalStateException("The subject is already unobserved")

        trait.extractVertex(subject).unregisterListener(
            handle = listenerHandle,
        )

        this.listenerHandle = null
    }

    init {
        ensureEnqueuedForCommitment(
            propagationContext = propagationContext,
        )
    }
}
