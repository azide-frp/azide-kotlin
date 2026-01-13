package dev.azide.core.collections.reactive_set

import dev.azide.core.collections.filter
import dev.azide.core.test_utils.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.TestReactiveSetObservationTrait
import dev.azide.core.test_utils.ExpectedReactiveSetReactionTestUtils
import dev.azide.core.test_utils.collections.reactive_set.ReactiveSetTestUtils
import dev.azide.core.test_utils.collections.reactive_set.correctingChange
import dev.azide.core.test_utils.collections.reactive_set.revokingChange
import dev.azide.core.test_utils.stimulation_test_strategies.PerceivedUpFrontStimulationTestStrategy
import dev.azide.core.test_utils.stimulation_test_strategies.PreStimulationTestStrategy
import dev.azide.core.test_utils.stimulation_test_strategies.PostStimulationTestStrategy
import dev.azide.core.test_utils.stimulation_test_strategies.StimulationTestStrategy
import kotlin.test.Test

@Suppress("ClassName")
class ReactiveSet_filter_tests {
    @Test
    fun test_passiveSample() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }

        ReactiveSetTestUtils.verifySampledElements(
            subjectReactiveSet = subjectReactiveSet,
            expectedElements = setOf(1, 3, 5),
        )
    }

    /**
     * "Accepted all" is a special case of "accepted some", so the full test strategy matrix is not applied.
     */
    @Test
    fun test_sourceChanges_predicateAcceptedAll() {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }

        PerceivedUpFrontStimulationTestStrategy.verifyStimulationEffectiveness(
            subjectPerceptionTrait = TestReactiveSetObservationTrait(),
            subject = subjectReactiveSet,
            inputStimulation = sourceReactiveSet.change(
                elementsToAdd = setOf(6, 7),
                elementsToRemove = setOf(1, 5),
            ),
            expectedSubjectReaction = ExpectedReactiveSetReactionTestUtils.expectChange(
                expectedNewElements = setOf(3, 6, 7),
            ),
        )
    }

    @Test
    fun test_sourceChanges_predicateRejectedAll_observedUpFront() {
        test_sourceChanges_predicateRejectedAll(
            stimulationTestStrategy = PerceivedUpFrontStimulationTestStrategy,
        )
    }

    @Test
    fun test_sourceChanges_predicateRejectedAll_preObserved() {
        test_sourceChanges_predicateRejectedAll(
            stimulationTestStrategy = PostStimulationTestStrategy,
        )
    }

    @Test
    fun test_sourceChanges_predicateRejectedAll_preStimulated() {
        test_sourceChanges_predicateRejectedAll(
            stimulationTestStrategy = PreStimulationTestStrategy,
        )
    }

    private fun test_sourceChanges_predicateRejectedAll(
        stimulationTestStrategy: StimulationTestStrategy,
    ) {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }

        stimulationTestStrategy.verifyStimulationEffectiveness(
            subjectPerceptionTrait = TestReactiveSetObservationTrait(),
            subject = subjectReactiveSet,
            inputStimulation = sourceReactiveSet.change(
                elementsToAdd = setOf(-6, -7),
                elementsToRemove = setOf(-2, -4),
            ),
            expectedSubjectReaction = ExpectedReactiveSetReactionTestUtils.expectNoReaction(),
        )
    }

    @Test
    fun test_sourceChanges_predicateAcceptedSome_observedUpFront() {
        test_sourceChanges_predicateAcceptedSome(
            stimulationTestStrategy = PerceivedUpFrontStimulationTestStrategy,
        )
    }

    @Test
    fun test_sourceChanges_predicateAcceptedSome_preObserved() {
        test_sourceChanges_predicateAcceptedSome(
            stimulationTestStrategy = PostStimulationTestStrategy,
        )
    }

    @Test
    fun test_sourceChanges_predicateAcceptedSome_preStimulated() {
        test_sourceChanges_predicateAcceptedSome(
            stimulationTestStrategy = PreStimulationTestStrategy,
        )
    }

    private fun test_sourceChanges_predicateAcceptedSome(
        stimulationTestStrategy: StimulationTestStrategy,
    ) {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }

        stimulationTestStrategy.verifyStimulationEffectiveness(
            subjectPerceptionTrait = TestReactiveSetObservationTrait(),
            subject = subjectReactiveSet,
            inputStimulation = sourceReactiveSet.change(
                elementsToAdd = setOf(6, -7, 8, -9),
                elementsToRemove = setOf(1, -2),
            ),
            expectedSubjectReaction = ExpectedReactiveSetReactionTestUtils.expectChange(
                expectedNewElements = setOf(3, 5, 6, 8),
            ),
        )
    }

    @Test
    fun test_sourceChanges_revoked_predicateAcceptedSome_observedUpFront() {
        test_sourceChanges_revoked_predicateAcceptedSome(
            stimulationTestStrategy = PerceivedUpFrontStimulationTestStrategy,
        )
    }

    @Test
    fun test_sourceChanges_revoked_predicateAcceptedSome_preObserved() {
        test_sourceChanges_revoked_predicateAcceptedSome(
            stimulationTestStrategy = PostStimulationTestStrategy,
        )
    }

    @Test
    fun test_sourceChanges_revoked_predicateAcceptedSome_preStimulated() {
        test_sourceChanges_revoked_predicateAcceptedSome(
            stimulationTestStrategy = PreStimulationTestStrategy,
        )
    }

    private fun test_sourceChanges_revoked_predicateAcceptedSome(
        stimulationTestStrategy: StimulationTestStrategy,
    ) {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }

        stimulationTestStrategy.verifyStimulationEffectiveness(
            subjectPerceptionTrait = TestReactiveSetObservationTrait(),
            subject = subjectReactiveSet,
            inputStimulation = sourceReactiveSet.revokingChange(
                elementsToAdd = setOf(6, 7),
                elementsToRemove = setOf(-2, 5),
            ),
            expectedSubjectReaction = ExpectedReactiveSetReactionTestUtils.expectNoReaction(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
            ),
        )
    }

    @Test
    fun test_sourceChanges_revoked_predicateRejectedAll_observedUpFront() {
        test_sourceChanges_revoked_predicateRejectedAll(
            stimulationTestStrategy = PerceivedUpFrontStimulationTestStrategy,
        )
    }

    @Test
    fun test_sourceChanges_revoked_predicateRejectedAll_preObserved() {
        test_sourceChanges_revoked_predicateRejectedAll(
            stimulationTestStrategy = PostStimulationTestStrategy,
        )
    }

    @Test
    fun test_sourceChanges_revoked_predicateRejectedAll_preStimulated() {
        test_sourceChanges_revoked_predicateRejectedAll(
            stimulationTestStrategy = PreStimulationTestStrategy,
        )
    }

    private fun test_sourceChanges_revoked_predicateRejectedAll(
        stimulationTestStrategy: StimulationTestStrategy,
    ) {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }

        stimulationTestStrategy.verifyStimulationEffectiveness(
            subjectPerceptionTrait = TestReactiveSetObservationTrait(),
            subject = subjectReactiveSet,
            inputStimulation = sourceReactiveSet.revokingChange(
                elementsToAdd = setOf(-6, -7),
                elementsToRemove = setOf(-2),
            ),
            expectedSubjectReaction = ExpectedReactiveSetReactionTestUtils.expectNoReaction(),
        )
    }

    @Test
    fun test_sourceChanges_corrected_predicateRejectedAllEarlier_predicateRejectedAllLater_observedUpFront() {
        test_sourceChanges_corrected_predicateRejectedAllEarlier_predicateRejectedAllLater(
            stimulationTestStrategy = PerceivedUpFrontStimulationTestStrategy,
        )
    }

    @Test
    fun test_sourceChanges_corrected_predicateRejectedAllEarlier_predicateRejectedAllLater_preObserved() {
        test_sourceChanges_corrected_predicateRejectedAllEarlier_predicateRejectedAllLater(
            stimulationTestStrategy = PostStimulationTestStrategy,
        )
    }

    @Test
    fun test_sourceChanges_corrected_predicateRejectedAllEarlier_predicateRejectedAllLater_preStimulated() {
        test_sourceChanges_corrected_predicateRejectedAllEarlier_predicateRejectedAllLater(
            stimulationTestStrategy = PreStimulationTestStrategy,
        )
    }

    private fun test_sourceChanges_corrected_predicateRejectedAllEarlier_predicateRejectedAllLater(
        stimulationTestStrategy: StimulationTestStrategy,
    ) {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }

        stimulationTestStrategy.verifyStimulationEffectiveness(
            subjectPerceptionTrait = TestReactiveSetObservationTrait(),
            subject = subjectReactiveSet,
            inputStimulation = sourceReactiveSet.correctingChange(
                intermediateElementsToAdd = setOf(-6, -7),
                intermediateElementsToRemove = setOf(-2, -4),
                correctedElementsToAdd = setOf(-8),
                correctedElementsToRemove = setOf(-2, -4),
            ),
            expectedSubjectReaction = ExpectedReactiveSetReactionTestUtils.expectNoReaction(),
        )
    }

    @Test
    fun test_sourceChanges_corrected_predicateRejectedAllEarlier_predicateAcceptedSomeLater_observedUpFront() {
        test_sourceChanges_corrected_predicateRejectedAllEarlier_predicateAcceptedSomeLater(
            stimulationTestStrategy = PerceivedUpFrontStimulationTestStrategy,
        )
    }

    @Test
    fun test_sourceChanges_corrected_predicateRejectedAllEarlier_predicateAcceptedSomeLater_preObserved() {
        test_sourceChanges_corrected_predicateRejectedAllEarlier_predicateAcceptedSomeLater(
            stimulationTestStrategy = PostStimulationTestStrategy,
        )
    }

    @Test
    fun test_sourceChanges_corrected_predicateRejectedAllEarlier_predicateAcceptedSomeLater_preStimulated() {
        test_sourceChanges_corrected_predicateRejectedAllEarlier_predicateAcceptedSomeLater(
            stimulationTestStrategy = PreStimulationTestStrategy,
        )
    }

    private fun test_sourceChanges_corrected_predicateRejectedAllEarlier_predicateAcceptedSomeLater(
        stimulationTestStrategy: StimulationTestStrategy,
    ) {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }

        stimulationTestStrategy.verifyStimulationEffectiveness(
            subjectPerceptionTrait = TestReactiveSetObservationTrait(),
            subject = subjectReactiveSet,
            inputStimulation = sourceReactiveSet.correctingChange(
                intermediateElementsToAdd = setOf(-6, -7),
                intermediateElementsToRemove = setOf(-2, -4),
                correctedElementsToAdd = setOf(6, 8),
                correctedElementsToRemove = setOf(-2, -4),
            ),
            expectedSubjectReaction = ExpectedReactiveSetReactionTestUtils.expectChange(
                expectedNewElements = setOf(1, 3, 5, 6, 8),
            ),
        )
    }

    @Test
    fun test_sourceChanges_corrected_predicateAcceptedSomeEarlier_predicateRejectedAllLater_observedUpFront() {
        test_sourceChanges_corrected_predicateAcceptedSomeEarlier_predicateRejectedAllLater(
            stimulationTestStrategy = PerceivedUpFrontStimulationTestStrategy,
        )
    }

    @Test
    fun test_sourceChanges_corrected_predicateAcceptedSomeEarlier_predicateRejectedAllLater_preObserved() {
        test_sourceChanges_corrected_predicateAcceptedSomeEarlier_predicateRejectedAllLater(
            stimulationTestStrategy = PostStimulationTestStrategy,
        )
    }

    @Test
    fun test_sourceChanges_corrected_predicateAcceptedSomeEarlier_predicateRejectedAllLater_preStimulated() {
        test_sourceChanges_corrected_predicateAcceptedSomeEarlier_predicateRejectedAllLater(
            stimulationTestStrategy = PreStimulationTestStrategy,
        )
    }

    private fun test_sourceChanges_corrected_predicateAcceptedSomeEarlier_predicateRejectedAllLater(
        stimulationTestStrategy: StimulationTestStrategy,
    ) {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }

        stimulationTestStrategy.verifyStimulationEffectiveness(
            subjectPerceptionTrait = TestReactiveSetObservationTrait(),
            subject = subjectReactiveSet,
            inputStimulation = sourceReactiveSet.correctingChange(
                intermediateElementsToAdd = setOf(6, 7),
                intermediateElementsToRemove = setOf(-2, 5),
                correctedElementsToAdd = setOf(-6, -8),
                correctedElementsToRemove = setOf(-2, -4),
            ),
            expectedSubjectReaction = ExpectedReactiveSetReactionTestUtils.expectNoReaction(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
            ),
        )
    }

    @Test
    fun test_sourceChanges_corrected_predicateAcceptedSomeEarlier_predicateAcceptedSomeLater_observedUpFront() {
        test_sourceChanges_corrected_predicateAcceptedSomeEarlier_predicateAcceptedSomeLater(
            stimulationTestStrategy = PerceivedUpFrontStimulationTestStrategy,
        )
    }

    @Test
    fun test_sourceChanges_corrected_predicateAcceptedSomeEarlier_predicateAcceptedSomeLater_preObserved() {
        test_sourceChanges_corrected_predicateAcceptedSomeEarlier_predicateAcceptedSomeLater(
            stimulationTestStrategy = PostStimulationTestStrategy,
        )
    }

    @Test
    fun test_sourceChanges_corrected_predicateAcceptedSomeEarlier_predicateAcceptedSomeLater_preStimulated() {
        test_sourceChanges_corrected_predicateAcceptedSomeEarlier_predicateAcceptedSomeLater(
            stimulationTestStrategy = PreStimulationTestStrategy,
        )
    }

    private fun test_sourceChanges_corrected_predicateAcceptedSomeEarlier_predicateAcceptedSomeLater(
        stimulationTestStrategy: StimulationTestStrategy,
    ) {
        val sourceReactiveSet = ReactiveSetTestUtils.createInputReactiveSet(
            initialElements = setOf(1, -2, 3, -4, 5),
        )

        val subjectReactiveSet = sourceReactiveSet.filter { it > 0 }

        stimulationTestStrategy.verifyStimulationEffectiveness(
            subjectPerceptionTrait = TestReactiveSetObservationTrait(),
            subject = subjectReactiveSet,
            inputStimulation = sourceReactiveSet.correctingChange(
                intermediateElementsToAdd = setOf(6, 7),
                intermediateElementsToRemove = setOf(-2, 5),
                correctedElementsToAdd = setOf(-6, 8),
                correctedElementsToRemove = setOf(-2, 3, 5),
            ),
            expectedSubjectReaction = ExpectedReactiveSetReactionTestUtils.expectChange(
                expectedNewElements = setOf(1, 8),
            ),
        )
    }
}
