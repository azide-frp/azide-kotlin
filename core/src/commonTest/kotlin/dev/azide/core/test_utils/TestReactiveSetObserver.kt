package dev.azide.core.test_utils

import dev.azide.core.collections.ReactiveSet
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.collections.reactive_set.ReactiveSetVertex
import dev.azide.core.impl.collections.reactive_set.ReactiveSetVertex.SetChange

class TestReactiveSetObserver<ElementT>(
    val observedReactiveSetVertex: ReactiveSetVertex<ElementT>,
) : ReactiveSetVertex.SetObserver<ElementT> {
    interface Handle {
        fun cancel()
    }

    private val receivedChanges = mutableListOf<SetChange<ElementT>?>()

    override fun handleChange(
        propagationContext: Transactions.PropagationContext,
        change: SetChange<ElementT>?,
    ) {
        receivedChanges.add(change)
    }

    fun resetReceivedChanges() {
        receivedChanges.clear()
    }

    fun getAndResetReceivedChanges(): List<SetChange<ElementT>?> = receivedChanges.toList().also {
        resetReceivedChanges()
    }
}

@Deprecated("Switch to the new test utils")
context(transactionTestContext: TransactionTestContext) fun <ElementT> ReactiveSet<ElementT>.observeForTestingCancellable(): Pair<TestReactiveSetObserver<ElementT>, TestReactiveSetObserver.Handle> {
    val observer = TestReactiveSetObserver(
        observedReactiveSetVertex = vertex,
    )

    val observerHandle = vertex.registerSetObserver(
        propagationContext = transactionTestContext.propagationContext,
        observer = observer,
    )

    return Pair(
        observer,
        object : TestReactiveSetObserver.Handle {
            override fun cancel() {
                vertex.unregisterSetObserver(
                    handle = observerHandle,
                )
            }
        },
    )
}

@Deprecated("Switch to the new test utils")
context(transactionTestContext: TransactionTestContext) fun <ElementT> ReactiveSet<ElementT>.observeForTesting(): TestReactiveSetObserver<ElementT> {
    val (testReactiveSetObserver, _) = this.observeForTestingCancellable()
    return testReactiveSetObserver
}

fun <ElementT> TestReactiveSetObserver<ElementT>.getOldContentCopy(
    propagationContext: Transactions.PropagationContext,
): Set<ElementT> = observedReactiveSetVertex.getOldContentView(
    propagationContext = propagationContext,
).toSet()
