package dev.azide.core.internal.event_stream

import dev.azide.core.internal.Transactions

class TerminatedEventStreamVertex<EventT> : EventStreamVertex<EventT> {
    override val ongoingEmission: Nothing?
        get() = null

    override fun registerSubscriber(
        propagationContext: Transactions.PropagationContext,
        subscriber: EventStreamVertex.Subscriber<EventT>,
    ): Nothing? = null

    override fun unregisterSubscriber(
        handle: EventStreamVertex.SubscriberHandle,
    ): Nothing {
        throw UnsupportedOperationException("Terminated event stream vertices do not support unregistering subscribers")
    }
}
