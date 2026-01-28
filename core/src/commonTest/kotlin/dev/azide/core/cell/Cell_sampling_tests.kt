package dev.azide.core.cell

import dev.azide.core.Cell
import dev.azide.core.holding
import dev.azide.core.map
import dev.azide.core.pullExternally
import dev.azide.core.sampling
import dev.azide.core.test_utils.cell.CellTestUtils
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.event_stream.EventStreamTestUtils
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("ClassName")
class Cell_sampling_tests {
    @Test
    fun test_subjectConst() {
        val subjectCell = Cell.Const(10)

        val sampledValue = subjectCell.sampling.pullExternally()

        assertEquals(
            expected = 10,
            actual = sampledValue,
        )
    }

    @Test
    fun test_subjectStateless_inactive() {
        val sourceCell = TestInputCell(10)

        val subjectCell = sourceCell.map { it.toString() }

        val sampledValue = subjectCell.sampling.pullExternally()

        assertEquals(
            expected = "10",
            actual = sampledValue,
        )
    }

    @Test
    fun test_subjectStateless_active() {
        val sourceCell = TestInputCell(10)

        val subjectCell = sourceCell.map { it.toString() }

        CellTestUtils.registerNoopListener(
            subjectCell = subjectCell,
        )

        val sampledValue = subjectCell.sampling.pullExternally()

        assertEquals(
            expected = "10",
            actual = sampledValue,
        )
    }

    @Test
    fun test_subjectStateful() {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

        val subjectCell = sourceEventStream.holding(
            initialValue = 10,
        ).pullExternally()


        val sampledValue = subjectCell.sampling.pullExternally()

        assertEquals(
            expected = 10,
            actual = sampledValue,
        )
    }
}
