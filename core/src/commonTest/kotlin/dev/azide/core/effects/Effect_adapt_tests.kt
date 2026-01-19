package dev.azide.core.effects

import dev.azide.core.Action
import dev.azide.core.Effect
import dev.azide.core.effects.test_utils.CustomTimerManager
import dev.azide.core.executeEachOf
import dev.azide.core.executeExternally
import dev.azide.core.external.ExternalEffectDelegate
import dev.azide.core.external.ExternalEventHandler
import dev.azide.core.external.ExternalStreamEffect
import dev.azide.core.external.ExternalTrigger
import dev.azide.core.startExternally
import dev.azide.core.test_utils.ExpectedEventStreamReactionTestUtils
import dev.azide.core.test_utils.ExpectedTestSubjectTransition
import dev.azide.core.test_utils.ExpectedTestTargetImpact
import dev.azide.core.test_utils.effects.EffectTestUtils_cancelled
import dev.azide.core.test_utils.effects.EffectTestUtils_cancelledRevoked
import dev.azide.core.test_utils.effects.EffectTestUtils_start
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

        EffectTestUtils_start.executeStartTransaction(
            subjectEffect = subjectEffect,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            expectedSubjectTransition = ExpectedEventStreamReactionTestUtils.expectNoEmission(),
            expectedTargetImpact = ExpectedTestTargetImpact.None,
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

        EffectTestUtils_start_quickCancelled.executeStartTransaction(
            subjectEffect = subjectEffect,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            expectedSubjectTransition = ExpectedEventStreamReactionTestUtils.expectNoEmission(),
            expectedTargetImpact = ExpectedTestTargetImpact.None,
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

        EffectTestUtils_start_quickCancelledRevoked.executeStartTransaction(
            subjectEffect = subjectEffect,
            subjectPerceptionStrategy = subjectPerceptionStrategy,
            expectedSubjectTransition = ExpectedEventStreamReactionTestUtils.expectNoEmission(),
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

        val subjectEffect = Effect.adapt(
            CustomTimerStreamEffect(
                timerManager = customTimerManager,
                intervalMs = 10,
            ),
        )

        EffectTestUtils_startRevoked.executeStartTransaction(
            subjectEffect = subjectEffect,
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

        val subjectEffect = Effect.adapt(
            CustomTimerStreamEffect(
                timerManager = customTimerManager,
                intervalMs = 10,
            ),
        )

        EffectTestUtils_startRevoked_quickCancelled.executeStartTransaction(
            subjectEffect = subjectEffect,
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

        val subjectEffect = Effect.adapt(
            CustomTimerStreamEffect(
                timerManager = customTimerManager,
                intervalMs = 10,
            ),
        )

        EffectTestUtils_startRevoked_quickCancelledRevoked.executeStartTransaction(
            subjectEffect = subjectEffect,
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
        val customTimerManager = CustomTimerManager()

        val subjectEffect = Effect.adapt(
            CustomTimerStreamEffect(
                timerManager = customTimerManager,
                intervalMs = 10,
            ),
        )

        val subjectOutcome = subjectEffect.startExternally()

        EffectTestUtils_cancelled.executeCancelTransaction(
            subjectOutcome = subjectOutcome,
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
    fun test_cancelledRevoked() {
        val customTimerManager = CustomTimerManager()

        val subjectEffect = Effect.adapt(
            CustomTimerStreamEffect(
                timerManager = customTimerManager,
                intervalMs = 10,
            ),
        )

        val subjectOutcome = subjectEffect.startExternally()

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
