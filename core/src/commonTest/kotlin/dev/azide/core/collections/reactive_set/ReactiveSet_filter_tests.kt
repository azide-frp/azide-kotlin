package dev.azide.core.collections.reactive_set

import dev.azide.core.collections.ReactiveSet
import dev.azide.core.collections.filter
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.collections.reactive_set.ReactiveSetTestUtils
import dev.azide.core.test_utils.collections.reactive_set.TestInputReactiveSet
import kotlin.test.Test

@Suppress("ClassName")
class ReactiveSet_filter_tests {
    @Test
    fun test_passiveSampling_sourceConst() {
        val sourceReactiveSet = ReactiveSet.Const(
            constElements = setOf(1, -2, 3, -4, 5),
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
    fun test_passiveSampling() {
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
    fun test_sourceChanges_predicateAcceptedAll() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter {
            it > 0
        }

        ReactiveSetTestUtils.verifyChangesAsExpected(
            subjectReactiveSet = subjectReactiveSet,
            inputStimulation = sourceReactiveSet.change(
                changeDescription = TestInputReactiveSet.ChangeDescription(
                    addedElements = setOf(6, 7),
                    removedElements = setOf(1, 5),
                ),
            ),
            expectedOldElements = setOf(1, 3, 5),
            expectedChangedElements = setOf(3, 6, 7),
        )
    }

    @Test
    fun test_sourceChanges_predicateRejectedAll() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter {
            it > 0
        }

        ReactiveSetTestUtils.verifyDoesNotChangeAtAll(
            subjectReactiveSet = subjectReactiveSet,
            inputStimulation = sourceReactiveSet.change(
                changeDescription = TestInputReactiveSet.ChangeDescription(
                    addedElements = setOf(-6, -7),
                    removedElements = setOf(-2, -4),
                ),
            ),
            expectedUnaffectedElements = setOf(1, 3, 5),
        )
    }

    @Test
    fun test_sourceChanges_predicateAcceptedSome() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter {
            it > 0
        }

        ReactiveSetTestUtils.verifyChangesAsExpected(
            subjectReactiveSet = subjectReactiveSet,
            inputStimulation = sourceReactiveSet.change(
                changeDescription = TestInputReactiveSet.ChangeDescription(
                    addedElements = setOf(6, -7, 8, -9),
                    removedElements = setOf(1, -2),
                ),
            ),
            expectedOldElements = setOf(1, 3, 5),
            expectedChangedElements = setOf(3, 5, 6, 8),
        )
    }

    @Test
    fun test_sourceChangesRevoked_predicateAcceptedSome() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter {
            it > 0
        }

        ReactiveSetTestUtils.verifyDoesNotChangeEffectively(
            subjectReactiveSet = subjectReactiveSet,
            inputStimulation = TestStimulation.combine(
                sourceReactiveSet.change(
                    changeDescription = TestInputReactiveSet.ChangeDescription(
                        addedElements = setOf(6, 7),
                        removedElements = setOf(-2, 5),
                    ),
                ),
                sourceReactiveSet.revokeChange(),
            ),
            expectedUnaffectedElements = setOf(1, 3, 5),
        )
    }

    @Test
    fun test_sourceChangesRevoked_predicateAcceptedNone() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter {
            it > 0
        }

        ReactiveSetTestUtils.verifyDoesNotChangeAtAll(
            subjectReactiveSet = subjectReactiveSet,
            inputStimulation = TestStimulation.combine(
                sourceReactiveSet.change(
                    changeDescription = TestInputReactiveSet.ChangeDescription(
                        addedElements = setOf(-6, -7),
                        removedElements = setOf(-2),
                    ),
                ),
                sourceReactiveSet.revokeChange(),
            ),
            expectedUnaffectedElements = setOf(1, 3, 5),
        )
    }

    @Test
    fun test_sourceChangesCorrected_predicateAcceptedNoneEarlier_predicateAcceptedNoneLater() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter {
            it > 0
        }

        ReactiveSetTestUtils.verifyDoesNotChangeAtAll(
            subjectReactiveSet = subjectReactiveSet,
            inputStimulation = TestStimulation.combine(
                sourceReactiveSet.change(
                    changeDescription = TestInputReactiveSet.ChangeDescription(
                        addedElements = setOf(-6, -7),
                        removedElements = setOf(-2, -4),
                    ),
                ),
                sourceReactiveSet.correctChange(
                    correctedChangeDescription = TestInputReactiveSet.ChangeDescription(
                        addedElements = setOf(-8),
                        removedElements = setOf(-2, -4),
                    ),
                ),
            ),
            expectedUnaffectedElements = setOf(1, 3, 5),
        )
    }

    @Test
    fun test_sourceChangesCorrected_predicateAcceptedNoneEarlier_predicateAcceptedSomeLater() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter {
            it > 0
        }

        ReactiveSetTestUtils.verifyChangesAsExpected(
            subjectReactiveSet = subjectReactiveSet,
            inputStimulation = TestStimulation.combine(
                sourceReactiveSet.change(
                    changeDescription = TestInputReactiveSet.ChangeDescription(
                        addedElements = setOf(-6, -7),
                        removedElements = setOf(-2, -4),
                    ),
                ),
                sourceReactiveSet.correctChange(
                    correctedChangeDescription = TestInputReactiveSet.ChangeDescription(
                        addedElements = setOf(6, 8),
                        removedElements = setOf(-2, -4),
                    ),
                ),
            ),
            expectedOldElements = setOf(1, 3, 5),
            expectedChangedElements = setOf(1, 3, 5, 6, 8),
        )
    }

    @Test
    fun test_sourceChangesCorrected_predicateAcceptedSomeEarlier_predicateAcceptedNoneLater() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter {
            it > 0
        }

        ReactiveSetTestUtils.verifyDoesNotChangeEffectively(
            subjectReactiveSet = subjectReactiveSet,
            inputStimulation = TestStimulation.combine(
                sourceReactiveSet.change(
                    changeDescription = TestInputReactiveSet.ChangeDescription(
                        addedElements = setOf(6, 7),
                        removedElements = setOf(-2, 5),
                    ),
                ),
                sourceReactiveSet.correctChange(
                    correctedChangeDescription = TestInputReactiveSet.ChangeDescription(
                        addedElements = setOf(-6, -8),
                        removedElements = setOf(-2, -4),
                    ),
                ),
            ),
            expectedUnaffectedElements = setOf(1, 3, 5),
        )
    }

    @Test
    fun test_sourceChangesCorrected_predicateAcceptedSomeEarlier_predicateAcceptedSomeLater() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter {
            it > 0
        }

        ReactiveSetTestUtils.verifyChangesAsExpected(
            subjectReactiveSet = subjectReactiveSet,
            inputStimulation = TestStimulation.combine(
                sourceReactiveSet.change(
                    changeDescription = TestInputReactiveSet.ChangeDescription(
                        addedElements = setOf(6, 7),
                        removedElements = setOf(-2, 5),
                    ),
                ),
                sourceReactiveSet.correctChange(
                    correctedChangeDescription = TestInputReactiveSet.ChangeDescription(
                        addedElements = setOf(-6, 8),
                        removedElements = setOf(-2, 3, 5),
                    ),
                ),
            ),
            expectedOldElements = setOf(1, 3, 5),
            expectedChangedElements = setOf(1, 8),
        )
    }
}
