package dev.azide.internal

expect object FinalizationTransactionRegistry {
    interface Handle {
        fun unregister()
    }

    /**
     * Register a [finalizationTransaction] to be executed after the [target] object is garbage collected. There's
     * no guarantee when the transaction will be executed, or even if it will be executed at all.
     */
    fun register(
        target: Any,
        finalizationTransaction: Transaction<Any?>,
    ): Handle
}
