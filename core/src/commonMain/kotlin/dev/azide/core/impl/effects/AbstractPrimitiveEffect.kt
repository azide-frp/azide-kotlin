package dev.azide.core.impl.effects

import dev.azide.core.Action
import dev.azide.core.Effect
import dev.azide.core.Trigger
import dev.azide.core.impl.RevocationHandle
import dev.azide.core.impl.Transactions

abstract class AbstractPrimitiveEffect<EffectVertexT, ResultT> :
    Effect<ResultT> where EffectVertexT : EffectVertex, EffectVertexT : RevocationHandle {
    final override val start: Action<Effect.Outcome<ResultT>> = object : Action<Effect.Outcome<ResultT>> {
        override fun executeInternally(
            propagationContext: Transactions.PropagationContext,
            wrapUpContext: Transactions.WrapUpContext,
        ): Action.Outcome<Effect.Outcome<ResultT>> {
            val effectVertex = startInternally(
                propagationContext = propagationContext,
            )

            val effectResult = wrap(effectVertex = effectVertex)

            return Action.Outcome.of(
                Effect.Outcome.of(
                    result = effectResult,
                    handle = object : Effect.Handle {
                        override val cancel: Trigger = object : AbstractExecutionMergingTrigger() {
                            override fun executeInternallyOnce(
                                propagationContext: Transactions.PropagationContext,
                                wrapUpContext: Transactions.WrapUpContext,
                            ): RevocationHandle = effectVertex.cancelInternally(
                                propagationContext = propagationContext,
                            )
                        }
                    },
                ),
                revocationHandle = effectVertex,
            )
        }
    }

    abstract fun startInternally(
        propagationContext: Transactions.PropagationContext,
    ): EffectVertexT

    abstract fun wrap(
        effectVertex: EffectVertexT,
    ): ResultT
}
