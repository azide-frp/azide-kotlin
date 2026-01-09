package dev.azide.core.collections.reactive_set

import dev.azide.core.collections.filter
import dev.azide.core.test_utils.TestInputStimulation
import dev.azide.core.test_utils.collections.reactive_set.ReactiveSetTestUtils
import kotlin.test.Test

@Suppress("ClassName")
class ReactiveSet_filter_tests {
    @Test
    fun test_passiveSample() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter {
            it > 0
        }

        ReactiveSetTestUtils.verifySampledElements(
            subjectReactiveSet = subjectReactiveSet,
            expectedElements = setOf(1, 3, 5),
        )
    }

    @Test
    fun test_change_predicateAcceptedAll() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter {
            it > 0
        }

        ReactiveSetTestUtils.verifyChangesAsExpected(
            subjectReactiveSet = subjectReactiveSet,
            inputStimulation = sourceReactiveSet.change(
                elementsToAdd = setOf(6, 7),
                elementsToRemove = setOf(1, 5),
            ),
            expectedOldElements = setOf(1, 3, 5),
            expectedChangedElements = setOf(3, 6, 7),
        )
    }

    @Test
    fun test_change_predicateRejectedAll() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter {
            it > 0
        }

        ReactiveSetTestUtils.verifyDoesNotChangeAtAll(
            subjectReactiveSet = subjectReactiveSet,
            inputStimulation = sourceReactiveSet.change(
                elementsToAdd = setOf(-6, -7),
                elementsToRemove = setOf(-2, -4),
            ),
            expectedUnaffectedElements = setOf(1, 3, 5),
        )
    }

    @Test
    fun test_change_predicateAcceptedSome() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter {
            it > 0
        }

        ReactiveSetTestUtils.verifyChangesAsExpected(
            subjectReactiveSet = subjectReactiveSet,
            inputStimulation = sourceReactiveSet.change(
                elementsToAdd = setOf(6, -7, 8, -9),
                elementsToRemove = setOf(1, -2),
            ),
            expectedOldElements = setOf(1, 3, 5),
            expectedChangedElements = setOf(3, 5, 6, 8),
        )
    }

    @Test
    fun test_change_revoked_predicateAcceptedSome() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter {
            it > 0
        }

        ReactiveSetTestUtils.verifyDoesNotChangeEffectively(
            subjectReactiveSet = subjectReactiveSet,
            inputStimulation = TestInputStimulation.combine(
                sourceReactiveSet.change(
                    elementsToAdd = setOf(6, 7),
                    elementsToRemove = setOf(-2, 5),
                ),
                sourceReactiveSet.revokeChange(),
            ),
            expectedUnaffectedElements = setOf(1, 3, 5),
        )
    }

    @Test
    fun test_change_revoked_predicateAcceptedNone() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter {
            it > 0
        }

        ReactiveSetTestUtils.verifyDoesNotChangeAtAll(
            subjectReactiveSet = subjectReactiveSet,
            inputStimulation = TestInputStimulation.combine(
                sourceReactiveSet.change(
                    elementsToAdd = setOf(-6, -7),
                    elementsToRemove = setOf(-2),
                ),
                sourceReactiveSet.revokeChange(),
            ),
            expectedUnaffectedElements = setOf(1, 3, 5),
        )
    }

    @Test
    fun test_change_corrected_predicateAcceptedNoneEarlier_predicateAcceptedNoneLater() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter {
            it > 0
        }

        ReactiveSetTestUtils.verifyDoesNotChangeAtAll(
            subjectReactiveSet = subjectReactiveSet,
            inputStimulation = TestInputStimulation.combine(
                sourceReactiveSet.change(
                    elementsToAdd = setOf(-6, -7),
                    elementsToRemove = setOf(-2, -4),
                ),
                sourceReactiveSet.correctChange(
                    correctedElementsToAdd = setOf(-8),
                    correctedElementsToRemove = setOf(-2, -4),
                ),
            ),
            expectedUnaffectedElements = setOf(1, 3, 5),
        )
    }

    @Test
    fun test_change_corrected_predicateAcceptedNoneEarlier_predicateAcceptedSomeLater() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter {
            it > 0
        }

        ReactiveSetTestUtils.verifyChangesAsExpected(
            subjectReactiveSet = subjectReactiveSet,
            inputStimulation = TestInputStimulation.combine(
                sourceReactiveSet.change(
                    elementsToAdd = setOf(-6, -7),
                    elementsToRemove = setOf(-2, -4),
                ),
                sourceReactiveSet.correctChange(
                    correctedElementsToAdd = setOf(6, 8),
                    correctedElementsToRemove = setOf(-2, -4),
                ),
            ),
            expectedOldElements = setOf(1, 3, 5),
            expectedChangedElements = setOf(1, 3, 5, 6, 8),
        )
    }

    @Test
    fun test_change_corrected_predicateAcceptedSomeEarlier_predicateAcceptedNoneLater() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter {
            it > 0
        }

        ReactiveSetTestUtils.verifyDoesNotChangeEffectively(
            subjectReactiveSet = subjectReactiveSet,
            inputStimulation = TestInputStimulation.combine(
                sourceReactiveSet.change(
                    elementsToAdd = setOf(6, 7),
                    elementsToRemove = setOf(-2, 5),
                ),
                sourceReactiveSet.correctChange(
                    correctedElementsToAdd = setOf(-6, -8),
                    correctedElementsToRemove = setOf(-2, -4),
                ),
            ),
            expectedUnaffectedElements = setOf(1, 3, 5),
        )
    }

    @Test
    fun test_change_corrected_predicateAcceptedSomeEarlier_predicateAcceptedSomeLater() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter {
            it > 0
        }

        ReactiveSetTestUtils.verifyChangesAsExpected(
            subjectReactiveSet = subjectReactiveSet,
            inputStimulation = TestInputStimulation.combine(
                sourceReactiveSet.change(
                    elementsToAdd = setOf(6, 7),
                    elementsToRemove = setOf(-2, 5),
                ),
                sourceReactiveSet.correctChange(
                    correctedElementsToAdd = setOf(-6, 8),
                    correctedElementsToRemove = setOf(-2, 3, 5),
                ),
            ),
            expectedOldElements = setOf(1, 3, 5),
            expectedChangedElements = setOf(1, 8),
        )
    }
}
