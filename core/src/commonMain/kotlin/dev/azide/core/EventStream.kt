package dev.azide.core

import dev.azide.core.external.ExternalStream
import dev.azide.core.impl.Transactions
import dev.azide.core.impl.Transactions.PropagationContext
import dev.azide.core.impl.cell.operated_vertices.HeldCellVertex
import dev.azide.core.impl.effects.ExecutedEachEventStreamVertex
import dev.azide.core.impl.effects.ExternalizedEffect
import dev.azide.core.impl.event_stream.EventStreamVertex
import dev.azide.core.impl.event_stream.LiveEventStreamVertex
import dev.azide.core.impl.event_stream.TerminatedEventStreamVertex
import dev.azide.core.impl.event_stream.operated_vertices.AdaptedExternalEventStreamVertex
import dev.azide.core.impl.event_stream.operated_vertices.FilteredEventStreamVertex
import dev.azide.core.impl.event_stream.operated_vertices.MappedEventStreamVertex
import dev.azide.core.impl.event_stream.operated_vertices.Merged2EventStreamVertex
import dev.azide.core.impl.event_stream.operated_vertices.SampledEachEventStreamVertex
import dev.azide.core.impl.event_stream.operated_vertices.SingleEventStreamVertex
import dev.azide.core.impl.utils.LoopClosure
import dev.azide.core.impl.utils.LoopUtils

interface EventStream<out EventT> {
    val vertex: EventStreamVertex<EventT>

    object Never : EventStream<Nothing> {
        override val vertex: EventStreamVertex<Nothing> = TerminatedEventStreamVertex()
    }

    class Ordinary<EventT> internal constructor(
        override val vertex: EventStreamVertex<EventT>,
    ) : EventStream<EventT>

    class Lazy<EventT> internal constructor(
        private val eventStreamLazy: kotlin.Lazy<EventStream<EventT>>,
    ) : EventStream<EventT> {
        override val vertex: EventStreamVertex<EventT> by lazy {
            eventStreamLazy.value.vertex
        }
    }

    companion object {
        fun <EventT> adapt(
            externalStream: ExternalStream<EventT>,
        ): EventStream<EventT> = Ordinary(
            vertex = AdaptedExternalEventStreamVertex(
                externalStream = externalStream,
            ),
        )

        fun <ResultT, LoopedEventT> looped(
            block: (EventStream<LoopedEventT>) -> LoopClosure<ResultT, EventStream<LoopedEventT>>,
        ): ResultT = LoopUtils.looped { loopedEventStreamLazy ->
            block(
                Lazy(
                    eventStreamLazy = loopedEventStreamLazy,
                ),
            )
        }

        fun <ResultT, LoopedEventT> loopedInMoment(
            block: (EventStream<LoopedEventT>) -> Moment<LoopClosure<ResultT, EventStream<LoopedEventT>>>,
        ): Moment<ResultT> = Moment.looped { loopedEventStreamLazy ->
            block(
                Lazy(
                    eventStreamLazy = loopedEventStreamLazy,
                ),
            )
        }

        fun <ResultT, LoopedEventT> loopedInAction(
            block: (EventStream<LoopedEventT>) -> Action<LoopClosure<ResultT, EventStream<LoopedEventT>>>,
        ): Action<ResultT> = Action.looped { loopedEventStreamLazy ->
            block(
                Lazy(
                    eventStreamLazy = loopedEventStreamLazy,
                ),
            )
        }

        fun <EventT> merge2(
            eventStream1: EventStream<EventT>,
            eventStream2: EventStream<EventT>,
        ): EventStream<EventT> = Ordinary(
            Merged2EventStreamVertex(
                sourceEventStream1 = eventStream1,
                sourceEventStream2 = eventStream2,
            ),
        )

        fun <EventT> merge3(
            eventStream1: EventStream<EventT>,
            eventStream2: EventStream<EventT>,
            eventStream3: EventStream<EventT>,
        ): EventStream<EventT> = TODO()
    }
}

fun <EventT, TransformedEventT> EventStream<EventT>.map(
    transform: (EventT) -> TransformedEventT,
): EventStream<TransformedEventT> = EventStream.Ordinary(
    vertex = MappedEventStreamVertex(
        sourceEventStream = this@map,
        transform = transform,
    ),
)

fun <EventT, TransformedEventT : Any> EventStream<EventT>.mapNotNull(
    transform: (EventT) -> TransformedEventT?,
): EventStream<TransformedEventT> = TODO()

fun <EventT, TransformedEventT> EventStream<EventT>.mapAt(
    transform: context(MomentContext) (EventT) -> TransformedEventT,
): EventStream<TransformedEventT> = sampleEachOf { event: EventT ->
    Moment.decontextualize {
        transform(event)
    }
}

fun <EventT, TransformedEventT : Any> EventStream<EventT>.mapNotNullAt(
    transform: context(MomentContext) (EventT) -> TransformedEventT?,
): EventStream<TransformedEventT> = TODO()

