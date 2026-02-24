package dev.azide.core.event_stream

import dev.azide.core.single
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.event_stream.EventStreamTestUtils_deprecated
import dev.azide.core.test_utils.event_stream.TestInputEventStream
import kotlin.test.Test

@Suppress("ClassName")
class EventStream_single_tests {
    @Test
    fun test_sourceEmits_afterSpawn() {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectEventStream = EventStreamTestUtils_deprecated.spawnStatefulEventStream {
            sourceEventStream.single()
        }

        // Verify that the subject emits the same event as the source for the single emission
        EventStreamTestUtils_deprecated.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = sourceEventStream.emit(
                emittedEvent = 11,
            ),
            expectedEmittedEvent = 11,
        )

        // Verify that the subject does not emit for subsequent source emissions
        EventStreamTestUtils_deprecated.verifyDoesNotEmitAtAll(
            subjectEventStream = subjectEventStream,
            inputStimulation = sourceEventStream.emit(
                emittedEvent = 12,
            ),
        )
    }

    @Test
    fun test_sourceEmits_atSpawn() {
        val sourceEventStream = TestInputEventStream<Int>()

        // Verify that the subject emits the same event as the source for the single emission (at spawn)
        val subjectEventStream = EventStreamTestUtils_deprecated.spawnStatefulEventStreamExpectingEmission(
            inputStimulation = sourceEventStream.emit(
                emittedEvent = 11,
            ),
            expectedEmittedEvent = 11,
        ) {
            sourceEventStream.single()
        }

        // Verify that the subject does not emit for subsequent source emissions
        EventStreamTestUtils_deprecated.verifyDoesNotEmitAtAll(
            subjectEventStream = subjectEventStream,
            inputStimulation = sourceEventStream.emit(
                emittedEvent = 12,
            ),
        )
    }

    @Test
    fun test_sourceEmits_revoked_afterSpawn() {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectEventStream = EventStreamTestUtils_deprecated.spawnStatefulEventStream {
            sourceEventStream.single()
        }

        // Verify that the subject does not emit when the source emission is revoked
        EventStreamTestUtils_deprecated.verifyDoesNotEmitEffectively(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestStimulation.combineInProvidedOrder(
                sourceEventStream.emit(
                    emittedEvent = 11,
                ),
                sourceEventStream.revokeEmission(),
            ),
        )

        // Verify that the subject emits the same event as the source for the single (non-revoked) emission
        EventStreamTestUtils_deprecated.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = sourceEventStream.emit(
                emittedEvent = 12,
            ),
            expectedEmittedEvent = 12,
        )

        // Verify that the subject does not emit for subsequent source emissions
        EventStreamTestUtils_deprecated.verifyDoesNotEmitAtAll(
            subjectEventStream = subjectEventStream,
            inputStimulation = sourceEventStream.emit(
                emittedEvent = 13,
            ),
        )
    }

    @Test
    fun test_sourceEmits_revoked_atSpawn() {
        val sourceEventStream = TestInputEventStream<Int>()

        // Verify that the subject emits the same event as the source for the single emission (at spawn)
        val subjectEventStream = EventStreamTestUtils_deprecated.spawnStatefulEventStream(
            inputStimulation = TestStimulation.combineInProvidedOrder(
                sourceEventStream.emit(
                    emittedEvent = 11,
                ),
                sourceEventStream.revokeEmission(),
            ),
        ) {
            sourceEventStream.single()
        }

        // Verify that the subject emits the same event as the source for the single (non-revoked) emission
        EventStreamTestUtils_deprecated.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = sourceEventStream.emit(
                emittedEvent = 12,
            ),
            expectedEmittedEvent = 12,
        )

        // Verify that the subject does not emit for subsequent source emissions
        EventStreamTestUtils_deprecated.verifyDoesNotEmitAtAll(
            subjectEventStream = subjectEventStream,
            inputStimulation = sourceEventStream.emit(
                emittedEvent = 13,
            ),
        )
    }

    @Test
    fun test_sourceEmits_corrected_afterSpawn() {
        val sourceEventStream = TestInputEventStream<Int>()

        val subjectEventStream = EventStreamTestUtils_deprecated.spawnStatefulEventStream {
            sourceEventStream.single()
        }

        // Verify that the subject emits the corrected event from the source for the single emission
        EventStreamTestUtils_deprecated.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestStimulation.combineInProvidedOrder(
                sourceEventStream.emit(
                    emittedEvent = 11,
                ),
                sourceEventStream.correctEmission(
                    correctedEmittedEvent = 12,
                ),
            ),
            expectedEmittedEvent = 12,
        )

        // Verify that the subject does not emit for subsequent source emissions
        EventStreamTestUtils_deprecated.verifyDoesNotEmitAtAll(
            subjectEventStream = subjectEventStream,
            inputStimulation = sourceEventStream.emit(
                emittedEvent = 13,
            ),
        )
    }

    @Test
    fun test_sourceEmits_corrected_atSpawn() {
        val sourceEventStream = TestInputEventStream<Int>()

        // Verify that the subject emits the corrected event from the source for the single emission (at spawn)
        val subjectEventStream = EventStreamTestUtils_deprecated.spawnStatefulEventStreamExpectingEmission(
            inputStimulation = TestStimulation.combineInProvidedOrder(
                sourceEventStream.emit(
                    emittedEvent = 11,
                ),
                sourceEventStream.correctEmission(
                    correctedEmittedEvent = 12,
                ),
            ),
            expectedEmittedEvent = 12,
        ) {
            sourceEventStream.single()
        }

        // Verify that the subject does not emit for subsequent source emissions
        EventStreamTestUtils_deprecated.verifyDoesNotEmitAtAll(
            subjectEventStream = subjectEventStream,
            inputStimulation = sourceEventStream.emit(
                emittedEvent = 13,
            ),
        )
    }
}
