package dev.azide.core.event_stream

import dev.azide.core.Cell
import dev.azide.core.EventStream
import dev.azide.core.test_utils.TestInputStimulation
import dev.azide.core.test_utils.TestUtils
import dev.azide.core.test_utils.cell.CellTestUtils
import dev.azide.core.test_utils.event_stream.EventStreamTestUtils
import kotlin.test.Test

@Suppress("ClassName")
@Ignore // TODO: Implement `divert`
class EventStream_divert_tests {
    @Test
    fun test_emission_onlyCurrentInnerEmits_outerConst() {
        val innerEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val outerSourceCell = Cell.Const(
            constValue = innerEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        EventStreamTestUtils.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = innerEventStream.emit(
                emittedEvent = 11,
            ),
            expectedEmittedEvent = 11,
        )
    }

    @Test
    fun test_emission_onlyCurrentInnerEmits_initial() {
        val innerEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val outerSourceCell = CellTestUtils.createInputCell(
            initialValue = innerEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        EventStreamTestUtils.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = innerEventStream.emit(
                emittedEvent = 11,
            ),
            expectedEmittedEvent = 11,
        )
    }

    @Test
    fun test_emission_onlyCurrentInnerEmits_initial_revoked() {
        val innerEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val outerSourceCell = CellTestUtils.createInputCell(
            initialValue = innerEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        EventStreamTestUtils.verifyDoesNotEmitEffectively(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestInputStimulation.combine(
                innerEventStream.emit(
                    emittedEvent = 11,
                ),
                innerEventStream.revokeEmission(),
            ),
        )
    }

    @Test
    fun test_emission_onlyCurrentInnerEmits_initial_corrected() {
        val innerEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val outerSourceCell = CellTestUtils.createInputCell(
            initialValue = innerEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        EventStreamTestUtils.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestInputStimulation.combine(
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
    fun test_emission_onlyCurrentInnerEmits_subsequent() {
        val earlierInnerSourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val laterInnerSourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val outerSourceCell = CellTestUtils.createInputCell(
            initialValue = earlierInnerSourceEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        val subscribingVerifier = EventStreamTestUtils.subscribeForVerification(
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
    fun test_emission_onlyCurrentInnerEmits_subsequent_revoked() {
        val earlierInnerSourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val laterInnerSourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val outerSourceCell = CellTestUtils.createInputCell(
            initialValue = earlierInnerSourceEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        val subscribingVerifier = EventStreamTestUtils.subscribeForVerification(
            subjectEventStream = subjectEventStream,
        )

        TestUtils.stimulateSeparately(
            outerSourceCell.update(
                newValue = laterInnerSourceEventStream,
            ),
        )

        subscribingVerifier.verifyDoesNotEmitEffectively(
            inputStimulation = TestInputStimulation.combine(
                laterInnerSourceEventStream.emit(
                    emittedEvent = 21,
                ),
                laterInnerSourceEventStream.revokeEmission(),
            ),
        )
    }

    @Test
    fun test_emission_onlyCurrentInnerEmits_subsequent_corrected() {
        val earlierInnerSourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val laterInnerSourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val outerSourceCell = CellTestUtils.createInputCell(
            initialValue = earlierInnerSourceEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        val subscribingVerifier = EventStreamTestUtils.subscribeForVerification(
            subjectEventStream = subjectEventStream,
        )

        TestUtils.stimulateSeparately(
            outerSourceCell.update(
                newValue = laterInnerSourceEventStream,
            ),
        )

        subscribingVerifier.verifyEmitsAsExpected(
            inputStimulation = TestInputStimulation.combine(
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
    fun test_emission_onlyPreviousInnerUpdates() {
        val earlierInnerSourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val laterInnerSourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val outerSourceCell = CellTestUtils.createInputCell(
            initialValue = earlierInnerSourceEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        val subscribingVerifier = EventStreamTestUtils.subscribeForVerification(
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
    fun test_emission_onlyOuterUpdates() {
        val earlierInnerSourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val laterInnerSourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val outerSourceCell = CellTestUtils.createInputCell(
            initialValue = earlierInnerSourceEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        EventStreamTestUtils.verifyDoesNotEmitAtAll(
            subjectEventStream = subjectEventStream,
            inputStimulation = outerSourceCell.update(
                newValue = laterInnerSourceEventStream,
            ),
        )
    }

    @Test
    fun test_emission_onlyOuterUpdates_updatedInnerNever() {
        val earlierInnerSourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val laterInnerSourceEventStream = EventStream.Never

        val outerSourceCell = CellTestUtils.createInputCell<EventStream<Int>>(
            initialValue = earlierInnerSourceEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        EventStreamTestUtils.verifyDoesNotEmitAtAll(
            subjectEventStream = subjectEventStream,
            inputStimulation = outerSourceCell.update(
                newValue = laterInnerSourceEventStream,
            ),
        )
    }

    @Test
    fun test_emission_onlyOuterUpdates_revoked() {
        val earlierInnerSourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val laterInnerSourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val outerSourceCell = CellTestUtils.createInputCell(
            initialValue = earlierInnerSourceEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        EventStreamTestUtils.verifyDoesNotEmitAtAll(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestInputStimulation.combine(
                outerSourceCell.update(
                    newValue = laterInnerSourceEventStream,
                ),
                outerSourceCell.revokeUpdate(),
            ),
        )

        EventStreamTestUtils.verifyDoesNotEmitAtAll(
            subjectEventStream = subjectEventStream,
            inputStimulation = laterInnerSourceEventStream.emit(
                emittedEvent = 21,
            ),
        )
    }

    @Test
    fun test_emission_onlyOuterUpdates_corrected() {
        val earlierInnerSourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val intermediateInnerSourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val laterInnerSourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val outerSourceCell = CellTestUtils.createInputCell(
            initialValue = earlierInnerSourceEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        EventStreamTestUtils.verifyDoesNotEmitAtAll(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestInputStimulation.combine(
                outerSourceCell.update(
                    newValue = intermediateInnerSourceEventStream,
                ),
                outerSourceCell.correctUpdate(
                    correctedNewValue = laterInnerSourceEventStream,
                ),
            ),
        )

        EventStreamTestUtils.verifyDoesNotEmitAtAll(
            subjectEventStream = subjectEventStream,
            inputStimulation = intermediateInnerSourceEventStream.emit(
                emittedEvent = 21,
            ),
        )
    }

    @Test
    fun test_emission_outerAndNewInnerUpdate_outerFirst() {
        val earlierInnerSourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val laterInnerSourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val outerSourceCell = CellTestUtils.createInputCell(
            initialValue = earlierInnerSourceEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        EventStreamTestUtils.verifyDoesNotEmitAtAll(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestInputStimulation.combine(
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
    fun test_emission_outerAndNewInnerUpdate_innerFirst() {
        val earlierInnerSourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val laterInnerSourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val outerSourceCell = CellTestUtils.createInputCell(
            initialValue = earlierInnerSourceEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        EventStreamTestUtils.verifyDoesNotEmitAtAll(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestInputStimulation.combine(
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
    fun test_emission_outerAndNewInnerUpdate_newInnerUpdateRevoked() {
        val earlierInnerSourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val laterInnerSourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val outerSourceCell = CellTestUtils.createInputCell(
            initialValue = earlierInnerSourceEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        EventStreamTestUtils.verifyDoesNotEmitAtAll(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestInputStimulation.combine(
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
    fun test_emission_outerAndNewInnerUpdate_outerUpdateRevoked() {
        val earlierInnerSourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val laterInnerSourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val outerSourceCell = CellTestUtils.createInputCell(
            initialValue = earlierInnerSourceEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        EventStreamTestUtils.verifyDoesNotEmitAtAll(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestInputStimulation.combine(
                outerSourceCell.update(
                    newValue = laterInnerSourceEventStream,
                ),
                laterInnerSourceEventStream.emit(
                    emittedEvent = 21,
                ),
                outerSourceCell.revokeUpdate(),
            ),
        )

        EventStreamTestUtils.verifyDoesNotEmitAtAll(
            subjectEventStream = subjectEventStream,
            inputStimulation = laterInnerSourceEventStream.emit(
                emittedEvent = 22,
            ),
        )
    }

    @Test
    fun test_emission_outerAndOldInnerUpdate_outerFirst() {
        val earlierInnerSourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val laterInnerSourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val outerSourceCell = CellTestUtils.createInputCell(
            initialValue = earlierInnerSourceEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        EventStreamTestUtils.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestInputStimulation.combine(
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
    fun test_emission_outerAndOldInnerUpdate_innerFirst() {
        val earlierInnerSourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val laterInnerSourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val outerSourceCell = CellTestUtils.createInputCell(
            initialValue = earlierInnerSourceEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        EventStreamTestUtils.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestInputStimulation.combine(
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
    fun test_emission_outerAndOldInnerUpdate_oldInnerUpdateRevoked() {
        val earlierInnerSourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val laterInnerSourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val outerSourceCell = CellTestUtils.createInputCell(
            initialValue = earlierInnerSourceEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        EventStreamTestUtils.verifyDoesNotEmitEffectively(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestInputStimulation.combine(
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
    fun test_emission_outerAndOldInnerUpdate_outerUpdateRevoked() {
        val earlierInnerSourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val laterInnerSourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val outerSourceCell = CellTestUtils.createInputCell(
            initialValue = earlierInnerSourceEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        EventStreamTestUtils.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestInputStimulation.combine(
                outerSourceCell.update(
                    newValue = laterInnerSourceEventStream,
                ),
                earlierInnerSourceEventStream.emit(
                    emittedEvent = 11,
                ),
                // Revoke the outer update after the old inner update, to verify that the vertex falls back to the
                // up-to-date value of the stable inner cell
                outerSourceCell.revokeUpdate(),
            ),
            expectedEmittedEvent = 11,
        )
    }

    @Test
    fun test_emission_outerAndOldInnerAndNewInnerUpdate() {
        val earlierInnerSourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val laterInnerSourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val outerSourceCell = CellTestUtils.createInputCell(
            initialValue = earlierInnerSourceEventStream,
        )

        val subjectEventStream = Cell.divert(outerSourceCell)

        EventStreamTestUtils.verifyEmitsAsExpected(
            subjectEventStream = subjectEventStream,
            inputStimulation = TestInputStimulation.combine(
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
    fun test_nested_outerCellsUpdate() {
        // Earlier inner source event stream (A1)
        val earlierInnerSourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        // Later inner source event stream (A2)
        val laterInnerSourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        // Outer source cell (B)
        val outerSourceCell = CellTestUtils.createInputCell<EventStream<Int>>(
            initialValue = earlierInnerSourceEventStream,
        )

        // Earlier inner source event stream (C1)
        val earlierInnerIntermediateEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        // Intermediate `divert` event stream (C2)
        val laterInnerIntermediateEventStream = Cell.divert(outerSourceCell)

        // Outer intermediate cell (D)
        val outerIntermediateCell = CellTestUtils.createInputCell<EventStream<Int>>(
            initialValue = earlierInnerIntermediateEventStream,
        )

        // Subject `divert` event stream (E)
        val subjectEventStream = Cell.divert(outerIntermediateCell)

        val subscribingVerifier = EventStreamTestUtils.subscribeForVerification(
            subjectEventStream = subjectEventStream,
        )

        subscribingVerifier.verifyDoesNotEmitAtAll(
            inputStimulation = TestInputStimulation.combine(
                // B updates to A2, but C2 shouldn't even be subscribed to B during the propagation phase. _But_ when C2
                // eventually activates, it should correctly subscribe to C2 for the sake of future transactions (_not_
                // to C1, as it would if it was activated mid-transaction).
                outerSourceCell.update(
                    newValue = laterInnerSourceEventStream,
                ),
                // D updates from C1 to C2. E should acknowledge it, yet keep subscribed to C1 for the duration of the
                // propagation phase.
                outerIntermediateCell.update(
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
}
