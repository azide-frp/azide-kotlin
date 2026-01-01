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
                wrapUpContext: Transactions.WrapUpContext,
            ): ResultT = LazyUtils.looped { loopedValue: Lazy<LoopedValueT> ->
                val moment: Moment<Pair<ResultT, LoopedValueT>> = block(loopedValue)

                return@looped moment.pullInternally(
                    propagationContext = propagationContext,
                    wrapUpContext = wrapUpContext,
                )
            }
        }

        fun <ResultT> pure(
            result: ResultT,
        ): Moment<ResultT> = object : Moment<ResultT> {
            override fun pullInternally(
                propagationContext: Transactions.PropagationContext,
                wrapUpContext: Transactions.WrapUpContext,
            ): ResultT = result
        }

        fun <ResultT> decontextualize(
            block: context(MomentContext) () -> ResultT,
        ): Moment<ResultT> = object : Moment<ResultT> {
            override fun pullInternally(
                propagationContext: Transactions.PropagationContext,
                wrapUpContext: Transactions.WrapUpContext,
            ): ResultT =  with(
                MomentContextImpl(
                    propagationContext = propagationContext,
                    wrapUpContext = wrapUpContext,
                ),
            ) {
                block()
            }
        }
    }

    fun pullInternally(
        propagationContext: Transactions.PropagationContext,
        wrapUpContext: Transactions.WrapUpContext,
    ): ResultT
}

context(momentContext: MomentContext) fun <ResultT> Moment<ResultT>.pullInContext(): ResultT = pullInternally(
    propagationContext = momentContext.propagationContext,
    wrapUpContext = momentContext.wrapUpContext,
)

fun <ResultT> Moment<ResultT>.pullInternallyWrappedUp(
    propagationContext: Transactions.PropagationContext,
): ResultT = Transactions.WrapUpContext.wrapUp(
    propagationContext,
) { wrapUpContext ->
    pullInternally(
        propagationContext = propagationContext,
        wrapUpContext = wrapUpContext,
    )
}

val <ResultT> Moment<ResultT>.asAction: Action<ResultT>
    get() = object : Action<ResultT> {
        override fun executeInternally(
            propagationContext: Transactions.PropagationContext,
            wrapUpContext: Transactions.WrapUpContext,
        ): Pair<ResultT, RevocationHandle> {
            val result: ResultT = this@asAction.pullInternally(
                propagationContext = propagationContext,
                wrapUpContext = wrapUpContext,
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
        wrapUpContext: Transactions.WrapUpContext,
    ): TransformedResultT {
        val result: ResultT = this@map.pullInternally(
            propagationContext = propagationContext,
            wrapUpContext = wrapUpContext,
        )

        return transform(result)
    }
}

fun <ResultT, TransformedResultT> Moment<ResultT>.joinOf(
    transform: (ResultT) -> Moment<TransformedResultT>,
): Moment<TransformedResultT> = object : Moment<TransformedResultT> {
    override fun pullInternally(
        propagationContext: Transactions.PropagationContext,
        wrapUpContext: Transactions.WrapUpContext,
    ): TransformedResultT {
        val result: ResultT = this@joinOf.pullInternally(
            propagationContext = propagationContext,
            wrapUpContext = wrapUpContext,
        )

        val transformedMoment = transform(result)

        val transformedResult: TransformedResultT = transformedMoment.pullInternally(
            propagationContext = propagationContext,
            wrapUpContext = wrapUpContext,
        )

        return transformedResult
    }
}
