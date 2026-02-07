package dev.azide.core.test_utils

import dev.azide.core.Action
import dev.azide.core.impl.Revocable
import dev.azide.core.impl.Transactions
import dev.azide.core.test_utils.ExpectedImpact.ImpactVerifier
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

abstract class TestTargetActionRecorder<ResultT>() {
    class ExecutionRecord<ResultT>(
        val result: ResultT,
    ) {
        private var _wasRevoked = false

        val wasRevoked: Boolean
            get() = _wasRevoked

        fun revoke() {
            if (_wasRevoked) {
                throw IllegalStateException("The target action was already revoked")
            }

            _wasRevoked = true
        }
    }

    class TriggerRecorder : TestTargetActionRecorder<Unit>() {
        override fun buildResult(): Unit = Unit
    }

    companion object {
        fun <ResultT> of(result: ResultT): TestTargetActionRecorder<ResultT> =
            object : TestTargetActionRecorder<ResultT>() {
            override fun buildResult(): ResultT = result
        }
    }

    val recordedAction: Action<ResultT> = object : Action<ResultT> {
        override fun executeInternally(
            propagationContext: Transactions.PropagationContext,
            wrapUpContext: Transactions.WrapUpContext,
        ): Action.Outcome<ResultT> {
            val result = buildResult()

            val execution = ExecutionRecord(result = result)

            executionRecords.add(execution)

            return Action.Outcome.of(
                result = result,
                revocable = object : Revocable {
                    override fun revoke() {
                        execution.revoke()
                    }
                },
            )
        }
    }

    private val executionRecords = mutableListOf<ExecutionRecord<ResultT>>()

    fun getAndResetExecutionRecords(): List<ExecutionRecord<ResultT>> = executionRecords.toList().also {
        resetExecutionRecords()
    }

    fun resetExecutionRecords() {
        executionRecords.clear()
    }

    abstract fun buildResult(): ResultT
}

fun <ResultT> TestTargetActionRecorder<ResultT>.verifyWasExecutedOnceWithoutRevocation(): ResultT {
    val executionRecord = assertNotNull(
        getAndResetExecutionRecords().singleOrNull(),
    )

    assertFalse(
        executionRecord.wasRevoked,
    )

    return executionRecord.result
}

fun <ResultT> TestTargetActionRecorder<ResultT>.verifyWasExecutedOnce(): TestTargetActionRecorder.ExecutionRecord<ResultT> {
    val executionRecords = getAndResetExecutionRecords()

    assertEquals(
        expected = 1,
        actual = executionRecords.size,
        message = "Expected exactly one execution of the action.",
    )

    return executionRecords.single()
}

fun <ResultT> TestTargetActionRecorder<ResultT>.verifyWasExecutedOnceAndRevoked(): ResultT {
    val executionRecord = assertNotNull(
        getAndResetExecutionRecords().singleOrNull(),
    )

    assertTrue(
        executionRecord.wasRevoked,
    )

    return executionRecord.result
}

fun TestTargetActionRecorder<*>.verifyWasNotExecuted() {
    assertTrue(
        getAndResetExecutionRecords().isEmpty()
    )
}

fun TestTargetActionRecorder.ExecutionRecord<*>.verifyWasRevoked() {
    assertTrue(
        actual = wasRevoked,
        message = "Expected action to have been revoked, but it was not.",
    )
}

fun TestTargetActionRecorder.ExecutionRecord<*>.verifyWasNotRevoked() {
    assertFalse(
        actual = wasRevoked,
        message = "Expected action to not have been revoked, but it was.",
    )
}

fun <ResultT> TestTargetActionRecorder<ResultT>.expectIsNotExecuted(): ExpectedImpact = expectIsExecutedNTimes(
    expectedExecutionCount = 0,
    message = "Expected no executions of the target action.",
)

fun <ResultT> TestTargetActionRecorder<ResultT>.expectIsExecutedOnce(): ExpectedImpact = expectIsExecutedNTimes(
    expectedExecutionCount = 1,
    message = "Expected a single execution of the target action during the stimulation.",
)

fun <ResultT> TestTargetActionRecorder<ResultT>.expectIsExecutedNTimes(
    expectedExecutionCount: Int,
    message: String,
): ExpectedImpact = object : ExpectedImpact {
    override fun prepareImpactVerifier(): ImpactVerifier {
        resetExecutionRecords()

        return object : ImpactVerifier {
            override fun verifyPostPropagation() {
                val effectiveExecutionRecords = getAndResetExecutionRecords().filter {
                    !it.wasRevoked
                }

                assertEquals(
                    expected = expectedExecutionCount,
                    actual = effectiveExecutionRecords.size,
                    message = message,
                )
            }
        }
    }
}
