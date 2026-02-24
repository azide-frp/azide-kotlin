package dev.azide.core.test_utils.event_stream

import dev.azide.core.EventStream
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.event_stream.EventStreamVertex
import dev.azide.core.impl.event_stream.EventStreamVertex.Emission
import dev.azide.core.impl.event_stream.abstract_vertices.AbstractLiveEventStreamVertex
import dev.azide.core.test_utils.DoubleTestStimulation
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationMap

class TestInputEventStream<EventT>() : EventStream<EventT> {
    companion object {
        fun <EventT> realizeInitially(
            semanticEventStream: dev.azide.core.test_utils.semantic.AnySemanticEventStream<EventT>,
        ): TestInputEventStream<EventT> = TestInputEventStream()

        fun <EventT> realizeIndirectly(
            semanticEventStream: dev.azide.core.test_utils.semantic.AnySemanticEventStream<EventT>,
        ): Pair<TestInputEventStream<EventT>, dev.azide.core.test_utils.semantic.AnySemanticEventStream<EventT>> {
            val input = TestInputEventStream<EventT>()

            val provider = object : dev.azide.core.test_utils.semantic.AnySemanticEventStream<EventT> {
                override val label: dev.azide.core.test_utils.semantic.SemanticEventStream.Label =
                    dev.azide.core.test_utils.semantic.SemanticEventStream.Label.Dependent

                override fun evaluate(timestamp: dev.azide.core.test_utils.semantic.Timestamp): EventT? =
                    semanticEventStream.evaluate(timestamp = timestamp)
            }

            return Pair(input, provider)
        }
    }

    private val _vertex = object : AbstractLiveEventStreamVertex<EventT>() {
        fun emit(
            propagationContext: Transactions.PropagationContext,
            emittedEvent: EventT,
        ) {
            if (ongoingEmission != null) {
                throw IllegalStateException("Another emission is already ongoing.")
            }

            exposeEmissionNotifyingListeners(
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

            exposeEmissionNotifyingListeners(
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

            exposeEmissionNotifyingListeners(
                propagationContext = propagationContext,
                emission = Emission(
                    emittedEvent = correctedEmittedEvent,
                ),
            )
        }
    }

    fun emit(
        emittedEvent: EventT,
    ): TestStimulation = object : TestStimulation {
        override fun stimulate(
            propagationContext: Transactions.PropagationContext,
        ) {
            _vertex.emit(
                propagationContext = propagationContext,
                emittedEvent = emittedEvent,
            )
        }
    }

    fun revokeEmission(): TestStimulation = object : TestStimulation {
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
    ): TestStimulation = object : TestStimulation {
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

fun <EventT> TestInputEventStream<EventT>.emitting(
    tag: TestInputEventStreamTag,
    emittedEvent: EventT,
): TestStimulationMap = TestStimulationMap.of(
    TestInputEventStreamStimulationTag.Emission(inputTag = tag) to emit(
        emittedEvent = emittedEvent,
    ),
)

fun <EventT> TestInputEventStream<EventT>.revokingEmission(
    tag: TestInputEventStreamTag,
    emittedEvent: EventT,
): TestStimulationMap = TestStimulationMap.of(
    TestInputEventStreamStimulationTag.Emission(inputTag = tag) to emit(
        emittedEvent = emittedEvent,
    ),
    TestInputEventStreamStimulationTag.EmissionRevocation(inputTag = tag) to revokeEmission(),
)

fun <EventT> TestInputEventStream<EventT>.correctingEmission(
    tag: TestInputEventStreamTag,
    intermediateEmittedEvent: EventT,
    correctedEmittedEvent: EventT,
): TestStimulationMap = TestStimulationMap.of(
    TestInputEventStreamStimulationTag.Emission(inputTag = tag) to emit(
        emittedEvent = intermediateEmittedEvent,
    ),
    TestInputEventStreamStimulationTag.EmissionCorrection(inputTag = tag) to correctEmission(
        correctedEmittedEvent = correctedEmittedEvent,
    ),
)

fun <ValueT> TestInputEventStream<ValueT>.revokingEmission(
    emittedEvent: ValueT,
): DoubleTestStimulation = DoubleTestStimulation(
    firstStimulation = emit(
        emittedEvent = emittedEvent,
    ),
    secondStimulation = revokeEmission(),
)

fun <ValueT> TestInputEventStream<ValueT>.correctingEmission(
    intermediateEmittedEvent: ValueT,
    correctedEmittedEvent: ValueT,
): DoubleTestStimulation = DoubleTestStimulation(
    firstStimulation = emit(
        emittedEvent = intermediateEmittedEvent,
    ),
    secondStimulation = correctEmission(
        correctedEmittedEvent = correctedEmittedEvent,
    ),
)
