package dev.azide.core.impl.event_stream

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.ListenableVertex

class TerminatedEventStreamVertex<EventT> : EventStreamVertex<EventT> {
    data object TerminatedListenerHandle : ListenableVertex.ListenerHandle

    override val ongoingEmission: Nothing?
        get() = null

    override val listenerCount: Int
        get() = 0

    override fun registerListener(
        propagationContext: Transactions.PropagationContext,
        listener: ListenableVertex.Listener,
        mode: ListenableVertex.ActivationMode,
    ): TerminatedListenerHandle = TerminatedListenerHandle

    override fun unregisterListener(
        handle: ListenableVertex.ListenerHandle,
    ) {
        if (handle != TerminatedListenerHandle) {
            throw IllegalArgumentException("Invalid handle")
        }
    }
}
