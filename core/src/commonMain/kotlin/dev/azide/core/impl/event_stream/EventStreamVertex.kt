package dev.azide.core.impl.event_stream

import dev.azide.core.impl.ListenableVertex
import dev.azide.core.impl.ListenableVertex.BoundListener
import dev.azide.core.impl.ListenableVertex.Listener
import dev.azide.core.impl.ListenableVertex.ListenerHandle
import dev.azide.core.impl.Transactions
import kotlin.jvm.JvmInline

sealed interface EventStreamVertex<out EventT> : ListenableVertex {
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
    processingContext = propagationContext,
    listener = listener,
)

fun <EventT> EventStreamVertex<EventT>.registerBoundListener(
    propagationContext: Transactions.ProcessingContext,
    listener: BoundListener,
): ListenerHandle = registerListener(
    processingContext = propagationContext,
    listener = object : Listener {
        override fun handle(
            propagationContext: Transactions.PropagationContext,
        ): ListenableVertex.ListenerStatus {
            listener.handle(
                propagationContext = propagationContext,
            )

            return ListenableVertex.ListenerStatus.Reachable
        }
    },
)

fun <EventT> EventStreamVertex<EventT>.registerBoundListenerOnline(
    propagationContext: Transactions.PropagationContext,
    listener: BoundListener,
): ListenerHandle = registerBoundListener(
    propagationContext = propagationContext,
    listener = listener,
)

fun <EventT> EventStreamVertex<EventT>.registerBoundListenerOffline(
    propagationContext: Transactions.PropagationContext,
    listener: BoundListener,
): ListenerHandle = registerBoundListener(
    propagationContext = propagationContext,
    listener = listener,
)
