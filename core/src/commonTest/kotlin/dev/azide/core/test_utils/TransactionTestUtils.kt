package dev.azide.core.test_utils

import dev.azide.core.Action
import dev.azide.core.Effect
import dev.azide.core.Moment
import dev.azide.core.impl.Revocable
import dev.azide.core.impl.Transactions

object TransactionTestUtils {
    fun <ResultT> executeInsideTransaction(
        block: context(TransactionTestContext) () -> ResultT,
    ): ResultT = Transactions.executeWithResult { propagationContext ->
        with(
            object : TransactionTestContext {
                override val propagationContext = propagationContext
            },
        ) {
            block()
        }
    }
}

@Deprecated("Switch to the new test utils")
context(transactionTestContext: TransactionTestContext) fun <ResultT> Action<ResultT>.executeForTesting(): ResultT =
    Transactions.WrapUpContext.wrapUp(
        propagationContext = transactionTestContext.propagationContext,
    ) { wrapUpContext ->
        val outcome = this.executeInternally(
            propagationContext = transactionTestContext.propagationContext,
            wrapUpContext = wrapUpContext,
        )

        outcome.result
    }

@Deprecated("Switch to the new test utils")
context(transactionTestContext: TransactionTestContext) fun <ResultT> Action<ResultT>.executeForTestingRevocable(): Pair<ResultT, Revocable> =
    Transactions.WrapUpContext.wrapUp(
        propagationContext = transactionTestContext.propagationContext,
    ) { wrapUpContext ->
        val outcome = this.executeInternally(
            propagationContext = transactionTestContext.propagationContext,
            wrapUpContext = wrapUpContext,
        )

        Pair(outcome.result, outcome.revocable)
    }

@Deprecated("Switch to the new test utils")
context(@Suppress("unused") transactionTestContext: TransactionTestContext) fun Revocable.revokeForTesting() {
    revoke()
}

@Deprecated("Switch to the new test utils")
context(transactionTestContext: TransactionTestContext) fun <ResultT> Effect<ResultT>.startForTesting(): ResultT =
    this.start.executeForTesting().result

@Deprecated("Switch to the new test utils")
context(transactionTestContext: TransactionTestContext) fun <ResultT> Effect<ResultT>.startForTestingRevocable(): Pair<ResultT, Revocable> {
    val (effectOutcome, revocable) = this.start.executeForTestingRevocable()

    return Pair(
        effectOutcome.result,
        revocable,
    )
}

@Deprecated("Switch to the new test utils")
context(transactionTestContext: TransactionTestContext) fun <ResultT> Effect<ResultT>.startForTestingCancellable(): Pair<ResultT, Effect.Handle> {
    val outcome = this.start.executeForTesting()

    return Pair(
        outcome.result,
        outcome.handle,
    )
}

@Deprecated("Switch to the new test utils")
context(transactionTestContext: TransactionTestContext) internal fun TestInputStimulation.stimulateForTesting() {
    this.stimulate(
        propagationContext = transactionTestContext.propagationContext,
    )
}
