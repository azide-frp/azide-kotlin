package dev.azide.core.collections.reactive_collection

import dev.azide.core.collections.ReactiveCollection
import dev.azide.core.collections.ReactiveCollection.Companion.map
import dev.azide.core.test_utils.TestInputStimulation
import dev.azide.core.test_utils.collections.reactive_collection.ReactiveCollectionTestUtils
import dev.azide.core.test_utils.collections.reactive_set.ReactiveSetTestUtils
import dev.kmpx.collections.multi_sets.multiSetOf
import kotlin.math.roundToInt
import kotlin.test.Ignore
import kotlin.test.Test

@Suppress("ClassName")
@Ignore // TODO: Rework collection mapping
class ReactiveCollection_map_tests {
    @Test
    fun test_passiveSample() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveCollection: ReactiveCollection<Int> = sourceReactiveSet.map { it * 2 }

        ReactiveCollectionTestUtils.verifySampledElements(
            subjectReactiveCollection = subjectReactiveCollection,
            expectedElements = multiSetOf(2, -4, 6, -8, 10),
        )
    }

    @Test
    fun test_change() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1.1, -2.2, 3.1, -2.1, -4.3, 5.2),
        )

        val subjectReactiveCollection: ReactiveCollection<Int> = sourceReactiveSet.map { it.roundToInt() }

        ReactiveCollectionTestUtils.verifyChangesAsExpected(
            subjectReactiveCollection = subjectReactiveCollection,
            inputStimulation = sourceReactiveSet.change(
                elementsToAdd = setOf(3.2, 7.1),
                elementsToRemove = setOf(1.1, 5.2),
            ),
            expectedOldElements = multiSetOf(1, -2, 3, -2, -4, 5),
            expectedChangedElements = multiSetOf(-2, 3, -2, -4, 3, 7),
        )
    }

    @Test
    fun test_change_revoked() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1.1, -2.2, 3.1, -2.1, -4.3, 5.2),
        )

        val subjectReactiveCollection: ReactiveCollection<Int> = sourceReactiveSet.map { it.roundToInt() }

        ReactiveCollectionTestUtils.verifyDoesNotChangeEffectively(
            subjectReactiveCollection = subjectReactiveCollection,
            inputStimulation = TestInputStimulation.combine(
                sourceReactiveSet.change(
                    elementsToAdd = setOf(3.2, 7.1),
                    elementsToRemove = setOf(1.1, 5.2),
                ),
                sourceReactiveSet.revokeChange(),
            ),
            expectedUnaffectedElements = multiSetOf(1, -2, 3, -2, -4, 5),
        )
    }

    @Test
    fun test_change_corrected() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveCollection: ReactiveCollection<Int> = sourceReactiveSet.map { it * 2 }

        ReactiveCollectionTestUtils.verifyChangesAsExpected(
            subjectReactiveCollection = subjectReactiveCollection,
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
            expectedOldElements = multiSetOf(2, -4, 6, -8, 10),
            expectedChangedElements = multiSetOf(2, 6, 10, -16),
        )
    }
}
