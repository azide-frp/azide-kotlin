package dev.azide.event_stream

import dev.azide.hold
import dev.azide.test_utils.async.AsyncTest
import dev.azide.test_utils.async.AsyncTestGroup
import dev.azide.test_utils.cell.CellTestUtils
import dev.azide.test_utils.cell.JsCellTestUtils
import dev.azide.test_utils.event_stream.EventStreamTestUtils

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
                CellTestUtils.spawnStatefulCell {
                    sourceEventStream.hold(initialValue = 10)
                }
            }
        }
    }

    data object collectible_observed_test : AsyncTest() {
        override suspend fun execute() {
            val sourceEventStream = EventStreamTestUtils.createInputEventStream<Int>()

            JsCellTestUtils.ensureCollectible {
                val subjectCell = CellTestUtils.spawnStatefulCell {
                    sourceEventStream.hold(initialValue = 10)
                }

                CellTestUtils.registerNoopObserver(
                    subjectCell = subjectCell,
                )

                subjectCell
            }
        }
    }
}
