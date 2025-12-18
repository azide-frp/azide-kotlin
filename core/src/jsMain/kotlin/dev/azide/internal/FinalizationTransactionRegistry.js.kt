package dev.azide.internal

import dev.kmpx.js.FinalizationRegistry

actual object FinalizationTransactionRegistry {
    actual interface Handle {
        actual fun unregister()
    }

    private val finalizationRegistry = FinalizationRegistry<Transaction<Any?>, Transaction<Any?>> { transaction ->
        // The FinalizationRegistry API guarantees that the cleanup functions will be called in a separate job, so it's
        // safe to just execute the transaction in-place.
        transaction.execute()
    }

    actual fun register(
        target: Any,
        finalizationTransaction: Transaction<Any?>,
    ): Handle {
        finalizationRegistry.register(
            target = target,
            heldValue = finalizationTransaction,
            unregisterToken = finalizationTransaction,
        )

        return object : Handle {
            override fun unregister() {
                finalizationRegistry.unregister(
                    unregisterToken = finalizationTransaction,
                )
            }
        }
    }
}
