package dev.azide.core.internal.utils

class LoopedLazy<ValueT : Any> : Lazy<ValueT> {
    class UninitializedValueAccessException() : IllegalStateException("The value is not initialized yet")

    private var loopedValue: ValueT? = null

    override val value: ValueT
        get() = loopedValue ?: throw UninitializedValueAccessException()

    override fun isInitialized(): Boolean = loopedValue != null

    fun loop(
        value: ValueT,
    ) {
        if (loopedValue != null) {
            throw IllegalStateException("The value is already initialized")
        }

        loopedValue = value
    }
}
