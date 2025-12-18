package dev.azide.test_utils

import dev.kmpx.js.FinalizationRegistry
import dev.kmpx.platform.PlatformWeakReference
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.time.Duration

object JsGarbageCollectorUtils {
    /**
     * @param minBlockSize The minimum size of the allocated memory block in bytes.
     * @param maxBlockSize The maximum size of the allocated memory block in bytes.
     * @param minAllocationDelay The minimum delay between allocations.
     * @param maxAllocationDelay The maximum delay between allocations.
     */
    data class PressureConfig(
        val minBlockSize: Int,
        val maxBlockSize: Int,
        val minAllocationDelay: Duration,
        val maxAllocationDelay: Duration,
    )

    private val finalizationRegistry =
        FinalizationRegistry<CancellableContinuation<Unit>, CancellableContinuation<Unit>> { continuation ->
            continuation.resume(Unit)
        }

    /**
     * Suspend until the target of this weak reference has been garbage collected.
     */
    suspend fun <T : Any> PlatformWeakReference<T>.awaitCollection() {
        suspendCancellableCoroutine { continuation ->
            when (val target = get()) {
                null -> {
                    continuation.resume(Unit)
                }

                else -> {
                    finalizationRegistry.register(
                        target = target,
                        heldValue = continuation,
                        unregisterToken = continuation,
                    )

                    continuation.invokeOnCancellation {
                        finalizationRegistry.unregister(
                            unregisterToken = continuation,
                        )
                    }
                }
            }
        }
    }

    /**
     * Run the given [block] while putting stress on the garbage collector by continuously allocating memory in the
     * background.
     */
    suspend fun <T> withContinuousGarbageCollectorPressure(
        config: PressureConfig,
        block: suspend () -> T,
    ) {
        runWithBackground(
            foregroundBlock = block,
            backgroundBlock = {
                applyGarbageCollectorPressureContinuously(
                    config = config,
                )
            },
        )
    }

    private suspend fun applyGarbageCollectorPressureContinuously(
        config: PressureConfig,
    ) {
        @Suppress("VariableNeverRead") var garbageArray: Any? = null

        var delay = config.minAllocationDelay
        var size = config.minBlockSize

        // We override the dispatcher to use the default one. By default, the dispatcher used during testing offers a so
        // called "delay-skipping", while these delays have to work in real time (as does the garbage collector).
        withContext(Dispatchers.Default) {
            @Suppress("AssignedValueIsNeverRead") while (true) {
                garbageArray = CharArray(size)

                delay(delay)

                size = (size * 2).coerceAtMost(config.maxBlockSize)
                delay = (delay * 2).coerceAtMost(config.maxAllocationDelay)
            }
        }
    }
}

private suspend fun <T> runWithBackground(
    foregroundBlock: suspend () -> T,
    backgroundBlock: suspend () -> Unit,
): T = coroutineScope {
    val backgroundJob = launch { backgroundBlock() }

    try {
        foregroundBlock()
    } finally {
        backgroundJob.cancelAndJoin()
    }
}
