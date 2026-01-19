package dev.azide.core

import dev.azide.core.external.ExternalTrigger
import dev.azide.core.impl.Revocable
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.effects.AbstractExecutionMergingTrigger
import dev.azide.core.impl.utils.LoopClosure
import dev.azide.core.impl.utils.LoopUtils
import kotlin.experimental.ExperimentalTypeInference
import kotlin.jvm.JvmName

interface Action<out ResultT> {
    interface Outcome<out ResultT> {
        companion object {
            fun <ResultT> of(
                result: ResultT,
                revocable: Revocable,
            ): Outcome<ResultT> = object : Outcome<ResultT> {
                override val result: ResultT = result
                override val revocable: Revocable = revocable
            }
        }

        val result: ResultT
        val revocable: Revocable
    }

    companion object {
        fun <ResultT, LoopedValueT : Any> looped(
            block: (Lazy<LoopedValueT>) -> Action<LoopClosure<ResultT, LoopedValueT>>,
        ): Action<ResultT> = object : Action<ResultT> {
            override fun executeInternally(
                propagationContext: Transactions.PropagationContext,
                wrapUpContext: Transactions.WrapUpContext,
            ): Outcome<ResultT> = LoopUtils.looped { loopedValue: Lazy<LoopedValueT> ->
                val action: Action<LoopClosure<ResultT, LoopedValueT>> = block(loopedValue)

                val actionOutcome = action.executeInternally(
                    propagationContext = propagationContext,
                    wrapUpContext = wrapUpContext,
                )

                val loopClosure: LoopClosure<ResultT, LoopedValueT> = actionOutcome.result
                val revocable: Revocable = actionOutcome.revocable

                return@looped LoopClosure(
                    result = Outcome.of(
                        result = loopClosure.result,
                        revocable = revocable,
                    ),
                    loopedValue = loopClosure.loopedValue,
                )
            }
        }

        fun <ResultT> pure(
            result: ResultT,
        ): Action<ResultT> = object : Action<ResultT> {
            override fun executeInternally(
                propagationContext: Transactions.PropagationContext,
                wrapUpContext: Transactions.WrapUpContext,
            ): Outcome<ResultT> = Outcome.of(
                result = result,
                revocable = Revocable.Noop,
            )
        }

        inline fun adapt(
            crossinline executeExternally: () -> Unit,
        ): Trigger = adapt(
            object : ExternalTrigger {
                override fun executeExternally() {
                    executeExternally()
                }
            },
        )

        fun adapt(
            externalTrigger: ExternalTrigger,
        ): Trigger = object : Trigger {
            override fun executeInternally(
                propagationContext: Transactions.PropagationContext,
                wrapUpContext: Transactions.WrapUpContext,
            ): Outcome<Unit> = Outcome.of(
                result = Unit,
                revocable = propagationContext.enqueueForExecution {
                    externalTrigger.executeExternally()
                },
            )
        }

        fun <ResultT1, ResultT2, TransformedResultT> map2(
            action1: Action<ResultT1>,
            action2: Action<ResultT2>,
            transform: (
                result1: ResultT1,
                result2: ResultT2,
            ) -> TransformedResultT,
        ): Action<TransformedResultT> = object : Action<TransformedResultT> {
            override fun executeInternally(
                propagationContext: Transactions.PropagationContext,
                wrapUpContext: Transactions.WrapUpContext,
            ): Outcome<TransformedResultT> {
                val outcome1 = action1.executeInternally(
                    propagationContext = propagationContext,
                    wrapUpContext = wrapUpContext,
                )

                val outcome2 = action2.executeInternally(
                    propagationContext = propagationContext,
                    wrapUpContext = wrapUpContext,
                )

                return Outcome.of(
                    result = transform(
                        outcome1.result,
                        outcome2.result,
                    ),
                    revocable = Revocable.combine(
                        outcome1.revocable,
                        outcome2.revocable,
                    ),
                )
            }
        }
    }

    fun executeInternally(
        propagationContext: Transactions.PropagationContext,
        wrapUpContext: Transactions.WrapUpContext,
    ): Outcome<ResultT>
}

fun <ResultT> Action<ResultT>.executeInternallyWrappedUp(
    propagationContext: Transactions.PropagationContext,
): Pair<ResultT, Revocable> = Transactions.WrapUpContext.wrapUp(
    propagationContext,
) { wrapUpContext ->
    val outcome = executeInternally(
        propagationContext = propagationContext,
        wrapUpContext = wrapUpContext,
    )

    Pair(outcome.result, outcome.revocable)
}

