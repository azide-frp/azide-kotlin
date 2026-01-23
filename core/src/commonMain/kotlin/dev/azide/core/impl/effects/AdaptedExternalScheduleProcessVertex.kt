package dev.azide.core.impl.effects

import dev.azide.core.external.ExternalEffectDelegate
import dev.azide.core.external.ExternalSchedule
import dev.azide.core.impl.Revocable
import dev.azide.core.impl.Transactions.PropagationContext

class AdaptedExternalScheduleProcessVertex(
    private val externalSchedule: ExternalSchedule,
) : ProcessVertex {
    private var externalEffectDelegate: ExternalEffectDelegate? = null

    override fun startInternally(
        propagationContext: PropagationContext,
    ): Revocable = propagationContext.enqueueForExecution {
        if (externalEffectDelegate != null) {
            throw IllegalStateException("External schedule has already been started.")
        }

        externalEffectDelegate = externalSchedule.start()
    }

    override fun cancelInternally(
        propagationContext: PropagationContext,
    ): Revocable = propagationContext.enqueueForExecution {
        val externalEffectDelegate = this.externalEffectDelegate ?: run {
            // The only reasonable explanation for this is that the external schedule start threw an exception.
            return@enqueueForExecution
        }

        externalEffectDelegate.cancel()

        this.externalEffectDelegate = null
    }
}
