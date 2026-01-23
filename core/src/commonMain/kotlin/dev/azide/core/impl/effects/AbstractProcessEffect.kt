package dev.azide.core.impl.effects

import dev.azide.core.Action
import dev.azide.core.Effect
import dev.azide.core.Trigger
import dev.azide.core.impl.Revocable
import dev.azide.core.impl.Transactions

abstract class AbstractProcessEffect<ProcessVertexT, ResultT> :
    Effect<ResultT> where ProcessVertexT : ProcessVertex {

    private enum class InternalState {
        New, Starting, QuickCancelled, StartRevoked, Running, OrdinarilyCancelled,
    }

    final override val start: Action<Effect.Outcome<ResultT>> = object : Action<Effect.Outcome<ResultT>> {
        override fun executeInternally(
            propagationContext: Transactions.PropagationContext,
            wrapUpContext: Transactions.WrapUpContext,
        ): Action.Outcome<Effect.Outcome<ResultT>> = object : Action.Outcome<Effect.Outcome<ResultT>> {
            private val internalEffectVertex: ProcessVertexT = buildProcessVertex()

            private var _internalStartRevocable: Revocable? = null

            private var _internalState = InternalState.New

            override val result = object : Effect.Outcome<ResultT> {
                override val result: ResultT = wrap(effectVertex = internalEffectVertex)

                override val handle = object : Effect.Handle {
                    override val cancel: Trigger = object : AbstractExecutionMergingTrigger() {
                        override fun executeInternallyOnce(
                            propagationContext: Transactions.PropagationContext,
                            wrapUpContext: Transactions.WrapUpContext,
                        ): Revocable {
                            when (val internalState = _internalState) {
                                InternalState.Starting -> {
                                    val internalEffectStartRevocable = _internalStartRevocable
                                        ?: throw IllegalStateException("Inconsistent internal state")

                                    internalEffectStartRevocable.revoke()

                                    _internalStartRevocable = null

                                    _internalState = InternalState.QuickCancelled

                                    return object : Revocable {
                                        override fun revoke() {
                                            if (_internalState == InternalState.StartRevoked) {
                                                // The effect was first quick-cancelled but then the start operation got
                                                // revoked, followed by the revocation of the quick-cancel. There's
                                                // nothing to do.
                                                return
                                            }

                                            if (_internalState != InternalState.QuickCancelled) {
                                                throw IllegalStateException("Unexpected internal state: $_internalState")
                                            }

                                            if (_internalStartRevocable != null) {
                                                throw IllegalStateException("Inconsistent internal state")
                                            }

                                            _internalStartRevocable = internalEffectVertex.startInternally(
                                                propagationContext = propagationContext,
                                            )

                                            _internalState = InternalState.Starting
                                        }
                                    }
                                }

                                InternalState.Running -> {
                                    val internalCancelRevocable = internalEffectVertex.cancelInternally(
                                        propagationContext = propagationContext,
                                    )

                                    _internalState = InternalState.OrdinarilyCancelled

                                    return object : Revocable {
                                        override fun revoke() {
                                            if (_internalState != InternalState.OrdinarilyCancelled) {
                                                throw IllegalStateException("Unexpected internal state: $internalState")
                                            }

                                            internalCancelRevocable.revoke()

                                            _internalState = InternalState.Running
                                        }
                                    }
                                }

                                else -> {
                                    throw IllegalStateException("Unexpected internal state: $internalState")
                                }
                            }
                        }
                    }
                }
            }

            override val revocable = object : Revocable {
                override fun revoke() {
                    when (val internalState = _internalState) {
                        InternalState.Starting -> {
                            val internalEffectStartRevocable =
                                _internalStartRevocable ?: throw IllegalStateException("Inconsistent internal state")

                            internalEffectStartRevocable.revoke()

                            _internalStartRevocable = null
                        }

                        InternalState.QuickCancelled -> {
                            // The effect was first quickly cancelled, but then the start action was revoked. There's
                            // nothing to do.

                            if (_internalStartRevocable != null) {
                                throw IllegalStateException("Inconsistent internal state")
                            }
                        }

                        else -> {
                            throw IllegalStateException("Unexpected internal state: $internalState")
                        }
                    }

                    _internalState = InternalState.StartRevoked
                }
            }

            init {
                wrapUpContext.enqueueForWrapUp {
                    if (_internalState != InternalState.New) {
                        throw IllegalStateException("Unexpected internal state: $_internalState")
                    }

                    _internalStartRevocable = internalEffectVertex.startInternally(
                        propagationContext = propagationContext,
                    )

                    _internalState = InternalState.Starting
                }

                propagationContext.enqueueCallbackForCommitment {
                    if (_internalState == InternalState.Starting) {
                        // The transaction in which the effect was started internally reach past the propagation phase. We
                        // know we'll not be revoking the effect's start.

                        _internalStartRevocable = null
                        _internalState = InternalState.Running
                    }
                }
            }
        }
    }

    abstract fun buildProcessVertex(): ProcessVertexT

    abstract fun wrap(
        effectVertex: ProcessVertexT,
    ): ResultT
}
