package dev.azide.core.event_stream

import dev.azide.core.CausalLoopException
import dev.azide.core.EventStream
import dev.azide.core.internal.event_stream.EventStreamVertex
import dev.azide.core.internal.utils.LoopClosure
import dev.azide.core.map
import dev.azide.core.test_utils.event_stream.EventStreamTestUtils
import dev.azide.core.test_utils.event_stream.assertIsStackOverflowError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertIs

@Suppress("ClassName")
class EventStream_loop_tests {
    @Test
    fun test_selfLoop() {
        // An attempt to create an event stream that directly loops to itself (without actually constructing any event
        // stream).
        val loopedEventStream = EventStream.looped<EventStream<Int>, Int> { loopedEventStream ->
            LoopClosure(
                result = loopedEventStream,
                loopedValue = loopedEventStream,
            )
        }

        assertIsStackOverflowError(
            assertFails {
                loopedEventStream.vertex
            },
        )
    }

    @Test
    fun test_selfCycle() {
        // Event stream cyclic with itself. Useless, but harmless.
        val loopedEventStream = EventStream.looped<EventStream<Int>, Int> { loopedEventStream ->
            val eventStream = loopedEventStream.map { it * 2 }

            LoopClosure(
                result = eventStream,
                loopedValue = eventStream,
            )
        }

        EventStreamTestUtils.registerNoopSubscriber(
            subjectEventStream = loopedEventStream,
        )
    }

    @Test
    fun test_smallCycle() {
        // Event stream forming a cycle with another event stream. Useless, but harmless.
        val loopedEventStream = EventStream.looped<EventStream<Int>, Int> { loopedEventStream ->
            val eventStream1 = loopedEventStream.map { it * 2 }
            val eventStream2 = eventStream1.map { it * 3 }

            LoopClosure(
                result = eventStream2,
                loopedValue = eventStream2,
            )
        }

        EventStreamTestUtils.registerNoopSubscriber(
            subjectEventStream = loopedEventStream,
        )
    }

    @Test
    fun test_selfCycle_harmful() {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        // Event stream forming a self-cycle, but depending also on another event stream.
        val loopedEventStream = EventStream.looped { loopedEventStream ->
            val mergedEventStream = EventStream.merge2(
                loopedEventStream,
                sourceEventStream,
            )

            LoopClosure(
                result = mergedEventStream,
                loopedValue = mergedEventStream,
            )
        }

        val sourceVertex = sourceEventStream.vertex
        val subjectVertex: EventStreamVertex<Int> = loopedEventStream.vertex

        val subscribingVerifier = EventStreamTestUtils.subscribeForVerification(loopedEventStream)

        assertEquals(
            expected = 1,
            actual = sourceVertex.subscriberCount,
        )

        assertEquals(
            expected = 2,
            actual = subjectVertex.subscriberCount,
        )

        assertIs<CausalLoopException>(
            assertFails {
                subscribingVerifier.verifyEmitsAsExpected(
                    inputStimulation = sourceEventStream.emit(10),
                    expectedEmittedEvent = 10,
                )
            },
        )

        subscribingVerifier.stop()

        // The source is still active (!)
        assertEquals(
            expected = 1,
            actual = sourceVertex.subscriberCount,
        )

        assertEquals(
            expected = 1,
            actual = subjectVertex.subscriberCount,
        )
    }

    @Test
    fun test_smallCycle_harmful() {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        // Event stream forming a small cycle, but depending also on another event stream.
        val loopedEventStream = EventStream.looped { loopedEventStream ->
            val mergedEventStream = EventStream.merge2(
                loopedEventStream,
                sourceEventStream,
            )

            val mappedEventStream = mergedEventStream.map { it * 2 }

            LoopClosure(
                result = mappedEventStream,
                loopedValue = mappedEventStream,
            )
        }

        val sourceVertex = sourceEventStream.vertex
        val subjectVertex: EventStreamVertex<Int> = loopedEventStream.vertex

        val subscribingVerifier = EventStreamTestUtils.subscribeForVerification(loopedEventStream)

        assertEquals(
            expected = 1,
            actual = sourceVertex.subscriberCount,
        )

        assertEquals(
            expected = 2,
            actual = subjectVertex.subscriberCount,
        )

        assertIs<CausalLoopException>(
            assertFails {
                subscribingVerifier.verifyEmitsAsExpected(
                    inputStimulation = sourceEventStream.emit(10),
                    expectedEmittedEvent = 10,
                )
            },
        )

        subscribingVerifier.stop()

        // The source is still active (!). This indicates a memory leak, as the garbage collector sees all objects in
        // this cycle as reachable, while they don't have any use and there's no mechanism that will ever clean this
        // up (as long as the source event stream is reachable itself). A potential solution would be to use weak
        // references to the target vertex in all subscribers / observers.
        assertEquals(
            expected = 1,
            actual = sourceVertex.subscriberCount,
        )

        assertEquals(
            expected = 1,
            actual = subjectVertex.subscriberCount,
        )
    }
}
