package dev.azide.core.impl.effects

import dev.azide.core.Action
import dev.azide.core.Effect
import dev.azide.core.Trigger
import dev.azide.core.impl.Revocable
import dev.azide.core.impl.Transactions

abstract class AbstractExternalizedEffect<SubjectT : InternalEffect.Subject, ResultT>(
    internalEffect: InternalEffect<SubjectT>,
) : Effect<ResultT> {
    final override val start: Action<Effect.Outcome<ResultT>> = object : Action<Effect.Outcome<ResultT>> {
        override fun executeInternally(
            propagationContext: Transactions.PropagationContext,
            wrapUpContext: Transactions.WrapUpContext,
        ): Action.Outcome<Effect.Outcome<ResultT>> {
            val internalRevocableOutcome = internalEffect.startInternally(
                propagationContext = propagationContext,
                wrapUpContext = wrapUpContext,
            )

            return object : Action.Outcome<Effect.Outcome<ResultT>> {
                private val subject = internalRevocableOutcome.subject

                override val result = object : Effect.Outcome<ResultT> {
                    override val result: ResultT = wrap(subject = subject)

                    override val handle = object : Effect.Handle {
                        override val cancel: Trigger = object : AbstractExecutionMergingTrigger() {
                            override fun executeInternallyOnce(
                                propagationContext: Transactions.PropagationContext,
                                wrapUpContext: Transactions.WrapUpContext,
                            ): Revocable = subject.cancelInternally(
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

    abstract fun wrap(
        subject: SubjectT,
    ): ResultT
}
