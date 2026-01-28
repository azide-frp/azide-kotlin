package dev.azide.core.effects

import dev.azide.core.Action
import dev.azide.core.Effect
import dev.azide.core.effects.test_utils.CustomTimerManager
import dev.azide.core.executeEachOf
import dev.azide.core.external.ExternalEffectDelegate
import dev.azide.core.external.ExternalEventHandler
import dev.azide.core.external.ExternalStreamEffect
import dev.azide.core.external.ExternalTrigger
import dev.azide.core.startExternally
import dev.azide.core.test_utils.EventStream_expectations_testUtils
import dev.azide.core.test_utils.ExpectedTestSubjectTransition
import dev.azide.core.test_utils.ExpectedImpact
import dev.azide.core.test_utils.effect_generic.Effect_generic_cancelled_testUtils
import dev.azide.core.test_utils.effect_generic.Effect_generic_cancelledRevoked_testUtils
import dev.azide.core.test_utils.effect_generic.Effect_generic_start_testUtils
import dev.azide.core.test_utils.effect_generic.Effect_generic_startRevoked_testUtils
import dev.azide.core.test_utils.effect_generic.Effect_generic_startRevoked_quickCancelled_testUtils
import dev.azide.core.test_utils.effect_generic.Effect_generic_startRevoked_quickCancelledRevoked_testUtils
import dev.azide.core.test_utils.effect_generic.Effect_generic_start_quickCancelled_testUtils
import dev.azide.core.test_utils.effect_generic.Effect_generic_start_quickCancelledRevoked_testUtils
import dev.azide.core.test_utils.effect_generic.TestSubjectPerceptionStrategy
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("ClassName")
class Effect_adapt_tests {
    class CustomTimerStreamEffect(
        private val timerManager: CustomTimerManager,
        private val intervalMs: Int,
    ) : ExternalStreamEffect<Int> {
        override fun start(
            handler: ExternalEventHandler<Int>,
        ): ExternalEffectDelegate {
            val timerHandle: CustomTimerManager.Handle = timerManager.startTimer(
                intervalMs = intervalMs,
                handler = object : CustomTimerManager.Handler {
                    override fun handleIntervalElapsed(actualElapsedTimeMs: Int) {
                        handler.handle(actualElapsedTimeMs)
                    }
                },
            )

            return object : ExternalEffectDelegate {
                override fun cancel() {
                    timerHandle.stop()
                }
            }
        }
    }

    @Test
    fun test_start_subscribed() {
        test_start(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
        )
    }

    @Test
    fun test_start_nonSubscribed() {
        test_start(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
        )
    }

    private fun test_start(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
    ) {
        val customTimerManager = CustomTimerManager()

        val subjectEffect = Effect.adapt(
            CustomTimerStreamEffect(
                timerManager = customTimerManager,
                intervalMs = 10,
            ),
        )

        Effect_generic_start_testUtils.executeStartTransaction(
            subjectEffect = subjectEffect,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            expectedSubjectTransition = EventStream_expectations_testUtils.expectNoEmission(),
            expectedTargetImpact = ExpectedImpact.None,
        )

        assertEquals(
            expected = 1,
            actual = customTimerManager.startedTimerCount,
        )
    }

    @Test
    fun test_start_quickCancelled_subscribed() {
        test_start_quickCancelled(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
        )
    }

    @Test
    fun test_start_quickCancelled_nonSubscribed() {
        test_start_quickCancelled(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
        )
    }

    private fun test_start_quickCancelled(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
    ) {
        val customTimerManager = CustomTimerManager()

        val subjectEffect = Effect.adapt(
            CustomTimerStreamEffect(
                timerManager = customTimerManager,
                intervalMs = 10,
            ),
        )

        Effect_generic_start_quickCancelled_testUtils.executeStartTransaction(
            subjectEffect = subjectEffect,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            expectedSubjectTransition = EventStream_expectations_testUtils.expectNoEmission(),
            expectedTargetImpact = ExpectedImpact.None,
        )

        assertEquals(
            expected = 0,
            actual = customTimerManager.startedTimerCount,
        )
    }

