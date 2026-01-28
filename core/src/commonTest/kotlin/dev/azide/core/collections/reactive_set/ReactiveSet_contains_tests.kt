package dev.azide.core.collections.reactive_set

import dev.azide.core.collections.contains
import dev.azide.core.collections.filter
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.cell.CellTestUtils
import dev.azide.core.test_utils.collections.reactive_set.ReactiveSetTestUtils
import kotlin.test.Test

@Suppress("ClassName")
class ReactiveSet_contains_tests {
    @Test
    fun test_passiveSample_contained() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.contains(3)

        CellTestUtils.verifyAtRest(
            subjectCell = subjectCell,
            expectedValue = true,
        )
    }

    @Test
    fun test_passiveSample_notContained() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.contains(10)

        CellTestUtils.verifyAtRest(
            subjectCell = subjectCell,
            expectedValue = false,
        )
    }

    @Test
    fun test_update_elementAdded() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.contains(6)

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = sourceReactiveSet.change(
                elementsToAdd = setOf(6, 7),
                elementsToRemove = setOf(),
            ),
            expectedOldValue = false,
            expectedNewValue = true,
        )
    }

    @Test
    fun test_update_elementRemoved() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.contains(1)

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = sourceReactiveSet.change(
                elementsToAdd = setOf(6, -7),
                elementsToRemove = setOf(1, 5),
            ),
            expectedOldValue = true,
            expectedNewValue = false,
        )
    }

    @Test
    fun test_update_elementRetained() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.contains(3)

        CellTestUtils.verifyDoesNotUpdateAtAll(
            subjectCell = subjectCell,
            inputStimulation = sourceReactiveSet.change(
                elementsToAdd = setOf(6, 7, -8),
                elementsToRemove = setOf(1, 5),
            ),
            expectedUnaffectedValue = true,
        )
    }

    @Test
    fun test_update_elementUnmentioned() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.contains(10)

        CellTestUtils.verifyDoesNotUpdateAtAll(
            subjectCell = subjectCell,
            inputStimulation = sourceReactiveSet.change(
                elementsToAdd = setOf(6, 7, -8),
                elementsToRemove = setOf(1, 5),
            ),
            expectedUnaffectedValue = false,
        )
    }

    @Test
    fun test_update_revoked_elementAdded() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.contains(6)

        CellTestUtils.verifyDoesNotUpdateEffectively(
            subjectCell = subjectCell,
            inputStimulation = TestStimulation.combine(
                sourceReactiveSet.change(
                    elementsToAdd = setOf(6, -7),
                    elementsToRemove = setOf(1, 3),
                ),
                sourceReactiveSet.revokeChange(),
            ),
            expectedUnaffectedValue = false,
        )
    }

    @Test
    fun test_update_revoked_elementRemoved() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.contains(1)

        CellTestUtils.verifyDoesNotUpdateEffectively(
            subjectCell = subjectCell,
            inputStimulation = TestStimulation.combine(
                sourceReactiveSet.change(
                    elementsToAdd = setOf(6, -7),
                    elementsToRemove = setOf(1, 5),
                ),
                sourceReactiveSet.revokeChange(),
            ),
            expectedUnaffectedValue = true,
        )
    }

    @Test
    fun test_update_revoked_elementUnmentioned() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.contains(10)

        CellTestUtils.verifyDoesNotUpdateAtAll(
            subjectCell = subjectCell,
            inputStimulation = TestStimulation.combine(
                sourceReactiveSet.change(
                    elementsToAdd = setOf(6, 7),
                    elementsToRemove = setOf(1, 5),
                ),
                sourceReactiveSet.revokeChange(),
            ),
            expectedUnaffectedValue = false,
        )
    }

    @Test
    fun test_update_corrected_unmentionedBefore_unmentionedLater() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.contains(10)

        CellTestUtils.verifyDoesNotUpdateAtAll(
            subjectCell = subjectCell,
            inputStimulation = TestStimulation.combine(
                sourceReactiveSet.change(
                    elementsToAdd = setOf(6, 7),
                    elementsToRemove = setOf(1, 5),
                ),
                sourceReactiveSet.correctChange(
                    correctedElementsToAdd = setOf(-6, -7),
                    correctedElementsToRemove = setOf(1, 5),
                ),
            ),
            expectedUnaffectedValue = false,
        )
    }

    @Test
    fun test_update_corrected_unmentionedBefore_mentionedLater() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.contains(8)

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = TestStimulation.combine(
                sourceReactiveSet.change(
                    elementsToAdd = setOf(6, 7),
                    elementsToRemove = setOf(1, 5),
                ),
                sourceReactiveSet.correctChange(
                    correctedElementsToAdd = setOf(6, 7, 8),
                    correctedElementsToRemove = setOf(1, 5),
                ),
            ),
            expectedOldValue = false,
            expectedNewValue = true,
        )
    }

    @Test
    fun test_update_corrected_mentionedBefore_unmentionedLater() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.contains(3)

        CellTestUtils.verifyDoesNotUpdateEffectively(
            subjectCell = subjectCell,
            inputStimulation = TestStimulation.combine(
                sourceReactiveSet.change(
                    elementsToAdd = setOf(6),
                    elementsToRemove = setOf(1, -2, 3, 5),
                ),
                sourceReactiveSet.correctChange(
                    correctedElementsToAdd = setOf(-6, 7),
                    correctedElementsToRemove = setOf(1, -2),
                ),
            ),
            expectedUnaffectedValue = true,
        )
    }

    @Test
    fun test_update_corrected_mentionedBefore_mentionedLater() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.contains(3)

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = TestStimulation.combine(
                sourceReactiveSet.change(
                    elementsToAdd = setOf(6, -7, 8),
                    elementsToRemove = setOf(1, -2),
                ),
                sourceReactiveSet.correctChange(
                    correctedElementsToAdd = setOf(-6, 7, 8, 9),
                    correctedElementsToRemove = setOf(3, -4),
                ),
            ),
            expectedOldValue = true,
            expectedNewValue = false,
        )
    }
}
