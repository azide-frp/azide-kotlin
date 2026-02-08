package dev.azide.core.impl.effects

import dev.azide.core.external.ExternalEffectDelegate
import dev.azide.core.external.ExternalSchedule
import dev.azide.core.impl.Revocable
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Transactions.PropagationContext

class AdaptedExternalSchedule(
    private val externalSchedule: ExternalSchedule,
) : InternalSchedule {
    private var externalEffectDelegate: ExternalEffectDelegate? = null

    override fun startInternally(
        propagationContext: PropagationContext,
        wrapUpContext: Transactions.WrapUpContext,
    ): InternalEffect.RevocableOutcome<Unit> {
        val revocable = propagationContext.enqueueForExecution {
            if (externalEffectDelegate != null) {
                throw IllegalStateException("External schedule has already been started.")
            }

            externalEffectDelegate = externalSchedule.start()
        }

        return object : InternalEffect.RevocableOutcome<Unit>, Revocable by revocable {
            override val result = Unit

            override fun cancelInternally(
                propagationContext: PropagationContext,
                wrapUpContext: Transactions.WrapUpContext,
            ): Revocable = propagationContext.enqueueForExecution {
                val externalEffectDelegate = this@AdaptedExternalSchedule.externalEffectDelegate ?: run {
                    // The only reasonable explanation for this is that the external schedule start threw an exception.
                    return@enqueueForExecution
                }

                externalEffectDelegate.cancel()

                this@AdaptedExternalSchedule.externalEffectDelegate = null
            }
        }
    }
}
