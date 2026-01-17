package dev.azide.core.impl.effects

import dev.azide.core.Action
import dev.azide.core.Effect
import dev.azide.core.Trigger
import dev.azide.core.impl.Revocable
import dev.azide.core.impl.Transactions

abstract class AbstractPrimitiveEffect<EffectVertexT, ResultT> :
    Effect<ResultT> where EffectVertexT : EffectVertex, EffectVertexT : Revocable {
    final override val start: Action<Effect.Outcome<ResultT>> = object : Action<Effect.Outcome<ResultT>> {
        override fun executeInternally(
            propagationContext: Transactions.PropagationContext,
            wrapUpContext: Transactions.WrapUpContext,
        ): Action.Outcome<Effect.Outcome<ResultT>> {
            val effectVertex = startInternally(
                propagationContext = propagationContext,
                wrapUpContext = wrapUpContext,
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
                            ): Revocable = effectVertex.cancelInternally(
                                propagationContext = propagationContext,
                            )
                        }
                    },
                ),
                revocable = effectVertex,
            )
        }
    }

    abstract fun startInternally(
        propagationContext: Transactions.PropagationContext,
        wrapUpContext: Transactions.WrapUpContext,
    ): EffectVertexT

    abstract fun wrap(
        effectVertex: EffectVertexT,
    ): ResultT
}
