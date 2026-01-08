package dev.azide.core

import dev.azide.core.external.ExternalSchedule
import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.Transactions.WrapUpContext

typealias Schedule = Effect<Unit>

val Schedule.launch: Action<Effect.Handle>
    get() = start.map { outcome -> outcome.handle }

object Schedules {
    typealias Outcome = Effect.Outcome<Unit>

    fun adapt(
        externalSchedule: ExternalSchedule,
    ): Schedule = object : AbstractSchedule() {
        override val launchImpl: Action<Effect.Handle> = object : Action<Effect.Handle> {
            override fun executeInternally(
                propagationContext: PropagationContext,
                wrapUpContext: WrapUpContext,
            ): Action.Outcome<Effect.Handle> = TODO()
        }
    }
}

abstract class AbstractSchedule : Schedule {
    final override val start: Action<Schedules.Outcome>
        get() = launchImpl.map { handle ->
            Effect.Outcome.of(
                result = Unit,
                handle = handle,
            )
        }

    protected abstract val launchImpl: Action<Effect.Handle>
}
