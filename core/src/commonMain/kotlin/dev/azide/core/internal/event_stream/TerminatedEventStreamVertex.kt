package dev.azide.core.internal.event_stream

import dev.azide.core.internal.Transactions

class TerminatedEventStreamVertex<EventT> : EventStreamVertex<EventT> {
    data object TerminatedSubscriberHandle : EventStreamVertex.SubscriberHandle

    override val ongoingEmission: Nothing?
        get() = null

    override fun registerSubscriber(
        propagationContext: Transactions.PropagationContext,
        subscriber: EventStreamVertex.Subscriber<EventT>,
    ): TerminatedSubscriberHandle = TerminatedSubscriberHandle

    override fun unregisterSubscriber(
        handle: EventStreamVertex.SubscriberHandle,
    ) {
        if (handle != TerminatedSubscriberHandle) {
            throw IllegalArgumentException("Invalid handle")
        }
    }
}
