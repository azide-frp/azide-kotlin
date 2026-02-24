package dev.azide.core.event_stream

import dev.azide.core.Cell
import dev.azide.core.EventStream
import dev.azide.core.divertOf
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.TestUtils
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.event_stream.EventStreamTestUtils_deprecated
import dev.azide.core.test_utils.event_stream.TestInputEventStream
import kotlin.test.Ignore
import kotlin.test.Test

@Suppress("ClassName")
class EventStream_divert_tests {
    @Test
    fun test_onlyCurrentInnerEmits_outerConst() {
        val innerEventStream = TestInputEventStream<Int>()

        val outerSourceCell = Cell.Const(
            constValue = innerEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        EventStreamTestUtils_deprecated.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = innerEventStream.emit(
                emittedEvent = 11,
            ),
            expectedEmittedEvent = 11,
        )
    }

    @Test
    fun test_onlyCurrentInnerEmits_initial() {
        val innerEventStream = TestInputEventStream<Int>()

        val outerSourceCell = TestInputCell(
            initialValue = innerEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        EventStreamTestUtils_deprecated.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = innerEventStream.emit(
                emittedEvent = 11,
            ),
            expectedEmittedEvent = 11,
        )
    }

    @Test
    fun test_onlyCurrentInnerEmits_initial_revoked() {
        val innerEventStream = TestInputEventStream<Int>()

        val outerSourceCell = TestInputCell(
            initialValue = innerEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        EventStreamTestUtils_deprecated.verifyDoesNotEmitEffectively(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestStimulation.combineInProvidedOrder(
                innerEventStream.emit(
                    emittedEvent = 11,
                ),
                innerEventStream.revokeEmission(),
            ),
        )
    }

    @Test
    fun test_onlyCurrentInnerEmits_initial_corrected() {
        val innerEventStream = TestInputEventStream<Int>()

        val outerSourceCell = TestInputCell(
            initialValue = innerEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        EventStreamTestUtils_deprecated.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestStimulation.combineInProvidedOrder(
                innerEventStream.emit(
                    emittedEvent = 11,
                ),
                innerEventStream.correctEmission(
                    correctedEmittedEvent = 12,
                ),
            ),
            expectedEmittedEvent = 12,
        )
    }

    @Test
    fun test_onlyCurrentInnerEmits_subsequent() {
        val earlierInnerSourceEventStream = TestInputEventStream<Int>()

        val laterInnerSourceEventStream = TestInputEventStream<Int>()

        val outerSourceCell = TestInputCell(
            initialValue = earlierInnerSourceEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        val subscribingVerifier = EventStreamTestUtils_deprecated.subscribeForVerification(
            subjectEventStream = subjectEventStream,
        )

        TestUtils.stimulateSeparately(
            outerSourceCell.update(
                newValue = laterInnerSourceEventStream,
            ),
        )

        subscribingVerifier.verifyEmitsAsExpected(
            inputStimulation = laterInnerSourceEventStream.emit(
                emittedEvent = 21,
            ),
            expectedEmittedEvent = 21,
        )
    }

    @Test
    fun test_onlyCurrentInnerEmits_subsequent_revoked() {
        val earlierInnerSourceEventStream = TestInputEventStream<Int>()

        val laterInnerSourceEventStream = TestInputEventStream<Int>()

        val outerSourceCell = TestInputCell(
            initialValue = earlierInnerSourceEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        val subscribingVerifier = EventStreamTestUtils_deprecated.subscribeForVerification(
            subjectEventStream = subjectEventStream,
        )

        TestUtils.stimulateSeparately(
            outerSourceCell.update(
                newValue = laterInnerSourceEventStream,
            ),
        )

        subscribingVerifier.verifyDoesNotEmitEffectively(
            inputStimulation = TestStimulation.combineInProvidedOrder(
                laterInnerSourceEventStream.emit(
                    emittedEvent = 21,
                ),
                laterInnerSourceEventStream.revokeEmission(),
            ),
        )
    }

    @Test
    fun test_onlyCurrentInnerEmits_subsequent_corrected() {
        val earlierInnerSourceEventStream = TestInputEventStream<Int>()

        val laterInnerSourceEventStream = TestInputEventStream<Int>()

        val outerSourceCell = TestInputCell(
            initialValue = earlierInnerSourceEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        val subscribingVerifier = EventStreamTestUtils_deprecated.subscribeForVerification(
            subjectEventStream = subjectEventStream,
        )

        TestUtils.stimulateSeparately(
            outerSourceCell.update(
                newValue = laterInnerSourceEventStream,
            ),
        )

        subscribingVerifier.verifyEmitsAsExpected(
            inputStimulation = TestStimulation.combineInProvidedOrder(
                laterInnerSourceEventStream.emit(
                    emittedEvent = 21,
                ),
                laterInnerSourceEventStream.correctEmission(
                    correctedEmittedEvent = 22,
                ),
            ),
            expectedEmittedEvent = 22,
        )
    }

    @Test
    fun test_onlyPreviousInnerUpdates() {
        val earlierInnerSourceEventStream = TestInputEventStream<Int>()

        val laterInnerSourceEventStream = TestInputEventStream<Int>()

        val outerSourceCell = TestInputCell(
            initialValue = earlierInnerSourceEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        val subscribingVerifier = EventStreamTestUtils_deprecated.subscribeForVerification(
            subjectEventStream = subjectEventStream,
        )

        TestUtils.stimulateSeparately(
            outerSourceCell.update(
                newValue = laterInnerSourceEventStream,
            ),
        )

        subscribingVerifier.verifyDoesNotEmitAtAll(
            inputStimulation = earlierInnerSourceEventStream.emit(
                emittedEvent = 11,
            ),
        )
    }

    @Test
    fun test_onlyOuterUpdates() {
        val earlierInnerSourceEventStream = TestInputEventStream<Int>()

        val laterInnerSourceEventStream = TestInputEventStream<Int>()

        val outerSourceCell = TestInputCell(
            initialValue = earlierInnerSourceEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        EventStreamTestUtils_deprecated.verifyDoesNotEmitAtAll(
            subjectEventStream = subjectEventStream,
            inputStimulation = outerSourceCell.update(
                newValue = laterInnerSourceEventStream,
            ),
        )
    }

    @Test
    fun test_onlyOuterUpdates_updatedInnerNever() {
        val earlierInnerSourceEventStream = TestInputEventStream<Int>()

        val laterInnerSourceEventStream = EventStream.Never

        val outerSourceCell = TestInputCell<EventStream<Int>>(
            initialValue = earlierInnerSourceEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        EventStreamTestUtils_deprecated.verifyDoesNotEmitAtAll(
            subjectEventStream = subjectEventStream,
            inputStimulation = outerSourceCell.update(
                newValue = laterInnerSourceEventStream,
            ),
        )
    }

    @Test
    fun test_onlyOuterUpdates_revoked() {
        val earlierInnerSourceEventStream = TestInputEventStream<Int>()

        val laterInnerSourceEventStream = TestInputEventStream<Int>()

        val outerSourceCell = TestInputCell(
            initialValue = earlierInnerSourceEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        EventStreamTestUtils_deprecated.verifyDoesNotEmitAtAll(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestStimulation.combineInProvidedOrder(
                outerSourceCell.update(
                    newValue = laterInnerSourceEventStream,
                ),
                outerSourceCell.revokeUpdate(),
            ),
        )

        EventStreamTestUtils_deprecated.verifyDoesNotEmitAtAll(
            subjectEventStream = subjectEventStream,
            inputStimulation = laterInnerSourceEventStream.emit(
                emittedEvent = 21,
            ),
        )
    }

    @Test
    fun test_onlyOuterUpdates_corrected() {
        val earlierInnerSourceEventStream = TestInputEventStream<Int>()

        val intermediateInnerSourceEventStream = TestInputEventStream<Int>()

        val laterInnerSourceEventStream = TestInputEventStream<Int>()

        val outerSourceCell = TestInputCell(
            initialValue = earlierInnerSourceEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        EventStreamTestUtils_deprecated.verifyDoesNotEmitAtAll(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestStimulation.combineInProvidedOrder(
                outerSourceCell.update(
                    newValue = intermediateInnerSourceEventStream,
                ),
                outerSourceCell.correctUpdate(
                    correctedNewValue = laterInnerSourceEventStream,
                ),
            ),
        )

        EventStreamTestUtils_deprecated.verifyDoesNotEmitAtAll(
            subjectEventStream = subjectEventStream,
            inputStimulation = intermediateInnerSourceEventStream.emit(
                emittedEvent = 21,
            ),
        )
    }

    @Test
    fun test_outerUpdatesAndNewInnerEmits_outerFirst() {
        val earlierInnerSourceEventStream = TestInputEventStream<Int>()

        val laterInnerSourceEventStream = TestInputEventStream<Int>()

        val outerSourceCell = TestInputCell(
            initialValue = earlierInnerSourceEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        EventStreamTestUtils_deprecated.verifyDoesNotEmitAtAll(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestStimulation.combineInProvidedOrder(
                outerSourceCell.update(
                    newValue = laterInnerSourceEventStream,
                ),
                laterInnerSourceEventStream.emit(
                    emittedEvent = 21,
                ),
            ),
        )
    }

    @Test
    fun test_outerUpdatesAndNewInnerEmits_innerFirst() {
        val earlierInnerSourceEventStream = TestInputEventStream<Int>()

        val laterInnerSourceEventStream = TestInputEventStream<Int>()

        val outerSourceCell = TestInputCell(
            initialValue = earlierInnerSourceEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        EventStreamTestUtils_deprecated.verifyDoesNotEmitAtAll(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestStimulation.combineInProvidedOrder(
                laterInnerSourceEventStream.emit(
                    emittedEvent = 21,
                ),
                outerSourceCell.update(
                    newValue = laterInnerSourceEventStream,
                ),
            ),
        )
    }

    @Test
    fun test_outerUpdatesAndNewInnerEmits_newInnerEmissionRevoked() {
        val earlierInnerSourceEventStream = TestInputEventStream<Int>()

        val laterInnerSourceEventStream = TestInputEventStream<Int>()

        val outerSourceCell = TestInputCell(
            initialValue = earlierInnerSourceEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        EventStreamTestUtils_deprecated.verifyDoesNotEmitAtAll(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestStimulation.combineInProvidedOrder(
                outerSourceCell.update(
                    newValue = laterInnerSourceEventStream,
                ),
                laterInnerSourceEventStream.emit(
                    emittedEvent = 21,
                ),
                laterInnerSourceEventStream.revokeEmission(),
            ),
        )
    }

    @Test
    fun test_outerUpdatesAndOldInnerEmits_outerFirst() {
        val earlierInnerSourceEventStream = TestInputEventStream<Int>()

        val laterInnerSourceEventStream = TestInputEventStream<Int>()

        val outerSourceCell = TestInputCell(
            initialValue = earlierInnerSourceEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        EventStreamTestUtils_deprecated.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestStimulation.combineInProvidedOrder(
                outerSourceCell.update(
                    newValue = laterInnerSourceEventStream,
                ),
                earlierInnerSourceEventStream.emit(
                    emittedEvent = 11,
                ),
            ),
            expectedEmittedEvent = 11,
        )
    }

    @Test
    fun test_outerUpdatesAndOldInnerEmits_innerFirst() {
        val earlierInnerSourceEventStream = TestInputEventStream<Int>()

        val laterInnerSourceEventStream = TestInputEventStream<Int>()

        val outerSourceCell = TestInputCell(
            initialValue = earlierInnerSourceEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        EventStreamTestUtils_deprecated.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestStimulation.combineInProvidedOrder(
                earlierInnerSourceEventStream.emit(
                    emittedEvent = 11,
                ),
                outerSourceCell.update(
                    newValue = laterInnerSourceEventStream,
                ),
            ),
            expectedEmittedEvent = 11,
        )
    }

    @Test
    fun test_outerUpdatesAndOldInnerEmits_oldInnerEmissionRevoked() {
        val earlierInnerSourceEventStream = TestInputEventStream<Int>()

        val laterInnerSourceEventStream = TestInputEventStream<Int>()

        val outerSourceCell = TestInputCell(
            initialValue = earlierInnerSourceEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        EventStreamTestUtils_deprecated.verifyDoesNotEmitEffectively(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestStimulation.combineInProvidedOrder(
                outerSourceCell.update(
                    newValue = laterInnerSourceEventStream,
                ),
                earlierInnerSourceEventStream.emit(
                    emittedEvent = 21,
                ),
                earlierInnerSourceEventStream.revokeEmission(),
            ),
        )
    }

    @Test
    fun test_outerUpdatesAndBothInnerEmit() {
        val earlierInnerSourceEventStream = TestInputEventStream<Int>()

        val laterInnerSourceEventStream = TestInputEventStream<Int>()

        val outerSourceCell = TestInputCell(
            initialValue = earlierInnerSourceEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        EventStreamTestUtils_deprecated.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestStimulation.combineInProvidedOrder(
                outerSourceCell.update(
                    newValue = laterInnerSourceEventStream,
                ),
                earlierInnerSourceEventStream.emit(
                    emittedEvent = 11,
                ),
                laterInnerSourceEventStream.emit(
                    emittedEvent = 21,
                ),
            ),
            expectedEmittedEvent = 11,
        )
    }

    /**
     * This test stacks two levels of `divert` event streams.
     *
     * The past-leaning nature of `divert` might lead to subtle bugs, when dependent `divert` eventually subscribes to
     * the updated event stream which happens to also be a `divert` event stream (in reality, there might be also
     * multiple intermediate layers involved). When the dependency `divert` activates, it might incorrectly lean to the
     * past and subscribe to its own old source event stream, even when there's an updated one available.
     */
    @Test
    fun test_nested_shallowOuterCellUpdates_deepOuterCellUpdates() {
        // Earlier inner source event stream (A1)
        val earlierInnerSourceEventStream = TestInputEventStream<Int>()

        // Later inner source event stream (A2)
        val laterInnerSourceEventStream = TestInputEventStream<Int>()

        // Outer source cell (B)
        val deepOuterSourceCell = TestInputCell<EventStream<Int>>(
            initialValue = earlierInnerSourceEventStream,
        )

        // Earlier inner source event stream (C1)
        val earlierInnerIntermediateEventStream = TestInputEventStream<Int>()

        // Intermediate `divert` event stream (C2)
        val laterInnerIntermediateEventStream = Cell.divert(deepOuterSourceCell)

        // Outer intermediate cell (D)
        val shallowOuterIntermediateCell = TestInputCell<EventStream<Int>>(
            initialValue = earlierInnerIntermediateEventStream,
        )

        // Subject `divert` event stream (E)
        val subjectEventStream = Cell.divert(shallowOuterIntermediateCell)

        val subscribingVerifier = EventStreamTestUtils_deprecated.subscribeForVerification(
            subjectEventStream = subjectEventStream,
        )

        subscribingVerifier.verifyDoesNotEmitAtAll(
            inputStimulation = TestStimulation.combineInProvidedOrder(
                // B updates to A2, but C2 shouldn't even be subscribed to B during the propagation phase. _But_ when C2
                // eventually activates, it should correctly subscribe to C2 for the sake of future transactions (_not_
                // to C1, as it would if it was activated mid-transaction).
                deepOuterSourceCell.update(
                    newValue = laterInnerSourceEventStream,
                ),
                // D updates from C1 to C2. E should acknowledge it, yet keep subscribed to C1 for the duration of the
                // propagation phase.
                shallowOuterIntermediateCell.update(
                    newValue = laterInnerIntermediateEventStream,
                ),
                // This A1 event should be ignored, as E should still be subscribed to C1, so C2/B/A1/A2 shouldn't even
                // be active yet.
                earlierInnerSourceEventStream.emit(
                    emittedEvent = 11,
                ),
            ),
        )

        subscribingVerifier.verifyEmitsAsExpected(
            // A2 should be active at this point and its events should be propagated down to E.
            inputStimulation = laterInnerSourceEventStream.emit(
                emittedEvent = 21,
            ),
            expectedEmittedEvent = 21,
        )

        subscribingVerifier.verifyDoesNotEmitAtAll(
            // A1 should be inactive at this point.
            inputStimulation = earlierInnerSourceEventStream.emit(
                emittedEvent = 12,
            ),
        )
    }

    private data class EventStreamBox<T>(
        val eventStream: EventStream<T>,
    )

    @Test
    @Ignore // FIXME: Offline-retrieved cell old values
    fun test_nested_shallowOuterCellUpdates() {
        // Earlier inner source event stream (A1)
        val deepInnerSourceEventStream = TestInputEventStream<Int>()

        // Outer source cell (B)
        val deepOuterSourceCell = TestInputCell(
            initialValue = EventStreamBox(
                eventStream = deepInnerSourceEventStream,
            ),
        )

        // Earlier inner source event stream (C1)
        val earlierInnerIntermediateEventStream = TestInputEventStream<Int>()

        // Intermediate `divert` event stream (C2)
        val laterInnerIntermediateEventStream = deepOuterSourceCell.divertOf { it.eventStream }

        // Outer intermediate cell (D)
        val shallowOuterIntermediateCell = TestInputCell<EventStream<Int>>(
            initialValue = earlierInnerIntermediateEventStream,
        )

        // Subject `divert` event stream (E)
        val subjectEventStream = Cell.divert(shallowOuterIntermediateCell)

        val subscribingVerifier = EventStreamTestUtils_deprecated.subscribeForVerification(
            subjectEventStream = subjectEventStream,
        )

        subscribingVerifier.verifyDoesNotEmitAtAll(
            inputStimulation = TestStimulation.combineInProvidedOrder(
                // D updates from C1 to C2. E should acknowledge it, yet keep subscribed to C1 for the duration of the
                // propagation phase.
                shallowOuterIntermediateCell.update(
                    newValue = laterInnerIntermediateEventStream,
                ),
                // This A1 event should be ignored, as E should still be subscribed to C1, so C2/B/A1/A2 shouldn't even
                // be active yet.
                deepInnerSourceEventStream.emit(
                    emittedEvent = 11,
                ),
            ),
        )

        subscribingVerifier.verifyEmitsAsExpected(
            // A2 should be active at this point and its events should be propagated down to E.
            inputStimulation = deepInnerSourceEventStream.emit(
                emittedEvent = 21,
            ),
            expectedEmittedEvent = 21,
        )
    }
}
