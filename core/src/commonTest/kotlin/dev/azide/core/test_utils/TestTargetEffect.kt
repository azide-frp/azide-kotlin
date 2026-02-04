package dev.azide.core.test_utils

import dev.azide.core.Effect
import dev.azide.core.test_utils.ExpectedImpact.ImpactVerifier
import dev.azide.core.test_utils.TestTargetEffect.StartRecord
import kotlin.test.assertEquals

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

fun <ResultT> TestTargetEffect<ResultT>.getAndResetSingleStartRecord(): StartRecord<ResultT> =
    getAndResetStartRecords().single()

fun <ResultT> TestTargetEffect<ResultT>.expectIsNotStarted(): ExpectedImpact =
    object : ExpectedImpact {
        override fun prepareImpactVerifier(): ImpactVerifier {
            resetStartRecords()

            return object : ImpactVerifier {
                override fun verifyPostPropagation() {
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

fun <ResultT> TestTargetEffect<ResultT>.expectIsStartedOnceButNotCancelled(): ExpectedImpact =
    object : ExpectedImpact {
        override fun prepareImpactVerifier(): ImpactVerifier {
            resetStartRecords()

            return object : ImpactVerifier {
                override fun verifyPostPropagation() {
                    val effectiveStartRecords = getAndResetStartRecords().filter {
                        !it.wasRevoked
                    }

                    assertEquals(
                        expected = 1,
                        actual = effectiveStartRecords.size,
                        message = "Expected exactly one start of the target effect during the stimulation.",
                    )

                    val effectiveStartRecord = effectiveStartRecords.single()

                    effectiveStartRecord.verifyWasNotCancelled()
                }
            }
        }
    }

fun <ResultT> TestTargetEffect<ResultT>.expectIsStartedOnceAndCancelledOnce(): ExpectedImpact =
    object : ExpectedImpact {
        override fun prepareImpactVerifier(): ImpactVerifier {
            resetStartRecords()

            return object : ImpactVerifier {
                override fun verifyPostPropagation() {
                    val effectiveStartRecords = getAndResetStartRecords().filter {
                        !it.wasRevoked
                    }

                    assertEquals(
                        expected = 1,
                        actual = effectiveStartRecords.size,
                        message = "Expected exactly one start of the target effect during the stimulation.",
                    )

                    val effectiveStartRecord = effectiveStartRecords.single()

                    effectiveStartRecord.verifyWasCancelledOnce()
                }
            }
        }
    }

fun <ResultT> StartRecord<ResultT>.expectIsNotCancelled(): ExpectedImpact =
    object : ExpectedImpact {
        override fun prepareImpactVerifier(): ImpactVerifier {
            resetCancellationRecords()

            return object : ImpactVerifier {
                override fun verifyPostPropagation() {
                    verifyWasNotCancelled()
                }
            }
        }
    }

private fun <ResultT> StartRecord<ResultT>.verifyWasNotCancelled() {
    val effectiveCancellationRecords = getAndResetCancellationRecords().filter {
        !it.wasRevoked
    }

    assertEquals(
        expected = 0,
        actual = effectiveCancellationRecords.size,
        message = "Expected no cancellations of the target effect during the stimulation.",
    )
}

fun <ResultT> StartRecord<ResultT>.expectIsCancelledOnce(): ExpectedImpact =
    object : ExpectedImpact {
        override fun prepareImpactVerifier(): ImpactVerifier {
            resetCancellationRecords()

            return object : ImpactVerifier {
                override fun verifyPostPropagation() {
                    verifyWasCancelledOnce()
                }
            }
        }
    }

private fun <ResultT> StartRecord<ResultT>.verifyWasCancelledOnce() {
    val effectiveCancellationRecords = getAndResetCancellationRecords().filter {
        !it.wasRevoked
    }

    assertEquals(
        expected = 1,
        actual = effectiveCancellationRecords.size,
        message = "Expected exactly one cancellation of the target effect during the stimulation.",
    )
}

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
