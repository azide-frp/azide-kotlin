package dev.azide.core

import dev.azide.core.Triggers.merging
import dev.azide.core.external.ExternalStreamEffect
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.effects.AbstractPrimitiveEffect
import dev.azide.core.impl.effects.AdaptedExternalStreamEffectVertex

interface Effect<ResultT> {
    interface Outcome<ResultT> {
        companion object {
            fun <ResultT> of(
                result: ResultT,
                handle: Handle,
            ): Outcome<ResultT> = object : Outcome<ResultT> {
                override val result: ResultT = result
                override val handle: Handle = handle
            }
        }

        val result: ResultT
        val handle: Handle
    }

    interface Handle {
        object Noop : Handle {
            override val cancel: Trigger = Triggers.Noop
        }

        companion object {
            fun of(
                cancelOnce: Trigger,
            ): Action<Handle> = cancelOnce.merging().map { cancel ->
                object : Handle {
                    override val cancel: Trigger = cancel
                }
            }

            fun combine(
                firstSubHandle: Handle,
                secondSubHandle: Handle,
            ): Handle = object : Handle {
                override val cancel: Trigger = Triggers.combine(
                    firstSubHandle.cancel,
                    secondSubHandle.cancel,
                )
            }
        }

        val cancel: Trigger
    }

    companion object {
        fun <EventT> adapt(
            externalStreamEffect: ExternalStreamEffect<EventT>,
        ): Effect<EventStream<EventT>> =
            object : AbstractPrimitiveEffect<AdaptedExternalStreamEffectVertex<EventT>, EventStream<EventT>>() {
                override fun startInternally(
                    propagationContext: PropagationContext,
                    wrapUpContext: Transactions.WrapUpContext,
                ): AdaptedExternalStreamEffectVertex<EventT> = AdaptedExternalStreamEffectVertex.start(
                    propagationContext = propagationContext,
                    externalStreamEffectVertex = externalStreamEffect,
                )

                override fun wrap(
                    effectVertex: AdaptedExternalStreamEffectVertex<EventT>,
                ): EventStream<EventT> = EventStream.Ordinary(
                    vertex = effectVertex,
                )
            }
    }

    val start: Action<Outcome<ResultT>>
}

fun <ResultT, TransformedResultT> Effect<ResultT>.map(
    transform: (ResultT) -> TransformedResultT,
): Effect<TransformedResultT> = object : Effect<TransformedResultT> {
    override val start: Action<Effect.Outcome<TransformedResultT>> = this@map.start.map { outcome ->
        Effect.Outcome.of(
            result = transform(outcome.result),
            handle = outcome.handle,
        )
    }
}

fun <ResultT, TransformedResultT> Effect<ResultT>.joinOf(
    transform: (ResultT) -> Effect<TransformedResultT>,
): Effect<TransformedResultT> = object : Effect<TransformedResultT> {
    override val start: Action<Effect.Outcome<TransformedResultT>> =
        this@joinOf.start.joinOf { outcome: Effect.Outcome<ResultT> ->
            val transformedEffect: Effect<TransformedResultT> = transform(outcome.result)

            transformedEffect.start.map { transformedOutcome: Effect.Outcome<TransformedResultT> ->
                Effect.Outcome.of(
                    result = transformedOutcome.result,
                    handle = Effect.Handle.combine(
                        outcome.handle,
                        transformedOutcome.handle,
                    ),
                )
            }
        }
}

fun <ResultT> Effect<ResultT>.startExternally(): Effect.Outcome<ResultT> = Transactions.executeWithResult {
    val (effectOutcome: Effect.Outcome<ResultT>, _) = start.executeInternallyWrappedUp(it)

    effectOutcome
}
