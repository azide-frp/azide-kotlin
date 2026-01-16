package dev.azide.core.event_stream

import dev.azide.core.EventStream
import dev.azide.core.test_utils.TestInputStimulation
import dev.azide.core.test_utils.event_stream.EventStreamTestUtils
import kotlin.test.Test

@Suppress("ClassName")
class EventStream_merge2_tests {
    @Test
    fun test_firstSourceEmits() {
        val sourceEventStream1 = EventStreamTestUtils.createInputEventStream<Int>()
        val sourceEventStream2 = EventStreamTestUtils.createInputEventStream<Int>()

        val subjectEventStream = EventStream.merge2(
            sourceEventStream1,
            sourceEventStream2,
        )

        EventStreamTestUtils.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = sourceEventStream1.emit(
                emittedEvent = 11,
            ),
            expectedEmittedEvent = 11,
        )
    }

    @Test
    fun test_secondSourceEmits() {
        val sourceEventStream1 = EventStreamTestUtils.createInputEventStream<Int>()
        val sourceEventStream2 = EventStreamTestUtils.createInputEventStream<Int>()

        val subjectEventStream = EventStream.merge2(
            sourceEventStream1,
            sourceEventStream2,
        )

        EventStreamTestUtils.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = sourceEventStream2.emit(
                emittedEvent = 21,
            ),
            expectedEmittedEvent = 21,
        )
    }

    @Test
    fun test_bothSourcesEmits() {
        val sourceEventStream1 = EventStreamTestUtils.createInputEventStream<Int>()
        val sourceEventStream2 = EventStreamTestUtils.createInputEventStream<Int>()

        val subjectEventStream = EventStream.merge2(
            sourceEventStream1,
            sourceEventStream2,
        )

        EventStreamTestUtils.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestInputStimulation.combine(
                sourceEventStream1.emit(
                    emittedEvent = 11,
                ),
                sourceEventStream2.emit(
                    emittedEvent = 21,
                ),
            ),
            expectedEmittedEvent = 11,
        )
    }

    @Test
    fun test_firstSourcesEmits_revoked() {
        val sourceEventStream1 = EventStreamTestUtils.createInputEventStream<Int>()
        val sourceEventStream2 = EventStreamTestUtils.createInputEventStream<Int>()

        val subjectEventStream = EventStream.merge2(
            sourceEventStream1,
            sourceEventStream2,
        )

        EventStreamTestUtils.verifyDoesNotEmitEffectively(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestInputStimulation.combine(
                sourceEventStream1.emit(
                    emittedEvent = 11,
                ),
                sourceEventStream1.revokeEmission(),
            ),
        )
    }

    @Test
    fun test_secondSourcesEmits_revoked() {
        val sourceEventStream1 = EventStreamTestUtils.createInputEventStream<Int>()
        val sourceEventStream2 = EventStreamTestUtils.createInputEventStream<Int>()

        val subjectEventStream = EventStream.merge2(
            sourceEventStream1,
            sourceEventStream2,
        )

        EventStreamTestUtils.verifyDoesNotEmitEffectively(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestInputStimulation.combine(
                sourceEventStream2.emit(
                    emittedEvent = 21,
                ),
                sourceEventStream2.revokeEmission(),
            ),
        )
    }

    @Test
    fun test_bothSourcesEmits_firstRevoked() {
        val sourceEventStream1 = EventStreamTestUtils.createInputEventStream<Int>()
        val sourceEventStream2 = EventStreamTestUtils.createInputEventStream<Int>()

        val subjectEventStream = EventStream.merge2(
            sourceEventStream1,
            sourceEventStream2,
        )

        EventStreamTestUtils.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestInputStimulation.combine(
                sourceEventStream1.emit(
                    emittedEvent = 11,
                ),
                sourceEventStream2.emit(
                    emittedEvent = 21,
                ),
                sourceEventStream1.revokeEmission(),
            ),
            expectedEmittedEvent = 21,
        )
    }

    @Test
    fun test_bothSourcesEmits_secondRevoked() {
        val sourceEventStream1 = EventStreamTestUtils.createInputEventStream<Int>()
        val sourceEventStream2 = EventStreamTestUtils.createInputEventStream<Int>()

        val subjectEventStream = EventStream.merge2(
            sourceEventStream1,
            sourceEventStream2,
        )

        EventStreamTestUtils.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestInputStimulation.combine(
                sourceEventStream1.emit(
                    emittedEvent = 11,
                ),
                sourceEventStream2.emit(
                    emittedEvent = 21,
                ),
                sourceEventStream2.revokeEmission(),
            ),
            expectedEmittedEvent = 11,
        )
    }

    @Test
    fun test_bothSourcesEmits_bothRevoked() {
        val sourceEventStream1 = EventStreamTestUtils.createInputEventStream<Int>()
        val sourceEventStream2 = EventStreamTestUtils.createInputEventStream<Int>()

        val subjectEventStream = EventStream.merge2(
            sourceEventStream1,
            sourceEventStream2,
        )

        EventStreamTestUtils.verifyDoesNotEmitEffectively(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestInputStimulation.combine(
                sourceEventStream1.emit(
                    emittedEvent = 11,
                ),
                sourceEventStream2.emit(
                    emittedEvent = 21,
                ),
                sourceEventStream2.revokeEmission(),
                sourceEventStream1.revokeEmission(),
            ),
        )
    }

    @Test
    fun test_firstSourcesEmits_corrected() {
        val sourceEventStream1 = EventStreamTestUtils.createInputEventStream<Int>()
        val sourceEventStream2 = EventStreamTestUtils.createInputEventStream<Int>()

        val subjectEventStream = EventStream.merge2(
            sourceEventStream1,
            sourceEventStream2,
        )

        EventStreamTestUtils.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestInputStimulation.combine(
                sourceEventStream1.emit(
                    emittedEvent = 11,
                ),
                sourceEventStream1.correctEmission(
                    correctedEmittedEvent = 12,
                ),
            ),
            expectedEmittedEvent = 12,
        )
    }

    @Test
    fun test_secondSourcesEmits_corrected() {
        val sourceEventStream1 = EventStreamTestUtils.createInputEventStream<Int>()
        val sourceEventStream2 = EventStreamTestUtils.createInputEventStream<Int>()

        val subjectEventStream = EventStream.merge2(
            sourceEventStream1,
            sourceEventStream2,
        )

        EventStreamTestUtils.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestInputStimulation.combine(
                sourceEventStream2.emit(
                    emittedEvent = 21,
                ),
                sourceEventStream2.correctEmission(
                    correctedEmittedEvent = 22,
                ),
            ),
            expectedEmittedEvent = 22,
        )
    }

    @Test
    fun test_bothSourcesEmits_firstCorrected() {
        val sourceEventStream1 = EventStreamTestUtils.createInputEventStream<Int>()
        val sourceEventStream2 = EventStreamTestUtils.createInputEventStream<Int>()

        val subjectEventStream = EventStream.merge2(
            sourceEventStream1,
            sourceEventStream2,
        )

        EventStreamTestUtils.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestInputStimulation.combine(
                sourceEventStream1.emit(
                    emittedEvent = 11,
                ),
                sourceEventStream2.emit(
                    emittedEvent = 21,
                ),
                sourceEventStream1.correctEmission(
                    correctedEmittedEvent = 12,
                ),
            ),
            expectedEmittedEvent = 12,
        )
    }

    @Test
    fun test_bothSourcesEmits_secondCorrected() {
        val sourceEventStream1 = EventStreamTestUtils.createInputEventStream<Int>()
        val sourceEventStream2 = EventStreamTestUtils.createInputEventStream<Int>()

        val subjectEventStream = EventStream.merge2(
            sourceEventStream1,
            sourceEventStream2,
        )

        EventStreamTestUtils.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestInputStimulation.combine(
                sourceEventStream1.emit(
                    emittedEvent = 11,
                ),
                sourceEventStream2.emit(
                    emittedEvent = 21,
                ),
                sourceEventStream2.correctEmission(
                    correctedEmittedEvent = 22,
                ),
            ),
            expectedEmittedEvent = 11,
        )
    }

    @Test
    fun test_bothSourcesEmits_bothCorrected() {
        val sourceEventStream1 = EventStreamTestUtils.createInputEventStream<Int>()
        val sourceEventStream2 = EventStreamTestUtils.createInputEventStream<Int>()

        val subjectEventStream = EventStream.merge2(
            sourceEventStream1,
            sourceEventStream2,
        )

        EventStreamTestUtils.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestInputStimulation.combine(
                sourceEventStream1.emit(
                    emittedEvent = 11,
                ),
                sourceEventStream2.emit(
                    emittedEvent = 21,
                ),
                sourceEventStream1.correctEmission(
                    correctedEmittedEvent = 12,
                ),
                sourceEventStream2.correctEmission(
                    correctedEmittedEvent = 22,
                ),
            ),
            expectedEmittedEvent = 12,
        )
    }
}
