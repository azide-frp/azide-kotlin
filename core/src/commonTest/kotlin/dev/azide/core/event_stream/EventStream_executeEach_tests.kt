package dev.azide.core.event_stream

import dev.azide.core.Action
import dev.azide.core.CausalLoopException
import dev.azide.core.Effect
import dev.azide.core.EventStream
import dev.azide.core.executeEach
import dev.azide.core.executeEachOf
import dev.azide.core.test_utils.TestEventStreamSubscriber
import dev.azide.core.test_utils.TestTargetAction
import dev.azide.core.test_utils.TransactionTestUtils
import dev.azide.core.test_utils.event_stream.EventStreamTestUtils
import dev.azide.core.test_utils.executeForTesting
import dev.azide.core.test_utils.executeForTestingRevocable
import dev.azide.core.test_utils.revokeForTesting
import dev.azide.core.test_utils.startForTesting
import dev.azide.core.test_utils.startForTestingCancellable
import dev.azide.core.test_utils.startForTestingRevocable
import dev.azide.core.test_utils.stimulateForTesting
import dev.azide.core.test_utils.subscribeForTesting
import dev.azide.core.test_utils.verifyDidNotPropagateNorExposesEmission
import dev.azide.core.test_utils.verifyDoesNotExposeEmission
import dev.azide.core.test_utils.verifyPropagatedAndExposesEmission
import dev.azide.core.test_utils.verifyPropagatedAndExposesRevocation
import dev.azide.core.test_utils.verifyWasExecutedOnce
import dev.azide.core.test_utils.verifyWasNotExecuted
import dev.azide.core.test_utils.verifyWasNotRevoked
import dev.azide.core.test_utils.verifyWasRevoked
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertIs

@Suppress("ClassName")
class EventStream_executeEach_tests {
    @Test
    fun test_executeEach_start() {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Action<Int>>()

        val subjectEffect = sourceEventStream.executeEach()

        val subjectEventStreamSubscriber = TransactionTestUtils.executeInsideTransaction {
            val subjectEventStream = subjectEffect.startForTesting()
            val subjectEventStreamSubscriber = subjectEventStream.subscribeForTesting()

            subjectEventStreamSubscriber.verifyDoesNotExposeEmission()

            subjectEventStreamSubscriber
        }

        subjectEventStreamSubscriber.verifyDidNotPropagateNorExposesEmission() //  ...at any point / now
    }

    @Test
    fun test_executeEach_start_cancelledInstantly_once() {
        test_executeEach_start_cancelledInstantly(count = 1)
    }

    @Test
    fun test_executeEach_start_cancelledInstantly_twice() {
        test_executeEach_start_cancelledInstantly(count = 2)
    }

    private fun test_executeEach_start_cancelledInstantly(count: Int) {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Action<Int>>()

        val subjectEffect = sourceEventStream.executeEach()

        val subjectEventStreamSubscriber = TransactionTestUtils.executeInsideTransaction {
            val (subjectEventStream, subjectEffectHandle) = subjectEffect.startForTestingCancellable()
            val subjectEventStreamSubscriber = subjectEventStream.subscribeForTesting()

            repeat(count) {
                subjectEffectHandle.cancel.executeForTesting()
            }

            subjectEventStreamSubscriber.verifyDoesNotExposeEmission()

            subjectEventStreamSubscriber
        }

        val targetAction = TestTargetAction.of(result = 10)

        TransactionTestUtils.executeInsideTransaction {
            sourceEventStream.emit(emittedEvent = targetAction).stimulateForTesting()

            subjectEventStreamSubscriber.verifyDoesNotExposeEmission() // ...because the effect is cancelled
        }

        targetAction.verifyWasNotExecuted() // ...at any point

        subjectEventStreamSubscriber.verifyDidNotPropagateNorExposesEmission() //  ...at any point / now
    }

    @Test
    fun test_executeEach_start_revokedInstantly() {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Action<Int>>()

        val subjectEffect = sourceEventStream.executeEach()

        TransactionTestUtils.executeInsideTransaction {
            val (_, revocable) = subjectEffect.startForTestingRevocable()

            revocable.revokeForTesting()
        }

        val targetAction = TestTargetAction.of(result = 10)

        TransactionTestUtils.executeInsideTransaction {
            sourceEventStream.emit(emittedEvent = targetAction).stimulateForTesting()
        }

        targetAction.verifyWasNotExecuted() // ...at any point
    }

