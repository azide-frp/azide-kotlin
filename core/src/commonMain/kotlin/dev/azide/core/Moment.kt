package dev.azide.core

import dev.azide.core.internal.Transactions

interface Moment<out ResultT> {
    fun pullInternally(
        propagationContext: Transactions.PropagationContext,
    ): ResultT
}

val <ResultT> Moment<ResultT>.asAction: Action<ResultT>
    get() = object : Action<ResultT> {
        override fun executeInternally(
            propagationContext: Transactions.PropagationContext,
        ): Pair<ResultT, Action.RevocationHandle> {
            val result: ResultT = this@asAction.pullInternally(
                propagationContext = propagationContext,
            )

            return Pair(
                result,
                Action.RevocationHandle.Noop,
            )
        }
    }

fun <ResultT, TransformedResultT> Moment<ResultT>.map(
    transform: (ResultT) -> TransformedResultT,
): Moment<TransformedResultT> = object : Moment<TransformedResultT> {
    override fun pullInternally(
        propagationContext: Transactions.PropagationContext,
    ): TransformedResultT {
        val result: ResultT = this@map.pullInternally(
            propagationContext = propagationContext,
        )

        return transform(result)
    }
}

fun <ResultT, TransformedResultT> Moment<ResultT>.joinOf(
    transform: (ResultT) -> Moment<TransformedResultT>,
): Moment<TransformedResultT> = object : Moment<TransformedResultT> {
    override fun pullInternally(
        propagationContext: Transactions.PropagationContext,
    ): TransformedResultT {
        val result: ResultT = this@joinOf.pullInternally(
            propagationContext = propagationContext,
        )

        val transformedMoment = transform(result)

        val transformedResult: TransformedResultT = transformedMoment.pullInternally(
            propagationContext = propagationContext,
        )

        return transformedResult
    }
}
