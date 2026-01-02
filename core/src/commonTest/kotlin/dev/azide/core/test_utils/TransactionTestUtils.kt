package dev.azide.core.test_utils

import dev.azide.core.Action
import dev.azide.core.internal.Transactions

interface TransactionTestContext {
    val propagationContext: Transactions.PropagationContext
}

object TransactionTestUtils {
    fun <ResultT> execute(
        block: context(TransactionTestContext) () -> ResultT,
    ): Unit = Transactions.executeWithResult { propagationContext ->
        with(
            object : TransactionTestContext {
                override val propagationContext = propagationContext
            },
        ) {
            block()
        }
    }
}

context(transactionTestContext: TransactionTestContext) fun <ResultT> Action<ResultT>.executeForTesting(): ResultT =
    Transactions.WrapUpContext.wrapUp(
        propagationContext = transactionTestContext.propagationContext,
    ) { wrapUpContext ->
        val (result, _) = this.executeInternally(
            propagationContext = transactionTestContext.propagationContext,
            wrapUpContext = wrapUpContext,
        )

        result
    }

context(transactionTestContext: TransactionTestContext) fun <ResultT> Action<ResultT>.executeForTestingRevocable(): Pair<ResultT, Action.RevocationHandle> =
    Transactions.WrapUpContext.wrapUp(
        propagationContext = transactionTestContext.propagationContext,
    ) { wrapUpContext ->
        this.executeInternally(
            propagationContext = transactionTestContext.propagationContext,
            wrapUpContext = wrapUpContext,
        )
    }

context(transactionTestContext: TransactionTestContext) internal fun TestInputStimulation.stimulateForTesting() {
    this.stimulate(
        propagationContext = transactionTestContext.propagationContext,
    )
}
