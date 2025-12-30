package dev.azide.core.event_stream

import dev.azide.core.Action
import dev.azide.core.ExternalSideEffect
import dev.azide.core.Trigger
import dev.azide.core.test_utils.TestInputStimulation
import dev.azide.core.test_utils.TestUtils
import dev.azide.core.test_utils.event_stream.EventStreamTestUtils
import dev.azide.core.triggerEachForever
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("ClassName")
class EventStream_triggerEachForever_tests {
    class MockSideEffect() : ExternalSideEffect {
        private var mutableWasCalled = false

        val wasCalled: Boolean
            get() = mutableWasCalled

        override fun executeExternally() {
            mutableWasCalled = true
        }
    }

    @Test
    fun test_triggerEachForever_sourceEmission() {
        val mockSideEffect = MockSideEffect()

        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Trigger>()

        TestUtils.executeSeparately(
            sourceEventStream.triggerEachForever(),
        )

        TestUtils.stimulateSeparately(
            inputStimulation = sourceEventStream.emit(
                emittedEvent = Action.wrap(mockSideEffect),
            ),
        )

        assertTrue(
            actual = mockSideEffect.wasCalled,
        )
    }

    @Test
    fun test_triggerEachForever_sourceEmission_revoked() {
        val mockSideEffect = MockSideEffect()

        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Trigger>()

        TestUtils.executeSeparately(
            sourceEventStream.triggerEachForever(),
        )

        TestUtils.stimulateSeparately(
            inputStimulation = TestInputStimulation.combine(
                sourceEventStream.emit(
                    emittedEvent = Action.wrap(mockSideEffect),
                ),
                sourceEventStream.revokeEmission(),
            ),
        )

        assertFalse(
            actual = mockSideEffect.wasCalled,
        )
    }

    @Test
    fun test_triggerEachForever_sourceEmission_corrected() {
        val mockSideEffect1 = MockSideEffect()
        val mockSideEffect2 = MockSideEffect()

        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Trigger>()

        TestUtils.executeSeparately(
            sourceEventStream.triggerEachForever(),
        )

        TestUtils.stimulateSeparately(
            inputStimulation = TestInputStimulation.combine(
                sourceEventStream.emit(
                    emittedEvent = Action.wrap(mockSideEffect1),
                ),
                sourceEventStream.correctEmission(
                    correctedEmittedEvent = Action.wrap(mockSideEffect2),
                ),
            ),
        )

        assertFalse(
            actual = mockSideEffect1.wasCalled,
        )

        assertTrue(
            actual = mockSideEffect2.wasCalled,
        )
    }

    @Test
    fun test_triggerEachForever_sourceEmitsOnStart() {
        val mockSideEffect = MockSideEffect()

        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Trigger>()

        TestUtils.executeSeparately(
            action = sourceEventStream.triggerEachForever(),
            inputStimulation = sourceEventStream.emit(
                emittedEvent = Action.wrap(mockSideEffect),
            ),
        )

        assertTrue(
            actual = mockSideEffect.wasCalled,
        )
    }
}
