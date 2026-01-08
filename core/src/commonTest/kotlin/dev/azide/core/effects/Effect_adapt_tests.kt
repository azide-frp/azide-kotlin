package dev.azide.core.effects

import dev.azide.core.Effect
import dev.azide.core.effects.test_utils.CustomTimerManager
import dev.azide.core.external.ExternalEffectDelegate
import dev.azide.core.external.ExternalEventHandler
import dev.azide.core.external.ExternalStreamEffect
import dev.azide.core.test_utils.TransactionTestUtils
import dev.azide.core.test_utils.executeForTesting
import dev.azide.core.test_utils.executeForTestingRevocable
import dev.azide.core.test_utils.revokeForTesting
import dev.azide.core.test_utils.startForTestingCancellable
import dev.azide.core.test_utils.startForTestingRevocable
import dev.azide.core.test_utils.subscribeForTesting
import dev.azide.core.test_utils.verifyDidNotPropagateNorExposesEmission
import dev.azide.core.test_utils.verifyDoesNotExposeEmission
import dev.azide.core.test_utils.verifyPropagatedEmission
import kotlin.test.Test

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
    fun test_basic() {
        val customTimerManager = CustomTimerManager()

        val subjectEffect = Effect.adapt(
            CustomTimerStreamEffect(
                timerManager = customTimerManager,
                intervalMs = 10,
            ),
        )

        val (eventStreamSubscriber, effectHandle: Effect.Handle) = TransactionTestUtils.executeInsideTransaction {
            val (eventStream, effectHandle: Effect.Handle) = subjectEffect.startForTestingCancellable()

            val eventStreamSubscriber = eventStream.subscribeForTesting()

            Pair(
                eventStreamSubscriber, effectHandle,
            )
        }

        customTimerManager.invokeAll(delayMs = 3)

        eventStreamSubscriber.verifyPropagatedEmission(expectedEmittedEvent = 13)
        eventStreamSubscriber.verifyDoesNotExposeEmission()

        TransactionTestUtils.executeInsideTransaction {
            effectHandle.cancel.executeForTesting()
        }

        customTimerManager.invokeAll(delayMs = 2)

        eventStreamSubscriber.verifyDidNotPropagateNorExposesEmission()
    }

    @Test
    fun test_cancel_quick() {
        val customTimerManager = CustomTimerManager()

        val subjectEffect = Effect.adapt(
            CustomTimerStreamEffect(
                timerManager = customTimerManager,
                intervalMs = 10,
            ),
        )

        val eventStreamSubscriber = TransactionTestUtils.executeInsideTransaction {
            val (eventStream, effectHandle: Effect.Handle) = subjectEffect.startForTestingCancellable()

            val eventStreamSubscriber = eventStream.subscribeForTesting()

            effectHandle.cancel.executeForTesting()

            eventStreamSubscriber
        }

        customTimerManager.invokeAll(delayMs = 3)

        eventStreamSubscriber.verifyDidNotPropagateNorExposesEmission()
    }

    @Test
    fun test_start_revoked() {
        val customTimerManager = CustomTimerManager()

        val subjectEffect = Effect.adapt(
            CustomTimerStreamEffect(
                timerManager = customTimerManager,
                intervalMs = 10,
            ),
        )

        val eventStreamSubscriber = TransactionTestUtils.executeInsideTransaction {
            val (eventStream, revocationHandle) = subjectEffect.startForTestingRevocable()

            val eventStreamSubscriber = eventStream.subscribeForTesting()

            revocationHandle.revokeForTesting()

            eventStreamSubscriber
        }

        customTimerManager.invokeAll()

        eventStreamSubscriber.verifyDidNotPropagateNorExposesEmission()
    }

    @Test
    fun test_cancel_revoked() {
        val customTimerManager = CustomTimerManager()

        val subjectEffect = Effect.adapt(
            CustomTimerStreamEffect(
                timerManager = customTimerManager,
                intervalMs = 10,
            ),
        )

        val (eventStreamSubscriber, subjectEffectHandle) = TransactionTestUtils.executeInsideTransaction {
            val (eventStream, subjectEffectHandle) = subjectEffect.startForTestingCancellable()

            val eventStreamSubscriber = eventStream.subscribeForTesting()

            Pair(eventStreamSubscriber, subjectEffectHandle)
        }

        TransactionTestUtils.executeInsideTransaction {
            val (_, revocationHandle) = subjectEffectHandle.cancel.executeForTestingRevocable()
            revocationHandle.revokeForTesting()
        }

        customTimerManager.invokeAll()

        eventStreamSubscriber.verifyPropagatedEmission(expectedEmittedEvent = 11)
        eventStreamSubscriber.verifyDoesNotExposeEmission()
    }
}
