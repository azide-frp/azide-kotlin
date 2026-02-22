package dev.azide.core.effects

import dev.azide.core.Schedules
import dev.azide.core.effects.test_utils.CustomTimerManager
import dev.azide.core.executeExternally
import dev.azide.core.external.ExternalEffectDelegate
import dev.azide.core.external.ExternalSchedule
import dev.azide.core.startExternally
import dev.azide.core.test_utils.generic.ExpectedImpact
import dev.azide.core.test_utils.schedule.Schedule_cancelledRevoked_testUtils
import dev.azide.core.test_utils.schedule.Schedule_startRevoked_quickCancelledRevoked_testUtils
import dev.azide.core.test_utils.schedule.Schedule_startRevoked_quickCancelled_testUtils
import dev.azide.core.test_utils.schedule.Schedule_startRevoked_testUtils
import dev.azide.core.test_utils.schedule.Schedule_start_quickCancelledRevoked_testUtils
import dev.azide.core.test_utils.schedule.Schedule_start_quickCancelled_testUtils
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
    fun test_start() {
        val customTimerManager = CustomTimerManager()

        val subjectSchedule = Schedules.adapt(
            externalSchedule = CustomTimerSchedule(
                customTimerManager = customTimerManager,
                intervalMs = 10,
                handler = object : CustomTimerManager.Handler {
                    override fun handleIntervalElapsed(actualElapsedTimeMs: Int) {
                    }
                },
            ),
        )

        subjectSchedule.startExternally()

        assertEquals(
            expected = 1,
            actual = customTimerManager.startedTimerCount,
        )
    }

    @Test
    fun test_start_quickCancelled() {
        val customTimerManager = CustomTimerManager()

        val subjectSchedule = Schedules.adapt(
            externalSchedule = CustomTimerSchedule(
                customTimerManager = customTimerManager,
                intervalMs = 10,
                handler = object : CustomTimerManager.Handler {
                    override fun handleIntervalElapsed(actualElapsedTimeMs: Int) {
                    }
                },
            ),
        )

        Schedule_start_quickCancelled_testUtils.testStart(
            subjectSchedule = subjectSchedule,
            expectedTargetImpact = ExpectedImpact.None,
        )

        assertEquals(
            expected = 0,
            actual = customTimerManager.startedTimerCount,
        )
    }

    @Test
    fun test_start_quickCancelledRevoked() {
        val customTimerManager = CustomTimerManager()

        val subjectSchedule = Schedules.adapt(
            externalSchedule = CustomTimerSchedule(
                customTimerManager = customTimerManager,
                intervalMs = 10,
                handler = object : CustomTimerManager.Handler {
                    override fun handleIntervalElapsed(actualElapsedTimeMs: Int) {
                    }
                },
            ),
        )

        Schedule_start_quickCancelledRevoked_testUtils.testStart(
            subjectSchedule = subjectSchedule,
            expectedTargetImpact = ExpectedImpact.None,
        )

        assertEquals(
            expected = 1,
            actual = customTimerManager.startedTimerCount,
        )
    }

    @Test
    fun test_startRevoked() {
        val customTimerManager = CustomTimerManager()

        val subjectSchedule = Schedules.adapt(
            externalSchedule = CustomTimerSchedule(
                customTimerManager = customTimerManager,
                intervalMs = 10,
                handler = object : CustomTimerManager.Handler {
                    override fun handleIntervalElapsed(actualElapsedTimeMs: Int) {
                    }
                },
            ),
        )

        Schedule_startRevoked_testUtils.testStart(
            subjectSchedule = subjectSchedule,
            expectedTargetImpact = ExpectedImpact.None,
        )

        assertEquals(
            expected = 0,
            actual = customTimerManager.startedTimerCount,
        )
    }

    @Test
    fun test_startRevoked_quickCancelled() {
        val customTimerManager = CustomTimerManager()

        val subjectSchedule = Schedules.adapt(
            externalSchedule = CustomTimerSchedule(
                customTimerManager = customTimerManager,
                intervalMs = 10,
                handler = object : CustomTimerManager.Handler {
                    override fun handleIntervalElapsed(actualElapsedTimeMs: Int) {
                    }
                },
            ),
        )

        Schedule_startRevoked_quickCancelled_testUtils.testStart(
            subjectSchedule = subjectSchedule,
            expectedTargetImpact = ExpectedImpact.None,
        )

        assertEquals(
            expected = 0,
            actual = customTimerManager.startedTimerCount,
        )
    }

    @Test
    fun test_startRevoked_quickCancelledRevoked() {
        val customTimerManager = CustomTimerManager()

        val subjectSchedule = Schedules.adapt(
            externalSchedule = CustomTimerSchedule(
                customTimerManager = customTimerManager,
                intervalMs = 10,
                handler = object : CustomTimerManager.Handler {
                    override fun handleIntervalElapsed(actualElapsedTimeMs: Int) {
                    }
                },
            ),
        )

        Schedule_startRevoked_quickCancelledRevoked_testUtils.testStart(
            subjectSchedule = subjectSchedule,
            expectedTargetImpact = ExpectedImpact.None,
        )

        assertEquals(
            expected = 0,
            actual = customTimerManager.startedTimerCount,
        )
    }

    @Test
    fun test_step() {
        val customTimerManager = CustomTimerManager()

        val receivedMeasures = mutableListOf<Int>()

        val subjectSchedule = Schedules.adapt(
            externalSchedule = CustomTimerSchedule(
                customTimerManager = customTimerManager,
                intervalMs = 10,
                handler = object : CustomTimerManager.Handler {
                    override fun handleIntervalElapsed(actualElapsedTimeMs: Int) {
                        receivedMeasures.add(actualElapsedTimeMs)
                    }
                },
            ),
        )

        subjectSchedule.startExternally()

        customTimerManager.invokeAll(delayMs = 3)

        assertEquals(
            expected = 1,
            actual = customTimerManager.startedTimerCount,
        )

        assertEquals(
            expected = listOf(13),
            actual = receivedMeasures,
        )
    }

    @Test
    fun test_cancelled() {
        val customTimerManager = CustomTimerManager()

        val subjectSchedule = Schedules.adapt(
            externalSchedule = CustomTimerSchedule(
                customTimerManager = customTimerManager,
                intervalMs = 10,
                handler = object : CustomTimerManager.Handler {
                    override fun handleIntervalElapsed(actualElapsedTimeMs: Int) {
                    }
                },
            ),
        )

        val subjectHandle = subjectSchedule.startExternally().handle

        subjectHandle.cancel.executeExternally()

        assertEquals(
            expected = 0,
            actual = customTimerManager.startedTimerCount,
        )
    }

    @Test
    fun test_cancelledRevoked() {
        val customTimerManager = CustomTimerManager()

        val subjectSchedule = Schedules.adapt(
            externalSchedule = CustomTimerSchedule(
                customTimerManager = customTimerManager,
                intervalMs = 10,
                handler = object : CustomTimerManager.Handler {
                    override fun handleIntervalElapsed(actualElapsedTimeMs: Int) {
                    }
                },
            ),
        )

        val subjectOutcome = subjectSchedule.startExternally()

        Schedule_cancelledRevoked_testUtils.executeCancelTransaction(
            subjectOutcome = subjectOutcome,
            expectedTargetImpact = ExpectedImpact.None,
        )

        assertEquals(
            expected = 1,
            actual = customTimerManager.startedTimerCount,
        )
    }
}
