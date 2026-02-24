package dev.azide.core.test_utils.semantic

import dev.azide.core.test_utils.RandomValueGenerator
import dev.azide.core.test_utils.semantic.SemanticCell.ValueSnapshot
import kotlin.random.Random

class HoldSemanticCell<out LabelT : SemanticCell.Label, out ValueT>(
    override val label: LabelT,
    /** The event stream driving this hold. */
    private val eventStream: SemanticEventStream<*, ValueT>,
    /** The initial value (at t = 0). */
    initialValue: ValueT,
) : SemanticCell<LabelT, ValueT> {
    companion object {
        fun <LabelT : SemanticCell.Label, ValueT> generateRandom(
            label: LabelT,
            random: Random,
            randomValueGenerator: RandomValueGenerator<ValueT>,
        ): HoldSemanticCell<LabelT, ValueT> {
            val semanticEventStream: AnySemanticEventStream<ValueT> = SemanticEventStream.generateRandom(
                label = SemanticEventStream.Label.Dependent,
                random = random,
                randomValueGenerator = randomValueGenerator,
            )

            val initialValue = randomValueGenerator.next()

            return HoldSemanticCell(
                label = label,
                eventStream = semanticEventStream,
                initialValue = initialValue,
            )
        }

        fun <LabelT : SemanticCell.Label, ValueT> fromEventStream(
            label: LabelT,
            eventStream: SemanticEventStream<*, ValueT>,
            initialValue: ValueT,
        ): HoldSemanticCell<LabelT, ValueT> = HoldSemanticCell(
            label = label,
            eventStream = eventStream,
            initialValue = initialValue,
        )
    }

    private val cache: MutableList<ValueSnapshot<ValueT>> = mutableListOf(
        ValueSnapshot(
            value = initialValue,
            timestamp = Timestamp.zero,
        ),
    )

    override fun evaluate(
        timestamp: Timestamp,
    ): ValueSnapshot<ValueT> {
        while (cache.size <= timestamp.t) {
            val oldSnapshot = cache.last()
            val nextTimestamp = Timestamp(cache.size)

            // If event stream emitted at nextTimestamp, update to that emitted value, otherwise retain old
            val emitted = eventStream.evaluate(timestamp = nextTimestamp)

            cache += if (emitted != null) {
                ValueSnapshot(
                    value = emitted,
                    timestamp = nextTimestamp,
                )
            } else {
                oldSnapshot
            }
        }

        return cache[timestamp.t]
    }
}
