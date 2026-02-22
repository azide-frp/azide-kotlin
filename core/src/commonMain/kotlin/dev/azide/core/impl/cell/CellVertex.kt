package dev.azide.core.impl.cell

import dev.azide.core.impl.ListenableVertex
import dev.azide.core.impl.Transactions
import kotlin.jvm.JvmInline

interface CellVertex<out ValueT> : ListenableVertex {
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

    /**
     * Get the cell's old value. If the cell is to be listened to, be sure to listen to the cell before getting its
     * old value for performance reasons.
     */
    fun getOldValue(
        processingContext: Transactions.ProcessingContext,
    ): ValueT
}

fun <ValueT> CellVertex<ValueT>.getNewValue(
    processingContext: Transactions.ProcessingContext,
): ValueT = when (val ongoingUpdate = this.ongoingUpdate) {
    null -> getOldValue(
        processingContext = processingContext,
    )

    else -> ongoingUpdate.updatedValue
}
