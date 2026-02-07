package dev.azide.core.impl.effects

import dev.azide.core.EventStream
import dev.azide.core.external.ExternalEventHandler
import dev.azide.core.external.ExternalStreamEffect
import dev.azide.core.external.bind
import dev.azide.core.impl.Revocable
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.event_stream.EventStreamVertex
import dev.azide.core.impl.event_stream.abstract_vertices.AbstractLiveEventStreamVertex

class AdaptedExternalStreamVertex<EventT>() : AbstractLiveEventStreamVertex<EventT>(), ExternalEventHandler<EventT> {
    class AdaptationEffect<EventT>(
        private val externalStreamEffect: ExternalStreamEffect<EventT>,
    ) : InternalEffect<EventStream<EventT>> {
        override fun startInternally(
            propagationContext: PropagationContext,
            wrapUpContext: Transactions.WrapUpContext,
        ): InternalEffect.RevocableOutcome<EventStream<EventT>> {
            val subjectVertex = AdaptedExternalStreamVertex<EventT>()

            val helperOutcome: InternalEffect.RevocableOutcome<Unit> = AdaptedExternalSchedule(
                externalSchedule = externalStreamEffect.bind(
                    handler = subjectVertex,
                ),
            ).startInternally(
                propagationContext = propagationContext,
                wrapUpContext = wrapUpContext,
            )

            return object : InternalEffect.RevocableOutcome<EventStream<EventT>>, Revocable by helperOutcome {
                override val result = EventStream.Ordinary(
                    vertex = subjectVertex,
                )

                /**
                 * Cancel the EventStream adaptation effect.
                 */
                override fun cancelInternally(
                    propagationContext: PropagationContext,
                    wrapUpContext: Transactions.WrapUpContext,
                ): Revocable = helperOutcome.cancelInternally(
                    propagationContext = propagationContext,
                    wrapUpContext = wrapUpContext,
                )
            }
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
