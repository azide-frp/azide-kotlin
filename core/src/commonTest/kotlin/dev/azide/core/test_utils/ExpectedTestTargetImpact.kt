package dev.azide.core.test_utils

import dev.azide.core.test_utils.ExpectedTestTargetImpact.TargetImpactVerifier
import kotlin.test.assertEquals

interface ExpectedTestTargetImpact {
    interface TargetImpactVerifier {
        fun verifyPostTransaction()
    }

    companion object {
        fun combine(
            vararg expectedStimulations: ExpectedTestTargetImpact,
        ): ExpectedTestTargetImpact = object : ExpectedTestTargetImpact {
            override fun prepareImpactVerifier(): TargetImpactVerifier {
                val subVerifiers = expectedStimulations.map { it.prepareImpactVerifier() }

                return object : TargetImpactVerifier {
                    override fun verifyPostTransaction() {
                        subVerifiers.forEach { it.verifyPostTransaction() }
                    }
                }
            }
        }
    }

    fun prepareImpactVerifier(): TargetImpactVerifier
}

fun <ResultT> TestTargetAction<ResultT>.expectIsNotExecuted(): ExpectedTestTargetImpact = expectIsExecutedNTimes(
    expectedExecutionCount = 0,
    message = "Expected no executions of the target action.",
)

fun <ResultT> TestTargetAction<ResultT>.expectIsExecutedOnce(): ExpectedTestTargetImpact = expectIsExecutedNTimes(
    expectedExecutionCount = 1,
    message = "Expected a single execution of the target action during the stimulation.",
)

fun <ResultT> TestTargetAction<ResultT>.expectIsExecutedNTimes(
    expectedExecutionCount: Int,
    message: String,
): ExpectedTestTargetImpact = object : ExpectedTestTargetImpact {
    override fun prepareImpactVerifier(): TargetImpactVerifier {
        resetExecutionRecords()

        return object : TargetImpactVerifier {
            override fun verifyPostTransaction() {
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
