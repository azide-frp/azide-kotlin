package dev.azide.core

import dev.azide.core.event_stream.EventStream_hold_garbageCollection_testGroup
import dev.azide.core.event_stream.EventStream_single_garbageCollection_testGroup
import dev.azide.core.test_utils.JsGarbageCollectorUtils
import dev.azide.core.test_utils.async.AsyncTestSuite
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds

@Suppress("ClassName")
class Reactive_garbageCollection_tests {
    val config = JsGarbageCollectorUtils.PressureConfig(
        minBlockSize = 128 * 1024, // 128 KiB
        maxBlockSize = 8 * 1024 * 1024, // 8 MiB
        minAllocationDelay = 1.milliseconds,
        maxAllocationDelay = 100.milliseconds,
    )

    private data object TestSuite : AsyncTestSuite() {
        override val groups = listOf(
            EventStream_hold_garbageCollection_testGroup,
            EventStream_single_garbageCollection_testGroup,
        )
    }

    @Test
    fun test_garbageCollection() = runTest {
        JsGarbageCollectorUtils.withContinuousGarbageCollectorPressure(config = config) {
            TestSuite.runAndPrintResult()
        }
    }
}
