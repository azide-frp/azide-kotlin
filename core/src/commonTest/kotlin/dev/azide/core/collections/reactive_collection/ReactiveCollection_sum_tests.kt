package dev.azide.core.collections.reactive_collection

import dev.azide.core.collections.ReactiveSet
import dev.azide.core.collections.sum
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.cell.CellTestUtils_deprecated
import dev.azide.core.test_utils.collections.reactive_set.ReactiveSetTestUtils
import dev.azide.core.test_utils.collections.reactive_set.TestInputReactiveSet
import kotlin.test.Test

@Suppress("ClassName")
class ReactiveCollection_sum_tests {
    @Test
    fun test_atRest_sourceConst() {
        val sourceReactiveSet = ReactiveSet.Const(
            constElements = setOf(1, 2, 3, 4),
        )

        val subjectCell = sourceReactiveSet.sum()

        CellTestUtils_deprecated.verifyAtRest(
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

        CellTestUtils_deprecated.verifyAtRest(
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

        CellTestUtils_deprecated.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = sourceReactiveSet.change(
                changeDescription = TestInputReactiveSet.ChangeDescription(
                    addedElements = setOf(5, 6),
                    removedElements = setOf(2, 3),
                ),
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

        CellTestUtils_deprecated.verifyDoesNotUpdateEffectively(
            subjectCell = subjectCell,
            inputStimulation = TestStimulation.combineInProvidedOrder(
                sourceReactiveSet.change(
                    changeDescription = TestInputReactiveSet.ChangeDescription(
                        addedElements = setOf(5, 6),
                        removedElements = setOf(2, 3),
                    ),
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

        CellTestUtils_deprecated.verifyUpdatesAsExpected(
            subjectCell = subjectCell,
            inputStimulation = TestStimulation.combineInProvidedOrder(
                sourceReactiveSet.change(
                    changeDescription = TestInputReactiveSet.ChangeDescription(
                        addedElements = setOf(5, 6),
                        removedElements = setOf(2, 3),
                    ),
                ),
                sourceReactiveSet.correctChange(
                    correctedChangeDescription = TestInputReactiveSet.ChangeDescription(
                        addedElements = setOf(7),
                        removedElements = setOf(4),
                    ),
                ),
            ),
            expectedOldValue = 10,
            expectedNewValue = 13,
        )
    }
}
