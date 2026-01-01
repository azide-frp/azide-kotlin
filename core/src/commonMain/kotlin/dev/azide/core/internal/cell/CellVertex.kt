package dev.azide.core.internal.cell

import dev.azide.core.internal.Transactions
import dev.azide.core.internal.Vertex
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
    ): ObserverHandle

    fun unregisterObserver(
        handle: ObserverHandle,
    )

    fun getOldValue(
        propagationContext: Transactions.PropagationContext,
    ): ValueT
}

fun <ValueT> CellVertex<ValueT>.getNewValue(
    propagationContext: Transactions.PropagationContext,
): ValueT = when (val ongoingUpdate = this.ongoingUpdate) {
    null -> getOldValue(
        propagationContext = propagationContext,
    )

    else -> ongoingUpdate.updatedValue
}
