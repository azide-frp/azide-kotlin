package dev.azide.core.impl.effects

import dev.azide.core.external.ExternalEventHandler
import dev.azide.core.external.ExternalStreamEffect
import dev.azide.core.external.bind
import dev.azide.core.impl.Revocable
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.event_stream.EventStreamVertex
import dev.azide.core.impl.event_stream.abstract_vertices.AbstractLiveEventStreamVertex
import dev.azide.core.impl.utils.LoopClosure

class AdaptedExternalStreamEffectVertex<EventT> private constructor(
    private val distributionScheduleVertex: AdaptedExternalScheduleVertex,
) : AbstractLiveEventStreamVertex<EventT>(), ExternalEventHandler<EventT>,
    EffectVertex by distributionScheduleVertex, Revocable by distributionScheduleVertex {
    companion object {
        fun <EventT> start(
            propagationContext: Transactions.PropagationContext,
            externalStreamEffectVertex: ExternalStreamEffect<EventT>,
        ): AdaptedExternalStreamEffectVertex<EventT> =
            ExternalEventHandler.looped { loopedEventHandler: ExternalEventHandler<EventT> ->
                // Start a schedule which distributes the external events within the reactive system
                val distributionScheduleVertex = AdaptedExternalScheduleVertex.startInternally(
                    propagationContext = propagationContext,
                    externalSchedule = externalStreamEffectVertex.bind(
                        handler = loopedEventHandler,
                    ),
                )

                val adaptedStreamVertex = AdaptedExternalStreamEffectVertex<EventT>(
                    distributionScheduleVertex = distributionScheduleVertex,
                )

                LoopClosure(
                    result = adaptedStreamVertex,
                    loopedValue = adaptedStreamVertex,
                )
            }
    }

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
}
