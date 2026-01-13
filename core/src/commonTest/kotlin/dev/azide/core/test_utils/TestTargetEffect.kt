package dev.azide.core.test_utils

import dev.azide.core.Effect
import dev.azide.core.test_utils.ExpectedTestTargetImpact.TargetImpactVerifier
import kotlin.test.assertEquals
import kotlin.test.assertFalse

abstract class TestTargetEffect<ResultT>() : Effect<ResultT> {
    class Handle : Effect.Handle {
        override val cancel: TestTargetAction.Trigger = TestTargetAction.Trigger()
    }

    interface Outcome<ResultT> : Effect.Outcome<ResultT> {
        override val handle: Handle
    }

    class StartRecord<ResultT>(
        private val executionRecord: TestTargetAction.ExecutionRecord<Outcome<ResultT>>,
    ) {
        val wasRevoked: Boolean
            get() = executionRecord.wasRevoked

        val outcome: Outcome<ResultT>
            get() = executionRecord.result

        val result: ResultT
            get() = outcome.result

        fun resetCancellationRecords() {
            outcome.handle.cancel.resetExecutionRecords()
        }

        fun getAndResetCancellationRecords(): List<TestTargetAction.ExecutionRecord<Unit>> =
            outcome.handle.cancel.getAndResetExecutionRecords()
    }

    companion object {
        fun <ResultT> pure(
            result: ResultT,
        ) = object : TestTargetEffect<ResultT>() {
            override fun buildResult(): ResultT = result
        }
    }

    private val _start = object : TestTargetAction<Outcome<ResultT>>() {
        override fun buildResult(): Outcome<ResultT> {
            val result = this@TestTargetEffect.buildResult()

            return object : Outcome<ResultT> {
                override val result: ResultT = result
                override val handle: Handle = Handle()
            }
        }
    }

    final override val start: TestTargetAction<Outcome<ResultT>> = _start

    fun resetStartRecords() {
        _start.resetExecutionRecords()
    }

    fun getAndResetStartRecords(): List<StartRecord<ResultT>> = _start.getAndResetExecutionRecords().map {
        StartRecord(executionRecord = it)
    }

    abstract fun buildResult(): ResultT
}

fun <ResultT> TestTargetEffect<ResultT>.expectIsNotStarted(): ExpectedTestTargetImpact =
    object : ExpectedTestTargetImpact {
        override fun prepareImpactVerifier(): TargetImpactVerifier {
            resetStartRecords()

            return object : TargetImpactVerifier {
                override fun verifyPostTransaction() {
                    val effectiveStartRecords = getAndResetStartRecords().filter {
                        !it.wasRevoked
                    }

                    assertEquals(
                        expected = 0,
                        actual = effectiveStartRecords.size,
                        message = "Expected no starts of the target effect during the stimulation.",
                    )
                }
            }
        }
    }

fun <ResultT> TestTargetEffect<ResultT>.expectIsStartedOnceAndNotCancelled(): ExpectedTestTargetImpact =
    object : ExpectedTestTargetImpact {
        override fun prepareImpactVerifier(): TargetImpactVerifier {
            resetStartRecords()

            return object : TargetImpactVerifier {
                override fun verifyPostTransaction() {
                    val effectiveStartRecords = getAndResetStartRecords().filter {
                        !it.wasRevoked
                    }

                    assertEquals(
                        expected = 1,
                        actual = effectiveStartRecords.size,
                        message = "Expected exactly one start of the target effect during the stimulation.",
                    )

                    val effectiveStartRecord = effectiveStartRecords.single()

                    val effectiveCancellationRecords = effectiveStartRecord.getAndResetCancellationRecords().filter {
                        !it.wasRevoked
                    }

                    assertEquals(
                        expected = 0,
                        actual = effectiveCancellationRecords.size,
                        message = "Expected no cancellations of the target effect during the stimulation.",
                    )
                }
            }
        }
    }

fun <ResultT> TestTargetEffect.StartRecord<ResultT>.expectIsNotCancelled(): ExpectedTestTargetImpact =
    object : ExpectedTestTargetImpact {
        override fun prepareImpactVerifier(): TargetImpactVerifier {
            resetCancellationRecords()

            return object : TargetImpactVerifier {
                override fun verifyPostTransaction() {
                    val effectiveCancellationRecords = getAndResetCancellationRecords().filter {
                        !it.wasRevoked
                    }

                    assertEquals(
                        expected = 0,
                        actual = effectiveCancellationRecords.size,
                        message = "Expected no cancellations of the target effect during the stimulation.",
                    )
                }
            }
        }
    }


fun <ResultT> TestTargetEffect.StartRecord<ResultT>.expectIsCancelledOnce(): ExpectedTestTargetImpact =
    object : ExpectedTestTargetImpact {
        override fun prepareImpactVerifier(): TargetImpactVerifier {
            resetCancellationRecords()

            return object : TargetImpactVerifier {
                override fun verifyPostTransaction() {
                    val effectiveCancellationRecords = getAndResetCancellationRecords().filter {
                        !it.wasRevoked
                    }

                    assertEquals(
                        expected = 1,
                        actual = effectiveCancellationRecords.size,
                        message = "Expected exactly one cancellation of the target effect during the stimulation.",
                    )
                }
            }
        }
    }

@Deprecated("Switch to the new test utils")
fun <ResultT> TestTargetEffect<ResultT>.verifyWasStartedOnceWithoutRevocation(): TestTargetEffect.Outcome<ResultT> =
    start.verifyWasExecutedOnceWithoutRevocation()

@Deprecated("Switch to the new test utils")
fun <ResultT> TestTargetEffect<ResultT>.verifyWasStartedOnce(): TestTargetAction.ExecutionRecord<TestTargetEffect.Outcome<ResultT>> =
    start.verifyWasExecutedOnce()

@Deprecated("Switch to the new test utils")
fun <ResultT> TestTargetEffect<ResultT>.verifyWasStartedOnceAndRevoked(): TestTargetEffect.Outcome<ResultT> =
    start.verifyWasExecutedOnceAndRevoked()

@Deprecated("Switch to the new test utils")
fun <ResultT> TestTargetEffect<ResultT>.verifyWasNotStarted() {
    start.verifyWasNotExecuted()
}

@Deprecated("Switch to the new test utils")
fun <ResultT> TestTargetEffect.Outcome<ResultT>.verifyWasCancelledOnce(): TestTargetAction.ExecutionRecord<Unit> =
    handle.cancel.verifyWasExecutedOnce()

@Deprecated("Switch to the new test utils")
fun <ResultT> TestTargetEffect.Outcome<ResultT>.verifyWasNotCancelled() {
    handle.cancel.verifyWasNotExecuted()
}
