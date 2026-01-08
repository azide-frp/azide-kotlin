package dev.azide.core

import dev.azide.core.impl.Revocable
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.utils.LoopClosure
import dev.azide.core.impl.utils.LoopUtils
import kotlin.experimental.ExperimentalTypeInference

interface Moment<out ResultT> {
    companion object {
        fun <ResultT, LoopedValueT : Any> looped(
            block: (Lazy<LoopedValueT>) -> Moment<LoopClosure<ResultT, LoopedValueT>>,
        ): Moment<ResultT> = object : Moment<ResultT> {
            override fun pullInternally(
                propagationContext: Transactions.PropagationContext,
                wrapUpContext: Transactions.WrapUpContext,
            ): ResultT = LoopUtils.looped { loopedValue: Lazy<LoopedValueT> ->
                val moment: Moment<LoopClosure<ResultT, LoopedValueT>> = block(loopedValue)

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
            ): ResultT = with(
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
        ): Action.Outcome<ResultT> {
            val result: ResultT = this@asAction.pullInternally(
                propagationContext = propagationContext,
                wrapUpContext = wrapUpContext,
            )

            return Action.Outcome.of(
                result = result,
                revocable = Revocable.Noop,
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

@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
fun <ResultT, TransformedResultT> Moment<ResultT>.joinOf(
    transform: (ResultT) -> Action<TransformedResultT>,
): Action<TransformedResultT> = asAction.joinOf(transform)
