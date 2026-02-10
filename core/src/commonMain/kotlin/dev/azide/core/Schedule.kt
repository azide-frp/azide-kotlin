package dev.azide.core

import dev.azide.core.external.ExternalSchedule
import dev.azide.core.impl.effects.AdaptedExternalSchedule
import dev.azide.core.impl.effects.ExternalizedSchedule

typealias Schedule = Effect<Unit>

val Schedule.launch: Action<Effect.Handle>
    get() = start.map { outcome -> outcome.handle }

object Schedules {
    typealias Outcome = Effect.Outcome<Unit>

    fun adapt(
        externalSchedule: ExternalSchedule,
    ): Schedule = ExternalizedSchedule(
        AdaptedExternalSchedule(
            externalSchedule = externalSchedule,
        )
    )

    fun combine(
        schedules: Iterable<Schedule>,
    ): Schedule {
        TODO()
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
