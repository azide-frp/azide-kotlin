package dev.azide.core.collections.reactive_set

import dev.azide.core.collections.filter
import dev.azide.core.collections.size
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.cell.CellTestUtils
import dev.azide.core.test_utils.collections.reactive_set.ReactiveSetTestUtils
import dev.azide.core.test_utils.collections.reactive_set.TestInputReactiveSet
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
    fun test_sourceChanges_sizeIncreased() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.size

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = sourceReactiveSet.change(
                changeDescription = TestInputReactiveSet.ChangeDescription(
                    addedElements = setOf(6, 7, -8, 9),
                    removedElements = setOf(1, 5),
                ),
            ),
            expectedOldValue = 3,
            expectedNewValue = 4,
        )
    }

    @Test
    fun test_sourceChanges_sizeDecreased() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.size

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = sourceReactiveSet.change(
                changeDescription = TestInputReactiveSet.ChangeDescription(
                    addedElements = setOf(6, -7),
                    removedElements = setOf(1, 5),
                ),
            ),
            expectedOldValue = 3,
            expectedNewValue = 2,
        )
    }

    @Test
    fun test_sourceChanges_sizeUnaffected() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.size

        CellTestUtils.verifyDoesNotUpdateAtAll(
            subjectCell = subjectCell,
            inputStimulation = sourceReactiveSet.change(
                changeDescription = TestInputReactiveSet.ChangeDescription(
                    addedElements = setOf(6, 7, -8),
                    removedElements = setOf(1, 5),
                ),
            ),
            expectedUnaffectedValue = 3,
        )
    }

    @Test
    fun test_sourceChanges_revoked_sizeAffected() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.size

        CellTestUtils.verifyDoesNotUpdateEffectively(
            subjectCell = subjectCell,
            inputStimulation = TestStimulation.combine(
                sourceReactiveSet.change(
                    changeDescription = TestInputReactiveSet.ChangeDescription(
                        addedElements = setOf(6, 7, -8, 9),
                        removedElements = setOf(1, 5),
                    ),
                ),
                sourceReactiveSet.revokeChange(),
            ),
            expectedUnaffectedValue = 3,
        )
    }

    @Test
    fun test_sourceChanges_revoked_sizeUnaffected() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.size

        CellTestUtils.verifyDoesNotUpdateAtAll(
            subjectCell = subjectCell,
            inputStimulation = TestStimulation.combine(
                sourceReactiveSet.change(
                    changeDescription = TestInputReactiveSet.ChangeDescription(
                        addedElements = setOf(6, -7),
                        removedElements = setOf(1, -2),
                    ),
                ),
                sourceReactiveSet.revokeChange(),
            ),
            expectedUnaffectedValue = 3,
        )
    }

    @Test
    fun test_sourceChanges_corrected_sizeUnaffectedEarlier_sizeUnaffectedLater() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.size

        CellTestUtils.verifyDoesNotUpdateAtAll(
            subjectCell = subjectCell,
            inputStimulation = TestStimulation.combine(
                sourceReactiveSet.change(
                    changeDescription = TestInputReactiveSet.ChangeDescription(
                        addedElements = setOf(6, -7),
                        removedElements = setOf(1, -2),
                    ),
                ),
                sourceReactiveSet.correctChange(
                    correctedChangeDescription = TestInputReactiveSet.ChangeDescription(
                        addedElements = setOf(-6, 7),
                        removedElements = setOf(3, -4),
                    ),
                ),
            ),
            expectedUnaffectedValue = 3,
        )
    }

    @Test
    fun test_sourceChanges_corrected_sizeUnaffectedEarlier_sizeAffectedLater() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.size

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = TestStimulation.combine(
                sourceReactiveSet.change(
                    changeDescription = TestInputReactiveSet.ChangeDescription(
                        addedElements = setOf(6, -7),
                        removedElements = setOf(1, -2),
                    ),
                ),
                sourceReactiveSet.correctChange(
                    correctedChangeDescription = TestInputReactiveSet.ChangeDescription(
                        addedElements = setOf(-6, 7, 8, 9),
                        removedElements = setOf(1, -2),
                    ),
                ),
            ),
            expectedOldValue = 3,
            expectedNewValue = 5,
        )
    }

    @Test
    fun test_sourceChanges_corrected_sizeAffectedEarlier_sizeUnaffectedLater() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.size

        CellTestUtils.verifyDoesNotUpdateEffectively(
            subjectCell = subjectCell,
            inputStimulation = TestStimulation.combine(
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
            expectedUnaffectedValue = 3,
        )
    }

    @Test
    fun test_sourceChanges_corrected_sizeAffectedEarlier_sizeAffectedLater() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }
        val subjectCell = subjectReactiveSet.size

        CellTestUtils.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = TestStimulation.combine(
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
            expectedOldValue = 3,
            expectedNewValue = 5,
        )
    }
}