    @Test
    fun test_executeEach_start_cancelledAndRevokedInstantly() {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Action<Int>>()

        val subjectEffect = sourceEventStream.executeEach()

        TransactionTestUtils.executeInsideTransaction {
            val (startActionOutcome, startRevocable) = subjectEffect.start.executeForTestingRevocable()

            val subjectEffectHandle = startActionOutcome.handle

            val (_, cancelRevocable) = subjectEffectHandle.cancel.executeForTestingRevocable()

            startRevocable.revokeForTesting()

            cancelRevocable.revokeForTesting()
        }

        val targetAction = TestTargetAction.of(result = 10)

        TransactionTestUtils.executeInsideTransaction {
            sourceEventStream.emit(emittedEvent = targetAction).stimulateForTesting()
        }

        targetAction.verifyWasNotExecuted() // ...at any point
    }

    @Test
    fun test_executeEach_start_sourceEmitsSimultaneously() {
        data class StartTransactionRecord(
            val subjectEventStreamSubscriber: TestEventStreamSubscriber<Int>,
            val targetActionExecutionRecord: TestTargetAction.ExecutionRecord<Int>,
        )

        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Action<Int>>()

        val targetAction = TestTargetAction.of(result = 10)

        val subjectEffect = sourceEventStream.executeEach()

        val transactionRecord = TransactionTestUtils.executeInsideTransaction {
            val subjectEventStream = subjectEffect.startForTesting()
            val subjectEventStreamSubscriber = subjectEventStream.subscribeForTesting()

            sourceEventStream.emit(
                emittedEvent = targetAction,
            ).stimulateForTesting()

            val testTargetActionExecutionRecord = targetAction.verifyWasExecutedOnce()

            subjectEventStreamSubscriber.verifyPropagatedAndExposesEmission(
                expectedEmittedEvent = 10,
            )

            StartTransactionRecord(
                subjectEventStreamSubscriber = subjectEventStreamSubscriber,
                targetActionExecutionRecord = testTargetActionExecutionRecord,
            )
        }

        transactionRecord.subjectEventStreamSubscriber.verifyDidNotPropagateNorExposesEmission() //  ...again / now

        transactionRecord.targetActionExecutionRecord.verifyWasNotRevoked() // ...at any point
    }

    @Test
    fun test_executeEach_start_sourceEmitsSimultaneously_cancelledInstantly() {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Action<Int>>()

        val subjectEffect = sourceEventStream.executeEach()

        val targetAction1 = TestTargetAction.of(result = 10)

        val subjectEventStreamSubscriber = TransactionTestUtils.executeInsideTransaction {
            val (subjectEventStream, subjectEffectHandle) = subjectEffect.startForTestingCancellable()
            val subjectEventStreamSubscriber = subjectEventStream.subscribeForTesting()

            sourceEventStream.emit(emittedEvent = targetAction1).stimulateForTesting()

            val targetActionExecutionRecord = targetAction1.verifyWasExecutedOnce().apply {
                verifyWasNotRevoked()
            }

            subjectEventStreamSubscriber.verifyPropagatedAndExposesEmission(expectedEmittedEvent = 10)

            subjectEffectHandle.cancel.executeForTesting()

            targetActionExecutionRecord.verifyWasRevoked()
            subjectEventStreamSubscriber.verifyPropagatedAndExposesRevocation()

            subjectEventStreamSubscriber
        }

        val targetAction2 = TestTargetAction.of(result = 20)

        TransactionTestUtils.executeInsideTransaction {
            sourceEventStream.emit(emittedEvent = targetAction2).stimulateForTesting()

            subjectEventStreamSubscriber.verifyDoesNotExposeEmission() // ...because the effect is cancelled
        }

        targetAction2.verifyWasNotExecuted() // ...at any point

        subjectEventStreamSubscriber.verifyDidNotPropagateNorExposesEmission() //  ...at any point / now
    }

