package dev.azide.core.event_stream

import dev.azide.core.holding
import dev.azide.core.pullExternally
import dev.azide.core.test_utils.async.AsyncTest
import dev.azide.core.test_utils.async.AsyncTestGroup
import dev.azide.core.test_utils.cell.CellTestUtils
import dev.azide.core.test_utils.cell.JsCellTestUtils
import dev.azide.core.test_utils.event_stream.EventStreamTestUtils

@Suppress("ClassName")
data object EventStream_hold_garbageCollection_testGroup : AsyncTestGroup() {
    override val tests = listOf(
        collectible_nonObserved_test,
        collectible_observed_test,
    )

    data object collectible_nonObserved_test : AsyncTest() {
        override suspend fun execute() {
            val sourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

            JsCellTestUtils.ensureCollectible {
                sourceEventStream.holding(initialValue = 10).pullExternally()
            }
        }
    }

    data object collectible_observed_test : AsyncTest() {
        override suspend fun execute() {
            val sourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

            JsCellTestUtils.ensureCollectible {
                val subjectCell = sourceEventStream.holding(initialValue = 10).pullExternally()

                CellTestUtils.registerNoopListener(
                    subjectCell = subjectCell,
                )

                subjectCell
            }
        }
    }
}
