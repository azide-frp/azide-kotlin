package dev.azide.core.impl.cell

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex
import dev.azide.core.impl.Vertex.ActivationMode
import dev.azide.core.impl.cell.CellVertex.Observer
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

    interface Observer<in ValueT> {
        fun handleUpdateWithStatus(
            propagationContext: Transactions.PropagationContext,
            update: Update<ValueT>?,
        ): ObserverStatus
    }

    interface ObserverHandle

    enum class ObserverStatus {
        Reachable, Unreachable,
    }

    val ongoingUpdate: Update<ValueT>?

    fun registerObserver(
        propagationContext: Transactions.PropagationContext,
        observer: Observer<ValueT>,
        mode: ActivationMode,
    ): ObserverHandle

    fun unregisterObserver(
        handle: ObserverHandle,
    )

    fun getOldValue(
        propagationContext: Transactions.PropagationContext,
    ): ValueT
}

fun <ValueT> CellVertex<ValueT>.registerObserverOnline(
    propagationContext: Transactions.PropagationContext,
    observer: Observer<ValueT>,
): CellVertex.ObserverHandle = registerObserver(
    propagationContext = propagationContext,
    observer = observer,
    mode = ActivationMode.Online,
)

fun <ValueT> CellVertex<ValueT>.registerObserverOffline(
    propagationContext: Transactions.PropagationContext,
    observer: Observer<ValueT>,
): CellVertex.ObserverHandle = registerObserver(
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
