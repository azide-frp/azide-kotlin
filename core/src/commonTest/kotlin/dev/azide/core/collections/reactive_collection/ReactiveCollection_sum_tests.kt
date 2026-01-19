package dev.azide.core.collections.reactive_collection

import dev.azide.core.collections.ReactiveSet
import dev.azide.core.collections.sum
import dev.azide.core.test_utils.TestInputStimulation
import dev.azide.core.test_utils.cell.CellTestUtils
import dev.azide.core.test_utils.collections.reactive_set.ReactiveSetTestUtils
import kotlin.test.Test

@Suppress("ClassName")
class ReactiveCollection_sum_tests {
    @Test
    fun test_atRest_sourceConst() {
        val sourceReactiveSet = ReactiveSet.Const(
            constElements = setOf(1, 2, 3, 4),
        )

        val subjectCell = sourceReactiveSet.sum()

        CellTestUtils.verifyAtRest(
            subjectCell = subjectCell,
            expectedValue = 10,
        )
    }

    @Test
    fun test_atRest() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, 2, 3, 4),
        )

        val subjectCell = sourceReactiveSet.sum()

        CellTestUtils.verifyAtRest(
            subjectCell = subjectCell,
            expectedValue = 10,
        )
    }

    @Test
    fun test_change() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, 2, 3, 4),
        )

        val subjectCell = sourceReactiveSet.sum()

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = sourceReactiveSet.change(
                elementsToAdd = setOf(5, 6),
                elementsToRemove = setOf(2, 3),
            ),
            expectedOldValue = 10,
            expectedNewValue = 16,
        )
    }

    @Test
    fun test_change_revoked() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, 2, 3, 4),
        )

        val subjectCell = sourceReactiveSet.sum()

        CellTestUtils.verifyDoesNotUpdateEffectively(
            subjectCell = subjectCell,
            inputStimulation = TestInputStimulation.combine(
                sourceReactiveSet.change(
                    elementsToAdd = setOf(5, 6),
                    elementsToRemove = setOf(2, 3),
                ),
                sourceReactiveSet.revokeChange(),
            ),
            expectedUnaffectedValue = 10,
        )
    }

    @Test
    fun test_change_corrected() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, 2, 3, 4),
        )

        val subjectCell = sourceReactiveSet.sum()

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = TestInputStimulation.combine(
                sourceReactiveSet.change(
                    elementsToAdd = setOf(5, 6),
                    elementsToRemove = setOf(2, 3),
                ),
                sourceReactiveSet.correctChange(
                    correctedElementsToAdd = setOf(7),
                    correctedElementsToRemove = setOf(4),
                ),
            ),
            expectedOldValue = 10,
            expectedNewValue = 13,
        )
    }
}
