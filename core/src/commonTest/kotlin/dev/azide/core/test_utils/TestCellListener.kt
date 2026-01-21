package dev.azide.core.test_utils

import dev.azide.core.Cell
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.cell.CellVertex
import dev.azide.core.impl.cell.CellVertex.BoundListener
import dev.azide.core.impl.cell.registerBoundListenerOnline
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TestCellListener<ValueT>(
    val observedCellVertex: CellVertex<ValueT>,
) : BoundListener {
    interface Handle {
        fun cancel()
    }

    private val receivedUpdates = mutableListOf<CellVertex.Update<ValueT>?>()

    override fun handleUpdate(
        propagationContext: Transactions.PropagationContext,
    ) {
        receivedUpdates.add(observedCellVertex.ongoingUpdate)
    }

    fun getAndResetReceivedUpdates(): List<CellVertex.Update<ValueT>?> = receivedUpdates.toList().also {
        resetReceivedUpdates()
    }

    fun resetReceivedUpdates() {
        receivedUpdates.clear()
    }
}

@Deprecated("Switch to the new test utils")
context(transactionTestContext: TransactionTestContext) fun <ValueT> Cell<ValueT>.observeForTestingCancellable(): Pair<TestCellListener<ValueT>, TestCellListener.Handle> {
    val vertex = vertex

    val listener = TestCellListener(
        observedCellVertex = vertex,
    )

    val listenerHandle = vertex.registerBoundListenerOnline(
        propagationContext = transactionTestContext.propagationContext,
        listener = listener,
    )

    return Pair(
        listener,
        object : TestCellListener.Handle {
            override fun cancel() {
                vertex.unregisterListener(
                    handle = listenerHandle,
                )
            }
        },
    )
}

@Deprecated("Switch to the new test utils")
context(transactionTestContext: TransactionTestContext) fun <ValueT> Cell<ValueT>.observeForTesting(): TestCellListener<ValueT> {
    val (testCellListener, _) = this.observeForTestingCancellable()
    return testCellListener
}

fun <ValueT> TestCellListener<ValueT>.verifyPropagatedAndExposesUpdate(
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

fun <ValueT> TestCellListener<ValueT>.verifyPropagatedAndExposesRevocation() {
    val receivedUpdates = getAndResetReceivedUpdates()

    assertEquals(
        expected = 1,
        actual = receivedUpdates.size,
        message = "Expected exactly one update to have been propagated.",
    )

    val receivedUpdate = receivedUpdates.single()

    assertNull(
        actual = receivedUpdate,
        message = "Expected the propagated update to be a revocation (null value), but it was not.",
    )

    val exposedUpdate = observedCellVertex.ongoingUpdate

    assertNull(
        actual = exposedUpdate,
        message = "Expected the exposed ongoing update to be a revocation (null value), but it was not.",
    )
}

fun <ValueT> TestCellListener<ValueT>.verifyDoesNotExposeUpdate() {
    val exposedUpdate = observedCellVertex.ongoingUpdate

    assertNull(
        actual = exposedUpdate,
        message = "Expected no ongoing update to be exposed.",
    )
}

fun <ValueT> TestCellListener<ValueT>.verifyDidNotPropagateNorExposesUpdate() {
    val receivedUpdates = getAndResetReceivedUpdates()

    assertEquals(
        expected = 0,
        actual = receivedUpdates.size,
        message = "Expected no updates to have been propagated.",
    )

    verifyDoesNotExposeUpdate()
}

context(transactionTestContext: TransactionTestContext) fun <ValueT> TestCellListener<ValueT>.verifyOldValue(
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

fun <ValueT> TestCellListener<ValueT>.verifyOldValueInsideTransaction(
    expectedOldValue: ValueT,
) {
    TransactionTestUtils.executeInsideTransaction {
        verifyOldValue(expectedOldValue = expectedOldValue)
    }
}
