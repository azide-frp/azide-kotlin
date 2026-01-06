package dev.azide.core.test_utils

import dev.azide.core.Action
import dev.azide.core.Effect
import dev.azide.core.internal.RevocationHandle
import dev.azide.core.internal.Transactions

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

    context(transactionTestContext: TransactionTestContext) internal fun <ResultT> verifyIsExecutedOnce(
        inputStimulation: TestInputStimulation,
        targetAction: TestTargetAction<ResultT>,
    ): TestTargetAction.ExecutionRecord<ResultT> {
        TODO()
    }
}

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

context(transactionTestContext: TransactionTestContext) fun <ResultT> Action<ResultT>.executeForTestingRevocable(): Pair<ResultT, RevocationHandle> =
    Transactions.WrapUpContext.wrapUp(
        propagationContext = transactionTestContext.propagationContext,
    ) { wrapUpContext ->
        val outcome = this.executeInternally(
            propagationContext = transactionTestContext.propagationContext,
            wrapUpContext = wrapUpContext,
        )

        Pair(outcome.result, outcome.revocationHandle)
    }

context(@Suppress("unused") transactionTestContext: TransactionTestContext) fun RevocationHandle.revokeForTesting() {
    revoke()
}

context(transactionTestContext: TransactionTestContext) fun <ResultT> Effect<ResultT>.startForTesting(): ResultT =
    this.start.executeForTesting().result

context(transactionTestContext: TransactionTestContext) fun <ResultT> Effect<ResultT>.startForTestingRevocable(): Pair<ResultT, RevocationHandle> {
    val (effectOutcome, revocationHandle) = this.start.executeForTestingRevocable()

    return Pair(
        effectOutcome.result,
        revocationHandle,
    )
}

context(transactionTestContext: TransactionTestContext) fun <ResultT> Effect<ResultT>.startForTestingCancellable(): Pair<ResultT, Effect.Handle> {
    val outcome = this.start.executeForTesting()

    return Pair(
        outcome.result,
        outcome.handle,
    )
}

context(transactionTestContext: TransactionTestContext) internal fun TestInputStimulation.stimulateForTesting() {
    this.stimulate(
        propagationContext = transactionTestContext.propagationContext,
    )
}
