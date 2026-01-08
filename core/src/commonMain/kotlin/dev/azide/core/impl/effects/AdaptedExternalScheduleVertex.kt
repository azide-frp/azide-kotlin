package dev.azide.core.impl.effects

import dev.azide.core.external.ExternalEffectDelegate
import dev.azide.core.external.ExternalSchedule
import dev.azide.core.impl.CommittableVertex
import dev.azide.core.impl.Revocable
import dev.azide.core.impl.Transactions
import kotlin.jvm.JvmInline

class AdaptedExternalScheduleVertex private constructor(
    propagationContext: Transactions.PropagationContext,
    private val externalSchedule: ExternalSchedule,
) : Revocable, EffectVertex, CommittableVertex {
    private sealed interface InternalState {
        @JvmInline
        value class Scheduled(
            val startRevocable: Revocable,
        ) : InternalState

        data object QuickCancelled : InternalState

        data object Revoked : InternalState

        data object AwaitingExternalStart : InternalState

        class StartedExternally(
            val externalEffectDelegate: ExternalEffectDelegate,
        ) : InternalState {
            var wasCancelledInternally: Boolean = false
        }

        data object CancelledExternally : InternalState
    }

    companion object {
        fun startInternally(
            propagationContext: Transactions.PropagationContext,
            externalSchedule: ExternalSchedule,
        ): AdaptedExternalScheduleVertex = AdaptedExternalScheduleVertex(
            propagationContext = propagationContext,
            externalSchedule = externalSchedule,
        )
    }

    private var internalState: InternalState = enqueueStart(
        propagationContext = propagationContext,
    )

    override fun cancelInternally(
        propagationContext: Transactions.PropagationContext,
    ): Revocable {
        when (val internalState = this.internalState) {
            is InternalState.Scheduled -> {
                // The schedule was (internally) cancelled before it ever had a chance to start externally. We'll need
                // to revoke the external schedule's start...
                internalState.startRevocable.revoke()
                this@AdaptedExternalScheduleVertex.internalState = InternalState.QuickCancelled

                return object : Revocable {
                    override fun revoke() {
                        // ...but as the internal cancellation can be revoked, we're ready to re-schedule the external
                        // schedule's start. In a certain sense, this revocation revokes another revocation.
                        this@AdaptedExternalScheduleVertex.internalState = enqueueStart(
                            propagationContext = propagationContext,
                        )
                    }
                }
            }

            is InternalState.StartedExternally -> {
                if (internalState.wasCancelledInternally) {
                    throw IllegalStateException("Cannot cancel an effect that is already being cancelled")
                }

                // The schedule was cancelled after it (successfully) started on the external side. Let's schedule its
                // external cancellation.

                val cancelRevocable = propagationContext.enqueueForExecution {
                    internalState.externalEffectDelegate.cancel()
                    this@AdaptedExternalScheduleVertex.internalState = InternalState.CancelledExternally
                }

                internalState.wasCancelledInternally = true

                return object : Revocable {
                    override fun revoke() {
                        cancelRevocable.revoke()

                        internalState.wasCancelledInternally = false
                    }
                }
            }

            InternalState.AwaitingExternalStart -> {
                // Seems like the external start failed with an exception. Not much we can do...
                return Revocable.Noop
            }

            InternalState.QuickCancelled, InternalState.CancelledExternally -> {
                // An attempt to cancel an already cancelled effect
                return Revocable.Noop
            }

            InternalState.Revoked -> {
                throw IllegalStateException("Cannot cancel an effect that has been revoked")
            }
        }
    }

    override fun commit() {
        if (internalState is InternalState.Scheduled) {
            // The transaction in which the schedule was started (internally!) ended. We know we'll not be revoking
            // the enqueued external schedule's start. We're waiting for the side effect which starts the external
            // schedule.

            internalState = InternalState.AwaitingExternalStart
        }
    }

    private fun enqueueStart(
        propagationContext: Transactions.PropagationContext,
    ): InternalState.Scheduled = InternalState.Scheduled(
        propagationContext.enqueueForExecution {
            internalState = InternalState.StartedExternally(
                externalEffectDelegate = externalSchedule.start(),
            )
        },
    )

    override fun revoke() {
        when (val internalState = this@AdaptedExternalScheduleVertex.internalState) {
            is InternalState.Scheduled -> {
                // The most typical revocation scenario

                internalState.startRevocable.revoke()

                this.internalState = InternalState.Revoked
            }

            is InternalState.QuickCancelled -> {
                // This is a possible scenario when the effect was first quickly cancelled, but then the start action
                // was revoked. There's nothing to do.

                this.internalState = InternalState.Revoked
            }

            else -> {
                throw IllegalStateException("Cannot revoke an effect in this state: $internalState")
            }
        }
    }

    init {
        propagationContext.enqueueForCommitment(this)
    }
}
