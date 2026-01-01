package dev.azide.core.cell

import dev.azide.core.Cell
import dev.azide.core.hold
import dev.azide.core.map
import dev.azide.core.sample
import dev.azide.core.test_utils.TestUtils
import dev.azide.core.test_utils.cell.CellTestUtils
import dev.azide.core.test_utils.event_stream.EventStreamTestUtils
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("ClassName")
class Cell_sample_tests {
    @Test
    fun test_subjectConst() {
        val subjectCell = Cell.Const(10)

        val sampledValue = TestUtils.pullSeparately {
            subjectCell.sample()
        }

        assertEquals(
            expected = 10,
            actual = sampledValue,
        )
    }

    @Test
    fun test_subjectStateless_inactive() {
        val sourceCell = CellTestUtils.createInputCell(10)

        val subjectCell = sourceCell.map { it.toString() }

        val sampledValue = TestUtils.pullSeparately {
            subjectCell.sample()
        }

        assertEquals(
            expected = "10",
            actual = sampledValue,
        )
    }

    @Test
    fun test_subjectStateless_active() {
        val sourceCell = CellTestUtils.createInputCell(10)

        val subjectCell = sourceCell.map { it.toString() }

        CellTestUtils.registerNoopObserver(
            subjectCell = subjectCell,
        )

        val sampledValue = TestUtils.pullSeparately {
            subjectCell.sample()
        }

        assertEquals(
            expected = "10",
            actual = sampledValue,
        )
    }

    @Test
    fun test_subjectStateful() {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val subjectCell = CellTestUtils.spawnStatefulCell {
            sourceEventStream.hold(initialValue = 10)
        }

        val sampledValue = TestUtils.pullSeparately {
            subjectCell.sample()
        }

        assertEquals(
            expected = 10,
            actual = sampledValue,
        )
    }
}