    @Test
    fun test_executeEach_start_sourceEmitsSimultaneously_revokedInstantly() {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Action<Int>>()

        val targetAction1 = TestTargetAction.of(result = 10)

        val subjectEffect = sourceEventStream.executeEach()

        TransactionTestUtils.executeInsideTransaction {
            val (_: EventStream<Int>, startRevocable) = subjectEffect.startForTestingRevocable()

            sourceEventStream.emit(
                emittedEvent = targetAction1,
            ).stimulateForTesting()

            val testTargetActionExecutionRecord = targetAction1.verifyWasExecutedOnce().apply {
                verifyWasNotRevoked()
            }

            startRevocable.revokeForTesting()

            testTargetActionExecutionRecord.verifyWasRevoked()
        }

        val targetAction2 = TestTargetAction.of(result = 20)

        TransactionTestUtils.executeInsideTransaction {
            sourceEventStream.emit(emittedEvent = targetAction2).stimulateForTesting()
        }

        targetAction2.verifyWasNotExecuted() // ...at any point
    }

    @Test
    fun test_executeEach_sourceEmits() {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Action<Int>>()

        val subjectEffect = sourceEventStream.executeEach()

        val subjectEventStreamSubscriber = TransactionTestUtils.executeInsideTransaction {
            val subjectEventStream = subjectEffect.startForTesting()
            val subjectEventStreamSubscriber = subjectEventStream.subscribeForTesting()

            subjectEventStreamSubscriber.verifyDoesNotExposeEmission()

            subjectEventStreamSubscriber
        }

        val targetAction = TestTargetAction.of(result = 10)

        val targetActionExecutionRecord = TransactionTestUtils.executeInsideTransaction {
            sourceEventStream.emit(emittedEvent = targetAction).stimulateForTesting()

            val targetActionExecutionRecord = targetAction.verifyWasExecutedOnce()

            subjectEventStreamSubscriber.verifyPropagatedAndExposesEmission(
                expectedEmittedEvent = 10,
            )

            targetActionExecutionRecord
        }

        targetAction.verifyWasNotExecuted() // ...again

        subjectEventStreamSubscriber.verifyDidNotPropagateNorExposesEmission() //  ...again / now

        targetActionExecutionRecord.verifyWasNotRevoked() // ...at any point
    }

    @Test
    fun test_executeEach_sourceEmitsAndRevokes() {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Action<Int>>()

        val subjectEffect = sourceEventStream.executeEach()

        val subjectEventStreamSubscriber = TransactionTestUtils.executeInsideTransaction {
            val subjectEventStream = subjectEffect.startForTesting()
            val subjectEventStreamSubscriber = subjectEventStream.subscribeForTesting()

            subjectEventStreamSubscriber.verifyDoesNotExposeEmission()

            subjectEventStreamSubscriber
        }

        val targetAction = TestTargetAction.of(result = 10)

        TransactionTestUtils.executeInsideTransaction {
            sourceEventStream.emit(emittedEvent = targetAction).stimulateForTesting()

            val targetActionExecutionRecord = targetAction.verifyWasExecutedOnce()

            subjectEventStreamSubscriber.verifyPropagatedAndExposesEmission(
                expectedEmittedEvent = 10,
            )

            sourceEventStream.revokeEmission().stimulateForTesting()

            targetActionExecutionRecord.verifyWasRevoked()

            subjectEventStreamSubscriber.verifyPropagatedAndExposesRevocation()
        }

        targetAction.verifyWasNotExecuted() // ...again

        subjectEventStreamSubscriber.verifyDidNotPropagateNorExposesEmission() //  ...again / now
    }

