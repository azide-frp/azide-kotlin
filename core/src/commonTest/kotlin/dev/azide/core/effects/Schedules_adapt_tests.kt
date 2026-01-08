package dev.azide.core.effects

import dev.azide.core.Effect
import dev.azide.core.Schedules
import dev.azide.core.effects.test_utils.CustomTimerManager
import dev.azide.core.external.ExternalEffectDelegate
import dev.azide.core.external.ExternalSchedule
import dev.azide.core.test_utils.TransactionTestUtils
import dev.azide.core.test_utils.executeForTesting
import dev.azide.core.test_utils.executeForTestingRevocable
import dev.azide.core.test_utils.revokeForTesting
import dev.azide.core.test_utils.startForTestingCancellable
import dev.azide.core.test_utils.startForTestingRevocable
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("ClassName")
class Schedules_adapt_tests {
    class CustomTimerSchedule(
        val customTimerManager: CustomTimerManager,
        val intervalMs: Int,
        val handler: CustomTimerManager.Handler,
    ) : ExternalSchedule {
        override fun start(): ExternalEffectDelegate {
            val timerHandle = customTimerManager.startTimer(
                intervalMs = intervalMs,
                handler = handler,
            )

            return object : ExternalEffectDelegate {
                override fun cancel() {
                    timerHandle.stop()
                }
            }
        }
    }

    @Test
    fun test_basic() {
        val customTimerManager = CustomTimerManager()

        var handledCallbackCount = 0

        val subjectSchedule = Schedules.adapt(
            externalSchedule = CustomTimerSchedule(
                customTimerManager = customTimerManager,
                intervalMs = 10,
                handler = object : CustomTimerManager.Handler {
                    override fun handleIntervalElapsed(actualElapsedTimeMs: Int) {
                        ++handledCallbackCount
                    }
                },
            ),
        )

        val (_, effectHandle: Effect.Handle) = TransactionTestUtils.executeInsideTransaction {
            subjectSchedule.startForTestingCancellable()
        }

        customTimerManager.invokeAll()

        assertEquals(
            expected = 1,
            actual = handledCallbackCount,
        )

        TransactionTestUtils.executeInsideTransaction {
            effectHandle.cancel.executeForTesting()
        }

        customTimerManager.invokeAll()

        assertEquals(
            expected = 1,
            actual = handledCallbackCount,
        )
    }

    @Test
    fun test_cancel_quick() {
        val customTimerManager = CustomTimerManager()

        var handledCallbackCount = 0

        val subjectSchedule = Schedules.adapt(
            externalSchedule = CustomTimerSchedule(
                customTimerManager = customTimerManager,
                intervalMs = 10,
                handler = object : CustomTimerManager.Handler {
                    override fun handleIntervalElapsed(actualElapsedTimeMs: Int) {
                        ++handledCallbackCount
                    }
                },
            ),
        )

        TransactionTestUtils.executeInsideTransaction {
            val (_, effectHandle: Effect.Handle) = subjectSchedule.startForTestingCancellable()

            effectHandle.cancel.executeForTesting()
        }

        customTimerManager.invokeAll()

        assertEquals(
            expected = 0,
            actual = handledCallbackCount,
        )
    }

    @Test
    fun test_start_revoked() {
        val customTimerManager = CustomTimerManager()

        var handledCallbackCount = 0

        val subjectSchedule = Schedules.adapt(
            externalSchedule = CustomTimerSchedule(
                customTimerManager = customTimerManager,
                intervalMs = 10,
                handler = object : CustomTimerManager.Handler {
                    override fun handleIntervalElapsed(actualElapsedTimeMs: Int) {
                        ++handledCallbackCount
                    }
                },
            ),
        )

        TransactionTestUtils.executeInsideTransaction {
            val (_, revocationHandle) = subjectSchedule.startForTestingRevocable()
            revocationHandle.revokeForTesting()
        }

        customTimerManager.invokeAll()

        assertEquals(
            expected = 0,
            actual = handledCallbackCount,
        )
    }

    @Test
    fun test_cancel_revoked() {
        val customTimerManager = CustomTimerManager()

        var handledCallbackCount = 0

        val subjectSchedule = Schedules.adapt(
            externalSchedule = CustomTimerSchedule(
                customTimerManager = customTimerManager,
                intervalMs = 10,
                handler = object : CustomTimerManager.Handler {
                    override fun handleIntervalElapsed(actualElapsedTimeMs: Int) {
                        ++handledCallbackCount
                    }
                },
            ),
        )

        val (_, subjectEffectHandle: Effect.Handle) = TransactionTestUtils.executeInsideTransaction {
            subjectSchedule.startForTestingCancellable()
        }

        TransactionTestUtils.executeInsideTransaction {
            val (_, revocationHandle) = subjectEffectHandle.cancel.executeForTestingRevocable()
            revocationHandle.revokeForTesting()
        }

        customTimerManager.invokeAll()

        assertEquals(
            expected = 1,
            actual = handledCallbackCount,
        )

        TransactionTestUtils.executeInsideTransaction {
            subjectEffectHandle.cancel.executeForTesting()
        }

        customTimerManager.invokeAll()

        assertEquals(
            expected = 1,
            actual = handledCallbackCount,
        )
    }
}
