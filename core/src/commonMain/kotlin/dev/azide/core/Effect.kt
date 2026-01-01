package dev.azide.core

interface Effect<ResultT> {
    interface Handle {
        object Noop : Handle {
            override val cancel: Trigger = Triggers.Noop
        }

        companion object {
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

    val start: Action<Pair<ResultT, Handle>>
}

typealias Schedule = Effect<Unit>

val Schedule.launch: Action<Effect.Handle>
    get() = start.map { (_, handle) -> handle }

fun <ResultT, TransformedResultT> Effect<ResultT>.map(
    transform: (ResultT) -> TransformedResultT,
): Effect<TransformedResultT> = object : Effect<TransformedResultT> {
    override val start: Action<Pair<TransformedResultT, Effect.Handle>> = this@map.start.map { (result, handle) ->
        Pair(
            transform(result),
            handle,
        )
    }
}

fun <ResultT, TransformedResultT> Effect<ResultT>.joinOf(
    transform: (ResultT) -> Effect<TransformedResultT>,
): Effect<TransformedResultT> = object : Effect<TransformedResultT> {
    override val start: Action<Pair<TransformedResultT, Effect.Handle>> =
        this@joinOf.start.joinOf { (result: ResultT, handle) ->
            val transformedEffect: Effect<TransformedResultT> = transform(result)

            transformedEffect.start.map { (transformedResult: TransformedResultT, transformedHandle) ->
                Pair(
                    transformedResult,
                    Effect.Handle.combine(
                        handle,
                        transformedHandle,
                    ),
                )
            }
        }
}

abstract class AbstractSchedule : Schedule {
    final override val start: Action<Pair<Unit, Effect.Handle>>
        get() = launchImpl.map { handle -> Pair(Unit, handle) }

    protected abstract val launchImpl: Action<Effect.Handle>
}
