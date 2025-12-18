package dev.azide.internal.cell

import dev.azide.internal.Transactions
import dev.azide.internal.Vertex
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
        fun handleUpdate(
            propagationContext: Transactions.PropagationContext,
            update: CellVertex.Update<ValueT>?,
        )
    }

    interface ObserverHandle

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
