package dev.azide.core

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
                cancel: Trigger,
            ): Handle = object : Handle {
                override val cancel: Trigger = cancel
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

    val start: Action<Outcome<ResultT>>
}

typealias Schedule = Effect<Unit>

val Schedule.launch: Action<Effect.Handle>
    get() = start.map { outcome -> outcome.handle }

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

abstract class AbstractSchedule : Schedule {
    final override val start: Action<Effect.Outcome<Unit>>
        get() = launchImpl.map { handle ->
            Effect.Outcome.of(
                result = Unit,
                handle = handle,
            )
        }

    protected abstract val launchImpl: Action<Effect.Handle>
}
