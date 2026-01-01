package dev.azide.core.cell

import dev.azide.core.Cell
import dev.azide.core.EventStream
import dev.azide.core.Moment
import dev.azide.core.holding
import dev.azide.core.joinOf
import dev.azide.core.map
import dev.azide.core.mapAt
import dev.azide.core.sample
import dev.azide.core.sampling
import dev.azide.core.test_utils.TestInputStimulation
import dev.azide.core.test_utils.TestUtils
import dev.azide.core.test_utils.cell.CellTestUtils
import dev.azide.core.test_utils.event_stream.EventStreamTestUtils
import kotlin.Int
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("ClassName")
class Cell_mapAt_tests {
    @Test
    fun test_passiveSample() {
        val sourceCell = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val externalCell = CellTestUtils.createInputCell(
            initialValue = 'A',
        )

        val subjectCell = CellTestUtils.spawnStatefulCell {
            sourceCell.mapAt {
                val externalValue: Char = externalCell.sample()
                "$it:$externalValue"
            }
        }

        CellTestUtils.verifyAtRest(
            subjectCell = subjectCell,
            expectedValue = "10:A",
        )
    }

    @Test
    fun test_passiveSample_sourceConst() {
        val sourceCell = Cell.Const(
            constValue = 10,
        )

        val externalCell = CellTestUtils.createInputCell(
            initialValue = 'A',
        )

        val subjectCell = CellTestUtils.spawnStatefulCell {
            sourceCell.mapAt {
                val externalValue: Char = externalCell.sample()
                "$it:$externalValue"
            }
        }

        CellTestUtils.verifyAtRest(
            subjectCell = subjectCell,
            expectedValue = "10:A",
        )
    }

    @Test
    fun test_update() {
        val sourceCell = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val externalCell = CellTestUtils.createInputCell(
            initialValue = 'A',
        )

        val subjectCell = CellTestUtils.spawnStatefulCell {
            sourceCell.mapAt {
                val externalValue: Char = externalCell.sample()
                "$it:$externalValue"
            }
        }

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = sourceCell.update(
                newValue = 11,
            ),
            expectedOldValue = "10:A",
            expectedNewValue = "11:A",
        )
    }

    @Test
    fun test_update_externalUpdatesSimultaneously() {
        val sourceCell = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val externalCell = CellTestUtils.createInputCell(
            initialValue = 'A',
        )

        val subjectCell = CellTestUtils.spawnStatefulCell {
            sourceCell.mapAt {
                val externalValue: Char = externalCell.sample()
                "$it:$externalValue"
            }
        }

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = TestInputStimulation.combine(
                sourceCell.update(
                    newValue = 11,
                ),
                externalCell.update(
                    newValue = 'B',
                ),
            ),
            expectedOldValue = "10:A",
            expectedNewValue = "11:A",
        )
    }

    @Test
    fun test_update_revoked() {
        val sourceCell = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val externalCell = CellTestUtils.createInputCell(
            initialValue = 'A',
        )

        val subjectCell = CellTestUtils.spawnStatefulCell {
            sourceCell.mapAt {
                val externalValue: Char = externalCell.sample()
                "$it:$externalValue"
            }
        }

        CellTestUtils.verifyDoesNotUpdateEffectively(
            subjectCell = subjectCell,
            inputStimulation = TestInputStimulation.combine(
                sourceCell.update(
                    newValue = 11,
                ),
                sourceCell.revokeUpdate(),
            ),
            expectedUnaffectedValue = "10:A",
        )
    }

    @Test
    fun test_update_corrected() {
        val sourceCell = CellTestUtils.createInputCell(
            initialValue = 10,
        )

        val externalCell = CellTestUtils.createInputCell(
            initialValue = 'A',
        )

        val subjectCell = CellTestUtils.spawnStatefulCell {
            sourceCell.mapAt {
                val externalValue: Char = externalCell.sample()
                "$it:$externalValue"
            }
        }

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = TestInputStimulation.combine(
                sourceCell.update(
                    newValue = 11,
                ),
                sourceCell.correctUpdate(
                    correctedNewValue = 12,
                ),
            ),
            expectedOldValue = "10:A",
            expectedNewValue = "12:A",
        )
    }

    @Test
    @Ignore // FIXME: Make this pass
    fun test_looped() {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val externalCell = CellTestUtils.createInputCell('A')

        val mapAtCell = TestUtils.pullSeparately(
            EventStream.loopedInMoment { loopedMultiplicationStream: EventStream<Int> ->
                loopedMultiplicationStream.holding(initialValue = 0).joinOf { memoryCell: Cell<Int> ->
                    Moment.decontextualize {
                        memoryCell.mapAt { value ->
                            "${externalCell.sample()}$value"
                        }
                    }.map { mapAtCell ->
                        Pair(
                            mapAtCell,
                            sourceEventStream,
                        )
                    }
                }
            },
        )

        CellTestUtils.verifyAtRest(
            subjectCell = mapAtCell,
            expectedValue = "A0",
        )
    }

    @Test
    @Ignore // FIXME: Make this pass
    fun test_looped_initialSample() {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val externalCell = CellTestUtils.createInputCell('A')

        val mapAtCellInitialValue: String = TestUtils.pullSeparately(
            EventStream.loopedInMoment { loopedMultiplicationStream: EventStream<Int> ->
                loopedMultiplicationStream.holding(initialValue = 0).joinOf { memoryCell: Cell<Int> ->
                    Moment.decontextualize {
                        memoryCell.mapAt { value ->
                            "${externalCell.sample()}$value"
                        }
                    }.joinOf { mapAtCell ->
                        mapAtCell.sampling.map { initialValue: String ->
                            Pair(
                                initialValue,
                                sourceEventStream,
                            )
                        }
                    }
                }
            },
        )

        assertEquals(
            expected = "A0",
            actual = mapAtCellInitialValue,
        )
    }
}
