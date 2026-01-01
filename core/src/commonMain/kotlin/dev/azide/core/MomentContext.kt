package dev.azide.core

import dev.azide.core.internal.Transactions

interface MomentContext {
    companion object {
        fun <ResultT> wrapUp(
            propagationContext: Transactions.PropagationContext,
            block: context(MomentContext) () -> ResultT,
        ): ResultT = Transactions.WrapUpContext.wrapUp(
            propagationContext = propagationContext,
        ) { wrapUpContext ->
            with(
                MomentContextImpl(
                    propagationContext = propagationContext,
                    wrapUpContext = wrapUpContext,
                ),
            ) {
                block()
            }
        }
    }

    val propagationContext: Transactions.PropagationContext

    val wrapUpContext: Transactions.WrapUpContext
}
