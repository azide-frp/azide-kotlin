package dev.azide.core.impl.effects

import dev.azide.core.Action
import dev.azide.core.Effect
import dev.azide.core.Trigger
import dev.azide.core.impl.Revocable
import dev.azide.core.impl.Transactions

class ExternalizedEffect<ResultT>(
    internalEffect: InternalEffect<ResultT>,
) : Effect<ResultT> {
    override val start: Action<Effect.Outcome<ResultT>> = object : Action<Effect.Outcome<ResultT>> {
        override fun executeInternally(
            propagationContext: Transactions.PropagationContext,
            wrapUpContext: Transactions.WrapUpContext,
        ): Action.Outcome<Effect.Outcome<ResultT>> {
            val internalRevocableOutcome = internalEffect.startInternally(
                propagationContext = propagationContext,
                wrapUpContext = wrapUpContext,
            )

            return object : Action.Outcome<Effect.Outcome<ResultT>> {
                override val result = object : Effect.Outcome<ResultT> {
                    override val result: ResultT = internalRevocableOutcome.result

                    override val handle = object : Effect.Handle {
                        override val cancel: Trigger = object : AbstractExecutionMergingTrigger() {
                            override fun executeInternallyOnce(
                                propagationContext: Transactions.PropagationContext,
                                wrapUpContext: Transactions.WrapUpContext,
                            ): Revocable = internalRevocableOutcome.cancelInternally(
                                propagationContext = propagationContext,
                                wrapUpContext = wrapUpContext,
                            )
                        }
                    }
                }

                override val revocable = internalRevocableOutcome
            }
        }
    }
}

typealias ExternalizedSchedule = ExternalizedEffect<Unit>
