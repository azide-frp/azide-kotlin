package dev.azide.core.event_stream

import dev.azide.core.Action
import dev.azide.core.executeEachForever
import dev.azide.core.map
import dev.azide.core.test_utils.MockExternalTrigger
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.TestUtils
import dev.azide.core.test_utils.event_stream.EventStreamTestUtils
import dev.azide.core.test_utils.event_stream.TestInputEventStream
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("ClassName")
class EventStream_executeEachForever_tests {
    @Test
    fun test_sourceEmits() {
        val mockSideEffect = MockExternalTrigger()

        val sourceEventStream = TestInputEventStream<Action<Int>>()

        val subjectEventStream = TestUtils.executeSeparately(
            sourceEventStream.executeEachForever(),
        )

        EventStreamTestUtils.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = sourceEventStream.emit(
                emittedEvent = Action.adapt(mockSideEffect).map { 10 },
            ),
            expectedEmittedEvent = 10,
        )

        assertTrue(
            actual = mockSideEffect.wasCalled,
        )
    }

    @Test
    fun test_sourceEmits_revoked() {
        val mockSideEffect = MockExternalTrigger()

        val sourceEventStream = TestInputEventStream<Action<Int>>()

        val subjectEventStream = TestUtils.executeSeparately(
            sourceEventStream.executeEachForever(),
        )

        EventStreamTestUtils.verifyDoesNotEmitEffectively(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestStimulation.combineInProvidedOrder(
                sourceEventStream.emit(
                    emittedEvent = Action.adapt(mockSideEffect).map { 10 },
                ),
                sourceEventStream.revokeEmission(),
            ),
        )

        assertFalse(
            actual = mockSideEffect.wasCalled,
        )
    }

    @Test
    fun test_sourceEmits_corrected() {
        val mockSideEffect1 = MockExternalTrigger()
        val mockSideEffect2 = MockExternalTrigger()

        val sourceEventStream = TestInputEventStream<Action<Int>>()

        val subjectEventStream = TestUtils.executeSeparately(
            sourceEventStream.executeEachForever(),
        )

        EventStreamTestUtils.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestStimulation.combineInProvidedOrder(
                sourceEventStream.emit(
                    emittedEvent = Action.adapt(mockSideEffect1).map { 10 },
                ),
                sourceEventStream.correctEmission(
                    correctedEmittedEvent = Action.adapt(mockSideEffect2).map { 20 },
                ),
            ),
            expectedEmittedEvent = 20,
        )

        assertFalse(
            actual = mockSideEffect1.wasCalled,
        )

        assertTrue(
            actual = mockSideEffect2.wasCalled,
        )
    }

    @Test
    fun test_sourceEmitsOnStart() {
        val mockSideEffect = MockExternalTrigger()

        val sourceEventStream = TestInputEventStream<Action<Int>>()

        TestUtils.executeSeparately(
            action = sourceEventStream.executeEachForever(),
            inputStimulation = sourceEventStream.emit(
                emittedEvent = Action.adapt(mockSideEffect).map { 10 },
            ),
        )

        assertTrue(
            actual = mockSideEffect.wasCalled,
        )
    }
}
