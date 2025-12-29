package dev.azide.core.event_stream

import dev.azide.core.EventStream
import dev.azide.core.ExternalSourceAdapter
import dev.azide.core.hold
import dev.azide.core.test_utils.cell.CellTestUtils
import kotlin.test.Test

@Suppress("ClassName")
class EventStream_wrap_tests {
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

    private class CustomEventSourceAdapter(
        private val customEventSource: CustomEventSource<Int>,
    ) : ExternalSourceAdapter<Int> {
        override fun bind(
            eventDistributor: ExternalSourceAdapter.EventDistributor<Int>,
        ): ExternalSourceAdapter.SubscriptionHandle {
            val customListener = object : CustomEventSource.CustomListener<Int> {
                override fun handle(event: Int) {
                    eventDistributor.distribute(event)
                }
            }

            return object : ExternalSourceAdapter.SubscriptionHandle {
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
    fun test_wrap_viaCell() {
        val customEventSource = CustomEventSource<Int>()

        val subjectEventStream = EventStream.wrap(
            externalSourceAdapter = CustomEventSourceAdapter(
                customEventSource = customEventSource,
            ),
        )

        val helperCell = CellTestUtils.spawnStatefulCell {
            subjectEventStream.hold(0)
        }

        customEventSource.notify(10)

        // Verify the effectiveness of the event delivery indirectly
        CellTestUtils.verifyAtRest(
            subjectCell = helperCell,
            expectedValue = 10,
        )
    }
}
