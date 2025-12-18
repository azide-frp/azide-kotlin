package dev.azide.test_utils.async

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

abstract class AsyncTestSuite {
    data class Result(
        val suite: AsyncTestSuite,
        val groupResults: List<AsyncTestGroup.Result>,
    ) {
        fun print() {
            groupResults.forEach { groupResult ->
                groupResult.print()
            }
        }

        val didSucceed: Boolean
            get() = groupResults.all { groupResult ->
                groupResult.didSucceed
            }
    }

    protected abstract val groups: List<AsyncTestGroup>

    suspend fun runAndPrintResult() {
        val result = run()

        result.print()

        if (!result.didSucceed) {
            throw AssertionError("Some async tests did not pass! ❌")
        }
    }

    private suspend fun run(): Result = coroutineScope {
        val groupResults = groups.map { group ->
            async {
                group.run()
            }
        }.awaitAll()

        return@coroutineScope Result(
            suite = this@AsyncTestSuite,
            groupResults = groupResults,
        )
    }
}
