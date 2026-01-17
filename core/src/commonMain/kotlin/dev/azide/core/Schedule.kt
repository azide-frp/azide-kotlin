package dev.azide.core

import dev.azide.core.external.ExternalSchedule
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.effects.AbstractPrimitiveSchedule
import dev.azide.core.impl.effects.AdaptedExternalScheduleVertex

typealias Schedule = Effect<Unit>

val Schedule.launch: Action<Effect.Handle>
    get() = start.map { outcome -> outcome.handle }

object Schedules {
    typealias Outcome = Effect.Outcome<Unit>

    fun adapt(
        externalSchedule: ExternalSchedule,
    ): Schedule = object : AbstractPrimitiveSchedule<AdaptedExternalScheduleVertex>() {
        override fun startInternally(
            propagationContext: PropagationContext,
            wrapUpContext: Transactions.WrapUpContext,
        ): AdaptedExternalScheduleVertex = AdaptedExternalScheduleVertex.startInternally(
            propagationContext = propagationContext,
            externalSchedule = externalSchedule,
        )
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
