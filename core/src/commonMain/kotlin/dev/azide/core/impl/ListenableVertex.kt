package dev.azide.core.impl

import dev.azide.core.impl.ListenableVertex.ActivationMode
import dev.azide.core.impl.ListenableVertex.BoundListener
import dev.azide.core.impl.ListenableVertex.Listener
import dev.azide.core.impl.ListenableVertex.ListenerHandle
import dev.azide.core.impl.ListenableVertex.ListenerStatus

interface ListenableVertex {
    enum class ActivationMode {
        /**
         * Online activation is the "full" activation mode, when the vertex is activated in the middle of the
         * propagation phase. Other vertices will expect the activated vertex to expose its volatile state. The vertex
         * should subscribe/observe its dependencies, having in mind that the propagation is still ongoing.
         */
        Online,
        /**
         * Online activation is the "quick" activation mode, when the vertex is activated after the propagation phase.
         * Other vertices won't expect the activated vertex to expose its volatile state. The vertex should subscribe/
         * observe its dependencies, having in mind that the propagation has ended and will happen again not sooner than
         * in the next transaction.
         */
        Offline,
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
        fun handle(
            propagationContext: Transactions.PropagationContext,
        )
    }

    interface ListenerHandle

    enum class ListenerStatus {
        Reachable, Unreachable,
    }

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

fun ListenableVertex.registerListenerOnline(
    propagationContext: Transactions.PropagationContext,
    listener: Listener,
): ListenerHandle = registerListener(
    propagationContext = propagationContext,
    listener = listener,
    mode = ActivationMode.Online,
)

fun ListenableVertex.registerListenerOffline(
    propagationContext: Transactions.PropagationContext,
    listener: Listener,
): ListenerHandle = registerListener(
    propagationContext = propagationContext,
    listener = listener,
    mode = ActivationMode.Offline,
)

fun ListenableVertex.registerBoundListener(
    propagationContext: Transactions.PropagationContext,
    listener: BoundListener,
    mode: ActivationMode,
): ListenerHandle = registerListener(
    propagationContext = propagationContext,
    listener = object : Listener {
        override fun handle(
            propagationContext: Transactions.PropagationContext,
        ): ListenerStatus {
            listener.handle(
                propagationContext = propagationContext,
            )

            return ListenerStatus.Reachable
        }
    },
    mode = mode,
)

fun ListenableVertex.registerBoundListenerOnline(
    propagationContext: Transactions.PropagationContext,
    listener: BoundListener,
): ListenerHandle = registerBoundListener(
    propagationContext = propagationContext,
    listener = listener,
    mode = ActivationMode.Online,
)

fun ListenableVertex.registerBoundListenerOffline(
    propagationContext: Transactions.PropagationContext,
    listener: BoundListener,
): ListenerHandle = registerBoundListener(
    propagationContext = propagationContext,
    listener = listener,
    mode = ActivationMode.Offline,
)
