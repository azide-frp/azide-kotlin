package dev.azide.core.test_utils.semantic

import kotlin.jvm.JvmInline

@JvmInline
value class Timestamp(
    val t: Int,
) : Comparable<Timestamp> {
    companion object {
        val zero = Timestamp(t = 0)

        /**
         * Generate [count] consecutive timestamps starting from t = 1.
         */
        fun generate(count: Int): Sequence<Timestamp> =
            generateSequence(1) { it + 1 }.map { Timestamp(t = it) }.take(count)

        fun newerOf(
            first: Timestamp,
            second: Timestamp,
        ): Timestamp = maxOf(first, second)
    }

    init {
        require(t >= 0) { "Timestamp cannot be negative, but was $t" }
    }

    val previous: Timestamp
        get() = Timestamp(t = t - 1)

    val next: Timestamp
        get() = Timestamp(t = t + 1)

    override fun compareTo(
        other: Timestamp,
    ): Int = compareValuesBy(this, other) { it.t }
}
