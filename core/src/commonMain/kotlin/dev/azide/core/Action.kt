package dev.azide.core

import dev.azide.core.external.ExternalTrigger
import dev.azide.core.impl.RevocationHandle
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
                revocationHandle: RevocationHandle,
            ): Outcome<ResultT> = object : Outcome<ResultT> {
                override val result: ResultT = result
                override val revocationHandle: RevocationHandle = revocationHandle
            }
        }

        val result: ResultT
        val revocationHandle: RevocationHandle
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
                val revocationHandle: RevocationHandle = actionOutcome.revocationHandle

                return@looped LoopClosure(
                    result = Outcome.of(
                        result = loopClosure.result,
                        revocationHandle = revocationHandle,
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
                revocationHandle = RevocationHandle.Noop,
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
                revocationHandle = propagationContext.enqueueForExecution {
                    externalTrigger.executeExternally()
                },
            )
        }
    }

    fun executeInternally(
        propagationContext: Transactions.PropagationContext,
        wrapUpContext: Transactions.WrapUpContext,
    ): Outcome<ResultT>
}

fun <ResultT> Action<ResultT>.executeInternallyWrappedUp(
    propagationContext: Transactions.PropagationContext,
): Pair<ResultT, RevocationHandle> = Transactions.WrapUpContext.wrapUp(
    propagationContext,
) { wrapUpContext ->
    val outcome = executeInternally(
        propagationContext = propagationContext,
        wrapUpContext = wrapUpContext,
    )

    Pair(outcome.result, outcome.revocationHandle)
}

typealias Trigger = Action<Unit>

object Triggers {
    object Noop : Trigger {
        override fun executeInternally(
            propagationContext: Transactions.PropagationContext,
            wrapUpContext: Transactions.WrapUpContext,
        ): Action.Outcome<Unit> = Action.Outcome.of(
            result = Unit,
            revocationHandle = RevocationHandle.Noop,
        )
    }

    typealias Outcome = Action.Outcome<Unit>

    object Outcomes {
        fun of(
            revocationHandle: RevocationHandle,
        ): Outcome = Action.Outcome.of(
            result = Unit,
            revocationHandle = revocationHandle,
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

            val firstRevocationHandle = firstOutcome.revocationHandle

            val secondOutcome = second.executeInternally(
                propagationContext = propagationContext,
                wrapUpContext = wrapUpContext,
            )

            val secondRevocationHandle = secondOutcome.revocationHandle

            return Action.Outcome.of(
                result = Unit,
                revocationHandle = RevocationHandle.combine(
                    firstRevocationHandle,
                    secondRevocationHandle,
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
                ): RevocationHandle {
                    val outcome: Triggers.Outcome = this@merging.executeInternally(
                        propagationContext,
                        wrapUpContext,
                    )

                    return outcome.revocationHandle
                }
            },
            // We just allocate the trigger's identity
            revocationHandle = RevocationHandle.Noop,
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
        val revocationHandle: RevocationHandle = outcome.revocationHandle

        val transformedResult: TransformedResultT = transform(result)

        return Action.Outcome.of(
            result = transformedResult,
            revocationHandle = revocationHandle,
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
        val revocationHandle: RevocationHandle = outcome.revocationHandle

        val transformedAction = transform(result)

        val transformedOutcome = transformedAction.executeInternally(
            propagationContext = propagationContext,
            wrapUpContext = wrapUpContext,
        )

        val transformedResult: TransformedResultT = transformedOutcome.result
        val transformedRevocationHandle: RevocationHandle = transformedOutcome.revocationHandle

        return Action.Outcome.of(
            result = transformedResult,
            revocationHandle = RevocationHandle.combine(
                revocationHandle,
                transformedRevocationHandle,
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
