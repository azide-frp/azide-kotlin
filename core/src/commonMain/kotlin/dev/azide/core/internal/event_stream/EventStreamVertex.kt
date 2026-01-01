package dev.azide.core.internal.event_stream

import dev.azide.core.internal.Transactions
import dev.azide.core.internal.Vertex
import kotlin.jvm.JvmInline

sealed interface EventStreamVertex<out EventT> : Vertex {
    @JvmInline
    value class Emission<out EventT>(
        val emittedEvent: EventT,
    ) {
        fun <TransformedEventT> map(
            transform: (EventT) -> TransformedEventT,
        ): Emission<TransformedEventT> = Emission(
            emittedEvent = transform(emittedEvent),
        )
    }

    interface Subscriber<in EventT> {
        object Noop : Subscriber<Any?> {
            override fun handleEmissionWithStatus(
                propagationContext: Transactions.PropagationContext,
                emission: Emission<Any?>?,
            ): SubscriberStatus = SubscriberStatus.Reachable
        }

        fun handleEmissionWithStatus(
            propagationContext: Transactions.PropagationContext,
            emission: EventStreamVertex.Emission<EventT>?,
        ): SubscriberStatus
    }

    interface SubscriberHandle

    enum class SubscriberStatus {
        Reachable, Unreachable,
    }

    val ongoingEmission: Emission<EventT>?

    fun registerSubscriber(
        propagationContext: Transactions.PropagationContext,
        subscriber: Subscriber<EventT>,
    ): SubscriberHandle

    fun unregisterSubscriber(
        handle: SubscriberHandle,
    )
}
