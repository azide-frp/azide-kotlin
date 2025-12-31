package dev.azide.core

interface Effect<ResultT> {
    interface Handle {
        object Noop : Handle {
            override val cancel: Trigger = Action.Noop
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
