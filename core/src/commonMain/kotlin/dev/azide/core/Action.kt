package dev.azide.core

import dev.azide.core.Action.RevocationHandle
import dev.azide.core.internal.Transactions
import dev.azide.core.internal.utils.LazyUtils

interface Action<out ResultT> {
    interface RevocationHandle {
        object Noop : RevocationHandle {
            override fun revoke() {
            }
        }

        companion object {
            fun combine(
                firstSubHandle: RevocationHandle,
                secondSubHandle: RevocationHandle,
            ): RevocationHandle = object : RevocationHandle {
                override fun revoke() {
                    firstSubHandle.revoke()
                    secondSubHandle.revoke()
                }
            }

            fun combine(
                vararg subHandles: RevocationHandle,
            ): RevocationHandle = object : RevocationHandle {
                override fun revoke() {
                    for (handle in subHandles) {
                        handle.revoke()
                    }
                }
            }
        }

        fun revoke()
    }

    companion object {
        fun <ResultT, LoopedValueT : Any> looped(
            block: (Lazy<LoopedValueT>) -> Action<Pair<ResultT, LoopedValueT>>,
        ): Action<ResultT> = object : Action<ResultT> {
            override fun executeInternally(
                propagationContext: Transactions.PropagationContext,
            ): Pair<ResultT, RevocationHandle> = LazyUtils.looped { loopedValue: Lazy<LoopedValueT> ->
                val action: Action<Pair<ResultT, LoopedValueT>> = block(loopedValue)

                val (
                    resultAndLoopedValue: Pair<ResultT, LoopedValueT>,
                    revocationHandle: RevocationHandle,
                ) = action.executeInternally(
                    propagationContext = propagationContext,
                )

                val (
                    result: ResultT,
                    loopedValue: LoopedValueT,
                ) = resultAndLoopedValue

                val resultAndRevocationHandle = Pair(
                    result,
                    revocationHandle,
                )

                return@looped Pair(
                    resultAndRevocationHandle,
                    loopedValue,
                )
            }
        }

        fun <ResultT> pure(
            result: ResultT,
        ): Action<ResultT> = object : Action<ResultT> {
            override fun executeInternally(
                propagationContext: Transactions.PropagationContext,
            ): Pair<ResultT, RevocationHandle> = Pair(
                result,
                RevocationHandle.Noop,
            )
        }

        inline fun wrap(
            crossinline executeExternally: () -> Unit,
        ): Trigger = wrap(
            object : ExternalSideEffect {
                override fun executeExternally() {
                    executeExternally()
                }
            },
        )

        fun wrap(
            externalSideEffect: ExternalSideEffect,
        ): Trigger = object : Trigger {
            override fun executeInternally(
                propagationContext: Transactions.PropagationContext,
            ): Pair<Unit, RevocationHandle> = Pair(
                Unit,
                propagationContext.enqueueForExecution(externalSideEffect),
            )
        }
    }

    fun executeInternally(
        propagationContext: Transactions.PropagationContext,
    ): Pair<ResultT, RevocationHandle>
}

typealias Trigger = Action<Unit>

object Triggers {
    object Noop : Trigger {
        override fun executeInternally(
            propagationContext: Transactions.PropagationContext,
        ): Pair<Unit, RevocationHandle> = Pair(
            Unit,
            RevocationHandle.Noop,
        )
    }

    fun combine(
        first: Trigger,
        second: Trigger,
    ): Trigger = object : Trigger {
        override fun executeInternally(
            propagationContext: Transactions.PropagationContext,
        ): Pair<Unit, Action.RevocationHandle> {
            val (_: Unit, firstRevocationHandle) = first.executeInternally(
                propagationContext = propagationContext,
            )

            val (_: Unit, secondRevocationHandle) = second.executeInternally(
                propagationContext = propagationContext,
            )

            return Pair(
                Unit,
                Action.RevocationHandle.combine(
                    firstRevocationHandle,
                    secondRevocationHandle,
                ),
            )
        }
    }
}

fun <ResultT, TransformedResultT> Action<ResultT>.map(
    transform: (ResultT) -> TransformedResultT,
): Action<TransformedResultT> = object : Action<TransformedResultT> {
    override fun executeInternally(
        propagationContext: Transactions.PropagationContext,
    ): Pair<TransformedResultT, Action.RevocationHandle> {
        val (result: ResultT, revocationHandle) = this@map.executeInternally(
            propagationContext = propagationContext,
        )

        val transformedResult: TransformedResultT = transform(result)

        return Pair(
            transformedResult,
            revocationHandle,
        )
    }
}

fun <ResultT, TransformedResultT> Action<ResultT>.joinOf(
    transform: (ResultT) -> Action<TransformedResultT>,
): Action<TransformedResultT> = object : Action<TransformedResultT> {
    override fun executeInternally(
        propagationContext: Transactions.PropagationContext,
    ): Pair<TransformedResultT, Action.RevocationHandle> {
        val (result: ResultT, revocationHandle) = this@joinOf.executeInternally(
            propagationContext = propagationContext,
        )

        val transformedAction = transform(result)

        val (transformedResult: TransformedResultT, transformedRevocationHandle) = transformedAction.executeInternally(
            propagationContext = propagationContext,
        )

        return Pair(
            transformedResult,
            Action.RevocationHandle.combine(
                revocationHandle,
                transformedRevocationHandle,
            ),
        )
    }
}
