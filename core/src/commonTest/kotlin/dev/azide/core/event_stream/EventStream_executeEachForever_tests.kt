package dev.azide.core.event_stream

import dev.azide.core.Action
import dev.azide.core.executeEachForever
import dev.azide.core.map
import dev.azide.core.test_utils.MockSideEffect
import dev.azide.core.test_utils.TestInputStimulation
import dev.azide.core.test_utils.TestUtils
import dev.azide.core.test_utils.event_stream.EventStreamTestUtils
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("ClassName")
class EventStream_executeEachForever_tests {
    @Test
    fun test_executeEachForever_sourceEmission() {
        val mockSideEffect = MockSideEffect()

        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Action<Int>>()

        val subjectEventStream = TestUtils.executeSeparately(
            sourceEventStream.executeEachForever(),
        )

        EventStreamTestUtils.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = sourceEventStream.emit(
                emittedEvent = Action.wrap(mockSideEffect).map { 10 },
            ),
            expectedEmittedEvent = 10,
        )

        assertTrue(
            actual = mockSideEffect.wasCalled,
        )
    }

    @Test
    fun test_executeEachForever_sourceEmission_revoked() {
        val mockSideEffect = MockSideEffect()

        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Action<Int>>()

        val subjectEventStream = TestUtils.executeSeparately(
            sourceEventStream.executeEachForever(),
        )

        EventStreamTestUtils.verifyDoesNotEmitEffectively(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestInputStimulation.combine(
                sourceEventStream.emit(
                    emittedEvent = Action.wrap(mockSideEffect).map { 10 },
                ),
                sourceEventStream.revokeEmission(),
            ),
        )

        assertFalse(
            actual = mockSideEffect.wasCalled,
        )
    }

    @Test
    fun test_executeEachForever_sourceEmission_corrected() {
        val mockSideEffect1 = MockSideEffect()
        val mockSideEffect2 = MockSideEffect()

        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Action<Int>>()

        val subjectEventStream = TestUtils.executeSeparately(
            sourceEventStream.executeEachForever(),
        )

        EventStreamTestUtils.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestInputStimulation.combine(
                sourceEventStream.emit(
                    emittedEvent = Action.wrap(mockSideEffect1).map { 10 },
                ),
                sourceEventStream.correctEmission(
                    correctedEmittedEvent = Action.wrap(mockSideEffect2).map { 20 },
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
    fun test_executeEachForever_sourceEmitsOnStart() {
        val mockSideEffect = MockSideEffect()

        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Action<Int>>()

        TestUtils.executeSeparately(
            action = sourceEventStream.executeEachForever(),
            inputStimulation = sourceEventStream.emit(
                emittedEvent = Action.wrap(mockSideEffect).map { 10 },
            ),
        )

        assertTrue(
            actual = mockSideEffect.wasCalled,
        )
    }
}
