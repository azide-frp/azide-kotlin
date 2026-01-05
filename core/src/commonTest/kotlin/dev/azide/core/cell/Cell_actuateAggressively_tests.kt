package dev.azide.core.cell

import dev.azide.core.Action
import dev.azide.core.Cell
import dev.azide.core.Effect
import dev.azide.core.actuateAggressively
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
import dev.azide.core.test_utils.verifyPropagatedAndExposesUpdate
import dev.azide.core.test_utils.verifyWasCancelledOnce
import dev.azide.core.test_utils.verifyWasNotCancelled
import dev.azide.core.test_utils.verifyWasNotRevoked
import dev.azide.core.test_utils.verifyWasNotStarted
import dev.azide.core.test_utils.verifyWasRevoked
import dev.azide.core.test_utils.verifyWasStartedOnce
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("ClassName")
class Cell_actuateAggressively_tests {
    @Test
    fun test_actuateAggressively_spawn() {
        val targetEffect = TestTargetEffect.pure(result = 10)

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = targetEffect,
        )

        val subjectEffect = sourceCell.actuateAggressively()

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
    fun test_actuateAggressively_spawn_cancelledInstantly() {
        data class SpawnOutcome(
            val subjectCellObserver: TestCellObserver<Int>,
            val targetEffectStartExecutionRecord: TestTargetAction.ExecutionRecord<TestTargetEffect.Outcome<Int>>,
            val targetEffectCancelExecutionRecord: TestTargetAction.ExecutionRecord<Unit>,
        )

        val targetEffect = TestTargetEffect.pure(result = 10)

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = targetEffect,
        )

        val subjectEffect = sourceCell.actuateAggressively()

        val outcome = TransactionTestUtils.executeInsideTransaction {
            val (subjectCell, subjectEffectHandle) = subjectEffect.startForTestingCancellable()
            val subjectCellObserver = subjectCell.observeForTesting()

            val targetEffectStartExecutionRecord = targetEffect.verifyWasStartedOnce()
            val targetEffectOutcome = targetEffectStartExecutionRecord.result

            subjectEffectHandle.cancel.executeForTesting()

            val targetEffectCancelExecutionRecord = targetEffectOutcome.verifyWasCancelledOnce()

            subjectCellObserver.verifyDoesNotExposeUpdate() // ...inside the spawn transaction

            subjectCellObserver.verifyOldValue(expectedOldValue = 10) // ...inside the spawn transaction

            SpawnOutcome(
                subjectCellObserver = subjectCellObserver,
                targetEffectStartExecutionRecord = targetEffectStartExecutionRecord,
                targetEffectCancelExecutionRecord = targetEffectCancelExecutionRecord,
            )
        }

        outcome.subjectCellObserver.verifyDidNotPropagateNorExposesUpdate() //  ...at any point / now
        outcome.subjectCellObserver.verifyOldValueInsideTransaction(expectedOldValue = 10)  // ...after the spawn transaction

        targetEffect.verifyWasNotStarted() // ...again

        outcome.targetEffectStartExecutionRecord.verifyWasNotRevoked() // ...at any point

        val targetEffectOutcome = outcome.targetEffectStartExecutionRecord.result
        targetEffectOutcome.verifyWasNotCancelled() // ...again

