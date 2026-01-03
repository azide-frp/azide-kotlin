package dev.azide.core.test_utils

import dev.azide.core.Action
import dev.azide.core.internal.Transactions
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

abstract class TestTargetAction<ResultT>() : Action<ResultT> {
    class ExecutionRecord<ResultT>(
        val result: ResultT,
    ) {
        private var _wasRevoked = false

        val wasRevoked: Boolean
            get() = _wasRevoked

        fun revoke() {
            if (_wasRevoked) {
                throw IllegalStateException("StartRecord already revoked")
            }

            _wasRevoked = true
        }
    }

    class Trigger : TestTargetAction<Unit>() {
        override fun buildResult(): Unit = Unit
    }

    private val executionRecords = mutableListOf<ExecutionRecord<ResultT>>()

    fun getAndResetExecutionRecords(): List<ExecutionRecord<ResultT>> = executionRecords.toList().also {
        executionRecords.clear()
    }

    override fun executeInternally(
        propagationContext: Transactions.PropagationContext,
        wrapUpContext: Transactions.WrapUpContext,
    ): Action.Outcome<ResultT> {
        val result = buildResult()

        val execution = ExecutionRecord(result = result)

        executionRecords.add(execution)

        return Action.Outcome.of(
            result = result,
            revocationHandle = object : Action.RevocationHandle {
                override fun revoke() {
                    execution.revoke()
                }
            },
        )
    }

    abstract fun buildResult(): ResultT
}

fun <ResultT> TestTargetAction<ResultT>.verifyWasExecutedOnceWithoutRevocation(): ResultT {
    val executionRecord = assertNotNull(
        getAndResetExecutionRecords().singleOrNull(),
    )

    assertFalse(
        executionRecord.wasRevoked,
    )

    return executionRecord.result
}

fun <ResultT> TestTargetAction<ResultT>.verifyWasExecutedOnce(): TestTargetAction.ExecutionRecord<ResultT> {
    val executionRecords = getAndResetExecutionRecords()

    assertEquals(
        expected = 1,
        actual = executionRecords.size,
        message = "Expected exactly one execution of the action.",
    )

    return executionRecords.single()
}

fun <ResultT> TestTargetAction<ResultT>.verifyWasExecutedOnceAndRevoked(): ResultT {
    val executionRecord = assertNotNull(
        getAndResetExecutionRecords().singleOrNull(),
    )

    assertTrue(
        executionRecord.wasRevoked,
    )

    return executionRecord.result
}

fun TestTargetAction<*>.verifyWasNotExecuted() {
    assertTrue(
        getAndResetExecutionRecords().isEmpty()
    )
}

fun TestTargetAction.ExecutionRecord<*>.verifyWasRevoked() {
    assertTrue(
        actual = wasRevoked,
        message = "Expected action to have been revoked, but it was not.",
    )
}

fun TestTargetAction.ExecutionRecord<*>.verifyWasNotRevoked() {
    assertFalse(
        actual = wasRevoked,
        message = "Expected action to not have been revoked, but it was.",
    )
}
