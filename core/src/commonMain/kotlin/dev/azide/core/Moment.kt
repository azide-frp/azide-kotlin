package dev.azide.core

import dev.azide.core.Action.RevocationHandle
import dev.azide.core.internal.Transactions
import dev.azide.core.internal.utils.LazyUtils

interface Moment<out ResultT> {
    companion object {
        fun <ResultT, LoopedValueT : Any> looped(
            block: (Lazy<LoopedValueT>) -> Moment<Pair<ResultT, LoopedValueT>>,
        ): Moment<ResultT> = object : Moment<ResultT> {
            override fun pullInternally(
                propagationContext: Transactions.PropagationContext,
            ): ResultT = LazyUtils.looped { loopedValue: Lazy<LoopedValueT> ->
                val moment: Moment<Pair<ResultT, LoopedValueT>> = block(loopedValue)

                return@looped moment.pullInternally(
                    propagationContext = propagationContext,
                )
            }
        }

        fun <ResultT> pure(
            result: ResultT,
        ): Moment<ResultT> = object : Moment<ResultT> {
            override fun pullInternally(
                propagationContext: Transactions.PropagationContext,
            ): ResultT = result
        }
    }

    fun pullInternally(
        propagationContext: Transactions.PropagationContext,
    ): ResultT
}

val <ResultT> Moment<ResultT>.asAction: Action<ResultT>
    get() = object : Action<ResultT> {
        override fun executeInternally(
            propagationContext: Transactions.PropagationContext,
        ): Pair<ResultT, RevocationHandle> {
            val result: ResultT = this@asAction.pullInternally(
                propagationContext = propagationContext,
            )

            return Pair(
                result,
                RevocationHandle.Noop,
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
