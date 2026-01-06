package dev.azide.core.test_utils

import dev.azide.core.Cell
import dev.azide.core.internal.Transactions
import dev.azide.core.internal.cell.CellVertex
import dev.azide.core.internal.cell.WarmCellVertex
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TestCellObserver<ValueT>(
    val observedCellVertex: CellVertex<ValueT>,
) : WarmCellVertex.BasicObserver<ValueT> {
    interface Handle {
        fun cancel()
    }

    private val receivedUpdates = mutableListOf<CellVertex.Update<ValueT>?>()

    override fun handleUpdate(
        propagationContext: Transactions.PropagationContext,
        update: CellVertex.Update<ValueT>?,
    ) {
        receivedUpdates.add(update)
    }

    fun getAndResetReceivedUpdates(): List<CellVertex.Update<ValueT>?> = receivedUpdates.toList().also {
        receivedUpdates.clear()
    }
}

context(transactionTestContext: TransactionTestContext) fun <ValueT> Cell<ValueT>.observeForTestingCancellable(): Pair<TestCellObserver<ValueT>, TestCellObserver.Handle> {
    val vertex = vertex

    val observer = TestCellObserver(
        observedCellVertex = vertex,
    )

    val observerHandle = vertex.registerObserver(
        propagationContext = transactionTestContext.propagationContext,
        observer = observer,
    )

    return Pair(
        observer,
        object : TestCellObserver.Handle {
            override fun cancel() {
                vertex.unregisterObserver(
                    handle = observerHandle,
                )
            }
        },
    )
}

context(transactionTestContext: TransactionTestContext) fun <ValueT> Cell<ValueT>.observeForTesting(): TestCellObserver<ValueT> {
    val (testCellObserver, _) = this.observeForTestingCancellable()
    return testCellObserver
}

fun <ValueT> TestCellObserver<ValueT>.verifyPropagatedAndExposesUpdate(
    expectedUpdatedValue: ValueT,
) {
    val expectedUpdate = CellVertex.Update(
        updatedValue = expectedUpdatedValue,
    )

    val receivedUpdates = getAndResetReceivedUpdates()

    assertEquals(
        expected = 1,
        actual = receivedUpdates.size,
        message = "Expected exactly one update to have been propagated.",
    )

    val receivedUpdate = receivedUpdates.single()

    assertEquals(
        expected = expectedUpdate,
        actual = receivedUpdate,
        message = "The propagated update did not match the expected update.",
    )

    val exposedUpdate = observedCellVertex.ongoingUpdate

    assertEquals(
        expected = expectedUpdate,
        actual = exposedUpdate,
        message = "The exposed ongoing update did not match the expected update.",
    )
}

fun <ValueT> TestCellObserver<ValueT>.verifyDoesNotExposeUpdate() {
    val exposedUpdate = observedCellVertex.ongoingUpdate

    assertNull(
        actual = exposedUpdate,
        message = "Expected no ongoing update to be exposed.",
    )
}

fun <ValueT> TestCellObserver<ValueT>.verifyDidNotPropagateNorExposesUpdate() {
    val receivedUpdates = getAndResetReceivedUpdates()

    assertEquals(
        expected = 0,
        actual = receivedUpdates.size,
        message = "Expected no updates to have been propagated.",
    )

    verifyDoesNotExposeUpdate()
}

context(transactionTestContext: TransactionTestContext) fun <ValueT> TestCellObserver<ValueT>.verifyOldValue(
    expectedOldValue: ValueT,
) {
    val oldValue = observedCellVertex.getOldValue(
        propagationContext = transactionTestContext.propagationContext,
    )
    assertEquals(
        expected = expectedOldValue,
        actual = oldValue,
        message = "The old value did not match the expected old value.",
    )
}

fun <ValueT> TestCellObserver<ValueT>.verifyOldValueInsideTransaction(
    expectedOldValue: ValueT,
) {
    TransactionTestUtils.executeInsideTransaction {
        verifyOldValue(expectedOldValue = expectedOldValue)
    }
}
