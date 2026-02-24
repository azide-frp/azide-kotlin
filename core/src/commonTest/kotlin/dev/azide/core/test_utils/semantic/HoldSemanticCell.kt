package dev.azide.core.test_utils.semantic

import dev.azide.core.test_utils.RandomValueGenerator
import dev.azide.core.test_utils.semantic.SemanticCell.ValueSnapshot
import kotlin.random.Random

class HoldSemanticCell<out LabelT : SemanticCell.Label, out ValueT>(
    override val label: LabelT,
    /**
     * The initial value (at t = 0).
     */
    initialValue: ValueT,
    /**
     * Given the current value, returns a [Command] describing what happens at the next timestamp.
     */
    private val buildNewValue: (oldValue: ValueT) -> Command<ValueT>,
) : SemanticCell<LabelT, ValueT> {
    sealed class Command<out ValueT> {
        /**
         * A command to update to [updatedValue].
         */
        data class Update<out ValueT>(
            val updatedValue: ValueT,
        ) : Command<ValueT>()

        /**
         * A command not to update.
         */
        data object Pass : Command<Nothing>()
    }

    companion object {
        fun <LabelT : SemanticCell.Label, ValueT> generateRandom(
            label: LabelT,
            random: Random,
            randomValueGenerator: RandomValueGenerator<ValueT>,
        ): HoldSemanticCell<LabelT, ValueT> {
            val initialValue = randomValueGenerator.next()

            return HoldSemanticCell(
                label = label,
                initialValue = initialValue,
                buildNewValue = {
                    if (random.nextBoolean()) {
                        Command.Update(updatedValue = randomValueGenerator.next())
                    } else {
                        Command.Pass
                    }
                },
            )
        }
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

            cache += when (val command = buildNewValue(oldSnapshot.value)) {
                is Command.Pass -> oldSnapshot
                is Command.Update -> ValueSnapshot(
                    value = command.updatedValue,
                    timestamp = nextTimestamp,
                )
            }
        }

        return cache[timestamp.t]
    }
}
