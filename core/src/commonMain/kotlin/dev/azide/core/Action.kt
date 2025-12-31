package dev.azide.core

import dev.azide.core.internal.Transactions

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

    object Noop : Trigger {
        override fun executeInternally(
            propagationContext: Transactions.PropagationContext,
        ): Pair<Unit, RevocationHandle> = Pair(
            Unit,
            RevocationHandle.Noop,
        )
    }

    companion object {
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
