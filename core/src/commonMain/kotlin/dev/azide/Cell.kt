package dev.azide

import dev.azide.internal.Transactions
import dev.azide.internal.cell.CellVertex
import dev.azide.internal.cell.FrozenCellVertex
import dev.azide.internal.cell.PureCellVertex
import dev.azide.internal.cell.WarmCellVertex
import dev.azide.internal.cell.operated_vertices.Mapped2FrozenCellVertex
import dev.azide.internal.cell.operated_vertices.Mapped2WarmCellVertex
import dev.azide.internal.cell.operated_vertices.MappedFrozenCellVertex
import dev.azide.internal.cell.operated_vertices.MappedWarmCellVertex
import dev.azide.internal.cell.operated_vertices.SwitchedCellVertex

interface Cell<out ValueT> {
    fun getVertex(
        propagationContext: Transactions.PropagationContext,
    ): CellVertex<ValueT>

    class Const<out ValueT>(
        constValue: ValueT,
    ) : Cell<ValueT> {
        private val pureVertex = PureCellVertex(
            value = constValue,
        )

        override fun getVertex(
            propagationContext: Transactions.PropagationContext,
        ): CellVertex<ValueT> = pureVertex
    }

    class Ordinary<out ValueT> internal constructor(
        private val buildVertex: (propagationContext: Transactions.PropagationContext) -> CellVertex<ValueT>,
    ) : Cell<ValueT> {
        private var cachedVertex: CellVertex<ValueT>? = null

        override fun getVertex(
            propagationContext: Transactions.PropagationContext,
        ): CellVertex<ValueT> {
            when (val cachedVertex = this.cachedVertex) {
                null -> {
                    val builtVertex = buildVertex(propagationContext)

                    this.cachedVertex = builtVertex

                    return builtVertex
                }

                else -> {
                    return cachedVertex
                }
            }
        }
    }

    companion object {
        fun <ValueT1, ValueT2, ResultT> map2(
            cell1: Cell<ValueT1>,
            cell2: Cell<ValueT2>,
            transform: (ValueT1, ValueT2) -> ResultT,
        ): Cell<ResultT> = Ordinary { propagationContext ->
            val sourceVertex1 = cell1.getVertex(
                propagationContext = propagationContext,
            )

            val sourceVertex2 = cell2.getVertex(
                propagationContext = propagationContext,
            )

            when {
                sourceVertex1 is FrozenCellVertex && sourceVertex2 is FrozenCellVertex -> Mapped2FrozenCellVertex(
                    sourceVertex1 = sourceVertex1,
                    sourceVertex2 = sourceVertex2,
                    transform = transform,
                )

                else -> Mapped2WarmCellVertex(
                    sourceVertex1 = sourceVertex1,
                    sourceVertex2 = sourceVertex2,
                    transform = transform,
                )
            }
        }

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

        fun <ValueT> of(
            value: ValueT,
        ): Cell<ValueT> = TODO()

        context(momentContext: MomentContext) fun <ValueT> define(
            initialValue: ValueT,
            newValues: EventStream<ValueT>,
        ): Cell<ValueT> = newValues.hold(
            initialValue = initialValue,
        )

        fun <ValueT> switch(
            outerCell: Cell<Cell<ValueT>>,
        ): Cell<ValueT> = Ordinary { propagationContext ->
            val outerSourceVertex = outerCell.getVertex(
                propagationContext = propagationContext,
            )

            when (outerSourceVertex) {
                is FrozenCellVertex -> {
                    val computedCell: Cell<ValueT> = outerSourceVertex.getOldValue(
                        propagationContext = propagationContext,
                    )

                    computedCell.getVertex(
                        propagationContext = propagationContext,
                    )
                }

                is WarmCellVertex -> SwitchedCellVertex(
                    outerSourceVertex = outerSourceVertex,
                )
            }
        }

        fun <ValueT> divert(
            outerCell: Cell<EventStream<ValueT>>,
        ): EventStream<ValueT> = TODO()
    }
}

context(momentContext: MomentContext) fun <ValueT> Cell<ValueT>.sample(): ValueT = TODO()

fun <ValueT, TransformedValueT> Cell<ValueT>.map(
    transform: (ValueT) -> TransformedValueT,
): Cell<TransformedValueT> = Cell.Ordinary { propagationContext ->
    val sourceVertex = this.getVertex(
        propagationContext = propagationContext,
    )

    when (sourceVertex) {
        is FrozenCellVertex -> MappedFrozenCellVertex(
            sourceVertex = sourceVertex,
            transform = transform,
        )

        is WarmCellVertex -> MappedWarmCellVertex(
            sourceVertex = sourceVertex,
            transform = transform,
        )
    }
}
