package dev.azide.core.test_utils.event_stream

import dev.azide.core.EventStream
import dev.azide.core.internal.Transactions
import dev.azide.core.internal.event_stream.EventStreamVertex
import dev.azide.core.internal.event_stream.EventStreamVertex.Emission
import dev.azide.core.internal.event_stream.abstract_vertices.AbstractLiveEventStreamVertex
import dev.azide.core.test_utils.TestInputStimulation

internal class TestInputEventStream<EventT>() : EventStream<EventT> {
    private val _vertex = object : AbstractLiveEventStreamVertex<EventT>() {
        fun emit(
            propagationContext: Transactions.PropagationContext,
            emittedEvent: EventT,
        ) {
            if (ongoingEmission != null) {
                throw IllegalStateException("Another emission is already ongoing.")
            }

            exposeAndPropagateEmission(
                propagationContext = propagationContext,
                emission = Emission(
                    emittedEvent = emittedEvent,
                ),
            )
        }

        fun revokeEmission(
            propagationContext: Transactions.PropagationContext,
        ) {
            if (ongoingEmission == null) {
                throw IllegalStateException("No ongoing emission to revoke.")
            }

            exposeAndPropagateEmission(
                propagationContext = propagationContext,
                emission = null,
            )
        }

        fun correctEmission(
            propagationContext: Transactions.PropagationContext,
            correctedEmittedEvent: EventT,
        ) {
            if (ongoingEmission == null) {
                throw IllegalStateException("No ongoing emission to correct.")
            }

            exposeAndPropagateEmission(
                propagationContext = propagationContext,
                emission = Emission(
                    emittedEvent = correctedEmittedEvent,
                ),
            )
        }
    }

    fun emit(
        emittedEvent: EventT,
    ): TestInputStimulation = object : TestInputStimulation {
        override fun stimulate(
            propagationContext: Transactions.PropagationContext,
        ) {
            _vertex.emit(
                propagationContext = propagationContext,
                emittedEvent = emittedEvent,
            )
        }
    }

    fun revokeEmission(): TestInputStimulation = object :
        TestInputStimulation {
        override fun stimulate(
            propagationContext: Transactions.PropagationContext,
        ) {
            _vertex.revokeEmission(
                propagationContext = propagationContext,
            )
        }
    }

    fun correctEmission(
        correctedEmittedEvent: EventT,
    ): TestInputStimulation = object : TestInputStimulation {
        override fun stimulate(
            propagationContext: Transactions.PropagationContext,
        ) {
            _vertex.correctEmission(
                propagationContext = propagationContext,
                correctedEmittedEvent = correctedEmittedEvent,
            )
        }
    }

    override val vertex: EventStreamVertex<EventT>
        get() = _vertex
}
