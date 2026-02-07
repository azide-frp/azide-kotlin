package dev.azide.core.test_utils

import dev.azide.core.Action
import dev.azide.core.Effect
import dev.azide.core.Trigger
import dev.azide.core.test_utils.generic.ExpectedImpact.ImpactVerifier
import dev.azide.core.test_utils.TestTargetEffect.StartRecord
import dev.azide.core.test_utils.generic.ExpectedImpact
import kotlin.test.assertEquals

abstract class TestTargetEffect<ResultT>() : Effect<ResultT> {
    class Handle : Effect.Handle {
        val cancelRecorder: TestTargetActionRecorder.TriggerRecorder = TestTargetActionRecorder.TriggerRecorder()

        override val cancel: Trigger = cancelRecorder.recordedAction
    }

    interface Outcome<ResultT> : Effect.Outcome<ResultT> {
        override val handle: Handle
    }

    class StartRecord<ResultT>(
        private val executionRecord: TestTargetActionRecorder.ExecutionRecord<Outcome<ResultT>>,
    ) {
        val wasRevoked: Boolean
            get() = executionRecord.wasRevoked

        private val handle: Handle
            get() = executionRecord.result.handle

        fun resetCancellationRecords() {
            handle.cancelRecorder.resetExecutionRecords()
        }

        fun getAndResetCancellationRecords(): List<TestTargetActionRecorder.ExecutionRecord<Unit>> =
            handle.cancelRecorder.getAndResetExecutionRecords()
    }

    companion object {
        fun <ResultT> pure(
            result: ResultT,
        ) = object : TestTargetEffect<ResultT>() {
            override fun buildResult(): ResultT = result
        }
    }

    private val startRecorder = object : TestTargetActionRecorder<Outcome<ResultT>>() {
        override fun buildResult(): Outcome<ResultT> {
            val result = this@TestTargetEffect.buildResult()

            return object : Outcome<ResultT> {
                override val result: ResultT = result
                override val handle: Handle = Handle()
            }
        }
    }

    final override val start: Action<Outcome<ResultT>> = startRecorder.recordedAction

    fun resetStartRecords() {
        startRecorder.resetExecutionRecords()
    }

    fun getAndResetStartRecords(): List<StartRecord<ResultT>> = startRecorder.getAndResetExecutionRecords().map {
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
