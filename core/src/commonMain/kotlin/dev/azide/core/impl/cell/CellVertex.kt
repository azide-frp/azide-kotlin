package dev.azide.core.impl.cell

import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Vertex
import kotlin.jvm.JvmInline

interface CellVertex<out ValueT> : Vertex {
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

    val ongoingUpdate: Update<ValueT>?

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
