package dev.azide.core

import dev.azide.core.external.ExternalSchedule
import dev.azide.core.impl.effects.AdaptedExternalSchedule
import dev.azide.core.impl.effects.CombinedSchedule
import dev.azide.core.impl.effects.ExternalizedSchedule

typealias Schedule = Effect<Unit>

val Schedule.launch: Action<Effect.Handle>
    get() = start.map { outcome -> outcome.handle }

typealias ScheduleOutcome = Effect.Outcome<Unit>

object Schedules {
    fun adapt(
        externalSchedule: ExternalSchedule,
    ): Schedule = ExternalizedSchedule(
        AdaptedExternalSchedule(
            externalSchedule = externalSchedule,
        )
    )

    fun combine(
        schedules: Iterable<Schedule>,
    ): Schedule = ExternalizedSchedule(
        internalEffect = CombinedSchedule(
            schedules = schedules,
        ),
    )
}

abstract class AbstractSchedule : Schedule {
    final override val start: Action<ScheduleOutcome>
        get() = launchImpl.map { handle ->
            Effect.Outcome.of(
                result = Unit,
                handle = handle,
            )
        }

    protected abstract val launchImpl: Action<Effect.Handle>
}
