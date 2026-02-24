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
     * The sequence of commands to execute at each timestamp, starting from a command for transition between t = 0 and
     * t = 1.
     */
    commandSequence: Sequence<Command<ValueT>>,
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
            timestampCount: Int,
        ): HoldSemanticCell<LabelT, ValueT> {
            val initialValue = randomValueGenerator.next()

            val commandSequence = Timestamp.generate(count = timestampCount).map {
                if (random.nextBoolean()) {
                    Command.Update(
                        updatedValue = randomValueGenerator.next(),
                    )
                } else {
                    Command.Pass
                }
            }

            return HoldSemanticCell(
                label = label,
                initialValue = initialValue,
                commandSequence = commandSequence,
            )
        }
    }

    private val valueSnapshotList: LazyBuiltList<ValueSnapshot<ValueT>> = LazyBuiltList.build(
        sequence = sequenceOf(
            ValueSnapshot(
                value = initialValue,
                timestamp = Timestamp.zero,
            ),
        ) + commandSequence.withIndex().scan(
            initial = ValueSnapshot(
                value = initialValue,
                timestamp = Timestamp.zero,
            ),
        ) { oldSnapshot, indexedCommand ->
            val (index, command) = indexedCommand

            command.execute(
                oldValueSnapshot = oldSnapshot,
                timestamp = Timestamp(index + 1),
            )
        },
    )

    override fun evaluate(
        timestamp: Timestamp,
    ): ValueSnapshot<ValueT> = valueSnapshotList.getOrNull(timestamp.t)
        ?: throw IllegalArgumentException("Cell not defined for t = ${timestamp.t}")
}

private fun <ValueT> HoldSemanticCell.Command<ValueT>.execute(
    oldValueSnapshot: ValueSnapshot<ValueT>,
    timestamp: Timestamp,
): ValueSnapshot<ValueT> = when (this) {
    is HoldSemanticCell.Command.Pass -> oldValueSnapshot

    is HoldSemanticCell.Command.Update -> ValueSnapshot(
        value = updatedValue,
        timestamp = timestamp.next,
    )
}

private class LazyBuiltList<E> private constructor(
    private val iterator: Iterator<E>,
) : AbstractList<E>() {
    companion object {
        fun <E> build(
            sequence: Sequence<E>,
        ): LazyBuiltList<E> = LazyBuiltList(
            iterator = sequence.iterator(),
        )
    }

    private val storageList = mutableListOf<E>()

    override val size: Int
        get() {
            buildUntil(maxSize = Int.MAX_VALUE)

            return storageList.size
        }

    private fun buildUntil(
        maxSize: Int,
    ) {
        while (storageList.size <= maxSize) {
            if (iterator.hasNext()) {
                storageList.add(iterator.next())
            } else {
                return
            }
        }
    }

    override fun get(index: Int): E {
        buildUntil(maxSize = index + 1)

        return storageList[index]
    }
}
