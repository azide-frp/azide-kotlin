package dev.azide.core.event_stream

import dev.azide.core.EventStream
import dev.azide.core.single
import dev.azide.core.test_utils.TestInputStimulation
import dev.azide.core.test_utils.event_stream.EventStreamTestUtils
import kotlin.test.Test

@Suppress("ClassName")
class EventStream_single_tests {
    @Test
    fun test_sourceNever() {
        val subjectEventStream = _root_ide_package_.dev.azide.core.test_utils.event_stream.EventStreamTestUtils.spawnStatefulEventStream {
            EventStream.Never.single()
        }

        _root_ide_package_.dev.azide.core.test_utils.event_stream.EventStreamTestUtils.verifyTerminated(
            subjectEventStream = subjectEventStream,
        )
    }

    @Test
    fun test_emission_afterSpawn() {
        val sourceEventStream = _root_ide_package_.dev.azide.core.test_utils.event_stream.EventStreamTestUtils.createInputEventStream<Int>()

        val subjectEventStream = _root_ide_package_.dev.azide.core.test_utils.event_stream.EventStreamTestUtils.spawnStatefulEventStream {
            sourceEventStream.single()
        }

        // Verify that the subject emits the same event as the source for the single emission
        _root_ide_package_.dev.azide.core.test_utils.event_stream.EventStreamTestUtils.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = sourceEventStream.emit(
                emittedEvent = 11,
            ),
            expectedEmittedEvent = 11,
        )

        // Verify that the subject does not emit for subsequent source emissions
        _root_ide_package_.dev.azide.core.test_utils.event_stream.EventStreamTestUtils.verifyDoesNotEmitAtAll(
            subjectEventStream = subjectEventStream,
            inputStimulation = sourceEventStream.emit(
                emittedEvent = 12,
            ),
        )
    }

    @Test
    fun test_emission_atSpawn() {
        val sourceEventStream = _root_ide_package_.dev.azide.core.test_utils.event_stream.EventStreamTestUtils.createInputEventStream<Int>()

        // Verify that the subject emits the same event as the source for the single emission (at spawn)
        val subjectEventStream = _root_ide_package_.dev.azide.core.test_utils.event_stream.EventStreamTestUtils.spawnStatefulEventStreamExpectingEmission(
            inputStimulation = sourceEventStream.emit(
                emittedEvent = 11,
            ),
            expectedEmittedEvent = 11,
        ) {
            sourceEventStream.single()
        }

        // Verify that the subject does not emit for subsequent source emissions
        _root_ide_package_.dev.azide.core.test_utils.event_stream.EventStreamTestUtils.verifyDoesNotEmitAtAll(
            subjectEventStream = subjectEventStream,
            inputStimulation = sourceEventStream.emit(
                emittedEvent = 12,
            ),
        )
    }

    @Test
    fun test_emission_revoked_afterSpawn() {
        val sourceEventStream = _root_ide_package_.dev.azide.core.test_utils.event_stream.EventStreamTestUtils.createInputEventStream<Int>()

        val subjectEventStream = _root_ide_package_.dev.azide.core.test_utils.event_stream.EventStreamTestUtils.spawnStatefulEventStream {
            sourceEventStream.single()
        }

        // Verify that the subject does not emit when the source emission is revoked
        _root_ide_package_.dev.azide.core.test_utils.event_stream.EventStreamTestUtils.verifyDoesNotEmitEffectively(
            subjectEventStream = subjectEventStream,
            inputStimulation = _root_ide_package_.dev.azide.core.test_utils.TestInputStimulation.Companion.combine(
                sourceEventStream.emit(
                    emittedEvent = 11,
                ),
                sourceEventStream.revokeEmission(),
            ),
        )

        // Verify that the subject emits the same event as the source for the single (non-revoked) emission
        _root_ide_package_.dev.azide.core.test_utils.event_stream.EventStreamTestUtils.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = sourceEventStream.emit(
                emittedEvent = 12,
            ),
            expectedEmittedEvent = 12,
        )

        // Verify that the subject does not emit for subsequent source emissions
        _root_ide_package_.dev.azide.core.test_utils.event_stream.EventStreamTestUtils.verifyDoesNotEmitAtAll(
            subjectEventStream = subjectEventStream,
            inputStimulation = sourceEventStream.emit(
                emittedEvent = 13,
            ),
        )
    }

    @Test
    fun test_emission_revoked_atSpawn() {
        val sourceEventStream = _root_ide_package_.dev.azide.core.test_utils.event_stream.EventStreamTestUtils.createInputEventStream<Int>()

        // Verify that the subject emits the same event as the source for the single emission (at spawn)
        val subjectEventStream = _root_ide_package_.dev.azide.core.test_utils.event_stream.EventStreamTestUtils.spawnStatefulEventStream(
            inputStimulation = _root_ide_package_.dev.azide.core.test_utils.TestInputStimulation.Companion.combine(
                sourceEventStream.emit(
                    emittedEvent = 11,
                ),
                sourceEventStream.revokeEmission(),
            ),
        ) {
            sourceEventStream.single()
        }

        // Verify that the subject emits the same event as the source for the single (non-revoked) emission
        _root_ide_package_.dev.azide.core.test_utils.event_stream.EventStreamTestUtils.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = sourceEventStream.emit(
                emittedEvent = 12,
            ),
            expectedEmittedEvent = 12,
        )

        // Verify that the subject does not emit for subsequent source emissions
        _root_ide_package_.dev.azide.core.test_utils.event_stream.EventStreamTestUtils.verifyDoesNotEmitAtAll(
            subjectEventStream = subjectEventStream,
            inputStimulation = sourceEventStream.emit(
                emittedEvent = 13,
            ),
        )
    }

    @Test
    fun test_emission_corrected_afterSpawn() {
        val sourceEventStream = _root_ide_package_.dev.azide.core.test_utils.event_stream.EventStreamTestUtils.createInputEventStream<Int>()

        val subjectEventStream = _root_ide_package_.dev.azide.core.test_utils.event_stream.EventStreamTestUtils.spawnStatefulEventStream {
            sourceEventStream.single()
        }

        // Verify that the subject emits the corrected event from the source for the single emission
        _root_ide_package_.dev.azide.core.test_utils.event_stream.EventStreamTestUtils.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = _root_ide_package_.dev.azide.core.test_utils.TestInputStimulation.Companion.combine(
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
        _root_ide_package_.dev.azide.core.test_utils.event_stream.EventStreamTestUtils.verifyDoesNotEmitAtAll(
            subjectEventStream = subjectEventStream,
            inputStimulation = sourceEventStream.emit(
                emittedEvent = 13,
            ),
        )
    }

    @Test
    fun test_emission_corrected_atSpawn() {
        val sourceEventStream = _root_ide_package_.dev.azide.core.test_utils.event_stream.EventStreamTestUtils.createInputEventStream<Int>()

        // Verify that the subject emits the corrected event from the source for the single emission (at spawn)
        val subjectEventStream = _root_ide_package_.dev.azide.core.test_utils.event_stream.EventStreamTestUtils.spawnStatefulEventStreamExpectingEmission(
            inputStimulation = _root_ide_package_.dev.azide.core.test_utils.TestInputStimulation.Companion.combine(
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
        _root_ide_package_.dev.azide.core.test_utils.event_stream.EventStreamTestUtils.verifyDoesNotEmitAtAll(
            subjectEventStream = subjectEventStream,
            inputStimulation = sourceEventStream.emit(
                emittedEvent = 13,
            ),
        )
    }
}
