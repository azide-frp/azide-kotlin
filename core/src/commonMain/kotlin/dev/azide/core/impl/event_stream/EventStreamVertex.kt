package dev.azide.core.impl.event_stream

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.Vertex.ActivationMode
import dev.azide.core.impl.event_stream.EventStreamVertex.BoundListener
import dev.azide.core.impl.event_stream.EventStreamVertex.Listener
import dev.azide.core.impl.event_stream.EventStreamVertex.ListenerHandle
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

    interface Listener {
        object Noop : Listener {
            override fun handle(
                propagationContext: Transactions.PropagationContext,
            ): ListenerStatus = ListenerStatus.Reachable
        }

        fun handle(
            propagationContext: Transactions.PropagationContext,
        ): ListenerStatus
    }

    interface BoundListener {
        fun handleEmission(
            propagationContext: Transactions.PropagationContext,
        )
    }

    interface ListenerHandle

    enum class ListenerStatus {
        Reachable, Unreachable,
    }

    val ongoingEmission: Emission<EventT>?

    val listenerCount: Int

    fun registerListener(
        propagationContext: Transactions.PropagationContext,
        listener: Listener,
        mode: ActivationMode,
    ): ListenerHandle

    fun unregisterListener(
        handle: ListenerHandle,
    )
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
        ): EventStreamVertex.ListenerStatus {
            listener.handleEmission(
                propagationContext = propagationContext,
            )

            return EventStreamVertex.ListenerStatus.Reachable
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