    @Test
    fun test_start_quickCancelledRevoked_subscribed() {
        test_start_quickCancelledRevoked(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.Perceived,
        )
    }

    @Test
    fun test_start_quickCancelledRevoked_nonSubscribed() {
        test_start_quickCancelledRevoked(
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
        )
    }

    private fun test_start_quickCancelledRevoked(
        subjectPerceptionStrategy: TestSubjectPerceptionStrategy,
    ) {
        val customTimerManager = CustomTimerManager()

        val subjectEffect = Effect.adapt(
            CustomTimerStreamEffect(
                timerManager = customTimerManager,
                intervalMs = 10,
            ),
        )

        Effect_generic_start_quickCancelledRevoked_testUtils.executeStartTransaction(
            subjectEffect = subjectEffect,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            expectedSubjectTransition = EventStream_expectations_testUtils.expectNoEmission(),
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

        val subjectEffect = Effect.adapt(
            CustomTimerStreamEffect(
                timerManager = customTimerManager,
                intervalMs = 10,
            ),
        )

        Effect_generic_startRevoked_testUtils.executeStartTransaction(
            subjectEffect = subjectEffect,
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

        val subjectEffect = Effect.adapt(
            CustomTimerStreamEffect(
                timerManager = customTimerManager,
                intervalMs = 10,
            ),
        )

        Effect_generic_startRevoked_quickCancelled_testUtils.executeStartTransaction(
            subjectEffect = subjectEffect,
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

        val subjectEffect = Effect.adapt(
            CustomTimerStreamEffect(
                timerManager = customTimerManager,
                intervalMs = 10,
            ),
        )

        Effect_generic_startRevoked_quickCancelledRevoked_testUtils.executeStartTransaction(
            subjectEffect = subjectEffect,
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

        val subjectEffect = Effect.adapt(
            CustomTimerStreamEffect(
                timerManager = customTimerManager,
                intervalMs = 10,
            ),
        )

        val subjectEventStream = subjectEffect.startExternally().result

        val receivedMeasures = mutableListOf<Int>()

        subjectEventStream.executeEachOf { event ->
            Action.adapt(
                externalTrigger = object : ExternalTrigger {
                    override fun executeExternally() {
                        receivedMeasures.add(event)
                    }
                },
            )
        }.startExternally()

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
        test_cancelled(
            cancelCount = 1,
        )
    }


    @Test
    fun test_cancelled_twice() {
        test_cancelled(
            cancelCount = 2,
        )
    }

    private fun test_cancelled(
        cancelCount: Int,
    ) {
        val customTimerManager = CustomTimerManager()

        val subjectEffect = Effect.adapt(
            CustomTimerStreamEffect(
                timerManager = customTimerManager,
                intervalMs = 10,
            ),
        )

        val subjectOutcome = subjectEffect.startExternally()

        Effect_generic_cancelled_testUtils.executeCancelTransaction(
            subjectOutcome = subjectOutcome,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            expectedSubjectTransition = ExpectedTestSubjectTransition.None,
            expectedTargetImpact = ExpectedImpact.None,
            cancelCount = cancelCount,
        )

        assertEquals(
            expected = 0,
            actual = customTimerManager.startedTimerCount,
        )
    }


    @Test
    fun test_cancelledRevoked() {
        val customTimerManager = CustomTimerManager()

        val subjectEffect = Effect.adapt(
            CustomTimerStreamEffect(
                timerManager = customTimerManager,
                intervalMs = 10,
            ),
        )

        val subjectOutcome = subjectEffect.startExternally()

        Effect_generic_cancelledRevoked_testUtils.executeCancelTransaction(
            subjectOutcome = subjectOutcome,
            subjectPerceptionStrategy = TestSubjectPerceptionStrategy.NonPerceived,
            expectedSubjectTransition = ExpectedTestSubjectTransition.None,
            expectedTargetImpact = ExpectedImpact.None,
        )

        assertEquals(
            expected = 1,
            actual = customTimerManager.startedTimerCount,
        )
    }
}