    @Test
    fun test_executeEach_sourceEmitsAndCorrects() {
        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Action<Int>>()

        val subjectEffect = sourceEventStream.executeEach()

        val subjectEventStreamSubscriber = TransactionTestUtils.executeInsideTransaction {
            val subjectEventStream = subjectEffect.startForTesting()
            val subjectEventStreamSubscriber = subjectEventStream.subscribeForTesting()

            subjectEventStreamSubscriber.verifyDoesNotExposeEmission()

            subjectEventStreamSubscriber
        }

        val targetAction1 = TestTargetAction.of(result = 10)
        val targetAction2 = TestTargetAction.of(result = 20)

        val targetActionCorrectedExecutionRecord = TransactionTestUtils.executeInsideTransaction {
            sourceEventStream.emit(
                emittedEvent = targetAction1,
            ).stimulateForTesting()

            val targetActionInitialExecutionRecord = targetAction1.verifyWasExecutedOnce()

            subjectEventStreamSubscriber.verifyPropagatedAndExposesEmission(
                expectedEmittedEvent = 10,
            )

            sourceEventStream.correctEmission(
                correctedEmittedEvent = targetAction2,
            ).stimulateForTesting()

            targetActionInitialExecutionRecord.verifyWasRevoked()

            val targetActionCorrectedExecutionRecord = targetAction2.verifyWasExecutedOnce()

            subjectEventStreamSubscriber.verifyPropagatedAndExposesEmission(
                expectedEmittedEvent = 20,
            )

            targetActionCorrectedExecutionRecord
        }

        targetAction1.verifyWasNotExecuted() // ...again
        targetAction2.verifyWasNotExecuted() // ...again

        targetActionCorrectedExecutionRecord.verifyWasNotRevoked() // ...at any point

        subjectEventStreamSubscriber.verifyDidNotPropagateNorExposesEmission() //  ...again / now
    }

    @Test
    fun test_executeEach_sourceEmits_cancelledSimultaneously() {
        data class StartTransactionRecord(
            val subjectEventStreamSubscriber: TestEventStreamSubscriber<Int>,
            val subjectEffectHandle: Effect.Handle,
        )

        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Action<Int>>()

        val subjectEffect = sourceEventStream.executeEach()

        val startTransactionRecord = TransactionTestUtils.executeInsideTransaction {
            val (subjectEventStream, subjectEffectHandle) = subjectEffect.startForTestingCancellable()
            val subjectEventStreamSubscriber = subjectEventStream.subscribeForTesting()

            subjectEventStreamSubscriber.verifyDoesNotExposeEmission()

            StartTransactionRecord(
                subjectEventStreamSubscriber = subjectEventStreamSubscriber,
                subjectEffectHandle = subjectEffectHandle,
            )
        }

        val subjectEventStreamSubscriber = startTransactionRecord.subjectEventStreamSubscriber
        val subjectEffectHandle = startTransactionRecord.subjectEffectHandle

        val targetAction = TestTargetAction.of(result = 10)

        TransactionTestUtils.executeInsideTransaction {
            sourceEventStream.emit(emittedEvent = targetAction).stimulateForTesting()

            val targetActionExecutionRecord = targetAction.verifyWasExecutedOnce().apply {
                verifyWasNotRevoked()
            }

            subjectEventStreamSubscriber.verifyPropagatedAndExposesEmission(
                expectedEmittedEvent = 10,
            )

            subjectEffectHandle.cancel.executeForTesting()

            targetActionExecutionRecord.verifyWasRevoked()

            subjectEventStreamSubscriber.verifyPropagatedAndExposesRevocation()
        }

        targetAction.verifyWasNotExecuted() // ...again

        subjectEventStreamSubscriber.verifyDidNotPropagateNorExposesEmission() //  ...again / now
    }

    @Test
    fun test_executeEach_cancel_once() {
        test_executeEach_cancel_once(count = 1)
    }

    @Test
    fun test_executeEach_cancel_twiceSimultaneously() {
        test_executeEach_cancel_once(count = 2)
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

    private fun test_executeEach_cancel_once(count: Int) {
        data class StartTransactionRecord(
            val subjectEventStreamSubscriber: TestEventStreamSubscriber<Int>,
            val subjectEffectHandle: Effect.Handle,
        )

        val sourceEventStream = EventStreamTestUtils.createInputEventStream<Action<Int>>()

        val subjectEffect = sourceEventStream.executeEach()

        val startTransactionRecord = TransactionTestUtils.executeInsideTransaction {
            val (subjectEventStream, subjectEffectHandle) = subjectEffect.startForTestingCancellable()
            val subjectEventStreamSubscriber = subjectEventStream.subscribeForTesting()

            StartTransactionRecord(
                subjectEventStreamSubscriber = subjectEventStreamSubscriber,
                subjectEffectHandle = subjectEffectHandle,
            )
        }

        val subjectEventStreamSubscriber = startTransactionRecord.subjectEventStreamSubscriber
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

        subjectEventStreamSubscriber.verifyDidNotPropagateNorExposesEmission() // ...at any point / now

        targetAction.verifyWasNotExecuted() // ...at any point
    }
}
