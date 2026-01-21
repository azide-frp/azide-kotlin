package dev.azide.core.test_utils

import dev.azide.core.EventStream
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.event_stream.EventStreamVertex
import dev.azide.core.impl.event_stream.EventStreamVertex.EmissionSubscriber
import dev.azide.core.impl.event_stream.registerEmissionSubscriberOnline
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TestEventStreamSubscriber<EventT>(
    val subscribedEventStreamVertex: EventStreamVertex<EventT>,
) : EmissionSubscriber<EventT> {
    interface Handle {
        fun cancel()
    }

    private val receivedEmissions = mutableListOf<EventStreamVertex.Emission<EventT>?>()

    override fun handleEmission(
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
): Pair<TestEventStreamSubscriber<EventT>, TestEventStreamSubscriber.Handle> {
    val vertex = cell.vertex

    val subscriber = TestEventStreamSubscriber(
        subscribedEventStreamVertex = vertex,
    )

    val subscriberHandle = vertex.registerEmissionSubscriberOnline(
        propagationContext = transactionTestContext.propagationContext,
        subscriber = subscriber,
    )

    return Pair(
        subscriber,
        object : TestEventStreamSubscriber.Handle {
            override fun cancel() {
                vertex.unregisterSubscriber(
                    handle = subscriberHandle,
                )
            }
        },
    )
}

context(transactionTestContext: TransactionTestContext) fun <EventT> EventStream<EventT>.subscribeForTesting(): TestEventStreamSubscriber<EventT> {
    val (testEventStreamObserver, _) = subscribeForTestingCancellable(cell = this)
    return testEventStreamObserver
}

fun <EventT> TestEventStreamSubscriber<EventT>.verifyPropagatedAndExposesEmission(
    expectedEmittedEvent: EventT,
) {
    verifyPropagatedEmission(
        expectedEmittedEvent = expectedEmittedEvent,
    )

    verifyExposesEmission(
        expectedExposedEvent = expectedEmittedEvent,
    )
}

fun <EventT> TestEventStreamSubscriber<EventT>.verifyPropagatedEmission(
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

fun <EventT> TestEventStreamSubscriber<EventT>.verifyExposesEmission(
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

fun <EventT> TestEventStreamSubscriber<EventT>.verifyPropagatedAndExposesRevocation() {
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

fun <EventT> TestEventStreamSubscriber<EventT>.verifyDoesNotExposeEmission() {
    val exposedEmission = subscribedEventStreamVertex.ongoingEmission

    assertNull(
        actual = exposedEmission,
        message = "Expected no ongoing emission to be exposed.",
    )
}

fun <EventT> TestEventStreamSubscriber<EventT>.verifyDidNotPropagateNorExposesEmission() {
    val receivedEmissions = getAndResetReceivedEmissions()

    assertEquals(
        expected = 0,
        actual = receivedEmissions.size,
        message = "Expected no emissions to have been propagated.",
    )

    verifyDoesNotExposeEmission()
}
