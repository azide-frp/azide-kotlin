package dev.azide.core.collections.reactive_set

import dev.azide.core.collections.asReactiveBag
import dev.azide.core.impl.collections.reactive_bag.taggedBagOf
import dev.azide.core.test_utils.collections.reactive_bag.ReactiveBag_expectations_testUtils
import dev.azide.core.test_utils.collections.reactive_bag.ReactiveBag_reaction_testUtils
import dev.azide.core.test_utils.collections.reactive_set.ReactiveSet_generic_testUtils
import dev.azide.core.test_utils.collections.reactive_set.ReactiveSet_generic_testUtils.SourceReactiveSetTag
import dev.azide.core.test_utils.collections.reactive_set.TestInputReactiveSet
import dev.azide.core.test_utils.collections.reactive_set.changing
import dev.azide.core.test_utils.collections.reactive_set.correctingChange
import dev.azide.core.test_utils.collections.reactive_set.revokingChange
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulationScenario
import dev.azide.core.test_utils.stimulation_combinatorics.bind
import kotlin.test.Test

@Suppress("ClassName", "PrivatePropertyName")
class ReactiveSet_asReactiveBag_tests {
    private typealias SuitableTestSlottedStimulationScenario = TestSlottedStimulationScenario<TestSlotCount.Count2>

    private val slotCount = TestSlotCount.Count2

    private val slottedStimulationBank_sourceSetChanges =
        ReactiveSet_generic_testUtils.stimulationBank_sourceSetChanges.distribute(slotCount = slotCount)

    private val slottedStimulationBank_sourceSetChangesRevoked =
        ReactiveSet_generic_testUtils.stimulationBank_sourceSetChangesRevoked.distribute(slotCount = slotCount)

    private val slottedStimulationBank_sourceSetChangesCorrected =
        ReactiveSet_generic_testUtils.stimulationBank_sourceSetChangesCorrected.distribute(slotCount = slotCount)

    @Test
    fun test_sourceSetChanges_additionsOnly() {
        slottedStimulationBank_sourceSetChanges.forEach {
            test_sourceSetChanges_additionsOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceSetChanges_additionsOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveSet = TestInputReactiveSet(
            initialElements = setOf(
                0,
                10,
                30,
            ),
        )

        val subjectReactiveBag = sourceReactiveSet.asReactiveBag

        ReactiveBag_reaction_testUtils.executeReactionTransaction(
            subjectReactiveBag,
            slottedInputStimulation = sourceReactiveSet.changing(
                tag = SourceReactiveSetTag,
                changeDescription = TestInputReactiveSet.ChangeDescription(
                    addedElements = setOf(
                        40,
                        50,
                    ),
                ),
            ).bind(slottedStimulationScenario),
            expectedSubjectElementTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                expectedOldTaggedElements = taggedBagOf(
                    0 to 0,
                    10 to 10,
                    30 to 30,
                ),
                expectedNewTaggedElements = taggedBagOf(
                    0 to 0,
                    10 to 10,
                    30 to 30,
                    40 to 40,
                    50 to 50,
                ),
            ),
        )
    }

    @Test
    fun test_sourceSetChanges_removalsOnly() {
        slottedStimulationBank_sourceSetChanges.forEach {
            test_sourceSetChanges_removalsOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceSetChanges_removalsOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveSet = TestInputReactiveSet(
            initialElements = setOf(
                0,
                10,
                20,
                30,
                40,
                50,
            ),
        )

        val subjectReactiveBag = sourceReactiveSet.asReactiveBag

        ReactiveBag_reaction_testUtils.executeReactionTransaction(
            subjectReactiveBag,
            slottedInputStimulation = sourceReactiveSet.changing(
                tag = SourceReactiveSetTag,
                changeDescription = TestInputReactiveSet.ChangeDescription(
                    removedElements = setOf(10, 20, 40),
                ),
            ).bind(slottedStimulationScenario),
            expectedSubjectElementTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                expectedOldTaggedElements = taggedBagOf(
                    0 to 0,
                    10 to 10,
                    20 to 20,
                    30 to 30,
                    40 to 40,
                    50 to 50,
                ),
                expectedNewTaggedElements = taggedBagOf(
                    0 to 0,
                    30 to 30,
                    50 to 50,
                ),
            ),
        )
    }

