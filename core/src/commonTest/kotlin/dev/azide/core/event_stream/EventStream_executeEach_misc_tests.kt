package dev.azide.core.event_stream

import dev.azide.core.Action
import dev.azide.core.CausalLoopException
import dev.azide.core.Cell
import dev.azide.core.Effect
import dev.azide.core.EventStream
import dev.azide.core.asAction
import dev.azide.core.executeEach
import dev.azide.core.executeEachOf
import dev.azide.core.holding
import dev.azide.core.impl.utils.LoopClosure
import dev.azide.core.sampleExternally
import dev.azide.core.startForever
import dev.azide.core.test_utils.TestEventStreamListener
import dev.azide.core.test_utils.TestTargetAction
import dev.azide.core.test_utils.TransactionTestUtils
import dev.azide.core.test_utils.event_stream.EventStreamTestUtils
import dev.azide.core.test_utils.executeForTesting
import dev.azide.core.test_utils.startForTestingCancellable
import dev.azide.core.test_utils.stimulateForTesting
import dev.azide.core.test_utils.subscribeForTesting
import dev.azide.core.test_utils.verifyDidNotPropagateNorExposesEmission
import dev.azide.core.test_utils.verifyDoesNotExposeEmission
import dev.azide.core.test_utils.verifyWasNotExecuted
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertIs

@Suppress("ClassName")
class EventStream_executeEach_misc_tests {
    @Test
    fun test_executeEach_looped1() {
        val targetAction = TestTargetAction.of(result = 10)

        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Action<Int>>()

        val subjectEffect = sourceEventStream.executeEach()

        val memoryCell = TransactionTestUtils.executeInsideTransaction {
            sourceEventStream.emit(targetAction).stimulateForTesting()

            EventStream.loopedInAction { loopedSubjectEventStream: EventStream<Int> ->
                Action.map2(
                    loopedSubjectEventStream.holding(0).asAction,
                    subjectEffect.startForever,
                ) { memoryCell: Cell<Int>, subjectEventStream: EventStream<Int> ->
                    LoopClosure(
                        result = memoryCell,
                        loopedValue = subjectEventStream,
                    )
                }
            }.executeForTesting()
        }

        assertEquals(
            expected = 10,
            actual = memoryCell.sampleExternally(),
        )
    }

    @Test
    fun test_executeEach_looped2() {
        val targetAction = TestTargetAction.of(result = 10)

        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Action<Int>>()

        val subjectEffect = sourceEventStream.executeEach()

        val memoryCell = TransactionTestUtils.executeInsideTransaction {
            sourceEventStream.emit(targetAction).stimulateForTesting()

            EventStream.loopedInAction { loopedSubjectEventStream: EventStream<Int> ->
                Action.map2(
                    subjectEffect.startForever,
                    loopedSubjectEventStream.holding(0).asAction,
                ) { subjectEventStream: EventStream<Int>, memoryCell: Cell<Int> ->
                    LoopClosure(
                        result = memoryCell,
                        loopedValue = subjectEventStream,
                    )
                }
            }.executeForTesting()
        }

        assertEquals(
            expected = 10,
            actual = memoryCell.sampleExternally(),
        )
    }


    @Test
    fun test_executeEach_start_cancelledInstantly_twice() {
        test_executeEach_start_cancelledInstantly(count = 2)
    }

    private fun test_executeEach_start_cancelledInstantly(count: Int) {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Action<Int>>()

        val subjectEffect = sourceEventStream.executeEach()

        val subjectEventStreamListener = TransactionTestUtils.executeInsideTransaction {
            val (subjectEventStream, subjectEffectHandle) = subjectEffect.startForTestingCancellable()
            val subjectEventStreamListener = subjectEventStream.subscribeForTesting()

            repeat(count) {
                subjectEffectHandle.cancel.executeForTesting()
            }

            subjectEventStreamListener.verifyDoesNotExposeEmission()

            subjectEventStreamListener
        }

        val targetAction = TestTargetAction.of(result = 10)

        TransactionTestUtils.executeInsideTransaction {
            sourceEventStream.emit(emittedEvent = targetAction).stimulateForTesting()

            subjectEventStreamListener.verifyDoesNotExposeEmission() // ...because the effect is cancelled
        }

        targetAction.verifyWasNotExecuted() // ...at any point

        subjectEventStreamListener.verifyDidNotPropagateNorExposesEmission() //  ...at any point / now
    }

    @Test
    fun test_executeEach_cancel_twiceSimultaneously() {
        test_executeEach_cancel_once(count = 2)
    }

    private fun test_executeEach_cancel_once(count: Int) {
        data class StartTransactionRecord(
            val subjectEventStreamListener: TestEventStreamListener<Int>,
            val subjectEffectHandle: Effect.Handle,
        )

        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Action<Int>>()

        val subjectEffect = sourceEventStream.executeEach()

        val startTransactionRecord = TransactionTestUtils.executeInsideTransaction {
            val (subjectEventStream, subjectEffectHandle) = subjectEffect.startForTestingCancellable()
            val subjectEventStreamListener = subjectEventStream.subscribeForTesting()

            StartTransactionRecord(
                subjectEventStreamListener = subjectEventStreamListener,
                subjectEffectHandle = subjectEffectHandle,
            )
        }

        val subjectEventStreamListener = startTransactionRecord.subjectEventStreamListener
        val subjectEffectHandle = startTransactionRecord.subjectEffectHandle

        TransactionTestUtils.executeInsideTransaction {
            repeat(count) {
                subjectEffectHandle.cancel.executeForTesting()
            }
        }

        val targetAction = TestTargetAction.of(result = 10)

        TransactionTestUtils.executeInsideTransaction {
            sourceEventStream.emit(emittedEvent = targetAction).stimulateForTesting()
        }

        subjectEventStreamListener.verifyDidNotPropagateNorExposesEmission() // ...at any point / now

        targetAction.verifyWasNotExecuted() // ...at any point
    }

    @Test
    fun test_executeEach_selfCancelling() {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Action<Int>>()

        val subjectEffect = sourceEventStream.executeEach()

        val subjectEffectOutcome = TransactionTestUtils.executeInsideTransaction {
            subjectEffect.start.executeForTesting()
        }

        val subjectEventStream = subjectEffectOutcome.result
        val subjectEffectHandle = subjectEffectOutcome.handle

        val nastyEffect = subjectEventStream.executeEachOf { _: Int ->
            // Attempt to cancel the effect in consequence of its own emission, which leads to a paradox
            subjectEffectHandle.cancel
        }

        TransactionTestUtils.executeInsideTransaction {
            nastyEffect.start.executeForTesting()
        }

        val targetAction = TestTargetAction.of(result = 10)

        assertIs<CausalLoopException>(
            assertFails {
                TransactionTestUtils.executeInsideTransaction {
                    sourceEventStream.emit(emittedEvent = targetAction).stimulateForTesting()
                }
            },
        )
    }
}