        outcome.targetEffectCancelExecutionRecord.verifyWasNotRevoked() // ...at any point
    }

    @Test
    fun test_actuateAggressively_spawn_revokedInstantly() {
        val targetEffect = TestTargetEffect.pure(result = 10)

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = targetEffect,
        )

        val subjectEffect = sourceCell.actuateAggressively()

        val targetEffectStartExecutionRecord = TransactionTestUtils.executeInsideTransaction {
            val (_: Cell<Int>, startRevocationHandle: Action.RevocationHandle) = subjectEffect.startForTestingRevocable()

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
    fun test_actuateAggressively_spawn_cancelledInstantly_revokedInstantly() {
        val targetEffect = TestTargetEffect.pure(result = 10)

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = targetEffect,
        )

        val subjectEffect = sourceCell.actuateAggressively()

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
    fun test_actuateAggressively_spawn_cancelledInstantlyTwice() {
        data class SpawnOutcome(
            val subjectCellObserver: TestCellObserver<Int>,
            val targetEffectStartExecutionRecord: TestTargetAction.ExecutionRecord<TestTargetEffect.Outcome<Int>>,
            val targetEffectCancelExecutionRecord: TestTargetAction.ExecutionRecord<Unit>,
        )

        val targetEffect = TestTargetEffect.pure(result = 10)

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = targetEffect,
        )

        val subjectEffect = sourceCell.actuateAggressively()

        val outcome = TransactionTestUtils.executeInsideTransaction {
            val (subjectCell, subjectEffectHandle) = subjectEffect.startForTestingCancellable()

            val subjectCellObserver = subjectCell.observeForTesting()

            val targetEffectStartExecutionRecord = targetEffect.verifyWasStartedOnce()
            val targetEffectOutcome = targetEffectStartExecutionRecord.result

            subjectEffectHandle.cancel.executeForTesting()
            subjectEffectHandle.cancel.executeForTesting()

            val targetEffectCancelExecutionRecord = targetEffectOutcome.verifyWasCancelledOnce()

            subjectCellObserver.verifyDoesNotExposeUpdate() // ...inside the spawn transaction

            SpawnOutcome(
                subjectCellObserver = subjectCellObserver,
                targetEffectStartExecutionRecord = targetEffectStartExecutionRecord,
                targetEffectCancelExecutionRecord = targetEffectCancelExecutionRecord,
            )
        }

        targetEffect.verifyWasNotStarted() // ...again

        outcome.targetEffectStartExecutionRecord.verifyWasNotRevoked() // ...at any point

        val targetEffectOutcome = outcome.targetEffectStartExecutionRecord.result
        targetEffectOutcome.verifyWasNotCancelled() // ...again

        outcome.targetEffectCancelExecutionRecord.verifyWasNotRevoked() // ...at any point
    }

    @Test
    fun test_actuateAggressively_spawn_sourceUpdatesSimultaneously() {
        data class SpawnOutcome(
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

        val subjectEffect = sourceCell.actuateAggressively()

        val outcome = TransactionTestUtils.executeInsideTransaction {
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

            SpawnOutcome(
                subjectCellObserver = subjectCellObserver,
                targetEffect1StartExecutionRecord = targetEffect1StartExecutionRecord,
                targetEffect1CancelExecutionRecord = targetEffect1CancelExecutionRecord,
                targetEffect2StartExecutionRecord = targetEffect2StartExecutionRecord,
            )
        }

        outcome.subjectCellObserver.verifyDidNotPropagateNorExposesUpdate() // ...again / now
        outcome.subjectCellObserver.verifyOldValueInsideTransaction(expectedOldValue = 20)  // ...after the spawn transaction

        targetEffect1.verifyWasNotStarted() // ...again

        outcome.targetEffect1StartExecutionRecord.verifyWasNotRevoked() // ...at any point

        val targetEffect1Outcome = outcome.targetEffect1StartExecutionRecord.result
        targetEffect1Outcome.verifyWasNotCancelled() // ...again

        outcome.targetEffect1CancelExecutionRecord.verifyWasNotRevoked() // ...at any point

        targetEffect2.verifyWasNotStarted() // ...again

        outcome.targetEffect2StartExecutionRecord.verifyWasNotRevoked() // ...at any point

        val targetEffect2Outcome = outcome.targetEffect2StartExecutionRecord.result
        targetEffect2Outcome.verifyWasNotCancelled() // ...at any point
    }

    @Test
    fun test_actuateAggressively_spawn_sourceUpdatesSimultaneously_revokedInstantly() {
        data class SpawnOutcome(
            val targetEffect1Outcome: TestTargetEffect.Outcome<Int>,
            val targetEffect2Outcome: TestTargetEffect.Outcome<Int>,
        )

        val targetEffect1 = TestTargetEffect.pure(result = 10)
        val targetEffect2 = TestTargetEffect.pure(result = 20)

        val sourceCell = CellTestUtils.createInputCell(
            initialValue = targetEffect1,
        )

        val subjectEffect = sourceCell.actuateAggressively()

        val outcome = TransactionTestUtils.executeInsideTransaction {
            val (_: Cell<Int>, startRevocationHandle: Action.RevocationHandle) = subjectEffect.startForTestingRevocable()

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

            SpawnOutcome(
                targetEffect1Outcome = targetEffect1StartExecutionRecord.result,
                targetEffect2Outcome = targetEffect2StartExecutionRecord.result,
            )
        }

        targetEffect1.verifyWasNotStarted() // ...again

        outcome.targetEffect1Outcome.verifyWasNotCancelled() // ...again

        targetEffect2.verifyWasNotStarted() // ...again

        outcome.targetEffect2Outcome.verifyWasNotCancelled() // ...at any point
    }

    @Test
    fun test_actuateAggressively_cancelledTwiceSimultaneously() {
        data class SpawnOutcome(
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

        val subjectEffect = sourceCell.actuateAggressively()

        val spawnOutcome = TransactionTestUtils.executeInsideTransaction {
            val (subjectCell, subjectEffectHandle) = subjectEffect.startForTestingCancellable()

            val subjectCellObserver = subjectCell.observeForTesting()

            val targetEffectStartExecutionRecord = targetEffect.verifyWasStartedOnce()

            subjectCellObserver.verifyDoesNotExposeUpdate() // ...inside the spawn transaction

            SpawnOutcome(
                subjectCellObserver = subjectCellObserver,
                subjectEffectHandle = subjectEffectHandle,
                targetEffectStartExecutionRecord = targetEffectStartExecutionRecord,
            )
        }

        val subjectEffectHandle = spawnOutcome.subjectEffectHandle
        val targetEffectOutcome = spawnOutcome.targetEffectStartExecutionRecord.result

        val laterOutcome = TransactionTestUtils.executeInsideTransaction {
            subjectEffectHandle.cancel.executeForTesting()
            subjectEffectHandle.cancel.executeForTesting()

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
