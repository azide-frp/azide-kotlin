package dev.azide.core.test_utils.event_stream

actual fun assertIsStackOverflowError(throwable: Throwable) {
    if (throwable !is StackOverflowError) {
        throw AssertionError("Expected a StackOverflowError, but got: ${throwable::class.simpleName}")
    }
}
