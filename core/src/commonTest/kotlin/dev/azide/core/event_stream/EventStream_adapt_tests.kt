package dev.azide.core.event_stream

import dev.azide.core.external.ExternalEventHandler
import dev.azide.core.EventStream
import dev.azide.core.external.ExternalStream
import dev.azide.core.hold
import dev.azide.core.test_utils.TransactionTestUtils
import dev.azide.core.test_utils.cell.CellTestUtils
import dev.azide.core.test_utils.event_stream.EventStreamTestUtils
import dev.azide.core.test_utils.subscribeForTesting
import dev.azide.core.test_utils.verifyDoesNotExposeEmission
import dev.azide.core.test_utils.verifyPropagatedAndExposesEmission
import dev.azide.core.test_utils.verifyPropagatedEmission
import kotlin.test.Test

@Suppress("ClassName")
class EventStream_adapt_tests {
    private class CustomEventSource<E> {
        private val listeners = mutableSetOf<CustomListener<E>>()

        interface CustomListener<E> {
            fun handle(event: E)
        }

        fun addListener(
            listener: CustomListener<E>,
        ) {
            val wasAdded = listeners.add(listener)

            if (!wasAdded) {
                throw IllegalStateException("Listener already added")
            }
        }

        fun removeListener(
            listener: CustomListener<E>,
        ) {
            val wasRemoved = listeners.remove(listener)

            if (!wasRemoved) {
                throw IllegalStateException("Listener not found")
            }
        }

        fun notify(
            event: E,
        ) {
            listeners.forEach { listener ->
                listener.handle(event)
            }
        }
    }

    private class CustomEventStream(
        private val customEventSource: CustomEventSource<Int>,
    ) : ExternalStream<Int> {
        override fun bind(
            handler: ExternalEventHandler<Int>,
        ): ExternalStream.SubscriptionDelegate {
            val customListener = object : CustomEventSource.CustomListener<Int> {
                override fun handle(event: Int) {
                    handler.handle(event)
                }
            }

            return object : ExternalStream.SubscriptionDelegate {
                override fun register() {
                    customEventSource.addListener(customListener)
                }

                override fun unregister() {
                    customEventSource.removeListener(customListener)
                }
            }
        }
    }

    @Test
    fun test() {
        val customEventSource = CustomEventSource<Int>()

        val subjectEventStream = EventStream.adapt(
            externalStream = CustomEventStream(
                customEventSource = customEventSource,
            ),
        )

        val eventStreamListener = TransactionTestUtils.executeInsideTransaction {
            subjectEventStream.subscribeForTesting()
        }

        customEventSource.notify(10)

        // Verify the effectiveness of the event delivery indirectly
        eventStreamListener.verifyPropagatedEmission(expectedEmittedEvent = 10)
        eventStreamListener.verifyDoesNotExposeEmission()
    }
}
