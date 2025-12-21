package dev.azide.core

import dev.azide.core.internal.cell.PureCellVertex
import dev.azide.core.internal.cell.operated_vertices.HeldCellVertex
import dev.azide.core.internal.event_stream.EventStreamVertex
import dev.azide.core.internal.event_stream.LiveEventStreamVertex
import dev.azide.core.internal.event_stream.TerminatedEventStreamVertex
import dev.azide.core.internal.event_stream.operated_vertices.FilteredEventStreamVertex
import dev.azide.core.internal.event_stream.operated_vertices.MappedEventStreamVertex
import dev.azide.core.internal.event_stream.operated_vertices.SingleEventStreamVertex

interface EventStream<out EventT> {
    val vertex: EventStreamVertex<EventT>

    object Never : EventStream<Nothing> {
        override val vertex: EventStreamVertex<Nothing> = TerminatedEventStreamVertex()
    }

    class Ordinary<EventT> internal constructor(
        override val vertex: EventStreamVertex<EventT>,
    ) : EventStream<EventT>

    companion object {
        fun <EventT, ResultT> looped(
            block: (EventStream<EventT>) -> Pair<ResultT, EventStream<EventT>>,
        ): ResultT = TODO()

        fun <EventT> merge2(
            eventStream1: EventStream<EventT>,
            eventStream2: EventStream<EventT>,
        ): EventStream<EventT> = TODO()

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
    vertex = when (val sourceVertex = this.vertex) {
        is LiveEventStreamVertex -> MappedEventStreamVertex(
            sourceVertex = sourceVertex,
            transform = transform,
        )

        is TerminatedEventStreamVertex -> TerminatedEventStreamVertex()
    },
)

fun <EventT, TransformedEventT : Any> EventStream<EventT>.mapNotNull(
    transform: (EventT) -> TransformedEventT?,
): EventStream<TransformedEventT> = TODO()

fun <EventT, TransformedEventT> EventStream<EventT>.mapAt(
    transform: context(MomentContext) (EventT) -> TransformedEventT,
): EventStream<TransformedEventT> = TODO()

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
        vertex = when (val sourceVertex = this.vertex) {
            is LiveEventStreamVertex -> SingleEventStreamVertex(
                propagationContext = momentContext.propagationContext,
                sourceVertex = sourceVertex,
            )

            is TerminatedEventStreamVertex -> TerminatedEventStreamVertex()
        }
    )

context(momentContext: MomentContext) fun <EventT> EventStream<EventT>.take(
    count: Int,
): EventStream<EventT> = TODO()

context(momentContext: MomentContext) fun <EventT> EventStream<EventT>.hold(
    initialValue: EventT,
): Cell<EventT> = Cell.Ordinary {
    when (val sourceVertex = this.vertex) {
        is LiveEventStreamVertex -> HeldCellVertex.start(
            propagationContext = momentContext.propagationContext,
            sourceVertex = sourceVertex,
            initialValue = initialValue,
        )

        is TerminatedEventStreamVertex -> PureCellVertex(
            value = initialValue,
        )
    }
}

context(momentContext: MomentContext) fun <EventT, AccT> EventStream<EventT>.accumulate(
    initialAccValue: AccT,
    transform: (accValue: AccT, newEvent: EventT) -> AccT,
): Cell<AccT> = EventStream.looped<AccT, Cell<AccT>> { loopedNewAccValues ->
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

    Pair(
        accCell,
        newAccValues,
    )
}
