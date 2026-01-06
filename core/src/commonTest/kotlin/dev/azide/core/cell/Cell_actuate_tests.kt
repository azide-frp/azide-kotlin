package dev.azide.core.cell

import dev.azide.core.Cell
import dev.azide.core.Effect
import dev.azide.core.actuate
import dev.azide.core.internal.RevocationHandle
import dev.azide.core.test_utils.TestCellObserver
import dev.azide.core.test_utils.TestTargetAction
import dev.azide.core.test_utils.TestTargetEffect
import dev.azide.core.test_utils.TransactionTestUtils
import dev.azide.core.test_utils.cell.CellTestUtils
import dev.azide.core.test_utils.executeForTesting
import dev.azide.core.test_utils.executeForTestingRevocable
import dev.azide.core.test_utils.observeForTesting
import dev.azide.core.test_utils.revokeForTesting
import dev.azide.core.test_utils.startForTesting
import dev.azide.core.test_utils.startForTestingCancellable
import dev.azide.core.test_utils.startForTestingRevocable
import dev.azide.core.test_utils.stimulateForTesting
import dev.azide.core.test_utils.verifyDidNotPropagateNorExposesUpdate
import dev.azide.core.test_utils.verifyDoesNotExposeUpdate
import dev.azide.core.test_utils.verifyOldValue
import dev.azide.core.test_utils.verifyOldValueInsideTransaction
import dev.azide.core.test_utils.verifyPropagatedAndExposesRevocation
import dev.azide.core.test_utils.verifyPropagatedAndExposesUpdate
import dev.azide.core.test_utils.verifyWasCancelledOnce
import dev.azide.core.test_utils.verifyWasNotCancelled
import dev.azide.core.test_utils.verifyWasNotRevoked
import dev.azide.core.test_utils.verifyWasNotStarted
import dev.azide.core.test_utils.verifyWasRevoked
import dev.azide.core.test_utils.verifyWasStartedOnce
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("ClassName")
class Cell_actuate_tests {
    @Test
    fun test_actuate_start() {
        val targetEffect = TestTargetEffect.pure(result = 10)

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = targetEffect,
        )

        val subjectEffect = sourceCell.actuate()

        val (subjectCellObserver, targetEffectStartExecutionRecord) = TransactionTestUtils.executeInsideTransaction {
            val subjectCell = subjectEffect.startForTesting()
            val subjectCellObserver = subjectCell.observeForTesting()

            val targetEffectStartExecutionRecord = targetEffect.verifyWasStartedOnce()
            val targetEffectOutcome = targetEffectStartExecutionRecord.result

            assertEquals(
                expected = 10,
                actual = targetEffectOutcome.result,
            )

            subjectCellObserver.verifyDoesNotExposeUpdate()

            subjectCellObserver.verifyOldValue(expectedOldValue = 10) // ...inside the spawn transaction

            Pair(subjectCellObserver, targetEffectStartExecutionRecord)
        }

        subjectCellObserver.verifyDidNotPropagateNorExposesUpdate() //  ...at any point / now
        subjectCellObserver.verifyOldValueInsideTransaction(expectedOldValue = 10)  // ...after the spawn transaction

        targetEffect.verifyWasNotStarted() // ...again
        targetEffectStartExecutionRecord.verifyWasNotRevoked() // ...at any point

