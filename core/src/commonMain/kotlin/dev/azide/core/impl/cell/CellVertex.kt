package dev.azide.core.impl.cell

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.Vertex.ActivationMode
import dev.azide.core.impl.cell.CellVertex.ObserverStatus
import dev.azide.core.impl.cell.CellVertex.UpdateObserver
import dev.azide.core.impl.cell.CellVertex.BoundUpdateObserver
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

    interface UpdateObserver {
        fun handleUpdate(
            propagationContext: Transactions.PropagationContext,
        ): ObserverStatus
    }

    interface BoundUpdateObserver {
        fun handleUpdate(
            propagationContext: Transactions.PropagationContext,
        )
    }

    interface ObserverHandle

    enum class ObserverStatus {
        Reachable, Unreachable,
    }

    val ongoingUpdate: Update<ValueT>?

    fun registerUpdateObserver(
        propagationContext: Transactions.PropagationContext,
        observer: UpdateObserver,
        mode: ActivationMode,
    ): ObserverHandle

    fun unregisterObserver(
        handle: ObserverHandle,
    )

    fun getOldValue(
        propagationContext: Transactions.PropagationContext,
    ): ValueT
}

fun <ValueT> CellVertex<ValueT>.registerUpdateObserverOnline(
    propagationContext: Transactions.PropagationContext,
    observer: UpdateObserver,
): CellVertex.ObserverHandle = registerUpdateObserver(
    propagationContext = propagationContext,
    observer = observer,
    mode = ActivationMode.Online,
)

fun <ValueT> CellVertex<ValueT>.registerUpdateObserverOffline(
    propagationContext: Transactions.PropagationContext,
    observer: UpdateObserver,
): CellVertex.ObserverHandle = registerUpdateObserver(
    propagationContext = propagationContext,
    observer = observer,
    mode = ActivationMode.Offline,
)

fun <ValueT> CellVertex<ValueT>.registerBoundUpdateObserver(
    propagationContext: Transactions.PropagationContext,
    observer: BoundUpdateObserver,
    mode: ActivationMode,
): CellVertex.ObserverHandle = registerUpdateObserver(
    propagationContext = propagationContext,
    observer = object : UpdateObserver {
        override fun handleUpdate(
            propagationContext: Transactions.PropagationContext,
        ): ObserverStatus {
            observer.handleUpdate(
                propagationContext = propagationContext,
            )

            return ObserverStatus.Reachable
        }
    },
    mode = mode,
)

fun <ValueT> CellVertex<ValueT>.registerBoundUpdateObserverOnline(
    propagationContext: Transactions.PropagationContext,
    observer: BoundUpdateObserver,
): CellVertex.ObserverHandle = registerBoundUpdateObserver(
    propagationContext = propagationContext,
    observer = observer,
    mode = ActivationMode.Online,
)

fun <ValueT> CellVertex<ValueT>.registerBoundUpdateObserverOffline(
    propagationContext: Transactions.PropagationContext,
    observer: BoundUpdateObserver,
): CellVertex.ObserverHandle = registerBoundUpdateObserver(
    propagationContext = propagationContext,
    observer = observer,
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
