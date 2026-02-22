package dev.azide.core.collections.reactive_bag

import dev.azide.core.collections.map
import dev.azide.core.impl.collections.reactive_bag.taggedBagOf
import dev.azide.core.test_utils.collections.reactive_bag.ReactiveBag_expectations_testUtils
import dev.azide.core.test_utils.collections.reactive_bag.ReactiveBag_generic_testUtils
import dev.azide.core.test_utils.collections.reactive_bag.ReactiveBag_reaction_testUtils
import dev.azide.core.test_utils.collections.reactive_bag.TestInputReactiveBag
import dev.azide.core.test_utils.collections.reactive_bag.changing
import dev.azide.core.test_utils.collections.reactive_bag.correctingChange
import dev.azide.core.test_utils.collections.reactive_bag.revokingChange
import dev.azide.core.test_utils.collections.reactive_list.ReactiveBag_sampling_testUtils
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulationScenario
import dev.azide.core.test_utils.stimulation_combinatorics.bind
import kotlin.test.Test

@Suppress("ClassName", "PrivatePropertyName")
class ReactiveBag_map_tests {
    private typealias SuitableSlotCount = TestSlotCount.Count2

    private typealias SuitableTestSlottedStimulationScenario = TestSlottedStimulationScenario<SuitableSlotCount>

    private val slottedStimulationScenarioBank_sourceBagChanges =
        ReactiveBag_generic_testUtils.stimulationScenarioBank_sourceBagChanges.distribute(slotCount = SuitableSlotCount)

    private val slottedStimulationScenarioBank_sourceBagChangesRevoked =
        ReactiveBag_generic_testUtils.stimulationScenarioBank_sourceBagChangesRevoked.distribute(slotCount = SuitableSlotCount)

    private val slottedStimulationScenarioBank_sourceBagChangesCorrected =
        ReactiveBag_generic_testUtils.stimulationScenarioBank_sourceBagChangesCorrected.distribute(slotCount = SuitableSlotCount)

    @Test
    fun test_passiveSample() {
        val sourceReactiveBag = TestInputReactiveBag(
            initialTaggedElements = taggedBagOf(
                0 to 0.1,
                10 to 10.1,
                11 to 10.1, // Element duplicate
                30 to 30.1,
            ),
        )

        val subjectReactiveBag = sourceReactiveBag.map { it.toString() }

        ReactiveBag_sampling_testUtils.executeSamplingTransaction(
            subjectReactiveBag = subjectReactiveBag,
            expectedSubjectContent = ReactiveBag_expectations_testUtils.expectStableTaggedContent(
                expectedTaggedElements = taggedBagOf(
                    0 to "0.1",
                    10 to "10.1",
                    11 to "10.1", // Element duplicate
                    30 to "30.1",
                ),
            ),
        )
    }

