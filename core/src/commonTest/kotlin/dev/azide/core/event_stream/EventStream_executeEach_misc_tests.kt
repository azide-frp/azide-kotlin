package dev.azide.core.event_stream

import dev.azide.core.Action
import dev.azide.core.CausalLoopException
import dev.azide.core.executeEach
import dev.azide.core.executeEachOf
import dev.azide.core.startExternally
import dev.azide.core.test_utils.TestTargetActionRecorder
import dev.azide.core.test_utils.TransactionTestUtils
import dev.azide.core.test_utils.event_stream.TestInputEventStream
import dev.azide.core.test_utils.stimulateForTesting
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertIs

@Suppress("ClassName")
class EventStream_executeEach_misc_tests {
    @Test
    fun test_executeEach_selfCancelling() {
        val sourceEventStream = TestInputEventStream<Action<Int>>()

        val subjectEffect = sourceEventStream.executeEach()

        val subjectEffectOutcome = subjectEffect.startExternally()

        val subjectEventStream = subjectEffectOutcome.result
        val subjectEffectHandle = subjectEffectOutcome.handle

        val nastyEffect = subjectEventStream.executeEachOf { _: Int ->
            // Attempt to cancel the effect in consequence of its own emission, which leads to a paradox
            subjectEffectHandle.cancel
        }

        nastyEffect.startExternally()

        val targetActionRecorder = TestTargetActionRecorder.of(result = 10)

        assertIs<CausalLoopException>(
            assertFails {
                TransactionTestUtils.executeInsideTransaction {
                    sourceEventStream.emit(emittedEvent = targetActionRecorder.recordedAction).stimulateForTesting()
                }
            },
        )
    }
}
