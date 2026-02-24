package dev.azide.core.event_stream

import dev.azide.core.Cell
import dev.azide.core.EventStream
import dev.azide.core.holding
import dev.azide.core.impl.utils.LoopClosure
import dev.azide.core.joinOf
import dev.azide.core.map
import dev.azide.core.mapAt
import dev.azide.core.sample
import dev.azide.core.sampling
import dev.azide.core.test_utils.TestUtils
import dev.azide.core.test_utils.cell.CellTestUtils_deprecated
import dev.azide.core.test_utils.event_stream.TestInputEventStream
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("ClassName")
class EventStream_hold_misc_tests {
    @Test
    fun test_looped() {
        val sourceEventStream = TestInputEventStream<Int>()

        val memoryCell = TestUtils.pullSeparately(
            EventStream.loopedInMoment { loopedMultiplicationStream: EventStream<Int> ->
                loopedMultiplicationStream.holding(initialValue = 0).map { memoryCell: Cell<Int> ->
                    val multiplicationStream: EventStream<Int> = sourceEventStream.mapAt { multiplier ->
                        memoryCell.sample() * multiplier
                    }

                    LoopClosure(
                        result = memoryCell,
                        loopedValue = multiplicationStream,
                    )
                }
            },
        )

        CellTestUtils_deprecated.verifyAtRest(
            subjectCell = memoryCell,
            expectedValue = 0,
        )
    }

    @Test
    fun test_looped_initialSample() {
        val sourceEventStream = TestInputEventStream<Int>()

        val memoryCellInitialValue: Int = TestUtils.pullSeparately(
            EventStream.loopedInMoment { loopedMultiplicationStream: EventStream<Int> ->
                loopedMultiplicationStream.holding(initialValue = 0).joinOf { memoryCell: Cell<Int> ->
                    val multiplicationStream: EventStream<Int> = sourceEventStream.mapAt { multiplier ->
                        memoryCell.sample() * multiplier
                    }

                    memoryCell.sampling.map { initialValue ->
                        LoopClosure(
                            result = initialValue,
                            loopedValue = multiplicationStream,
                        )
                    }
                }
            },
        )

        assertEquals(
            expected = 0,
            actual = memoryCellInitialValue,
        )
    }
}
