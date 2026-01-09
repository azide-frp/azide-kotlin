package dev.azide.core.collections.reactive_set

import dev.azide.core.collections.filter
import dev.azide.core.collections.size
import dev.azide.core.test_utils.cell.CellTestUtils
import dev.azide.core.test_utils.collections.reactive_set.ReactiveSetTestUtils
import kotlin.test.Test

@Suppress("ClassName")
class ReactiveSet_size_tests {
    @Test
    fun test_passiveSample() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.size

        CellTestUtils.verifyAtRest(
            subjectCell = subjectCell,
            expectedValue = 3,
        )
    }

    @Test
    fun test_update_sizeIncreased() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.size

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = sourceReactiveSet.change(
                elementsToAdd = setOf(6, 7, -8, 9),
                elementsToRemove = setOf(1, 5),
            ),
            expectedOldValue = 3,
            expectedNewValue = 4,
        )
    }

    @Test
    fun test_update_sizeDecreased() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.size

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = sourceReactiveSet.change(
                elementsToAdd = setOf(6, -7),
                elementsToRemove = setOf(1, 5),
            ),
            expectedOldValue = 3,
            expectedNewValue = 2,
        )
    }

    @Test
    fun test_update_sizeUnaffected() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.size

        CellTestUtils.verifyDoesNotUpdateAtAll(
            subjectCell = subjectCell,
            inputStimulation = sourceReactiveSet.change(
                elementsToAdd = setOf(6, 7, -8),
                elementsToRemove = setOf(1, 5),
            ),
            expectedUnaffectedValue = 3,
        )
    }

    @Test
    fun test_update_revoked_sizeAffected() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.size

        CellTestUtils.verifyDoesNotUpdateEffectively(
            subjectCell = subjectCell,
            inputStimulation = _root_ide_package_.dev.azide.core.test_utils.TestInputStimulation.Companion.combine(
                sourceReactiveSet.change(
                    elementsToAdd = setOf(6, 7, -8, 9),
                    elementsToRemove = setOf(1, 5),
                ),
                sourceReactiveSet.revokeChange(),
            ),
            expectedUnaffectedValue = 3,
        )
    }

    @Test
    fun test_update_revoked_sizeUnaffected() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.size

        CellTestUtils.verifyDoesNotUpdateAtAll(
            subjectCell = subjectCell,
            inputStimulation = _root_ide_package_.dev.azide.core.test_utils.TestInputStimulation.Companion.combine(
                sourceReactiveSet.change(
                    elementsToAdd = setOf(6, -7),
                    elementsToRemove = setOf(1, -2),
                ),
                sourceReactiveSet.revokeChange(),
            ),
            expectedUnaffectedValue = 3,
        )
    }

    @Test
    fun test_update_corrected_sizeUnaffectedEarlier_sizeUnaffectedLater() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.size

        CellTestUtils.verifyDoesNotUpdateAtAll(
            subjectCell = subjectCell,
            inputStimulation = _root_ide_package_.dev.azide.core.test_utils.TestInputStimulation.Companion.combine(
                sourceReactiveSet.change(
                    elementsToAdd = setOf(6, -7),
                    elementsToRemove = setOf(1, -2),
                ),
                sourceReactiveSet.correctChange(
                    correctedElementsToAdd = setOf(-6, 7),
                    correctedElementsToRemove = setOf(3, -4),
                ),
            ),
            expectedUnaffectedValue = 3,
        )
    }

    @Test
    fun test_update_corrected_sizeUnaffectedEarlier_sizeAffectedLater() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.size

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = _root_ide_package_.dev.azide.core.test_utils.TestInputStimulation.Companion.combine(
                sourceReactiveSet.change(
                    elementsToAdd = setOf(6, -7),
                    elementsToRemove = setOf(1, -2),
                ),
                sourceReactiveSet.correctChange(
                    correctedElementsToAdd = setOf(-6, 7, 8, 9),
                    correctedElementsToRemove = setOf(1, -2),
                ),
            ),
            expectedOldValue = 3,
            expectedNewValue = 5,
        )
    }

    @Test
    fun test_update_corrected_sizeAffectedEarlier_sizeUnaffectedLater() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.size

        CellTestUtils.verifyDoesNotUpdateEffectively(
            subjectCell = subjectCell,
            inputStimulation = _root_ide_package_.dev.azide.core.test_utils.TestInputStimulation.Companion.combine(
                sourceReactiveSet.change(
                    elementsToAdd = setOf(6),
                    elementsToRemove = setOf(1, -2, 3, 5),
                ),
                sourceReactiveSet.correctChange(
                    correctedElementsToAdd = setOf(-6, 7),
                    correctedElementsToRemove = setOf(1, -2),
                ),
            ),
            expectedUnaffectedValue = 3,
        )
    }

    @Test
    fun test_update_corrected_sizeAffectedEarlier_sizeAffectedLater() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.size

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = _root_ide_package_.dev.azide.core.test_utils.TestInputStimulation.Companion.combine(
                sourceReactiveSet.change(
                    elementsToAdd = setOf(6, -7, 8),
                    elementsToRemove = setOf(1, -2),
                ),
                sourceReactiveSet.correctChange(
                    correctedElementsToAdd = setOf(-6, 7, 8, 9),
                    correctedElementsToRemove = setOf(3, -4),
                ),
            ),
            expectedOldValue = 3,
            expectedNewValue = 5,
        )
    }
}