        val targetEffectOutcome = targetEffectStartExecutionRecord.result
        targetEffectOutcome.verifyWasNotCancelled() // ...at any point
    }

    @Test
    fun test_actuate_start_cancelledInstantly_once() {
        test_actuate_start_cancelledInstantly(count = 1)
    }

    @Test
    fun test_actuate_start_cancelledInstantly_twice() {
        test_actuate_start_cancelledInstantly(count = 2)
    }

    private fun test_actuate_start_cancelledInstantly(count: Int) {
        data class StartTransactionRecord(
            val subjectCellObserver: TestCellObserver<Int>,
            val targetEffectStartExecutionRecord: TestTargetAction.ExecutionRecord<TestTargetEffect.Outcome<Int>>,
            val targetEffectCancelExecutionRecord: TestTargetAction.ExecutionRecord<Unit>,
        )

        val targetEffect = TestTargetEffect.pure(result = 10)

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = targetEffect,
        )

        val subjectEffect = sourceCell.actuate()

        val transactionRecord = TransactionTestUtils.executeInsideTransaction {
            val (subjectCell, subjectEffectHandle) = subjectEffect.startForTestingCancellable()
            val subjectCellObserver = subjectCell.observeForTesting()

            val targetEffectStartExecutionRecord = targetEffect.verifyWasStartedOnce()
            val targetEffectOutcome = targetEffectStartExecutionRecord.result

            repeat(count) {
                subjectEffectHandle.cancel.executeForTesting()
            }

            val targetEffectCancelExecutionRecord = targetEffectOutcome.verifyWasCancelledOnce()

            subjectCellObserver.verifyDoesNotExposeUpdate() // ...inside the spawn transaction

            subjectCellObserver.verifyOldValue(expectedOldValue = 10) // ...inside the spawn transaction

            StartTransactionRecord(
                subjectCellObserver = subjectCellObserver,
                targetEffectStartExecutionRecord = targetEffectStartExecutionRecord,
                targetEffectCancelExecutionRecord = targetEffectCancelExecutionRecord,
            )
        }

        transactionRecord.subjectCellObserver.verifyDidNotPropagateNorExposesUpdate() //  ...at any point / now
        transactionRecord.subjectCellObserver.verifyOldValueInsideTransaction(expectedOldValue = 10)  // ...after the spawn transaction

        targetEffect.verifyWasNotStarted() // ...again

        transactionRecord.targetEffectStartExecutionRecord.verifyWasNotRevoked() // ...at any point

        val targetEffectOutcome = transactionRecord.targetEffectStartExecutionRecord.result
        targetEffectOutcome.verifyWasNotCancelled() // ...again

        transactionRecord.targetEffectCancelExecutionRecord.verifyWasNotRevoked() // ...at any point
    }

    @Test
    fun test_actuate_start_revokedInstantly() {
        val targetEffect = TestTargetEffect.pure(result = 10)

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = targetEffect,
        )

        val subjectEffect = sourceCell.actuate()

        val targetEffectStartExecutionRecord = TransactionTestUtils.executeInsideTransaction {
            val (_: Cell<Int>, startRevocationHandle: RevocationHandle) = subjectEffect.startForTestingRevocable()

            val targetEffectStartExecutionRecord = targetEffect.verifyWasStartedOnce()

            startRevocationHandle.revokeForTesting()

            targetEffectStartExecutionRecord.verifyWasRevoked()

            targetEffectStartExecutionRecord
        }

        targetEffect.verifyWasNotStarted() // ...again

        val targetEffectOutcome = targetEffectStartExecutionRecord.result
        targetEffectOutcome.verifyWasNotCancelled() // ...at any point
    }

    @Test
    fun test_actuate_start_cancelledAndRevokedInstantly() {
        val targetEffect = TestTargetEffect.pure(result = 10)

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = targetEffect,
        )

        val subjectEffect = sourceCell.actuate()

        TransactionTestUtils.executeInsideTransaction {
            val (startActionOutcome, startRevocationHandle) = subjectEffect.start.executeForTestingRevocable()

            val subjectEffectHandle = startActionOutcome.handle

            val targetEffectStartExecutionRecord = targetEffect.verifyWasStartedOnce()
            val targetEffectOutcome = targetEffectStartExecutionRecord.result

            val (_, cancelRevocationHandle) = subjectEffectHandle.cancel.executeForTestingRevocable()

            val targetEffectCancelExecutionRecord = targetEffectOutcome.verifyWasCancelledOnce()

            startRevocationHandle.revokeForTesting()

            targetEffectStartExecutionRecord.verifyWasRevoked()

            // Revocation of the effect's start action must be followed by the revocation of all cancellations; the
            // operator itself does not track the subsequent executions of the inner actions of the entity it
            // constructed. In the real system, a chain of message revocations might happen between the start action
            // revocation and the cancellation revocation.
            cancelRevocationHandle.revokeForTesting()

            targetEffectCancelExecutionRecord.verifyWasRevoked()
        }

        targetEffect.verifyWasNotStarted() // ...again
    }

    @Test
    fun test_actuate_start_sourceUpdatesSimultaneously() {
        data class StartTransactionRecord(
            val subjectCellObserver: TestCellObserver<Int>,
            val targetEffect1StartExecutionRecord: TestTargetAction.ExecutionRecord<TestTargetEffect.Outcome<Int>>,
            val targetEffect1CancelExecutionRecord: TestTargetAction.ExecutionRecord<Unit>,
            val targetEffect2StartExecutionRecord: TestTargetAction.ExecutionRecord<TestTargetEffect.Outcome<Int>>,
        )

        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect = sourceCell.actuate()

        val transactionRecord = TransactionTestUtils.executeInsideTransaction {
            val subjectCell = subjectEffect.startForTesting()
            val subjectCellObserver = subjectCell.observeForTesting()

            val targetEffect1StartExecutionRecord = targetEffect1.verifyWasStartedOnce()
            val targetEffect1Outcome = targetEffect1StartExecutionRecord.result

            sourceCell.update(
                newValue = targetEffect2,
            ).stimulateForTesting()

            val targetEffect1CancelExecutionRecord = targetEffect1Outcome.verifyWasCancelledOnce()
            val targetEffect2StartExecutionRecord = targetEffect2.verifyWasStartedOnce()

            subjectCellObserver.verifyPropagatedAndExposesUpdate(
                expectedUpdatedValue = 20,
            )

            subjectCellObserver.verifyOldValue(expectedOldValue = 10) // ...inside the spawn transaction

            StartTransactionRecord(
                subjectCellObserver = subjectCellObserver,
                targetEffect1StartExecutionRecord = targetEffect1StartExecutionRecord,
                targetEffect1CancelExecutionRecord = targetEffect1CancelExecutionRecord,
                targetEffect2StartExecutionRecord = targetEffect2StartExecutionRecord,
            )
        }

        transactionRecord.subjectCellObserver.verifyDidNotPropagateNorExposesUpdate() // ...again / now
        transactionRecord.subjectCellObserver.verifyOldValueInsideTransaction(expectedOldValue = 20)  // ...after the spawn transaction

        targetEffect1.verifyWasNotStarted() // ...again

        transactionRecord.targetEffect1StartExecutionRecord.verifyWasNotRevoked() // ...at any point

        val targetEffect1Outcome = transactionRecord.targetEffect1StartExecutionRecord.result
        targetEffect1Outcome.verifyWasNotCancelled() // ...again

        transactionRecord.targetEffect1CancelExecutionRecord.verifyWasNotRevoked() // ...at any point

        targetEffect2.verifyWasNotStarted() // ...again

        transactionRecord.targetEffect2StartExecutionRecord.verifyWasNotRevoked() // ...at any point

        val targetEffect2Outcome = transactionRecord.targetEffect2StartExecutionRecord.result
        targetEffect2Outcome.verifyWasNotCancelled() // ...at any point
    }

    @Test
    @Ignore // FIXME: Make this pass
    fun test_actuate_start_sourceUpdatesSimultaneously_cancelledInstantly() {
        data class StartTransactionRecord(
            val subjectCellObserver: TestCellObserver<Int>,
            val targetEffect1StartExecutionRecord: TestTargetAction.ExecutionRecord<TestTargetEffect.Outcome<Int>>,
            val targetEffect1SubsequentCancelExecutionRecord: TestTargetAction.ExecutionRecord<Unit>,
        ) {
            val targetEffect1Outcome: TestTargetEffect.Outcome<Int>
                get() = targetEffect1StartExecutionRecord.result
        }

        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect = sourceCell.actuate()

        val transactionRecord = TransactionTestUtils.executeInsideTransaction {
            val (subjectCell: Cell<Int>, subjectEffectHandle: Effect.Handle) = subjectEffect.startForTestingCancellable()
            val subjectCellObserver = subjectCell.observeForTesting()

            val targetEffect1StartExecutionRecord = targetEffect1.verifyWasStartedOnce()
            val targetEffect1Outcome = targetEffect1StartExecutionRecord.result

            sourceCell.update(
                newValue = targetEffect2,
            ).stimulateForTesting()

            subjectCellObserver.verifyPropagatedAndExposesUpdate(expectedUpdatedValue = 20)

            val targetEffect1InitialCancelExecutionRecord = targetEffect1Outcome.verifyWasCancelledOnce().apply {
                verifyWasNotRevoked()
            }

            val targetEffect2StartExecutionRecord = targetEffect2.verifyWasStartedOnce().apply {
                verifyWasNotRevoked()
            }

            subjectEffectHandle.cancel.executeForTesting()

            targetEffect1InitialCancelExecutionRecord.verifyWasRevoked()
            targetEffect2StartExecutionRecord.verifyWasRevoked()

            val targetEffect1SubsequentCancelExecutionRecord = targetEffect1Outcome.verifyWasCancelledOnce()

            subjectCellObserver.verifyPropagatedAndExposesRevocation()

            StartTransactionRecord(
                subjectCellObserver = subjectCellObserver,
                targetEffect1StartExecutionRecord = targetEffect1StartExecutionRecord,
                targetEffect1SubsequentCancelExecutionRecord = targetEffect1SubsequentCancelExecutionRecord,
            )
        }

        val subjectCellObserver = transactionRecord.subjectCellObserver
        val targetEffect1StartExecutionRecord = transactionRecord.targetEffect1StartExecutionRecord
        val targetEffect1Outcome = transactionRecord.targetEffect1Outcome

        targetEffect1.verifyWasNotStarted() // ...again
        targetEffect2.verifyWasNotStarted() // ...again

        targetEffect1StartExecutionRecord.verifyWasNotRevoked()

        targetEffect1Outcome.verifyWasNotCancelled() // ...again

        subjectCellObserver.verifyOldValueInsideTransaction(expectedOldValue = 10)

    }

    @Test
    fun test_actuate_start_sourceUpdatesSimultaneously_revokedInstantly() {
        data class StartTransactionRecord(
            val targetEffect1Outcome: TestTargetEffect.Outcome<Int>,
            val targetEffect2Outcome: TestTargetEffect.Outcome<Int>,
        )

        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect = sourceCell.actuate()

        val transactionRecord = TransactionTestUtils.executeInsideTransaction {
            val (_: Cell<Int>, startRevocationHandle: RevocationHandle) = subjectEffect.startForTestingRevocable()

            val targetEffect1StartExecutionRecord = targetEffect1.verifyWasStartedOnce()
            val targetEffect1Outcome = targetEffect1StartExecutionRecord.result

            sourceCell.update(
                newValue = targetEffect2,
            ).stimulateForTesting()

            val targetEffect1CancelExecutionRecord = targetEffect1Outcome.verifyWasCancelledOnce()
            val targetEffect2StartExecutionRecord = targetEffect2.verifyWasStartedOnce()

            startRevocationHandle.revokeForTesting()

            targetEffect1StartExecutionRecord.verifyWasRevoked()
            targetEffect1CancelExecutionRecord.verifyWasRevoked()
            targetEffect2StartExecutionRecord.verifyWasRevoked()

            StartTransactionRecord(
                targetEffect1Outcome = targetEffect1StartExecutionRecord.result,
                targetEffect2Outcome = targetEffect2StartExecutionRecord.result,
            )
        }

        targetEffect1.verifyWasNotStarted() // ...again

        transactionRecord.targetEffect1Outcome.verifyWasNotCancelled() // ...again

        targetEffect2.verifyWasNotStarted() // ...again

        transactionRecord.targetEffect2Outcome.verifyWasNotCancelled() // ...at any point
    }

    @Test
    fun test_actuate_sourceUpdates() {
        data class StartTransactionRecord(
            val subjectCellObserver: TestCellObserver<Int>,
            val targetEffect1StartExecutionRecord: TestTargetAction.ExecutionRecord<TestTargetEffect.Outcome<Int>>,
        ) {
            val targetEffect1Outcome: TestTargetEffect.Outcome<Int>
                get() = targetEffect1StartExecutionRecord.result
        }

        data class LaterTransactionRecord(
            val targetEffect1CancelExecutionRecord: TestTargetAction.ExecutionRecord<Unit>,
            val targetEffect2StartExecutionRecord: TestTargetAction.ExecutionRecord<TestTargetEffect.Outcome<Int>>,
        ) {
            val targetEffect2Outcome: TestTargetEffect.Outcome<Int>
                get() = targetEffect2StartExecutionRecord.result
        }

        val targetEffect1 = TestTargetEffect.pure(result = 10)

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect = sourceCell.actuate()

        val startTransactionRecord = TransactionTestUtils.executeInsideTransaction {
            val subjectCell = subjectEffect.startForTesting()
            val subjectCellObserver = subjectCell.observeForTesting()

            val targetEffect1StartExecutionRecord = targetEffect1.verifyWasStartedOnce()

            StartTransactionRecord(
                subjectCellObserver = subjectCellObserver,
                targetEffect1StartExecutionRecord = targetEffect1StartExecutionRecord,
            )
        }

        startTransactionRecord.targetEffect1StartExecutionRecord.verifyWasNotRevoked()

        val subjectCellObserver = startTransactionRecord.subjectCellObserver
        val targetEffect1Outcome = startTransactionRecord.targetEffect1Outcome

        targetEffect1Outcome.verifyWasNotCancelled()

        subjectCellObserver.verifyDidNotPropagateNorExposesUpdate() // ...at any point until now / now

        val targetEffect2 = TestTargetEffect.pure(result = 20)

        val laterTransactionRecord = TransactionTestUtils.executeInsideTransaction {
            sourceCell.update(
                newValue = targetEffect2,
            ).stimulateForTesting()

            val targetEffect1CancelExecutionRecord = targetEffect1Outcome.verifyWasCancelledOnce()
            val targetEffect2StartExecutionRecord = targetEffect2.verifyWasStartedOnce()

            subjectCellObserver.verifyPropagatedAndExposesUpdate(expectedUpdatedValue = 20)

            LaterTransactionRecord(
                targetEffect1CancelExecutionRecord = targetEffect1CancelExecutionRecord,
                targetEffect2StartExecutionRecord = targetEffect2StartExecutionRecord,
            )
        }

        val targetEffect2Outcome = laterTransactionRecord.targetEffect2Outcome

        targetEffect1Outcome.verifyWasNotCancelled() // ...again
        targetEffect2Outcome.verifyWasNotCancelled() // ...at all

        targetEffect1.verifyWasNotStarted() // ...again
        targetEffect2.verifyWasNotStarted() // ...again

        subjectCellObserver.verifyDidNotPropagateNorExposesUpdate() // ...again / now
    }

    @Test
    fun test_actuate_sourceUpdatesAndRevokes() {
        data class StartTransactionRecord(
            val subjectCellObserver: TestCellObserver<Int>,
            val targetEffect1StartExecutionRecord: TestTargetAction.ExecutionRecord<TestTargetEffect.Outcome<Int>>,
        ) {
            val targetEffect1Outcome: TestTargetEffect.Outcome<Int>
                get() = targetEffect1StartExecutionRecord.result
        }

        val targetEffect1 = TestTargetEffect.pure(result = 10)

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect = sourceCell.actuate()

        val startTransactionRecord = TransactionTestUtils.executeInsideTransaction {
            val subjectCell = subjectEffect.startForTesting()
            val subjectCellObserver = subjectCell.observeForTesting()

            val targetEffect1StartExecutionRecord = targetEffect1.verifyWasStartedOnce()

            StartTransactionRecord(
                subjectCellObserver = subjectCellObserver,
                targetEffect1StartExecutionRecord = targetEffect1StartExecutionRecord,
            )
        }

        startTransactionRecord.targetEffect1StartExecutionRecord.verifyWasNotRevoked()

        val subjectCellObserver = startTransactionRecord.subjectCellObserver
        val targetEffect1Outcome = startTransactionRecord.targetEffect1Outcome

        targetEffect1Outcome.verifyWasNotCancelled()

        subjectCellObserver.verifyDidNotPropagateNorExposesUpdate() // ...at any point until now / now

        val targetEffect2 = TestTargetEffect.pure(result = 20)

        TransactionTestUtils.executeInsideTransaction {
            sourceCell.update(
                newValue = targetEffect2,
            ).stimulateForTesting()

            val targetEffect1CancelExecutionRecord = targetEffect1Outcome.verifyWasCancelledOnce()
            val targetEffect2StartExecutionRecord = targetEffect2.verifyWasStartedOnce()

            subjectCellObserver.verifyPropagatedAndExposesUpdate(
                expectedUpdatedValue = 20,
            )

            sourceCell.revokeUpdate().stimulateForTesting()

            targetEffect1CancelExecutionRecord.verifyWasRevoked()
            targetEffect2StartExecutionRecord.verifyWasRevoked()

            subjectCellObserver.verifyPropagatedAndExposesRevocation()
        }

        targetEffect1Outcome.verifyWasNotCancelled() // ...again

        targetEffect1.verifyWasNotStarted() // ...again
        targetEffect2.verifyWasNotStarted() // ...again
    }

    @Test
    fun test_actuate_sourceUpdatesAndCorrects() {
        data class StartTransactionRecord(
            val subjectCellObserver: TestCellObserver<Int>,
            val targetEffect1StartExecutionRecord: TestTargetAction.ExecutionRecord<TestTargetEffect.Outcome<Int>>,
        ) {
            val targetEffect1Outcome: TestTargetEffect.Outcome<Int>
                get() = targetEffect1StartExecutionRecord.result
        }

        data class LaterTransactionRecord(
            val targetEffect1SubsequentCancelExecutionRecord: TestTargetAction.ExecutionRecord<Unit>,
            val targetEffect3StartExecutionRecord: TestTargetAction.ExecutionRecord<TestTargetEffect.Outcome<Int>>,
        ) {
            val targetEffect3Outcome: TestTargetEffect.Outcome<Int>
                get() = targetEffect3StartExecutionRecord.result
        }

        val targetEffect1 = TestTargetEffect.pure(result = 10)

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect = sourceCell.actuate()

        val startTransactionRecord = TransactionTestUtils.executeInsideTransaction {
            val subjectCell = subjectEffect.startForTesting()
            val subjectCellObserver = subjectCell.observeForTesting()

            val targetEffect1StartExecutionRecord = targetEffect1.verifyWasStartedOnce()

            StartTransactionRecord(
                subjectCellObserver = subjectCellObserver,
                targetEffect1StartExecutionRecord = targetEffect1StartExecutionRecord,
            )
        }

        startTransactionRecord.targetEffect1StartExecutionRecord.verifyWasNotRevoked()

        val subjectCellObserver = startTransactionRecord.subjectCellObserver
        val targetEffect1Outcome = startTransactionRecord.targetEffect1Outcome

        targetEffect1Outcome.verifyWasNotCancelled()

        subjectCellObserver.verifyDidNotPropagateNorExposesUpdate() // ...at any point until now / now

        val targetEffect2 = TestTargetEffect.pure(result = 20)
        val targetEffect3 = TestTargetEffect.pure(result = 30)

        val laterTransactionRecord = TransactionTestUtils.executeInsideTransaction {
            sourceCell.update(
                newValue = targetEffect2,
            ).stimulateForTesting()

            val targetEffect1InitialCancelExecutionRecord = targetEffect1Outcome.verifyWasCancelledOnce()
            val targetEffect2StartExecutionRecord = targetEffect2.verifyWasStartedOnce()

            subjectCellObserver.verifyPropagatedAndExposesUpdate(expectedUpdatedValue = 20)

            sourceCell.correctUpdate(
                correctedNewValue = targetEffect3,
            ).stimulateForTesting()

            targetEffect1InitialCancelExecutionRecord.verifyWasRevoked()
            targetEffect2StartExecutionRecord.verifyWasRevoked()

            val targetEffect1SubsequentCancelExecutionRecord = targetEffect1Outcome.verifyWasCancelledOnce()
            val targetEffect3StartExecutionRecord = targetEffect3.verifyWasStartedOnce()

            subjectCellObserver.verifyPropagatedAndExposesUpdate(expectedUpdatedValue = 30)

            LaterTransactionRecord(
                targetEffect1SubsequentCancelExecutionRecord = targetEffect1SubsequentCancelExecutionRecord,
                targetEffect3StartExecutionRecord = targetEffect3StartExecutionRecord,
            )
        }

        val targetEffect1SubsequentCancelExecutionRecord =
            laterTransactionRecord.targetEffect1SubsequentCancelExecutionRecord
        val targetEffect3Outcome = laterTransactionRecord.targetEffect3Outcome

        targetEffect1SubsequentCancelExecutionRecord.verifyWasNotRevoked() // ...at all

        targetEffect1Outcome.verifyWasNotCancelled() // ...again
        targetEffect3Outcome.verifyWasNotCancelled() // ...at all

        targetEffect1.verifyWasNotStarted() // ...again
        targetEffect2.verifyWasNotStarted() // ...again

        subjectCellObserver.verifyDidNotPropagateNorExposesUpdate() // ...again / now
    }

    @Test
    @Ignore // FIXME: Make this pass
    fun test_actuate_sourceUpdates_cancelledSimultaneously() {
        data class StartTransactionRecord(
            val subjectCellObserver: TestCellObserver<Int>,
            val subjectEffectHandle: Effect.Handle,
            val targetEffect1StartExecutionRecord: TestTargetAction.ExecutionRecord<TestTargetEffect.Outcome<Int>>,
        ) {
            val targetEffect1Outcome: TestTargetEffect.Outcome<Int>
                get() = targetEffect1StartExecutionRecord.result
        }

        data class LaterTransactionRecord(
            val targetEffect1SubsequentCancelExecutionRecord: TestTargetAction.ExecutionRecord<Unit>,
        )

        val targetEffect1 = TestTargetEffect.pure(result = 10)

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect = sourceCell.actuate()

        val startTransactionRecord = TransactionTestUtils.executeInsideTransaction {
            val (subjectCell, subjectEffectHandle) = subjectEffect.startForTestingCancellable()
            val subjectCellObserver = subjectCell.observeForTesting()

            val targetEffect1StartExecutionRecord = targetEffect1.verifyWasStartedOnce()

            StartTransactionRecord(
                subjectCellObserver = subjectCellObserver,
                subjectEffectHandle = subjectEffectHandle,
                targetEffect1StartExecutionRecord = targetEffect1StartExecutionRecord,
            )
        }

        startTransactionRecord.targetEffect1StartExecutionRecord.verifyWasNotRevoked()

        val subjectCellObserver = startTransactionRecord.subjectCellObserver
        val subjectEffectHandle = startTransactionRecord.subjectEffectHandle
        val targetEffect1Outcome = startTransactionRecord.targetEffect1Outcome

        targetEffect1Outcome.verifyWasNotCancelled()

        subjectCellObserver.verifyDidNotPropagateNorExposesUpdate() // ...at any point until now / now

        val targetEffect2 = TestTargetEffect.pure(result = 20)

        val laterTransactionRecord = TransactionTestUtils.executeInsideTransaction {
            sourceCell.update(
                newValue = targetEffect2,
            ).stimulateForTesting()

            val targetEffect1InitialCancelExecutionRecord = targetEffect1Outcome.verifyWasCancelledOnce()
            val targetEffect2StartExecutionRecord = targetEffect2.verifyWasStartedOnce()

            subjectCellObserver.verifyPropagatedAndExposesUpdate(expectedUpdatedValue = 20)

            subjectEffectHandle.cancel.executeForTesting()

            targetEffect1InitialCancelExecutionRecord.verifyWasRevoked()
            targetEffect2StartExecutionRecord.verifyWasRevoked()

            val targetEffect1SubsequentCancelExecutionRecord = targetEffect1Outcome.verifyWasCancelledOnce()

            subjectCellObserver.verifyPropagatedAndExposesRevocation()

            LaterTransactionRecord(
                targetEffect1SubsequentCancelExecutionRecord = targetEffect1SubsequentCancelExecutionRecord,
            )
        }

        val targetEffect1SubsequentCancelExecutionRecord =
            laterTransactionRecord.targetEffect1SubsequentCancelExecutionRecord

        targetEffect1SubsequentCancelExecutionRecord.verifyWasNotRevoked() // ...at all

        targetEffect1Outcome.verifyWasNotCancelled() // ...again

        targetEffect1.verifyWasNotStarted() // ...again
        targetEffect2.verifyWasNotStarted() // ...again

        subjectCellObserver.verifyDidNotPropagateNorExposesUpdate() // ...again / now
    }

    @Test
    fun test_actuate_cancel_once() {
        test_actuate_cancel(count = 1)
    }

    @Test
    fun test_actuate_cancel_twiceSimultaneously() {
        test_actuate_cancel(count = 2)
    }

    private fun test_actuate_cancel(count: Int) {
        data class StartTransactionRecord(
            val subjectCellObserver: TestCellObserver<Int>,
            val subjectEffectHandle: Effect.Handle,
            val targetEffectStartExecutionRecord: TestTargetAction.ExecutionRecord<TestTargetEffect.Outcome<Int>>,
        )

        data class LaterOutcome(
            val targetEffectCancelExecutionRecord: TestTargetAction.ExecutionRecord<Unit>,
        )

        val targetEffect = TestTargetEffect.pure(result = 10)

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = targetEffect,
        )

        val subjectEffect = sourceCell.actuate()

        val spawnOutcome = TransactionTestUtils.executeInsideTransaction {
            val (subjectCell, subjectEffectHandle) = subjectEffect.startForTestingCancellable()

            val subjectCellObserver = subjectCell.observeForTesting()

            val targetEffectStartExecutionRecord = targetEffect.verifyWasStartedOnce()

            subjectCellObserver.verifyDoesNotExposeUpdate() // ...inside the spawn transaction

            StartTransactionRecord(
                subjectCellObserver = subjectCellObserver,
                subjectEffectHandle = subjectEffectHandle,
                targetEffectStartExecutionRecord = targetEffectStartExecutionRecord,
            )
        }

        val subjectEffectHandle = spawnOutcome.subjectEffectHandle
        val targetEffectOutcome = spawnOutcome.targetEffectStartExecutionRecord.result

        val laterOutcome = TransactionTestUtils.executeInsideTransaction {
            repeat(count) {
                subjectEffectHandle.cancel.executeForTesting()
            }

            val targetEffectCancelExecutionRecord = targetEffectOutcome.verifyWasCancelledOnce()

            LaterOutcome(
                targetEffectCancelExecutionRecord = targetEffectCancelExecutionRecord,
            )
        }

        targetEffect.verifyWasNotStarted() // ...again

        spawnOutcome.targetEffectStartExecutionRecord.verifyWasNotRevoked() // ...at any point

        targetEffectOutcome.verifyWasNotCancelled() // ...again

        laterOutcome.targetEffectCancelExecutionRecord.verifyWasNotRevoked() // ...at any point
    }
}