    @Test
    fun test_sourceBagChanges_additionsOnly() {
        slottedStimulationScenarioBank_sourceBagChanges.forEach {
            test_sourceBagChanges_additionsOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceBagChanges_additionsOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveBag = TestInputReactiveBag(
            initialTaggedElements = taggedBagOf(
                0 to 0.1,
                10 to 10.1,
                11 to 10.1, // Element duplicate
                30 to 30.1,
            ),
        )

        val subjectReactiveBag = sourceReactiveBag.map { it.toString() }

        ReactiveBag_reaction_testUtils.testReaction(
            subjectReactiveBag,
            slottedInputStimulation = sourceReactiveBag.changing(
                tag = ReactiveBag_generic_testUtils.SourceReactiveBagTag,
                changeDescription = TestInputReactiveBag.ChangeDescription(
                    addedElementByTag = mapOf(
                        40 to 40.1,
                        50 to 50.1,
                    ),
                ),
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectElementTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                expectedOldTaggedElements = taggedBagOf(
                    0 to "0.1",
                    10 to "10.1",
                    11 to "10.1", // Element duplicate
                    30 to "30.1",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    0 to "0.1",
                    10 to "10.1",
                    11 to "10.1", // Element duplicate
                    30 to "30.1",
                    40 to "40.1",
                    50 to "50.1",
                ),
            ),
        )
    }

    @Test
    fun test_sourceChanges_removalsOnly() {
        slottedStimulationScenarioBank_sourceBagChanges.forEach {
            test_sourceChanges_removalsOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceChanges_removalsOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveBag = TestInputReactiveBag(
            initialTaggedElements = taggedBagOf(
                0 to 0.1,
                10 to 10.1,
                20 to 20.1,
                30 to 30.1,
                40 to 40.1,
                50 to 50.1,
                60 to 60.1,
            ),
        )

        val subjectReactiveBag = sourceReactiveBag.map { it.toString() }

        ReactiveBag_reaction_testUtils.testReaction(
            subjectReactiveBag,
            slottedInputStimulation = sourceReactiveBag.changing(
                tag = ReactiveBag_generic_testUtils.SourceReactiveBagTag,
                changeDescription = TestInputReactiveBag.ChangeDescription(
                    removedTags = setOf(10, 20, 40, 50),
                ),
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectElementTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                expectedOldTaggedElements = taggedBagOf(
                    0 to "0.1",
                    10 to "10.1",
                    20 to "20.1",
                    30 to "30.1",
                    40 to "40.1",
                    50 to "50.1",
                    60 to "60.1",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    0 to "0.1",
                    30 to "30.1",
                    60 to "60.1",
                ),
            ),
        )
    }

    @Test
    fun test_sourceChanges_replacementsOnly() {
        slottedStimulationScenarioBank_sourceBagChanges.forEach {
            test_sourceChanges_replacementsOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceChanges_replacementsOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveBag = TestInputReactiveBag(
            initialTaggedElements = taggedBagOf(
                0 to 0.1,
                10 to 10.1,
                20 to 20.1,
                30 to 30.1,
                40 to 40.1,
                50 to 50.1,
                60 to 60.1,
            ),
        )

        val subjectReactiveBag = sourceReactiveBag.map { it.toString() }

        ReactiveBag_reaction_testUtils.testReaction(
            subjectReactiveBag,
            slottedInputStimulation = sourceReactiveBag.changing(
                tag = ReactiveBag_generic_testUtils.SourceReactiveBagTag,
                changeDescription = TestInputReactiveBag.ChangeDescription(
                    replacedElementByTag = mapOf(
                        10 to 11.1,
                        20 to 12.1,
                        40 to 41.1,
                        50 to 42.1,
                        60 to 43.1,
                    ),
                ),
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectElementTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                expectedOldTaggedElements = taggedBagOf(
                    0 to "0.1",
                    10 to "10.1",
                    20 to "20.1",
                    30 to "30.1",
                    40 to "40.1",
                    50 to "50.1",
                    60 to "60.1",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    0 to "0.1",
                    10 to "11.1",
                    20 to "12.1",
                    30 to "30.1",
                    40 to "41.1",
                    50 to "42.1",
                    60 to "43.1",
                ),
            ),
        )
    }

    @Test
    fun test_sourceChanges_mixed() {
        slottedStimulationScenarioBank_sourceBagChanges.forEach {
            test_sourceChanges_mixed(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceChanges_mixed(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveBag = TestInputReactiveBag(
            initialTaggedElements = taggedBagOf(
                0 to 0.1,
                10 to 10.1,
                20 to 20.1,
                30 to 30.1,
                40 to 40.1,
                50 to 50.1,
                60 to 60.1,
            ),
        )

        val subjectReactiveBag = sourceReactiveBag.map { it.toString() }

        ReactiveBag_reaction_testUtils.testReaction(
            subjectReactiveBag,
            slottedInputStimulation = sourceReactiveBag.changing(
                tag = ReactiveBag_generic_testUtils.SourceReactiveBagTag,
                changeDescription = TestInputReactiveBag.ChangeDescription(
                    addedElementByTag = mapOf(
                        15 to 15.1,
                        16 to 16.1,
                    ),
                    replacedElementByTag = mapOf(
                        40 to 200.1,
                    ),
                    removedTags = setOf(20, 30, 50),
                ),
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectElementTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                expectedOldTaggedElements = taggedBagOf(
                    0 to "0.1",
                    10 to "10.1",
                    20 to "20.1",
                    30 to "30.1",
                    40 to "40.1",
                    50 to "50.1",
                    60 to "60.1",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    0 to "0.1",
                    10 to "10.1",
                    15 to "15.1",
                    16 to "16.1",
                    40 to "200.1",
                    60 to "60.1",
                ),
            ),
        )
    }

    @Test
    fun test_sourceChangesRevoked_additionsOnly() {
        slottedStimulationScenarioBank_sourceBagChangesRevoked.forEach {
            test_sourceChangesRevoked_additionsOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceChangesRevoked_additionsOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveBag = TestInputReactiveBag(
            initialTaggedElements = taggedBagOf(
                0 to 0.1,
                10 to 10.1,
                20 to 20.1,
                30 to 30.1,
                40 to 40.1,
                50 to 50.1,
                60 to 60.1,
            ),
        )

        val subjectReactiveBag = sourceReactiveBag.map { it.toString() }

        ReactiveBag_reaction_testUtils.testReaction(
            subjectReactiveBag,
            slottedInputStimulation = sourceReactiveBag.revokingChange(
                tag = ReactiveBag_generic_testUtils.SourceReactiveBagTag,
                temporaryChangeDescription = TestInputReactiveBag.ChangeDescription(
                    addedElementByTag = mapOf(
                        70 to 70.1,
                        80 to 80.1,
                    ),
                ),
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectElementTransition = ReactiveBag_expectations_testUtils.expectNoTaggedContentTransition(
                intermediatePropagationTolerance = ExpectedTestSubjectReaction.IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedTaggedElements = taggedBagOf(
                    0 to "0.1",
                    10 to "10.1",
                    20 to "20.1",
                    30 to "30.1",
                    40 to "40.1",
                    50 to "50.1",
                    60 to "60.1",
                ),
            ),
        )
    }

    @Test
    fun test_sourceChangesRevoked_removalsOnly() {
        slottedStimulationScenarioBank_sourceBagChangesRevoked.forEach {
            test_sourceChangesRevoked_removalsOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceChangesRevoked_removalsOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveBag = TestInputReactiveBag(
            initialTaggedElements = taggedBagOf(
                0 to 0.1,
                10 to 10.1,
                20 to 20.1,
                30 to 30.1,
                40 to 40.1,
                50 to 50.1,
                60 to 60.1,
            ),
        )

        val subjectReactiveBag = sourceReactiveBag.map { it.toString() }

        ReactiveBag_reaction_testUtils.testReaction(
            subjectReactiveBag,
            slottedInputStimulation = sourceReactiveBag.revokingChange(
                tag = ReactiveBag_generic_testUtils.SourceReactiveBagTag,
                temporaryChangeDescription = TestInputReactiveBag.ChangeDescription(
                    removedTags = setOf(10, 20, 40, 50),
                ),
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectElementTransition = ReactiveBag_expectations_testUtils.expectNoTaggedContentTransition(
                intermediatePropagationTolerance = ExpectedTestSubjectReaction.IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedTaggedElements = taggedBagOf(
                    0 to "0.1",
                    10 to "10.1",
                    20 to "20.1",
                    30 to "30.1",
                    40 to "40.1",
                    50 to "50.1",
                    60 to "60.1",
                ),
            ),
        )
    }

    @Test
    fun test_sourceChangesRevoked_replacementsOnly() {
        slottedStimulationScenarioBank_sourceBagChangesRevoked.forEach {
            test_sourceChangesRevoked_replacementsOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceChangesRevoked_replacementsOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveBag = TestInputReactiveBag(
            initialTaggedElements = taggedBagOf(
                0 to 0.1,
                10 to 10.1,
                20 to 20.1,
                30 to 30.1,
                40 to 40.1,
                50 to 50.1,
                60 to 60.1,
            ),
        )

        val subjectReactiveBag = sourceReactiveBag.map { it.toString() }

        ReactiveBag_reaction_testUtils.testReaction(
            subjectReactiveBag,
            slottedInputStimulation = sourceReactiveBag.revokingChange(
                tag = ReactiveBag_generic_testUtils.SourceReactiveBagTag,
                temporaryChangeDescription = TestInputReactiveBag.ChangeDescription(
                    replacedElementByTag = mapOf(
                        10 to 11.1,
                        20 to 12.1,
                        40 to 41.1,
                        50 to 42.1,
                        60 to 43.1,
                    ),
                ),
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectElementTransition = ReactiveBag_expectations_testUtils.expectNoTaggedContentTransition(
                intermediatePropagationTolerance = ExpectedTestSubjectReaction.IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedTaggedElements = taggedBagOf(
                    0 to "0.1",
                    10 to "10.1",
                    20 to "20.1",
                    30 to "30.1",
                    40 to "40.1",
                    50 to "50.1",
                    60 to "60.1",
                ),
            ),
        )
    }

    @Test
    fun test_sourceChangesRevoked_mixed() {
        slottedStimulationScenarioBank_sourceBagChangesRevoked.forEach {
            test_sourceChangesRevoked_mixed(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceChangesRevoked_mixed(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveBag = TestInputReactiveBag(
            initialTaggedElements = taggedBagOf(
                0 to 0.1,
                10 to 10.1,
                20 to 20.1,
                30 to 30.1,
                40 to 40.1,
                50 to 50.1,
                60 to 60.1,
            ),
        )

        val subjectReactiveBag = sourceReactiveBag.map { it.toString() }

        ReactiveBag_reaction_testUtils.testReaction(
            subjectReactiveBag,
            slottedInputStimulation = sourceReactiveBag.revokingChange(
                tag = ReactiveBag_generic_testUtils.SourceReactiveBagTag,
                temporaryChangeDescription = TestInputReactiveBag.ChangeDescription(
                    addedElementByTag = mapOf(
                        70 to 70.1,
                        80 to 80.1,
                    ),
                    replacedElementByTag = mapOf(
                        10 to 11.1,
                        40 to 200.1,
                    ),
                    removedTags = setOf(20, 30, 50),
                ),
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectElementTransition = ReactiveBag_expectations_testUtils.expectNoTaggedContentTransition(
                intermediatePropagationTolerance = ExpectedTestSubjectReaction.IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedTaggedElements = taggedBagOf(
                    0 to "0.1",
                    10 to "10.1",
                    20 to "20.1",
                    30 to "30.1",
                    40 to "40.1",
                    50 to "50.1",
                    60 to "60.1",
                ),
            ),
        )
    }

    @Test
    fun test_sourceChangesCorrected_additionsOnly() {
        slottedStimulationScenarioBank_sourceBagChangesCorrected.forEach {
            test_sourceChangesCorrected_additionsOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceChangesCorrected_additionsOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveBag = TestInputReactiveBag(
            initialTaggedElements = taggedBagOf(
                0 to 0.1,
                10 to 10.1,
                20 to 20.1,
                30 to 30.1,
                40 to 40.1,
                50 to 50.1,
                60 to 60.1,
            ),
        )

        val subjectReactiveBag = sourceReactiveBag.map { it.toString() }

        ReactiveBag_reaction_testUtils.testReaction(
            subjectReactiveBag,
            slottedInputStimulation = sourceReactiveBag.correctingChange(
                tag = ReactiveBag_generic_testUtils.SourceReactiveBagTag,
                intermediateChangeDescription = TestInputReactiveBag.ChangeDescription(
                    addedElementByTag = mapOf(
                        25 to 25.1, // not corrected
                        26 to 26.1, // corrected: added differently
                        70 to 70.1, // corrected: not added
                    ),
                ),
                correctedChangeDescription = TestInputReactiveBag.ChangeDescription(
                    addedElementByTag = mapOf(
                        25 to 25.1,
                        26 to 26.2,
                        27 to 27.1, // (not mentioned before)
                    ),
                ),
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectElementTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = ExpectedTestSubjectReaction.IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    0 to "0.1",
                    10 to "10.1",
                    20 to "20.1",
                    30 to "30.1",
                    40 to "40.1",
                    50 to "50.1",
                    60 to "60.1",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    0 to "0.1",
                    10 to "10.1",
                    20 to "20.1",
                    25 to "25.1",
                    26 to "26.2",
                    27 to "27.1",
                    30 to "30.1",
                    40 to "40.1",
                    50 to "50.1",
                    60 to "60.1",
                ),
            ),
        )
    }

    @Test
    fun test_sourceChangesCorrected_removalsOnly() {
        slottedStimulationScenarioBank_sourceBagChangesCorrected.forEach {
            test_sourceChangesCorrected_removalsOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceChangesCorrected_removalsOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveBag = TestInputReactiveBag(
            initialTaggedElements = taggedBagOf(
                0 to 0.1,
                10 to 10.1,
                20 to 20.1,
                30 to 30.1,
                40 to 40.1,
                50 to 50.1,
                60 to 60.1,
            ),
        )

        val subjectReactiveBag = sourceReactiveBag.map { it.toString() }

        ReactiveBag_reaction_testUtils.testReaction(
            subjectReactiveBag,
            slottedInputStimulation = sourceReactiveBag.correctingChange(
                tag = ReactiveBag_generic_testUtils.SourceReactiveBagTag,
                intermediateChangeDescription = TestInputReactiveBag.ChangeDescription(
                    removedTags = setOf(
                        10, // corrected: not removed
                        20, // not corrected
                        30, // not corrected
                        40, // corrected: not removed
                        50, // not corrected
                    ),
                ),
                correctedChangeDescription = TestInputReactiveBag.ChangeDescription(
                    removedTags = setOf(
                        20,
                        30,
                        50,
                        60, // (not mentioned before)
                    ),
                ),
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectElementTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = ExpectedTestSubjectReaction.IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    0 to "0.1",
                    10 to "10.1",
                    20 to "20.1",
                    30 to "30.1",
                    40 to "40.1",
                    50 to "50.1",
                    60 to "60.1",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    0 to "0.1",
                    10 to "10.1",
                    40 to "40.1",
                ),
            ),
        )
    }

    @Test
    fun test_sourceChangesCorrected_replacementsOnly() {
        slottedStimulationScenarioBank_sourceBagChangesCorrected.forEach {
            test_sourceChangesCorrected_replacementsOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceChangesCorrected_replacementsOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveBag = TestInputReactiveBag(
            initialTaggedElements = taggedBagOf(
                0 to 0.1,
                10 to 10.1,
                20 to 20.1,
                30 to 30.1,
                40 to 40.1,
                50 to 50.1,
                60 to 60.1,
            ),
        )

        val subjectReactiveBag = sourceReactiveBag.map { it.toString() }

        ReactiveBag_reaction_testUtils.testReaction(
            subjectReactiveBag,
            slottedInputStimulation = sourceReactiveBag.correctingChange(
                tag = ReactiveBag_generic_testUtils.SourceReactiveBagTag,
                intermediateChangeDescription = TestInputReactiveBag.ChangeDescription(
                    replacedElementByTag = mapOf(
                        10 to 11.1, // not corrected
                        20 to 12.1, // corrected: replaced differently
                        30 to 13.1, // corrected: replaced differently
                        40 to 41.1, // corrected: not replaced
                        50 to 42.1, // corrected: not replaced
                        60 to 43.1, // corrected: not replaced
                    ),
                ),
                correctedChangeDescription = TestInputReactiveBag.ChangeDescription(
                    replacedElementByTag = mapOf(
                        10 to 11.1,
                        20 to 12.2,
                        30 to 13.2,
                    ),
                ),
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectElementTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = ExpectedTestSubjectReaction.IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    0 to "0.1",
                    10 to "10.1",
                    20 to "20.1",
                    30 to "30.1",
                    40 to "40.1",
                    50 to "50.1",
                    60 to "60.1",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    0 to "0.1",
                    10 to "11.1",
                    20 to "12.2",
                    30 to "13.2",
                    40 to "40.1",
                    50 to "50.1",
                    60 to "60.1",
                ),
            ),
        )
    }

    @Test
    fun test_sourceChangesCorrected_mixed() {
        slottedStimulationScenarioBank_sourceBagChangesCorrected.forEach {
            test_sourceChangesCorrected_mixed(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceChangesCorrected_mixed(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveBag = TestInputReactiveBag(
            initialTaggedElements = taggedBagOf(
                0 to 0.1,
                10 to 10.1,
                20 to 20.1,
                30 to 30.1,
                40 to 40.1,
                50 to 50.1,
                60 to 60.1,
            ),
        )

        val subjectReactiveBag = sourceReactiveBag.map { it.toString() }

        ReactiveBag_reaction_testUtils.testReaction(
            subjectReactiveBag,
            slottedInputStimulation = sourceReactiveBag.correctingChange(
                tag = ReactiveBag_generic_testUtils.SourceReactiveBagTag,
                intermediateChangeDescription = TestInputReactiveBag.ChangeDescription(
                    addedElementByTag = mapOf(
                        5 to 5.1, // not corrected
                        70 to 70.1, // corrected: not added
                    ),
                    replacedElementByTag = mapOf(
                        10 to 11.1, // not corrected
                        40 to -41.1, // corrected: removed instead
                    ),
                    removedTags = setOf(20, 50),
                ),
                correctedChangeDescription = TestInputReactiveBag.ChangeDescription(
                    addedElementByTag = mapOf(
                        5 to 5.1,
                    ),
                    replacedElementByTag = mapOf(
                        10 to 11.1,
                    ),
                    removedTags = setOf(
                        20,
                        30, // (not mentioned before)
                        40,
                        50,
                        60, // (not mentioned before)
                    ),
                ),
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectElementTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = ExpectedTestSubjectReaction.IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    0 to "0.1",
                    10 to "10.1",
                    20 to "20.1",
                    30 to "30.1",
                    40 to "40.1",
                    50 to "50.1",
                    60 to "60.1",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    0 to "0.1",
                    5 to "5.1",
                    10 to "11.1",
                ),
            ),
        )
    }
}
