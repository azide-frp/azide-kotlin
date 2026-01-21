package dev.azide.core.test_utils

import dev.azide.core.collections.ReactiveSet
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.collections.reactive_collection.TrackedGenericCollectionVertex.Listener
import dev.azide.core.impl.collections.reactive_set.SetChange
import dev.azide.core.impl.collections.reactive_set.TrackedSetVertex
import dev.azide.core.impl.collections.reactive_set.registerSetChangeListener

class TestReactiveSetListener<ElementT>(
    val observedReactiveSetVertex: TrackedSetVertex<ElementT>,
) : Listener {
    interface Handle {
        fun cancel()
    }

    private val receivedChanges = mutableListOf<SetChange<ElementT>?>()

    override fun handle(
        propagationContext: Transactions.PropagationContext,
    ) {
        receivedChanges.add(observedReactiveSetVertex.ongoingChange)
    }

    fun resetReceivedChanges() {
        receivedChanges.clear()
    }

    fun getAndResetReceivedChanges(): List<SetChange<ElementT>?> = receivedChanges.toList().also {
        resetReceivedChanges()
    }
}

@Deprecated("Switch to the new test utils")
context(transactionTestContext: TransactionTestContext) fun <ElementT> ReactiveSet<ElementT>.observeForTestingCancellable(): Pair<TestReactiveSetListener<ElementT>, TestReactiveSetListener.Handle> {
    val listener = TestReactiveSetListener(
        observedReactiveSetVertex = trackedVertex,
    )

    val listenerHandle = trackedVertex.registerSetChangeListener(
        propagationContext = transactionTestContext.propagationContext,
        listener = listener,
    )

    return Pair(
        listener,
        object : TestReactiveSetListener.Handle {
            override fun cancel() {
                trackedVertex.unregisterListener(
                    handle = listenerHandle,
                )
            }
        },
    )
}

@Deprecated("Switch to the new test utils")
context(transactionTestContext: TransactionTestContext) fun <ElementT> ReactiveSet<ElementT>.observeForTesting(): TestReactiveSetListener<ElementT> {
    val (testReactiveSetListener, _) = this.observeForTestingCancellable()
    return testReactiveSetListener
}

fun <ElementT> TrackedSetVertex<ElementT>.getOldContentCopy(
    propagationContext: Transactions.PropagationContext,
): Set<ElementT> = getOldContentView(
    propagationContext = propagationContext,
).toSet()
