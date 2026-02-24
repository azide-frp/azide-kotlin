package dev.azide.core.impl

import dev.azide.core.impl.ListenableVertex.BoundListener
import dev.azide.core.impl.ListenableVertex.Listener
import dev.azide.core.impl.ListenableVertex.ListenerHandle
import dev.azide.core.impl.ListenableVertex.ListenerStatus

interface ListenableVertex {
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
        processingContext: Transactions.ProcessingContext,
        listener: Listener,
    ): ListenerHandle

    fun unregisterListener(
        handle: ListenerHandle,
    )
}

fun ListenableVertex.registerListenerOnline(
    propagationContext: Transactions.PropagationContext,
    listener: Listener,
): ListenerHandle = registerListener(
    processingContext = propagationContext,
    listener = listener,
)

fun ListenableVertex.registerListenerOffline(
    propagationContext: Transactions.PropagationContext,
    listener: Listener,
): ListenerHandle = registerListener(
    processingContext = propagationContext,
    listener = listener,
)

fun ListenableVertex.registerBoundListener(
    processingContext: Transactions.ProcessingContext,
    listener: BoundListener,
): ListenerHandle = registerListener(
    processingContext = processingContext,
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
)

fun ListenableVertex.registerBoundListenerOnline(
    propagationContext: Transactions.PropagationContext,
    listener: BoundListener,
): ListenerHandle = registerBoundListener(
    processingContext = propagationContext,
    listener = listener,
)

fun ListenableVertex.registerBoundListenerOffline(
    commitmentContext: Transactions.CommitmentContext,
    listener: BoundListener,
): ListenerHandle = registerBoundListener(
    processingContext = commitmentContext,
    listener = listener,
)
