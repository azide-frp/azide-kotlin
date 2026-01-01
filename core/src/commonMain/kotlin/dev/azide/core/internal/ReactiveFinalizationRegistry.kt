package dev.azide.core.internal

typealias FinalizationCallback = () -> Unit

expect object ReactiveFinalizationRegistry {
    interface Handle {
        fun unregister()
    }

    /**
     * Register a [finalizationCallback] to be executed after the [target] object is garbage collected. There's
     * no guarantee when the transaction will be executed, or even if it will be executed at all.
     */
    fun register(
        target: Any,
        finalizationCallback: FinalizationCallback,
    ): Handle
}
