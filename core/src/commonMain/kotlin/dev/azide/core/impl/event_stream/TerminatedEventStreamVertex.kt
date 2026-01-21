package dev.azide.core.impl.event_stream

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex

class TerminatedEventStreamVertex<EventT> : EventStreamVertex<EventT> {
    data object TerminatedSubscriberHandle : EventStreamVertex.SubscriberHandle

    override val ongoingEmission: Nothing?
        get() = null

    override val subscriberCount: Int
        get() = 0

    override fun registerEmissionNotificationSubscriber(
        propagationContext: Transactions.PropagationContext,
        subscriber: EventStreamVertex.EmissionNotificationSubscriber,
        mode: Vertex.ActivationMode,
    ): TerminatedSubscriberHandle = TerminatedSubscriberHandle

    override fun unregisterSubscriber(
        handle: EventStreamVertex.SubscriberHandle,
    ) {
        if (handle != TerminatedSubscriberHandle) {
            throw IllegalArgumentException("Invalid handle")
        }
    }
}
