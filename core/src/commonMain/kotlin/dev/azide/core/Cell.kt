package dev.azide.core

import dev.azide.core.internal.Transactions
import dev.azide.core.internal.cell.CellVertex
import dev.azide.core.internal.cell.FrozenCellVertex
import dev.azide.core.internal.cell.PureCellVertex
import dev.azide.core.internal.cell.WarmCellVertex
import dev.azide.core.internal.cell.operated_vertices.Mapped2WarmCellVertex
import dev.azide.core.internal.cell.operated_vertices.MappedAtCellVertex
import dev.azide.core.internal.cell.operated_vertices.MappedWarmCellVertex
import dev.azide.core.internal.cell.operated_vertices.SwitchedCellVertex
import dev.azide.core.internal.event_stream.operated_vertices.ValuesEventStreamVertex

interface Cell<out ValueT> {
    val vertex: CellVertex<ValueT>

    class Const<out ValueT>(
        constValue: ValueT,
    ) : Cell<ValueT> {
        private val pureVertex = PureCellVertex(
            value = constValue,
        )

        override val vertex: CellVertex<ValueT>
            get() = pureVertex
    }

    class Ordinary<out ValueT> internal constructor(
        override val vertex: CellVertex<ValueT>,
    ) : Cell<ValueT>

    companion object {
        fun <ValueT1, ValueT2, ResultT> map2(
            cell1: Cell<ValueT1>,
            cell2: Cell<ValueT2>,
            transform: (ValueT1, ValueT2) -> ResultT,
        ): Cell<ResultT> = Ordinary(
            vertex = Mapped2WarmCellVertex(
                sourceVertex1 = cell1.vertex,
                sourceVertex2 = cell2.vertex,
                transform = transform,
            ),
        )

        fun <ValueT1, ValueT2, ValueT3, ResultT> map3(
            cell1: Cell<ValueT1>,
            cell2: Cell<ValueT2>,
            cell3: Cell<ValueT3>,
            transform: (ValueT1, ValueT2, ValueT3) -> ResultT,
        ): Cell<ResultT> = TODO()

        fun <ValueT1, ValueT2, ValueT3, ValueT4, ResultT> map4(
            cell1: Cell<ValueT1>,
            cell2: Cell<ValueT2>,
            cell3: Cell<ValueT3>,
            cell4: Cell<ValueT4>,
            transform: (ValueT1, ValueT2, ValueT3, ValueT4) -> ResultT,
        ): Cell<ResultT> = TODO()

        context(momentContext: MomentContext) fun <ValueT> define(
            initialValue: ValueT,
            newValues: EventStream<ValueT>,
        ): Cell<ValueT> = newValues.hold(
            initialValue = initialValue,
        )

        fun <ValueT> switch(
            outerCell: Cell<Cell<ValueT>>,
        ): Cell<ValueT> = Ordinary(
            SwitchedCellVertex(
                outerSourceVertex = outerCell.vertex,
            ),
        )

        fun <ValueT> divert(
            outerCell: Cell<EventStream<ValueT>>,
        ): EventStream<ValueT> = TODO()
    }
}

val <ValueT> Cell<ValueT>.sampling: Moment<ValueT>
    get() = object : Moment<ValueT> {
        override fun pullInternally(
            propagationContext: Transactions.PropagationContext,
            wrapUpContext: Transactions.WrapUpContext,
        ): ValueT = vertex.getOldValue(
            propagationContext = propagationContext,
        )
    }

val <ValueT> Cell<ValueT>.values: Moment<EventStream<ValueT>>
    get() = object : Moment<EventStream<ValueT>> {
        override fun pullInternally(
            propagationContext: Transactions.PropagationContext,
            wrapUpContext: Transactions.WrapUpContext,
        ): EventStream<ValueT> {
            val sourceVertex = vertex as? WarmCellVertex ?: return EventStream.Never

            val valuesEventStreamVertex = ValuesEventStreamVertex.start(
                propagationContext = propagationContext,
                sourceVertex = sourceVertex,
            )

            return EventStream.Ordinary(
                vertex = valuesEventStreamVertex,
            )
        }
    }

val <ValueT> Cell<ValueT>.updatedValues: EventStream<ValueT>
    get() = TODO()

context(momentContext: MomentContext) fun <ValueT> Cell<ValueT>.sample(): ValueT {
    val propagationContext = momentContext.propagationContext

    return vertex.getOldValue(
        propagationContext = propagationContext,
    )
}

fun <ValueT, TransformedValueT> Cell<ValueT>.map(
    transform: (ValueT) -> TransformedValueT,
): Cell<TransformedValueT> = Cell.Ordinary(
    vertex = MappedWarmCellVertex(
        sourceVertex = this@map.vertex,
        transform = transform,
    ),
)

context(momentContext: MomentContext) fun <ValueT, TransformedValueT> Cell<ValueT>.mapAt(
    transform: context(MomentContext) (ValueT) -> TransformedValueT,
): Cell<TransformedValueT> {
    val initialPropagationContext = momentContext.propagationContext

    return when (val sourceVertex = this.vertex) {
        is FrozenCellVertex -> Cell.Const(
            constValue = transform(
                sourceVertex.getOldValue(
                    propagationContext = initialPropagationContext,
                ),
            )
        )

        is WarmCellVertex -> Cell.Ordinary(
            MappedAtCellVertex.start(
                propagationContext = initialPropagationContext,
                wrapUpContext = momentContext.wrapUpContext,
                sourceVertex = sourceVertex,
                transform = { propagationContext, updatedValue ->
                    MomentContext.wrapUp(
                        propagationContext = propagationContext,
                    ) {
                        transform(updatedValue)
                    }
                },
            ),
        )
    }
}
