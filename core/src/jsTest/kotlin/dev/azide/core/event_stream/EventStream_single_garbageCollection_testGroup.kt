package dev.azide.core.event_stream

import dev.azide.core.single
import dev.azide.core.test_utils.async.AsyncTest
import dev.azide.core.test_utils.async.AsyncTestGroup
import dev.azide.core.test_utils.event_stream.EventStreamTestUtils
import dev.azide.core.test_utils.event_stream.JsEventStreamTestUtils
import dev.azide.core.test_utils.event_stream.TestInputEventStream

@Suppress("ClassName")
data object EventStream_single_garbageCollection_testGroup : AsyncTestGroup() {
    override val tests = listOf(
        collectible_nonSubscribed_test,
        collectible_subscribed_test,
    )

    data object collectible_nonSubscribed_test : AsyncTest() {
        override suspend fun execute() {
            val sourceEventStream = TestInputEventStream<Int>()

            JsEventStreamTestUtils.ensureCollectible {
                EventStreamTestUtils.spawnStatefulEventStream {
                    sourceEventStream.single()
                }
            }
        }
    }

    data object collectible_subscribed_test : AsyncTest() {
        override suspend fun execute() {
            val sourceEventStream = TestInputEventStream<Int>()

            JsEventStreamTestUtils.ensureCollectible {

                val subjectEventStream = EventStreamTestUtils.spawnStatefulEventStream {
                    sourceEventStream.single()
                }

                EventStreamTestUtils.registerNoopListener(
                    subjectEventStream = subjectEventStream,
                )

                EventStreamTestUtils.spawnStatefulEventStream {
                    sourceEventStream.single()
                }
            }
        }
    }
}
