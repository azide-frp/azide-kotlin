package dev.azide.core.impl.effects

import dev.azide.core.impl.Revocable
import dev.azide.core.impl.Transactions

interface InternalEffect<ResultT> {
    interface RevocableOutcome<ResultT> : Revocable {
        val result: ResultT

        fun cancelInternally(
            propagationContext: Transactions.PropagationContext,
            wrapUpContext: Transactions.WrapUpContext,
        ): Revocable
    }

    fun startInternally(
        propagationContext: Transactions.PropagationContext,
        wrapUpContext: Transactions.WrapUpContext,
    ): RevocableOutcome<ResultT>
}
