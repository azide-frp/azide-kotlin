package dev.azide.core.test_utils

import dev.azide.core.EventStream
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.event_stream.EventStreamVertex
import dev.azide.core.impl.Vertex.BoundListener
import dev.azide.core.impl.event_stream.registerBoundListenerOnline
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TestEventStreamListener<EventT>(
    val subscribedEventStreamVertex: EventStreamVertex<EventT>,
) : BoundListener {
    interface Handle {
        fun cancel()
    }

    private val receivedEmissions = mutableListOf<EventStreamVertex.Emission<EventT>?>()

    override fun handle(
        propagationContext: Transactions.PropagationContext,
    ) {
        receivedEmissions.add(subscribedEventStreamVertex.ongoingEmission)
    }

    fun getAndResetReceivedEmissions(): List<EventStreamVertex.Emission<EventT>?> = receivedEmissions.toList().also {
        receivedEmissions.clear()
    }
}

context(transactionTestContext: TransactionTestContext) fun <EventT> subscribeForTestingCancellable(
    cell: EventStream<EventT>,
): Pair<TestEventStreamListener<EventT>, TestEventStreamListener.Handle> {
    val vertex = cell.vertex

    val listener = TestEventStreamListener(
        subscribedEventStreamVertex = vertex,
    )

    val listenerHandle = vertex.registerBoundListenerOnline(
        propagationContext = transactionTestContext.propagationContext,
        listener = listener,
    )

    return Pair(
        listener,
        object : TestEventStreamListener.Handle {
            override fun cancel() {
                vertex.unregisterListener(
                    handle = listenerHandle,
                )
            }
        },
    )
}

context(transactionTestContext: TransactionTestContext) fun <EventT> EventStream<EventT>.subscribeForTesting(): TestEventStreamListener<EventT> {
    val (testEventStreamListener, _) = subscribeForTestingCancellable(cell = this)
    return testEventStreamListener
}

fun <EventT> TestEventStreamListener<EventT>.verifyPropagatedAndExposesEmission(
    expectedEmittedEvent: EventT,
) {
    verifyPropagatedEmission(
        expectedEmittedEvent = expectedEmittedEvent,
    )

    verifyExposesEmission(
        expectedExposedEvent = expectedEmittedEvent,
    )
}

fun <EventT> TestEventStreamListener<EventT>.verifyPropagatedEmission(
    expectedEmittedEvent: EventT,
) {
    val expectedEmission = EventStreamVertex.Emission(
        emittedEvent = expectedEmittedEvent,
    )

    val receivedEmissions = getAndResetReceivedEmissions()

    assertEquals(
        expected = 1,
        actual = receivedEmissions.size,
        message = "Expected exactly one emission to have been propagated.",
    )

    val receivedEmission = receivedEmissions.single()

    assertEquals(
        expected = expectedEmission,
        actual = receivedEmission,
        message = "The propagated emission did not match the expected emission.",
    )
}

fun <EventT> TestEventStreamListener<EventT>.verifyExposesEmission(
    expectedExposedEvent: EventT,
) {
    val exposedEmission = subscribedEventStreamVertex.ongoingEmission

    assertEquals(
        expected = EventStreamVertex.Emission(
            emittedEvent = expectedExposedEvent,
        ),
        actual = exposedEmission,
        message = "The exposed ongoing emission did not match the expected emission.",
    )
}

fun <EventT> TestEventStreamListener<EventT>.verifyPropagatedAndExposesRevocation() {
    val receivedEmissions = getAndResetReceivedEmissions()

    assertEquals(
        expected = 1,
        actual = receivedEmissions.size,
        message = "Expected exactly one emission to have been propagated.",
    )

    val receivedEmission = receivedEmissions.single()

    assertNull(
        actual = receivedEmission,
        message = "Expected the propagated emission to be a revocation (null value), but it was not.",
    )

    val exposedEmission = subscribedEventStreamVertex.ongoingEmission

    assertNull(
        actual = exposedEmission,
        message = "Expected the exposed ongoing emission to be a revocation (null value), but it was not.",
    )
}

fun <EventT> TestEventStreamListener<EventT>.verifyDoesNotExposeEmission() {
    val exposedEmission = subscribedEventStreamVertex.ongoingEmission

    assertNull(
        actual = exposedEmission,
        message = "Expected no ongoing emission to be exposed.",
    )
}

fun <EventT> TestEventStreamListener<EventT>.verifyDidNotPropagateNorExposesEmission() {
    val receivedEmissions = getAndResetReceivedEmissions()

    assertEquals(
        expected = 0,
        actual = receivedEmissions.size,
        message = "Expected no emissions to have been propagated.",
    )

    verifyDoesNotExposeEmission()
}
