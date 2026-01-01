package dev.azide.core.event_stream

import dev.azide.core.Action
import dev.azide.core.executeEach
import dev.azide.core.map
import dev.azide.core.test_utils.MockSideEffect
import dev.azide.core.test_utils.TestInputStimulation
import dev.azide.core.test_utils.TestUtils
import dev.azide.core.test_utils.event_stream.EventStreamTestUtils
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("ClassName")
class EventStream_executeEach_tests {
    @Test
    fun test_executeEach_sourceEmission() {
        val mockSideEffect = MockSideEffect()

        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Action<Int>>()

        val (subjectEventStream, _) = TestUtils.executeSeparately(
            sourceEventStream.executeEach().start,
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
    fun test_executeEach_multipleSourceEmissions() {
        val mockSideEffect1 = MockSideEffect()
        val mockSideEffect2 = MockSideEffect()

        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Action<Int>>()

        val (subjectEventStream, _) = TestUtils.executeSeparately(
            sourceEventStream.executeEach().start,
        )

        TestUtils.stimulateSeparately(
            sourceEventStream.emit(
                emittedEvent = Action.wrap(mockSideEffect1).map { 10 },
            ),
        )

        assertTrue(
            actual = mockSideEffect1.wasCalled,
        )

        EventStreamTestUtils.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = sourceEventStream.emit(
                emittedEvent = Action.wrap(mockSideEffect2).map { 20 },
            ),
            expectedEmittedEvent = 20,
        )

        assertTrue(
            actual = mockSideEffect2.wasCalled,
        )
    }

    @Test
    fun test_executeEach_sourceEmission_revoked() {
        val mockSideEffect = MockSideEffect()

        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Action<Int>>()

        val (subjectEventStream, _) = TestUtils.executeSeparately(
            sourceEventStream.executeEach().start,
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
    fun test_executeEach_sourceEmission_corrected() {
        val mockSideEffect1 = MockSideEffect()
        val mockSideEffect2 = MockSideEffect()

        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Action<Int>>()

        val (subjectEventStream, _) = TestUtils.executeSeparately(
            sourceEventStream.executeEach().start,
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
    fun test_executeEach_sourceEmitsOnStart() {
        val mockSideEffect = MockSideEffect()

        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Action<Int>>()

        TestUtils.executeSeparately(
            action = sourceEventStream.executeEach().start,
            inputStimulation = sourceEventStream.emit(
                emittedEvent = Action.wrap(mockSideEffect).map { 10 },
            ),
        )

        assertTrue(
            actual = mockSideEffect.wasCalled,
        )
    }

    @Test
    fun test_executeEach_cancel() {
        val mockSideEffect = MockSideEffect()

        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Action<Int>>()

        val (subjectEventStream, handle) = TestUtils.executeSeparately(
            sourceEventStream.executeEach().start,
        )

        TestUtils.executeSeparately(
            handle.cancel,
        )

        EventStreamTestUtils.verifyDoesNotEmitAtAll(
            subjectEventStream = subjectEventStream,
            inputStimulation = sourceEventStream.emit(
                emittedEvent = Action.wrap(mockSideEffect).map { 10 },
            ),
        )

        assertFalse(
            actual = mockSideEffect.wasCalled,
        )
    }
}
