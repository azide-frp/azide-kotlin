package dev.azide.core.test_utils.semantic

import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.semantic.SemanticCell.Transition

interface SemanticCell<out LabelT : SemanticCell.Label, out ValueT> {
    interface Label {
        data object Dependent : Label
    }

    data class ValueSnapshot<out ValueT>(
        val value: ValueT,
        val timestamp: Timestamp,
    ) {
        fun <TransformedValueT> map(
            transform: (ValueT) -> TransformedValueT,
        ): ValueSnapshot<TransformedValueT> = ValueSnapshot(
            value = transform(value),
            timestamp = timestamp,
        )

        fun touched(
            newTimestamp: Timestamp,
        ): ValueSnapshot<ValueT> = copy(
            timestamp = newTimestamp,
        )
    }

    sealed class Transition<out ValueT> {
        /**
         * An update at a given timestamp.
         */
        data class Update<out ValueT>(
            val oldValue: ValueT,
            val updatedValue: ValueT,
        ) : Transition<ValueT>()

        /**
         * A lack of update at a given timestamp.
         */
        data class Pass<out ValueT>(
            val unaffectedValue: ValueT,
        ) : Transition<ValueT>()

        companion object {
            fun <ValueT> determine(
                oldSnapshot: ValueSnapshot<ValueT>,
                newSnapshot: ValueSnapshot<ValueT>,
            ): Transition<ValueT> = when {
                oldSnapshot != newSnapshot -> Update(
                    oldValue = oldSnapshot.value,
                    updatedValue = newSnapshot.value,
                )

                else -> Pass(
                    unaffectedValue = oldSnapshot.value,
                )
            }
        }

    }

    companion object {
        fun <ValueT> switch(
            outerCell: AnySemanticCell<AnySemanticCell<ValueT>>,
        ): AnySemanticCell<ValueT> = object : SemanticCell<Label, ValueT> {
            override val label: Label = Label.Dependent

            override fun evaluate(
                timestamp: Timestamp,
            ): ValueSnapshot<ValueT> {
                val innerCellSnapshot = outerCell.evaluate(timestamp = timestamp)

                val innerCell: AnySemanticCell<ValueT> = innerCellSnapshot.value
                val innerCellTimestamp = innerCellSnapshot.timestamp

                val innerValueSnapshot = innerCell.evaluate(timestamp = timestamp)

                val innerValue: ValueT = innerValueSnapshot.value
                val innerValueTimestamp = innerValueSnapshot.timestamp

                return ValueSnapshot(
                    value = innerValue,
                    timestamp = Timestamp.newerOf(innerCellTimestamp, innerValueTimestamp),
                )
            }
        }
    }

    val label: LabelT

    fun evaluate(
        timestamp: Timestamp,
    ): ValueSnapshot<ValueT>
}

fun <ValueT, TransformedValueT> Transition<ValueT>.map(
    transform: (ValueT) -> TransformedValueT,
): Transition<TransformedValueT> = when (this) {
    is Transition.Update -> Transition.Update(
        oldValue = transform(oldValue),
        updatedValue = transform(updatedValue),
    )

    is Transition.Pass -> Transition.Pass(
        unaffectedValue = transform(unaffectedValue),
    )
}

fun <SemanticValueT, RealValueT> Transition<SemanticValueT>.realize(
    valueRealValueT: TestInputCell.ValueRealizer<SemanticValueT, RealValueT>,
): Transition<RealValueT> = map { semanticValue ->
    valueRealValueT.realize(semanticValue = semanticValue)
}

typealias AnySemanticCell<ValueT> = SemanticCell<SemanticCell.Label, ValueT>

fun <ValueT> AnySemanticCell<ValueT>.evaluateTransition(
    newTimestamp: Timestamp,
): Transition<ValueT> {
    val oldSnapshot = evaluate(timestamp = newTimestamp.previous)
    val newSnapshot = evaluate(timestamp = newTimestamp)

    return Transition.determine(
        oldSnapshot = oldSnapshot,
        newSnapshot = newSnapshot,
    )
}
