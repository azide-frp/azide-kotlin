package dev.azide.core.test_utils

import dev.azide.core.Effect

abstract class TestTargetEffect<ResultT>() : Effect<ResultT> {
    class Handle : Effect.Handle {
        override val cancel: TestTargetAction.Trigger = TestTargetAction.Trigger()
    }

    interface Outcome<ResultT> : Effect.Outcome<ResultT> {
        override val handle: Handle
    }

    companion object {
        fun <ResultT> pure(
            result: ResultT,
        ) = object : TestTargetEffect<ResultT>() {
            override fun buildResult(): ResultT = result
        }
    }

    override val start: TestTargetAction<Outcome<ResultT>> = object : TestTargetAction<Outcome<ResultT>>() {
        override fun buildResult(): Outcome<ResultT> {
            val result = this@TestTargetEffect.buildResult()

            return object : Outcome<ResultT> {
                override val result: ResultT = result
                override val handle: Handle = Handle()
            }
        }
    }

    abstract fun buildResult(): ResultT
}

fun <ResultT> TestTargetEffect<ResultT>.verifyWasStartedOnceWithoutRevocation(): TestTargetEffect.Outcome<ResultT> =
    start.verifyWasExecutedOnceWithoutRevocation()

fun <ResultT> TestTargetEffect<ResultT>.verifyWasStartedOnce(): TestTargetAction.ExecutionRecord<TestTargetEffect.Outcome<ResultT>> =
    start.verifyWasExecutedOnce()

fun <ResultT> TestTargetEffect<ResultT>.verifyWasStartedOnceAndRevoked(): TestTargetEffect.Outcome<ResultT> =
    start.verifyWasExecutedOnceAndRevoked()

fun <ResultT> TestTargetEffect<ResultT>.verifyWasNotStarted() {
    start.verifyWasNotExecuted()
}

fun <ResultT> TestTargetEffect.Outcome<ResultT>.verifyWasCancelledOnce(): TestTargetAction.ExecutionRecord<Unit> =
    handle.cancel.verifyWasExecutedOnce()

fun <ResultT> TestTargetEffect.Outcome<ResultT>.verifyWasNotCancelled() {
    handle.cancel.verifyWasNotExecuted()
}