    @Test
    fun test_sourceSetChanges_mixed() {
        slottedStimulationBank_sourceSetChanges.forEach {
            test_sourceSetChanges_mixed(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceSetChanges_mixed(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveSet = TestInputReactiveSet(
            initialElements = setOf(
                0,
                10,
                20,
                30,
                40,
                50,
                60,
            ),
        )

        val subjectReactiveBag = sourceReactiveSet.asReactiveBag

        ReactiveBag_reaction_testUtils.executeReactionTransaction(
            subjectReactiveBag,
            slottedInputStimulation = sourceReactiveSet.changing(
                tag = SourceReactiveSetTag,
                changeDescription = TestInputReactiveSet.ChangeDescription(
                    addedElements = setOf(
                        15,
                        16,
                    ),
                    removedElements = setOf(20, 30, 50),
                ),
            ).bind(slottedStimulationScenario),
            expectedSubjectElementTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                expectedOldTaggedElements = taggedBagOf(
                    0 to 0,
                    10 to 10,
                    20 to 20,
                    30 to 30,
                    40 to 40,
                    50 to 50,
                    60 to 60,
                ),
                expectedNewTaggedElements = taggedBagOf(
                    0 to 0,
                    10 to 10,
                    15 to 15,
                    16 to 16,
                    40 to 40,
                    60 to 60,
                ),
            ),
        )
    }

    @Test
    fun test_sourceChangesRevoked_additionsOnly() {
        slottedStimulationBank_sourceSetChangesRevoked.forEach {
            test_sourceChangesRevoked_additionsOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceChangesRevoked_additionsOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveSet = TestInputReactiveSet(
            initialElements = setOf(
                0,
                10,
                20,
                30,
                40,
                50,
                60,
            ),
        )

        val subjectReactiveBag = sourceReactiveSet.asReactiveBag

        ReactiveBag_reaction_testUtils.executeReactionTransaction(
            subjectReactiveBag,
            slottedInputStimulation = sourceReactiveSet.revokingChange(
                tag = SourceReactiveSetTag,
                temporaryChangeDescription = TestInputReactiveSet.ChangeDescription(
                    addedElements = setOf(
                        70,
                        80,
                    ),
                ),
            ).bind(slottedStimulationScenario),
            expectedSubjectElementTransition = ReactiveBag_expectations_testUtils.expectNoTaggedContentTransition(
                intermediatePropagationTolerance = ExpectedTestSubjectReaction.IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedTaggedElements = taggedBagOf(
                    0 to 0,
                    10 to 10,
                    20 to 20,
                    30 to 30,
                    40 to 40,
                    50 to 50,
                    60 to 60,
                ),
            ),
        )
    }

    @Test
    fun test_sourceChangesRevoked_removalsOnly() {
        slottedStimulationBank_sourceSetChangesRevoked.forEach {
            test_sourceChangesRevoked_removalsOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceChangesRevoked_removalsOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveSet = TestInputReactiveSet(
            initialElements = setOf(
                0,
                10,
                20,
                30,
                40,
                50,
                60,
            ),
        )

        val subjectReactiveBag = sourceReactiveSet.asReactiveBag

        ReactiveBag_reaction_testUtils.executeReactionTransaction(
            subjectReactiveBag,
            slottedInputStimulation = sourceReactiveSet.revokingChange(
                tag = SourceReactiveSetTag,
                temporaryChangeDescription = TestInputReactiveSet.ChangeDescription(
                    removedElements = setOf(10, 20, 40, 50),
                ),
            ).bind(slottedStimulationScenario),
            expectedSubjectElementTransition = ReactiveBag_expectations_testUtils.expectNoTaggedContentTransition(
                intermediatePropagationTolerance = ExpectedTestSubjectReaction.IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedTaggedElements = taggedBagOf(
                    0 to 0,
                    10 to 10,
                    20 to 20,
                    30 to 30,
                    40 to 40,
                    50 to 50,
                    60 to 60,
                ),
            ),
        )
    }

    @Test
    fun test_sourceChangesRevoked_mixed() {
        slottedStimulationBank_sourceSetChangesRevoked.forEach {
            test_sourceChangesRevoked_mixed(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceChangesRevoked_mixed(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveSet = TestInputReactiveSet(
            initialElements = setOf(
                0,
                10,
                20,
                30,
                40,
                50,
                60,
            ),
        )

        val subjectReactiveBag = sourceReactiveSet.asReactiveBag

        ReactiveBag_reaction_testUtils.executeReactionTransaction(
            subjectReactiveBag,
            slottedInputStimulation = sourceReactiveSet.revokingChange(
                tag = SourceReactiveSetTag,
                temporaryChangeDescription = TestInputReactiveSet.ChangeDescription(
                    addedElements = setOf(
                        70,
                        80,
                    ),
                    removedElements = setOf(20, 30, 50),
                ),
            ).bind(slottedStimulationScenario),
            expectedSubjectElementTransition = ReactiveBag_expectations_testUtils.expectNoTaggedContentTransition(
                intermediatePropagationTolerance = ExpectedTestSubjectReaction.IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedTaggedElements = taggedBagOf(
                    0 to 0,
                    10 to 10,
                    20 to 20,
                    30 to 30,
                    40 to 40,
                    50 to 50,
                    60 to 60,
                ),
            ),
        )
    }

    @Test
    fun test_sourceChangesCorrected_additionsOnly() {
        slottedStimulationBank_sourceSetChangesCorrected.forEach {
            test_sourceChangesCorrected_additionsOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceChangesCorrected_additionsOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveSet = TestInputReactiveSet(
            initialElements = setOf(
                0,
                10,
                20,
                30,
                40,
                50,
                60,
            ),
        )

        val subjectReactiveBag = sourceReactiveSet.asReactiveBag

        ReactiveBag_reaction_testUtils.executeReactionTransaction(
            subjectReactiveBag,
            slottedInputStimulation = sourceReactiveSet.correctingChange(
                tag = SourceReactiveSetTag,
                intermediateChangeDescription = TestInputReactiveSet.ChangeDescription(
                    addedElements = setOf(
                        25, // not corrected
                        26, // corrected: added differently
                        70, // corrected: not added
                    ),
                ),
                correctedChangeDescription = TestInputReactiveSet.ChangeDescription(
                    addedElements = setOf(
                        25,
                        26,
                        27, // (not mentioned before)
                    ),
                ),
            ).bind(slottedStimulationScenario),
            expectedSubjectElementTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = ExpectedTestSubjectReaction.IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    0 to 0,
                    10 to 10,
                    20 to 20,
                    30 to 30,
                    40 to 40,
                    50 to 50,
                    60 to 60,
                ),
                expectedNewTaggedElements = taggedBagOf(
                    0 to 0,
                    10 to 10,
                    20 to 20,
                    25 to 25,
                    26 to 26,
                    27 to 27,
                    30 to 30,
                    40 to 40,
                    50 to 50,
                    60 to 60,
                ),
            ),
        )
    }

    @Test
    fun test_sourceChangesCorrected_removalsOnly() {
        slottedStimulationBank_sourceSetChangesCorrected.forEach {
            test_sourceChangesCorrected_removalsOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceChangesCorrected_removalsOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveSet = TestInputReactiveSet(
            initialElements = setOf(
                0,
                10,
                20,
                30,
                40,
                50,
                60,
            ),
        )

        val subjectReactiveBag = sourceReactiveSet.asReactiveBag

        ReactiveBag_reaction_testUtils.executeReactionTransaction(
            subjectReactiveBag,
            slottedInputStimulation = sourceReactiveSet.correctingChange(
                tag = SourceReactiveSetTag,
                intermediateChangeDescription = TestInputReactiveSet.ChangeDescription(
                    removedElements = setOf(
                        10, // corrected: not removed
                        20, // not corrected
                        30, // not corrected
                        40, // corrected: not removed
                        50, // not corrected
                    ),
                ),
                correctedChangeDescription = TestInputReactiveSet.ChangeDescription(
                    removedElements = setOf(
                        20,
                        30,
                        50,
                        60, // (not mentioned before)
                    ),
                ),
            ).bind(slottedStimulationScenario),
            expectedSubjectElementTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = ExpectedTestSubjectReaction.IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    0 to 0,
                    10 to 10,
                    20 to 20,
                    30 to 30,
                    40 to 40,
                    50 to 50,
                    60 to 60,
                ),
                expectedNewTaggedElements = taggedBagOf(
                    0 to 0,
                    10 to 10,
                    40 to 40,
                ),
            ),
        )
    }

    @Test
    fun test_sourceChangesCorrected_mixed() {
        slottedStimulationBank_sourceSetChangesCorrected.forEach {
            test_sourceChangesCorrected_mixed(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceChangesCorrected_mixed(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveSet = TestInputReactiveSet(
            initialElements = setOf(
                0,
                10,
                20,
                30,
                40,
                50,
                60,
            ),
        )

        val subjectReactiveBag = sourceReactiveSet.asReactiveBag

        ReactiveBag_reaction_testUtils.executeReactionTransaction(
            subjectReactiveBag,
            slottedInputStimulation = sourceReactiveSet.correctingChange(
                tag = SourceReactiveSetTag,
                intermediateChangeDescription = TestInputReactiveSet.ChangeDescription(
                    addedElements = setOf(
                        5, // not corrected
                        70, // corrected: not added
                    ),
                    removedElements = setOf(20, 50),
                ),
                correctedChangeDescription = TestInputReactiveSet.ChangeDescription(
                    addedElements = setOf(
                        5,
                    ),
                    removedElements = setOf(
                        20,
                        30, // (not mentioned before)
                        40,
                        50,
                        60, // (not mentioned before)
                    ),
                ),
            ).bind(slottedStimulationScenario),
            expectedSubjectElementTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = ExpectedTestSubjectReaction.IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    0 to 0,
                    10 to 10,
                    20 to 20,
                    30 to 30,
                    40 to 40,
                    50 to 50,
                    60 to 60,
                ),
                expectedNewTaggedElements = taggedBagOf(
                    0 to 0,
                    5 to 5,
                    10 to 10,
                ),
            ),
        )
    }
}
