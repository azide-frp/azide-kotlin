package dev.azide.core.test_utils.semantic

import kotlin.random.Random

interface SemanticMoment<out LabelT : SemanticMoment.Label, out ValueT> {
    interface Label {
        data object Dependent : Label
    }

    val label: LabelT

    fun evaluate(timestamp: Timestamp): ValueT

    companion object {
        fun <LabelT : Label, ValueT> generateRandom(
            label: LabelT,
            random: Random,
            randomValueGenerator: dev.azide.core.test_utils.RandomValueGenerator<ValueT>,
        ): SemanticMoment<LabelT, ValueT> = object : SemanticMoment<LabelT, ValueT> {
            override val label: LabelT = label

            // Keep a cache of values per timestamp. Start with value at t=0.
            private val cache: MutableList<ValueT> = mutableListOf(randomValueGenerator.next())

            override fun evaluate(timestamp: Timestamp): ValueT {
                while (cache.size <= timestamp.t) {
                    val next = if (random.nextDouble() < 0.5) {
                        randomValueGenerator.next()
                    } else {
                        cache.last()
                    }

                    cache += next
                }

                return cache[timestamp.t]
            }
        }
    }
}


typealias AnySemanticMoment<ValueT> = SemanticMoment<SemanticMoment.Label, ValueT>
