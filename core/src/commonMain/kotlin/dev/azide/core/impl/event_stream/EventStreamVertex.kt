package dev.azide.core.impl.event_stream

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.Vertex.ActivationMode
import dev.azide.core.impl.Vertex.BoundListener
import dev.azide.core.impl.Vertex.Listener
import dev.azide.core.impl.Vertex.ListenerHandle
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

    val ongoingEmission: Emission<EventT>?
}

fun <EventT> EventStreamVertex<EventT>.registerListenerOnline(
    propagationContext: Transactions.PropagationContext,
    listener: Listener,
): ListenerHandle = registerListener(
    propagationContext = propagationContext,
    listener = listener,
    mode = ActivationMode.Online,
)

fun <EventT> EventStreamVertex<EventT>.registerBoundListener(
    propagationContext: Transactions.PropagationContext,
    listener: BoundListener,
    mode: ActivationMode,
): ListenerHandle = registerListener(
    propagationContext = propagationContext,
    listener = object : Listener {
        override fun handle(
            propagationContext: Transactions.PropagationContext,
        ): Vertex.ListenerStatus {
            listener.handle(
                propagationContext = propagationContext,
            )

            return Vertex.ListenerStatus.Reachable
        }
    },
    mode = mode,
)

fun <EventT> EventStreamVertex<EventT>.registerBoundListenerOnline(
    propagationContext: Transactions.PropagationContext,
    listener: BoundListener,
): ListenerHandle = registerBoundListener(
    propagationContext = propagationContext,
    listener = listener,
    mode = ActivationMode.Online,
)

fun <EventT> EventStreamVertex<EventT>.registerBoundListenerOffline(
    propagationContext: Transactions.PropagationContext,
    listener: BoundListener,
): ListenerHandle = registerBoundListener(
    propagationContext = propagationContext,
    listener = listener,
    mode = ActivationMode.Offline,
)
