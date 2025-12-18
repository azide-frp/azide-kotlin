package dev.azide.core.internal

actual object FinalizationTransactionRegistry {
    actual interface Handle {
        actual fun unregister()
    }

    actual fun register(
        target: Any,
        finalizationTransaction: Transaction<Any?>,
    ): Handle {
        // For now, do nothing.
        // The JVM target for the reactive framework is currently used only for unit testing. Although JVM offers an API
        // similar to JS `FinalizationRegistry` (`java.lang.ref.Cleaner`), it's multithreaded and would require some
        // form of synchronization.

        return object : Handle {
            override fun unregister() {
                // Also do nothing
            }
        }
    }
}
