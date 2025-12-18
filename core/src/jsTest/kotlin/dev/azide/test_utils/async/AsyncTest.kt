package dev.azide.test_utils.async

abstract class AsyncTest {
    data class Result(
        val test: AsyncTest,
        val status: Status,
    ) {
        fun print() {
            println(">> Test ${test::class.simpleName}")
            println()

            println("Status: ${status.toPrintableString()}")

            (status as? Failed)?.let { failedStatus ->
                println()
                failedStatus.throwable.printStackTrace()
                println()
            }
        }

        val didSucceed: Boolean
            get() = status is Passed
    }

    sealed interface Status {
        fun toPrintableString(): String
    }

    data object Passed : Status {
        override fun toPrintableString(): String = "Passed ✅"
    }

    data class Failed(
        val throwable: Throwable,
    ) : Status {
        override fun toPrintableString(): String = "Failed ❌"
    }

    suspend fun run(): Result {
        val status = try {
            execute()

            Passed
        } catch (throwable: Throwable) {
            Failed(throwable = throwable)
        }

        return Result(
            test = this,
            status = status,
        )
    }

    protected abstract suspend fun execute()
}
