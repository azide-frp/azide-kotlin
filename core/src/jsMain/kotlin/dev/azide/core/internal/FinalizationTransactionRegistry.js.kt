package dev.azide.core.internal

import dev.kmpx.js.FinalizationRegistry

actual object ReactiveFinalizationRegistry {
    actual interface Handle {
        actual fun unregister()
    }

    private val finalizationRegistry =
        FinalizationRegistry<FinalizationCallback, FinalizationCallback> { finalizationCallback ->
            // The FinalizationRegistry API guarantees that the cleanup functions will be called in a separate job, so it's
            // safe to just execute the transaction in-place.
            finalizationCallback()
        }

    actual fun register(
        target: Any,
        finalizationCallback: FinalizationCallback,
    ): Handle {
        finalizationRegistry.register(
            target = target,
            heldValue = finalizationCallback,
            unregisterToken = finalizationCallback,
        )

        return object : Handle {
            override fun unregister() {
                finalizationRegistry.unregister(
                    unregisterToken = finalizationCallback,
                )
            }
        }
    }
}
