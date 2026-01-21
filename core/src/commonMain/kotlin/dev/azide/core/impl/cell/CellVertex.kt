package dev.azide.core.impl.cell

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.Vertex.ActivationMode
import dev.azide.core.impl.cell.CellVertex.ListenerStatus
import dev.azide.core.impl.cell.CellVertex.Listener
import dev.azide.core.impl.cell.CellVertex.BoundListener
import dev.azide.core.impl.cell.CellVertex.ListenerHandle
import kotlin.jvm.JvmInline

sealed interface CellVertex<out ValueT> : Vertex {
    @JvmInline
    value class Update<out ValueT>(
        val updatedValue: ValueT,
    ) {
        fun <TransformedValueT> map(
            transform: (ValueT) -> TransformedValueT,
        ): Update<TransformedValueT> = Update(
            updatedValue = transform(updatedValue),
        )
    }

    interface Listener {
        fun handle(
            propagationContext: Transactions.PropagationContext,
        ): ListenerStatus
    }

    interface BoundListener {
        fun handleUpdate(
            propagationContext: Transactions.PropagationContext,
        )
    }

    interface ListenerHandle

    enum class ListenerStatus {
        Reachable, Unreachable,
    }

    val ongoingUpdate: Update<ValueT>?

    fun registerListener(
        propagationContext: Transactions.PropagationContext,
        listener: Listener,
        mode: ActivationMode,
    ): ListenerHandle

    fun unregisterListener(
        handle: ListenerHandle,
    )

    fun getOldValue(
        propagationContext: Transactions.PropagationContext,
    ): ValueT
}

fun <ValueT> CellVertex<ValueT>.registerListenerOnline(
    propagationContext: Transactions.PropagationContext,
    listener: Listener,
): ListenerHandle = registerListener(
    propagationContext = propagationContext,
    listener = listener,
    mode = ActivationMode.Online,
)

fun <ValueT> CellVertex<ValueT>.registerListenerOffline(
    propagationContext: Transactions.PropagationContext,
    listener: Listener,
): ListenerHandle = registerListener(
    propagationContext = propagationContext,
    listener = listener,
    mode = ActivationMode.Offline,
)

fun <ValueT> CellVertex<ValueT>.registerBoundListener(
    propagationContext: Transactions.PropagationContext,
    listener: BoundListener,
    mode: ActivationMode,
): ListenerHandle = registerListener(
    propagationContext = propagationContext,
    listener = object : Listener {
        override fun handle(
            propagationContext: Transactions.PropagationContext,
        ): ListenerStatus {
            listener.handleUpdate(
                propagationContext = propagationContext,
            )

            return ListenerStatus.Reachable
        }
    },
    mode = mode,
)

fun <ValueT> CellVertex<ValueT>.registerBoundListenerOnline(
    propagationContext: Transactions.PropagationContext,
    listener: BoundListener,
): ListenerHandle = registerBoundListener(
    propagationContext = propagationContext,
    listener = listener,
    mode = ActivationMode.Online,
)

fun <ValueT> CellVertex<ValueT>.registerBoundListenerOffline(
    propagationContext: Transactions.PropagationContext,
    listener: BoundListener,
): ListenerHandle = registerBoundListener(
    propagationContext = propagationContext,
    listener = listener,
    mode = ActivationMode.Offline,
)

fun <ValueT> CellVertex<ValueT>.getNewValue(
    propagationContext: Transactions.PropagationContext,
): ValueT = when (val ongoingUpdate = this.ongoingUpdate) {
    null -> getOldValue(
        propagationContext = propagationContext,
    )

    else -> ongoingUpdate.updatedValue
}
