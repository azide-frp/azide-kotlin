package dev.azide.core.collections.reactive_set

import dev.azide.core.impl.collections.reactive_set.SetChange
import dev.azide.core.impl.collections.reactive_set.applyTo
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("ClassName")
class SetChange_tests {
    @Test
    fun test_applyTo_toEmpty() {
        val mutableSet = mutableSetOf<Int>()

        SetChange(
            addedElements = setOf(1, 2, 3),
            removedElements = emptySet(),
        ).applyTo(
            mutableSet = mutableSet,
        )

        assertEquals(
            expected = setOf(1, 2, 3),
            actual = mutableSet,
        )
    }

    @Test
    fun test_applyTo_nonEmpty_addedOnly() {
        val mutableSet = mutableSetOf(1, 2, 3, 4)

        SetChange(
            addedElements = setOf(5, 6),
            removedElements = emptySet(),
        ).applyTo(
            mutableSet = mutableSet,
        )

        assertEquals(
            expected = setOf(1, 2, 3, 4, 5, 6),
            actual = mutableSet,
        )
    }

    @Test
    fun test_applyTo_nonEmpty_removedOnly() {
        val mutableSet = mutableSetOf(1, 2, 3, 4)

        SetChange(
            addedElements = emptySet(),
            removedElements = setOf(2, 4),
        ).applyTo(
            mutableSet = mutableSet,
        )

        assertEquals(
            expected = setOf(1, 3),
            actual = mutableSet,
        )
    }

    @Test
    fun test_applyTo_nonEmpty_addedAndRemoved() {
        val mutableSet = mutableSetOf(1, 2, 3, 4)

        SetChange(
            addedElements = setOf(5, 6),
            removedElements = setOf(2, 3),
        ).applyTo(
            mutableSet = mutableSet,
        )

        assertEquals(
            expected = setOf(1, 4, 5, 6),
            actual = mutableSet,
        )
    }
}
