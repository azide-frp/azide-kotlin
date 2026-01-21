package dev.azide.core.impl.event_stream

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex

class TerminatedEventStreamVertex<EventT> : EventStreamVertex<EventT> {
    data object TerminatedListenerHandle : EventStreamVertex.ListenerHandle

    override val ongoingEmission: Nothing?
        get() = null

    override val listenerCount: Int
        get() = 0

    override fun registerListener(
        propagationContext: Transactions.PropagationContext,
        listener: EventStreamVertex.Listener,
        mode: Vertex.ActivationMode,
    ): TerminatedListenerHandle = TerminatedListenerHandle

    override fun unregisterListener(
        handle: EventStreamVertex.ListenerHandle,
    ) {
        if (handle != TerminatedListenerHandle) {
            throw IllegalArgumentException("Invalid handle")
        }
    }
}
