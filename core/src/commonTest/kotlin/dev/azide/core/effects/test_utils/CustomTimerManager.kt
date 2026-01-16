package dev.azide.core.effects.test_utils

class CustomTimerManager {
    private data class Entry(
        val requestedIntervalMs: Int,
        val handler: Handler,
    )

    private val startedTimerEntries = mutableSetOf<Entry>()

    val startedTimerCount: Int
        get() = startedTimerEntries.size

    interface Handler {
        fun handleIntervalElapsed(
            actualElapsedTimeMs: Int,
        )
    }

    interface Handle {
        fun stop()
    }

    fun startTimer(
        @Suppress("unused") intervalMs: Int,
        handler: Handler,
    ): Handle {
        val wasStarted = startedTimerEntries.add(
            Entry(
                requestedIntervalMs = intervalMs,
                handler = handler,
            )
        )

        if (!wasStarted) {
            throw IllegalStateException("Handler already registered")
        }

        return object : Handle {
            override fun stop() {
                val wasStopped = startedTimerEntries.removeAll { it.handler == handler }

                if (!wasStopped) {
                    throw IllegalStateException("Timer not found")
                }
            }
        }
    }

    fun invokeAll(
        delayMs: Int = 1,
    ) {
        startedTimerEntries.forEach { entry ->
            entry.handler.handleIntervalElapsed(
                // Simulate a tiny delay (just to introduce any behavior)
                actualElapsedTimeMs = entry.requestedIntervalMs + delayMs,
            )
        }
    }
}
