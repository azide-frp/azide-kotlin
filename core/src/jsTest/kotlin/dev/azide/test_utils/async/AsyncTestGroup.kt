package dev.azide.test_utils.async

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

abstract class AsyncTestGroup {
    data class Result(
        val group: AsyncTestGroup,
        val testResults: List<AsyncTest.Result>,
    ) {
        fun print() {
            println("> Group ${group::class.simpleName}")
            println()

            testResults.forEach { testResult ->
                testResult.print()
            }
        }

        val didSucceed: Boolean
            get() = testResults.all { testResult ->
                testResult.didSucceed
            }
    }

    suspend fun run(): Result = coroutineScope {
        val testResults = tests.map { test ->
            async {
                test.run()
            }
        }.awaitAll()

        return@coroutineScope Result(
            group = this@AsyncTestGroup,
            testResults = testResults,
        )
    }

    protected abstract val tests: List<AsyncTest>
}
