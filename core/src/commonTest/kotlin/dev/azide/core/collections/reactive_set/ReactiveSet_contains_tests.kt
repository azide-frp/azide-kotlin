package dev.azide.core.collections.reactive_set

import dev.azide.core.collections.contains
import dev.azide.core.collections.filter
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.cell.CellTestUtils_deprecated
import dev.azide.core.test_utils.collections.reactive_set.ReactiveSetTestUtils
import dev.azide.core.test_utils.collections.reactive_set.TestInputReactiveSet
import kotlin.test.Test

@Suppress("ClassName")
class ReactiveSet_contains_tests {
    @Test
    fun test_passiveSampling_contained() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.contains(3)

        CellTestUtils_deprecated.verifyAtRest(
            subjectCell = subjectCell,
            expectedValue = true,
        )
    }

    @Test
    fun test_passiveSampling_notContained() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.contains(10)

        CellTestUtils_deprecated.verifyAtRest(
            subjectCell = subjectCell,
            expectedValue = false,
        )
    }

    @Test
    fun test_sourceChanges_elementAdded() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.contains(6)

        CellTestUtils_deprecated.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = sourceReactiveSet.change(
                changeDescription = TestInputReactiveSet.ChangeDescription(
                    addedElements = setOf(6, 7),
                    removedElements = emptySet(),
                ),
            ),
            expectedOldValue = false,
            expectedNewValue = true,
        )
    }

    @Test
    fun test_sourceChanges_elementRemoved() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.contains(1)

        CellTestUtils_deprecated.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = sourceReactiveSet.change(
                changeDescription = TestInputReactiveSet.ChangeDescription(
                    addedElements = setOf(6, -7),
                    removedElements = setOf(1, 5),
                ),
            ),
            expectedOldValue = true,
            expectedNewValue = false,
        )
    }

    @Test
    fun test_sourceChanges_elementRetained() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.contains(3)

        CellTestUtils_deprecated.verifyDoesNotUpdateAtAll(
            subjectCell = subjectCell,
            inputStimulation = sourceReactiveSet.change(
                changeDescription = TestInputReactiveSet.ChangeDescription(
                    addedElements = setOf(6, 7, -8),
                    removedElements = setOf(1, 5),
                ),
            ),
            expectedUnaffectedValue = true,
        )
    }    @Test
    fun test_sourceChanges_elementUnmentioned() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.contains(10)

        CellTestUtils_deprecated.verifyDoesNotUpdateAtAll(
            subjectCell = subjectCell,
            inputStimulation = sourceReactiveSet.change(
                changeDescription = TestInputReactiveSet.ChangeDescription(
                    addedElements = setOf(6, 7, -8),
                    removedElements = setOf(1, 5),
                ),
            ),
            expectedUnaffectedValue = false,
        )
    }

    @Test
    fun test_sourceChanges_revoked_elementAdded() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.contains(6)

        CellTestUtils_deprecated.verifyDoesNotUpdateEffectively(
            subjectCell = subjectCell,
            inputStimulation = TestStimulation.combineInProvidedOrder(
                sourceReactiveSet.change(
                    changeDescription = TestInputReactiveSet.ChangeDescription(
                        addedElements = setOf(6, -7),
                        removedElements = setOf(1, 3),
                    ),
                ),
                sourceReactiveSet.revokeChange(),
            ),
            expectedUnaffectedValue = false,
        )
    }

    @Test
    fun test_sourceChanges_revoked_elementRemoved() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.contains(1)

        CellTestUtils_deprecated.verifyDoesNotUpdateEffectively(
            subjectCell = subjectCell,
            inputStimulation = TestStimulation.combineInProvidedOrder(
                sourceReactiveSet.change(
                    changeDescription = TestInputReactiveSet.ChangeDescription(
                        addedElements = setOf(6, -7),
                        removedElements = setOf(1, 5),
                    ),
                ),
                sourceReactiveSet.revokeChange(),
            ),
            expectedUnaffectedValue = true,
        )
    }

    @Test
    fun test_sourceChanges_revoked_elementUnmentioned() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.contains(10)

        CellTestUtils_deprecated.verifyDoesNotUpdateAtAll(
            subjectCell = subjectCell,
            inputStimulation = TestStimulation.combineInProvidedOrder(
                sourceReactiveSet.change(
                    changeDescription = TestInputReactiveSet.ChangeDescription(
                        addedElements = setOf(6, 7),
                        removedElements = setOf(1, 5),
                    ),
                ),
                sourceReactiveSet.revokeChange(),
            ),
            expectedUnaffectedValue = false,
        )
    }

    @Test
    fun test_sourceChanges_corrected_unmentionedBefore_unmentionedLater() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.contains(10)

        CellTestUtils_deprecated.verifyDoesNotUpdateAtAll(
            subjectCell = subjectCell,
            inputStimulation = TestStimulation.combineInProvidedOrder(
                sourceReactiveSet.change(
                    changeDescription = TestInputReactiveSet.ChangeDescription(
                        addedElements = setOf(6, 7),
                        removedElements = setOf(1, 5),
                    ),
                ),
                sourceReactiveSet.correctChange(
                    correctedChangeDescription = TestInputReactiveSet.ChangeDescription(
                        addedElements = setOf(-6, -7),
                        removedElements = setOf(1, 5),
                    ),
                ),
            ),
            expectedUnaffectedValue = false,
        )
    }

    @Test
    fun test_sourceChanges_corrected_unmentionedBefore_mentionedLater() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.contains(8)

        CellTestUtils_deprecated.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = TestStimulation.combineInProvidedOrder(
                sourceReactiveSet.change(
                    changeDescription = TestInputReactiveSet.ChangeDescription(
                        addedElements = setOf(6, 7),
                        removedElements = setOf(1, 5),
                    ),
                ),
                sourceReactiveSet.correctChange(
                    correctedChangeDescription = TestInputReactiveSet.ChangeDescription(
                        addedElements = setOf(6, 7, 8),
                        removedElements = setOf(1, 5),
                    ),
                ),
            ),
            expectedOldValue = false,
            expectedNewValue = true,
        )
    }

    @Test
    fun test_sourceChanges_corrected_mentionedBefore_unmentionedLater() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.contains(3)

        CellTestUtils_deprecated.verifyDoesNotUpdateEffectively(
            subjectCell = subjectCell,
            inputStimulation = TestStimulation.combineInProvidedOrder(
                sourceReactiveSet.change(
                    changeDescription = TestInputReactiveSet.ChangeDescription(
                        addedElements = setOf(6),
                        removedElements = setOf(1, -2, 3, 5),
                    ),
                ),
                sourceReactiveSet.correctChange(
                    correctedChangeDescription = TestInputReactiveSet.ChangeDescription(
                        addedElements = setOf(-6, 7),
                        removedElements = setOf(1, -2),
                    ),
                ),
            ),
            expectedUnaffectedValue = true,
        )
    }

    @Test
    fun test_sourceChanges_corrected_mentionedBefore_mentionedLater() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.contains(3)

        CellTestUtils_deprecated.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = TestStimulation.combineInProvidedOrder(
                sourceReactiveSet.change(
                    changeDescription = TestInputReactiveSet.ChangeDescription(
                        addedElements = setOf(6, -7, 8),
                        removedElements = setOf(1, -2),
                    ),
                ),
                sourceReactiveSet.correctChange(
                    correctedChangeDescription = TestInputReactiveSet.ChangeDescription(
                        addedElements = setOf(-6, 7, 8, 9),
                        removedElements = setOf(3, -4),
                    ),
                ),
            ),
            expectedOldValue = true,
            expectedNewValue = false,
        )
    }
}
