package dev.azide.core.impl.effects

import dev.azide.core.Action
import dev.azide.core.Effect
import dev.azide.core.Schedule
import dev.azide.core.impl.Revocable
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Transactions.PropagationContext

class CombinedSchedule(
    private val schedules: Iterable<Schedule>,
) : InternalSchedule {
    override fun startInternally(
        propagationContext: PropagationContext,
        wrapUpContext: Transactions.WrapUpContext,
    ): InternalEffect.RevocableOutcome<Unit> {
        val subOutcomes: List<Action.Outcome<Effect.Outcome<Unit>>> = schedules.map { schedule ->
            schedule.start.executeInternally(
                propagationContext = propagationContext,
                wrapUpContext = wrapUpContext,
            )
        }

        return object : InternalEffect.RevocableOutcome<Unit> {
            override val result = Unit

            override fun cancelInternally(
                propagationContext: PropagationContext,
                wrapUpContext: Transactions.WrapUpContext,
            ): Revocable {
                val subCancellationOutcomes = subOutcomes.map {
                    it.result.handle.cancel.executeInternally(
                        propagationContext = propagationContext, wrapUpContext = wrapUpContext
                    )
                }

                return Revocable.combine(
                    subCancellationOutcomes.map { it.revocable },
                )
            }

            override fun revoke() {
                subOutcomes.forEach {
                    it.revocable.revoke()
                }
            }
        }
    }
}
