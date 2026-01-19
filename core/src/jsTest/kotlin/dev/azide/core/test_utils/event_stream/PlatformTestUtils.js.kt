package dev.azide.core.test_utils.event_stream

actual fun assertIsStackOverflowError(throwable: Throwable) {
    val message = throwable.message ?: ""

    if (!message.contains("call stack")) {
        throw AssertionError("Expected an error indicating stack overflow, but got: $throwable")
    }
}