fun <EventT> EventStream<EventT>.filter(
    predicate: (EventT) -> Boolean,
): EventStream<EventT> = EventStream.Ordinary(
    vertex = when (val sourceVertex = this.vertex) {
        is LiveEventStreamVertex -> FilteredEventStreamVertex(
            sourceVertex = sourceVertex,
            predicate = predicate,
        )

        is TerminatedEventStreamVertex -> TerminatedEventStreamVertex()
    },
)

fun <EventT> EventStream<EventT>.filterAt(
    predicate: context(MomentContext) (EventT) -> Boolean,
): EventStream<EventT> = TODO()

context(momentContext: MomentContext) fun <EventT> EventStream<EventT>.single(): EventStream<EventT> =
    EventStream.Ordinary(
        vertex = SingleEventStreamVertex(
            wrapUpContext = momentContext.wrapUpContext,
            sourceEventStream = this,
        ),
    )

context(momentContext: MomentContext) fun <EventT> EventStream<EventT>.take(
    count: Int,
): EventStream<EventT> = TODO()

fun <EventT> EventStream<EventT>.holding(
    initialValue: EventT,
): Moment<Cell<EventT>> = object : Moment<Cell<EventT>> {
    override fun pullInternally(
        propagationContext: PropagationContext,
        wrapUpContext: Transactions.WrapUpContext,
    ): Cell<EventT> = Cell.Ordinary(
        vertex = HeldCellVertex.start(
            wrapUpContext = wrapUpContext,
            sourceEventStream = this@holding,
            initialValue = initialValue,
        ),
    )
}

context(momentContext: MomentContext) fun <EventT> EventStream<EventT>.hold(
    initialValue: EventT,
): Cell<EventT> = holding(
    initialValue = initialValue,
).pullInternally(
    propagationContext = momentContext.propagationContext,
    wrapUpContext = momentContext.wrapUpContext,
)

fun <EventT, AccT> EventStream<EventT>.accumulating(
    initialAccValue: AccT,
    transform: (accValue: AccT, newEvent: EventT) -> AccT,
): Moment<Cell<AccT>> = EventStream.loopedInMoment<Cell<AccT>, AccT> { loopedNewAccValues ->
    Cell.defining(
        initialValue = initialAccValue,
        newValues = loopedNewAccValues,
    ).map { accCell: Cell<AccT> ->
        val newAccValues = this@accumulating.sampleEachOf { newEvent: EventT ->
            accCell.sampling.map { sampledAccValue: AccT ->
                transform(
                    sampledAccValue,
                    newEvent,
                )
            }
        }

        LoopClosure(
            result = accCell,
            loopedValue = newAccValues,
        )
    }
}

context(momentContext: MomentContext) fun <EventT, AccT> EventStream<EventT>.accumulate(
    initialAccValue: AccT,
    transform: (accValue: AccT, newEvent: EventT) -> AccT,
): Cell<AccT> = EventStream.looped<Cell<AccT>, AccT> { loopedNewAccValues ->
    val accCell = Cell.define(
        initialValue = initialAccValue,
        newValues = loopedNewAccValues,
    )

    val newAccValues = this@accumulate.mapAt { newEvent ->
        transform(
            accCell.sample(),
            newEvent,
        )
    }

    LoopClosure(
        result = accCell,
        loopedValue = newAccValues,
    )
}

fun <EventT> EventStream<Moment<EventT>>.sampleEach(): EventStream<EventT> = EventStream.Ordinary(
    vertex = SampledEachEventStreamVertex(
        sourceEventStream = this,
    ),
)

fun <EventT, TransformedEventT> EventStream<EventT>.sampleEachOf(
    transform: (EventT) -> Moment<TransformedEventT>,
): EventStream<TransformedEventT> = map(transform).sampleEach()

fun <EventT> EventStream<Action<EventT>>.executeEach(): Effect<EventStream<EventT>> =
    ExternalizedEffect<EventStream<EventT>>(
        internalEffect = ExecutedEachEventStreamVertex.ExecutionEffect(
            sourceEventStream = this@executeEach,
        ),
    )

fun EventStream<Trigger>.triggerEach(): Schedule = executeEach().map { }

fun <EventT> EventStream<Action<EventT>>.executeEachForever(): Action<EventStream<EventT>> =
    executeEach().start.map { outcome -> outcome.result }

fun EventStream<Trigger>.triggerEachForever(): Trigger = executeEachForever().map { }

fun <EventT, TransformedEventT> EventStream<EventT>.executeEachOf(
    transform: (EventT) -> Action<TransformedEventT>,
): Effect<EventStream<TransformedEventT>> = map(transform).executeEach()

fun <T1, T2> EventStream<Pair<T1, T2>>.split(): Pair<EventStream<T1>, EventStream<T2>> = Pair(
    first = this.map { it.first },
    second = this.map { it.second },
)
