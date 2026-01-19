package dev.azide.core.effects

import dev.azide.core.Schedules
import dev.azide.core.effects.test_utils.CustomTimerManager
import dev.azide.core.executeExternally
import dev.azide.core.external.ExternalEffectDelegate
import dev.azide.core.external.ExternalSchedule
import dev.azide.core.startExternally
import dev.azide.core.test_utils.ExpectedTestSubjectTransition
import dev.azide.core.test_utils.ExpectedTestTargetImpact
import dev.azide.core.test_utils.effects.EffectTestUtils_cancelledRevoked
import dev.azide.core.test_utils.effects.EffectTestUtils_startRevoked
import dev.azide.core.test_utils.effects.EffectTestUtils_startRevoked_quickCancelled
import dev.azide.core.test_utils.effects.EffectTestUtils_startRevoked_quickCancelledRevoked
import dev.azide.core.test_utils.effects.EffectTestUtils_start_quickCancelled
import dev.azide.core.test_utils.effects.EffectTestUtils_start_quickCancelledRevoked
import dev.azide.core.test_utils.effects.TestSubjectPerceptionStrategy
import kotlin.test.Ignore
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

        EffectTestUtils_start_quickCancelled.executeStartTransaction(
            subjectEffect = subjectSchedule,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            expectedSubjectTransition = ExpectedTestSubjectTransition.None,
            expectedTargetImpact = ExpectedTestTargetImpact.None,
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

        EffectTestUtils_start_quickCancelledRevoked.executeStartTransaction(
            subjectEffect = subjectSchedule,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            expectedSubjectTransition = ExpectedTestSubjectTransition.None,
            expectedTargetImpact = ExpectedTestTargetImpact.None,
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

        EffectTestUtils_startRevoked.executeStartTransaction(
            subjectEffect = subjectSchedule,
            expectedTargetImpact = ExpectedTestTargetImpact.None,
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

        EffectTestUtils_startRevoked_quickCancelled.executeStartTransaction(
            subjectEffect = subjectSchedule,
            expectedTargetImpact = ExpectedTestTargetImpact.None,
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

        EffectTestUtils_startRevoked_quickCancelledRevoked.executeStartTransaction(
            subjectEffect = subjectSchedule,
            expectedTargetImpact = ExpectedTestTargetImpact.None,
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

        EffectTestUtils_cancelledRevoked.executeCancelTransaction(
            subjectOutcome = subjectOutcome,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            expectedSubjectTransition = ExpectedTestSubjectTransition.None,
            expectedTargetImpact = ExpectedTestTargetImpact.None,
        )

        assertEquals(
            expected = 1,
            actual = customTimerManager.startedTimerCount,
        )
    }
}
