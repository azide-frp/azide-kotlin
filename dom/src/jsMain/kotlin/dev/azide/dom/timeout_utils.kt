package dev.azide.dom

import dev.azide.core.Effect
import dev.azide.core.EventStream
import dev.azide.core.external.ExternalEffectDelegate
import dev.azide.core.external.ExternalEventHandler
import dev.azide.core.external.ExternalStreamEffect
import org.w3c.dom.WindowOrWorkerGlobalScope
import kotlin.time.Duration

fun WindowOrWorkerGlobalScope.timeoutEffect(
    duration: Duration,
): Effect<EventStream<Unit>> = Effect.adapt(
    externalStreamEffect = object : ExternalStreamEffect<Unit> {
        override fun start(
            handler: ExternalEventHandler<Unit>,
        ): ExternalEffectDelegate = startTimeoutEffectExternally(
            duration = duration,
        ) {
            handler.handle(Unit)
        }
    },
)

fun WindowOrWorkerGlobalScope.intervalTimeoutEffect(
    intervalDuration: Duration,
): Effect<EventStream<Unit>> = Effect.adapt(
    externalStreamEffect = object : ExternalStreamEffect<Unit> {
        override fun start(
            handler: ExternalEventHandler<Unit>,
        ): ExternalEffectDelegate = startIntervalTimeoutEffectExternally(
            intervalDuration = intervalDuration,
        ) {
            handler.handle(Unit)
        }
    },
)

private fun WindowOrWorkerGlobalScope.startTimeoutEffectExternally(
    duration: Duration,
    handler: () -> Unit,
): ExternalEffectDelegate = object : ExternalEffectDelegate {
    private var timeoutId: Int? = setTimeout(
        handler = handler,
        timeout = duration.inWholeMilliseconds.toInt(),
    )

    override fun cancel() {
        val timeoutId = this.timeoutId ?: return

        clearTimeout(timeoutId)

        this.timeoutId = null
    }
}

private fun WindowOrWorkerGlobalScope.startIntervalTimeoutEffectExternally(
    intervalDuration: Duration,
    handler: () -> Unit,
): ExternalEffectDelegate = object : ExternalEffectDelegate {
    private var timeoutId: Int? = registerIntervalTimeoutHandler()

    private fun registerIntervalTimeoutHandler(): Int = setTimeout(
        handler = this::handleIntervalTimeout,
        timeout = intervalDuration.inWholeMilliseconds.toInt(),
    )

    private fun handleIntervalTimeout() {
        handler()

        timeoutId = registerIntervalTimeoutHandler()
    }

    override fun cancel() {
        val timeoutId = this.timeoutId ?: return

        clearTimeout(timeoutId)

        this.timeoutId = null
    }
}
