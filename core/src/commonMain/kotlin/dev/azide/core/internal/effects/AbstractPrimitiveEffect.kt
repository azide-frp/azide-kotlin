package dev.azide.core.internal.effects

import dev.azide.core.Action
import dev.azide.core.Effect
import dev.azide.core.Trigger
import dev.azide.core.internal.CommittableVertex
import dev.azide.core.internal.RevocationHandle
import dev.azide.core.internal.Transactions

abstract class AbstractPrimitiveEffect<EffectVertexT : EffectVertex, ResultT> : Effect<ResultT> {
    private class EffectManagementVertex<ResultT>(
        private val propagationContext: Transactions.PropagationContext,
        private val effectVertex: EffectVertex,
        effectResult: ResultT,
    ) : Action.Outcome<Effect.Outcome<ResultT>>, RevocationHandle, CommittableVertex {
        private enum class EffectState {
            Started, Stopped, Aborted,
        }

        private var effectState = EffectState.Started

        private var isEnqueuedForCommitment = false

        override val result: Effect.Outcome<ResultT> = Effect.Outcome.of(
            result = effectResult,
            handle = object : Effect.Handle {
                override val cancel: Trigger = object : AbstractExecutionMergingTrigger() {
                    /**
                     * Cancel the effect
                     */
                    override fun executeInternallyOnce(
                        propagationContext: Transactions.PropagationContext,
                        wrapUpContext: Transactions.WrapUpContext,
                    ): RevocationHandle {
                        ensureEnqueuedForCommitment(propagationContext = propagationContext)

                        when (effectState) {
                            EffectState.Started -> { // Healthy cancellation
                                effectVertex.stop(
                                    propagationContext = propagationContext,
                                )

                                effectState = EffectState.Stopped
                            }

                            EffectState.Stopped -> {
                                throw AssertionError("Unexpected effect state: $effectState")
                            }

                            EffectState.Aborted -> {
                                // An attempt to cancel the effect that is revoked or cancelled earlier
                            }
                        }

                        return object : RevocationHandle {
                            /**
                             * Revoke the effect's cancellation
                             */
                            override fun revoke() {
                                when (effectState) {
                                    EffectState.Started -> {
                                        throw AssertionError("Unexpected effect state: $effectState")
                                    }

                                    EffectState.Stopped -> { // Healthy cancellation revocation
                                        effectVertex.start(
                                            propagationContext = propagationContext,
                                        )

                                        effectState = EffectState.Started
                                    }

                                    EffectState.Aborted -> {
                                        // Typically a healthy revocation of a cancellation in consequence of the
                                        // revocation of the effect's start
                                    }
                                }
                            }
                        }
                    }
                }
            },
        )

        override val revocationHandle: RevocationHandle
            get() = this

        /**
         * Revoke the effect's start
         */
        override fun revoke() {
            when (effectState) {
                EffectState.Started -> { // A healthy effect's start revocation
                    effectVertex.stop(
                        propagationContext = propagationContext,
                    )

                    effectState = EffectState.Aborted
                }

                EffectState.Stopped -> { // Revocation of the effect's start after it was instantly cancelled
                    effectState = EffectState.Aborted
                }

                EffectState.Aborted -> {
                    throw AssertionError("Unexpected effect state: $effectState")
                }
            }
        }

        override fun commit() {
            if (effectState == EffectState.Stopped) {
                effectState = EffectState.Aborted
            }
        }

        private fun ensureEnqueuedForCommitment(
            propagationContext: Transactions.PropagationContext,
        ) {
            if (!isEnqueuedForCommitment) {
                propagationContext.enqueueForCommitment(this)

                isEnqueuedForCommitment = true
            }
        }

        init {
            effectVertex.start(
                propagationContext = propagationContext,
            )
        }
    }

    final override val start: Action<Effect.Outcome<ResultT>> = object : Action<Effect.Outcome<ResultT>> {
        override fun executeInternally(
            propagationContext: Transactions.PropagationContext,
            wrapUpContext: Transactions.WrapUpContext,
        ): EffectManagementVertex<ResultT> {
            val effectVertex = buildVertex()

            val effectResult = buildResult(effectVertex = effectVertex)

            return EffectManagementVertex(
                propagationContext = propagationContext,
                effectVertex = effectVertex,
                effectResult = effectResult,
            )
        }
    }

    abstract fun buildVertex(): EffectVertexT

    abstract fun buildResult(
        effectVertex: EffectVertexT,
    ): ResultT
}
