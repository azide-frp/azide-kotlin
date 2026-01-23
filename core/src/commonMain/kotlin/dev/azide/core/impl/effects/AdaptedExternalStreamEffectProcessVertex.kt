package dev.azide.core.impl.effects

import dev.azide.core.external.ExternalEventHandler
import dev.azide.core.external.ExternalStreamEffect
import dev.azide.core.external.bind
import dev.azide.core.impl.Revocable
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.event_stream.EventStreamVertex
import dev.azide.core.impl.event_stream.abstract_vertices.AbstractLiveEventStreamVertex

class AdaptedExternalStreamEffectProcessVertex<EventT>(
    externalStreamEffectVertex: ExternalStreamEffect<EventT>,
) : AbstractLiveEventStreamVertex<EventT>(), ProcessVertex, ExternalEventHandler<EventT> {
    private val eventDistributionEffectVertex: AdaptedExternalScheduleProcessVertex =
        AdaptedExternalScheduleProcessVertex(
            externalSchedule = externalStreamEffectVertex.bind(
                handler = this,
            ),
        )

    override fun handle(event: EventT) {
        Transactions.execute { propagationContext ->
            exposeEmissionNotifyingListeners(
                propagationContext = propagationContext,
                emission = EventStreamVertex.Emission(
                    emittedEvent = event,
                ),
            )
        }
    }

    override fun startInternally(
        propagationContext: PropagationContext,
    ): Revocable = eventDistributionEffectVertex.startInternally(
        propagationContext = propagationContext,
    )

    override fun cancelInternally(
        propagationContext: PropagationContext,
    ): Revocable = eventDistributionEffectVertex.cancelInternally(
        propagationContext = propagationContext,
    )
}