typealias Trigger = Action<Unit>

object Triggers {
    object Noop : Trigger {
        override fun executeInternally(
            propagationContext: Transactions.PropagationContext,
            wrapUpContext: Transactions.WrapUpContext,
        ): Action.Outcome<Unit> = Action.Outcome.of(
            result = Unit,
            revocable = Revocable.Noop,
        )
    }

    typealias Outcome = Action.Outcome<Unit>

    object Outcomes {
        fun of(
            revocable: Revocable,
        ): Outcome = Action.Outcome.of(
            result = Unit,
            revocable = revocable,
        )
    }

    fun combine(
        first: Trigger,
        second: Trigger,
    ): Trigger = object : Trigger {
        override fun executeInternally(
            propagationContext: Transactions.PropagationContext,
            wrapUpContext: Transactions.WrapUpContext,
        ): Action.Outcome<Unit> {
            val firstOutcome = first.executeInternally(
                propagationContext = propagationContext,
                wrapUpContext = wrapUpContext,
            )

            val firstRevocable = firstOutcome.revocable

            val secondOutcome = second.executeInternally(
                propagationContext = propagationContext,
                wrapUpContext = wrapUpContext,
            )

            val secondRevocable = secondOutcome.revocable

            return Action.Outcome.of(
                result = Unit,
                revocable = Revocable.combine(
                    firstRevocable,
                    secondRevocable,
                ),
            )
        }
    }

    fun Trigger.merging(): Action<Trigger> = object : Action<Trigger> {
        override fun executeInternally(
            propagationContext: Transactions.PropagationContext,
            wrapUpContext: Transactions.WrapUpContext,
        ): Action.Outcome<Trigger> = Action.Outcome.of(
            result = object : AbstractExecutionMergingTrigger() {
                override fun executeInternallyOnce(
                    propagationContext: Transactions.PropagationContext,
                    wrapUpContext: Transactions.WrapUpContext,
                ): Revocable {
                    val outcome: Outcome = this@merging.executeInternally(
                        propagationContext,
                        wrapUpContext,
                    )

                    return outcome.revocable
                }
            },
            // We just allocate the trigger's identity
            revocable = Revocable.Noop,
        )
    }
}

fun <ResultT, TransformedResultT> Action<ResultT>.map(
    transform: (ResultT) -> TransformedResultT,
): Action<TransformedResultT> = object : Action<TransformedResultT> {
    override fun executeInternally(
        propagationContext: Transactions.PropagationContext,
        wrapUpContext: Transactions.WrapUpContext,
    ): Action.Outcome<TransformedResultT> {
        val outcome = this@map.executeInternally(
            propagationContext = propagationContext,
            wrapUpContext = wrapUpContext,
        )

        val result: ResultT = outcome.result
        val revocable: Revocable = outcome.revocable

        val transformedResult: TransformedResultT = transform(result)

        return Action.Outcome.of(
            result = transformedResult,
            revocable = revocable,
        )
    }
}

fun <ResultT, TransformedResultT> Action<ResultT>.joinOf(
    transform: (ResultT) -> Action<TransformedResultT>,
): Action<TransformedResultT> = object : Action<TransformedResultT> {
    override fun executeInternally(
        propagationContext: Transactions.PropagationContext,
        wrapUpContext: Transactions.WrapUpContext,
    ): Action.Outcome<TransformedResultT> {
        val outcome = this@joinOf.executeInternally(
            propagationContext = propagationContext,
            wrapUpContext = wrapUpContext,
        )

        val result: ResultT = outcome.result
        val revocable: Revocable = outcome.revocable

        val transformedAction = transform(result)

        val transformedOutcome = transformedAction.executeInternally(
            propagationContext = propagationContext,
            wrapUpContext = wrapUpContext,
        )

        val transformedResult: TransformedResultT = transformedOutcome.result
        val transformedRevocable: Revocable = transformedOutcome.revocable

        return Action.Outcome.of(
            result = transformedResult,
            revocable = Revocable.combine(
                revocable,
                transformedRevocable,
            ),
        )
    }
}

@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
@JvmName("joinOfActionFromMoment")
fun <ResultT, TransformedResultT> Action<ResultT>.joinOf(
    transform: (ResultT) -> Moment<TransformedResultT>,
): Action<TransformedResultT> = joinOf {
    transform(it).asAction
}

fun <ResultT> Action<ResultT>.executeExternally(): ResultT = Transactions.executeWithResult {
    val (result: ResultT, _) = executeInternallyWrappedUp(it)

    result
}
