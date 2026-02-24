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
        fun <ValueT> updates(
            inputCell: AnySemanticCell<ValueT>,
        ): AnySemanticEventStream<ValueT> = object : AnySemanticEventStream<ValueT> {
            override val label = SemanticEventStream.Label.Dependent

            override fun evaluate(timestamp: Timestamp): ValueT? {
                val oldSnapshot = inputCell.evaluate(timestamp.previous)
                val newSnapshot = inputCell.evaluate(timestamp)

                return Transition.determine(
                    oldSnapshot = oldSnapshot,
                    newSnapshot = newSnapshot,
                ).toEmission()
            }
        }

        fun <ValueT> hold(
            initialValue: ValueT,
            inputEventStream: AnySemanticEventStream<ValueT>,
        ): AnySemanticCell<ValueT> = HoldSemanticCell(
            label = Label.Dependent,
            eventStream = inputEventStream,
            initialValue = initialValue,
        )

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

        fun <ValueT> sampleEvery(
            outerCell: AnySemanticCell<AnySemanticMoment<ValueT>>,
        ): AnySemanticMoment<AnySemanticCell<ValueT>> = object : AnySemanticMoment<AnySemanticCell<ValueT>> {
            override val label = SemanticMoment.Label.Dependent

            override fun evaluate(
                timestamp: Timestamp,
            ): AnySemanticCell<ValueT> {
                val initialMomentSnapshot = outerCell.evaluate(timestamp = timestamp)
                val initialMoment: AnySemanticMoment<ValueT> = initialMomentSnapshot.value

                val initialValue: ValueT = initialMoment.evaluate(timestamp = timestamp)

                val updatedValues = SemanticEventStream.mapAt(
                    SemanticCell.updates(outerCell)
                ) { moment, updateTimestamp ->
                    moment.evaluate(timestamp = updateTimestamp)
                }

                return SemanticCell.hold(
                    initialValue = initialValue,
                    inputEventStream = updatedValues,
                )
            }
        }
    }

    val label: LabelT

    fun evaluate(
        timestamp: Timestamp,
    ): ValueSnapshot<ValueT>
}

private fun <ValueT> Transition<ValueT>.toEmission(): ValueT? = when (this) {
    is Transition.Pass -> null
    is Transition.Update -> this.updatedValue
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

/**
 * Map a Transition where the transform needs access to the timestamp at which each value should be
 * observed (useful when ValueT is a time-varying semantic entity such as SemanticMoment).
 */
fun <ValueT, TransformedValueT> Transition<ValueT>.mapAtTimestamp(
    newTimestamp: Timestamp,
    transform: (ValueT, Timestamp) -> TransformedValueT,
): Transition<TransformedValueT> = when (this) {
    is Transition.Update -> Transition.Update(
        oldValue = transform(oldValue, newTimestamp.previous),
        updatedValue = transform(updatedValue, newTimestamp),
    )

    is Transition.Pass -> Transition.Pass(
        unaffectedValue = transform(unaffectedValue, newTimestamp.previous),
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

/**
 * Expose a semantic view over a semantic cell's sampled value: SemanticCell<ValueT>.sampling -> SemanticMoment<ValueT>
 * The returned semantic moment evaluates to the cell's value snapshot.value at the requested timestamp.
 */
fun <ValueT> AnySemanticCell<ValueT>.sampling(): AnySemanticMoment<ValueT> = object : SemanticMoment<SemanticMoment.Label, ValueT> {
    private val source: AnySemanticCell<ValueT> = this@sampling

    override val label: SemanticMoment.Label = object : SemanticMoment.Label {}

    override fun evaluate(timestamp: Timestamp): ValueT = source.evaluate(timestamp = timestamp).value
}
