package dev.azide.core.collections.reactive_list

import dev.azide.core.collections.helpers.withSortKey
import dev.azide.core.collections.sortedUniquely
import dev.azide.core.test_utils.collections.ReactiveCollection_generic_testUtils
import dev.azide.core.test_utils.collections.ReactiveCollection_generic_testUtils.SourceReactiveCollectionTag
import dev.azide.core.test_utils.collections.reactive_list.ReactiveList_expectations_testUtils
import dev.azide.core.test_utils.collections.reactive_list.ReactiveList_reaction_testUtils
import dev.azide.core.test_utils.collections.reactive_set.TestInputReactiveSet
import dev.azide.core.test_utils.collections.reactive_set.changing
import dev.azide.core.test_utils.collections.reactive_set.correctingChange
import dev.azide.core.test_utils.collections.reactive_set.revokingChange
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulationScenario
import dev.azide.core.test_utils.stimulation_combinatorics.bind
import kotlin.test.Test

@Suppress("ClassName", "PrivatePropertyName")
class ReactiveList_sortedUniquely_tests {
    private typealias SuitableSlotCount = TestSlotCount.Count2

    private typealias SuitableTestSlottedStimulationScenario = TestSlottedStimulationScenario<SuitableSlotCount>

    private val slottedStimulationScenarioBank_sourceCollectionChanges =
        ReactiveCollection_generic_testUtils.stimulationScenarioBank_sourceCollectionChanges.distribute(slotCount = SuitableSlotCount)

    private val slottedStimulationScenarioBank_sourceCollectionChangesRevoked =
        ReactiveCollection_generic_testUtils.stimulationScenarioBank_sourceCollectionChangesRevoked.distribute(slotCount = SuitableSlotCount)

    private val slottedStimulationScenarioBank_sourceCollectionChangesCorrected =
        ReactiveCollection_generic_testUtils.stimulationScenarioBank_sourceCollectionChangesCorrected.distribute(
            slotCount = SuitableSlotCount
        )

    @Test
    fun test_sourceCollectionChanges_additionsOnly() {
        slottedStimulationScenarioBank_sourceCollectionChanges.forEach {
            test_sourceCollectionChanges_additionsOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceCollectionChanges_additionsOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveSet = TestInputReactiveSet(
            initialElements = setOf(
                "#10" withSortKey 10.8,
                "^0" withSortKey 0.3,
                "?30" withSortKey 30.1,
                "$20" withSortKey 20.6,
                ".50" withSortKey 50.4,
            ),
        )

        val subjectReactiveList = sourceReactiveSet.sortedUniquely()

        ReactiveList_reaction_testUtils.executeReactionTransaction(
            subjectReactiveList = subjectReactiveList,
            slottedInputStimulation = sourceReactiveSet.changing(
                tag = SourceReactiveCollectionTag,
                changeDescription = TestInputReactiveSet.ChangeDescription(
                    addedElements = setOf(
                        ".11" withSortKey 11.5,
                        "!21" withSortKey 21.9,
                        ".60" withSortKey 60.2,
                    ),
                ),
            ).bind(slottedStimulationScenario),
            expectedSubjectElementTransition = ReactiveList_expectations_testUtils.expectContentTransition(
                expectedOldContent = listOf(
                    "^0",
                    "#10",
                    "$20",
                    "?30",
                    ".50",
                ),
                expectedNewContent = listOf(
                    "^0",
                    "#10",
                    ".11",
                    "$20",
                    "!21",
                    "?30",
                    ".50",
                    ".60",
                ),
            ),
        )
    }

    @Test
    fun test_sourceCollectionChanges_removalsOnly() {
        slottedStimulationScenarioBank_sourceCollectionChanges.forEach {
            test_sourceCollectionChanges_removalsOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceCollectionChanges_removalsOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveSet = TestInputReactiveSet(
            initialElements = setOf(
                "#10" withSortKey 10.8,
                "^0" withSortKey 0.3,
                "?30" withSortKey 30.1,
                "$20" withSortKey 20.6,
                ".50" withSortKey 50.4,
            ),
        )

        val subjectReactiveList = sourceReactiveSet.sortedUniquely()

        ReactiveList_reaction_testUtils.executeReactionTransaction(
            subjectReactiveList,
            slottedInputStimulation = sourceReactiveSet.changing(
                tag = SourceReactiveCollectionTag,
                changeDescription = TestInputReactiveSet.ChangeDescription(
                    removedElements = setOf(
                        "#10" withSortKey 10.8,
                        "?30" withSortKey 30.1,
                    ),
                ),
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectElementTransition = ReactiveList_expectations_testUtils.expectContentTransition(
                expectedOldContent = listOf(
                    "^0",
                    "#10",
                    "$20",
                    "?30",
                    ".50",
                ),
                expectedNewContent = listOf(
                    "^0",
                    "$20",
                    ".50",
                ),
            ),
        )
    }

    @Test
    fun test_sourceCollectionChanges_mixed() {
        slottedStimulationScenarioBank_sourceCollectionChanges.forEach {
            test_sourceCollectionChanges_mixed(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceCollectionChanges_mixed(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveSet = TestInputReactiveSet(
            initialElements = setOf(
                "#10" withSortKey 10.8,
                "^0" withSortKey 0.3,
                "?30" withSortKey 30.1,
                "$20" withSortKey 20.6,
                ".50" withSortKey 50.4,
                "!60" withSortKey 60.7,
            ),
        )

        val subjectReactiveList = sourceReactiveSet.sortedUniquely()

        ReactiveList_reaction_testUtils.executeReactionTransaction(
            subjectReactiveList,
            slottedInputStimulation = sourceReactiveSet.changing(
                tag = SourceReactiveCollectionTag,
                changeDescription = TestInputReactiveSet.ChangeDescription(
                    addedElements = setOf(
                        ".15" withSortKey 15.2,
                        ".16" withSortKey 16.9,
                    ),
                    removedElements = setOf(
                        "$20" withSortKey 20.6,
                        "?30" withSortKey 30.1,
                        ".50" withSortKey 50.4,
                    ),
                ),
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectElementTransition = ReactiveList_expectations_testUtils.expectContentTransition(
                expectedOldContent = listOf(
                    "^0",
                    "#10",
                    "$20",
                    "?30",
                    ".50",
                    "!60",
                ),
                expectedNewContent = listOf(
                    "^0",
                    "#10",
                    ".15",
                    ".16",
                    "!60",
                ),
            ),
        )
    }

    @Test
    fun test_sourceCollectionChangesRevoked_additionsOnly() {
        slottedStimulationScenarioBank_sourceCollectionChangesRevoked.forEach {
            test_sourceCollectionChangesRevoked_additionsOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceCollectionChangesRevoked_additionsOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveSet = TestInputReactiveSet(
            initialElements = setOf(
                "#10" withSortKey 10.8,
                "^0" withSortKey 0.3,
                "?30" withSortKey 30.1,
                "$20" withSortKey 20.6,
                ".50" withSortKey 50.4,
                "!60" withSortKey 60.7,
            ),
        )

        val subjectReactiveList = sourceReactiveSet.sortedUniquely()

        ReactiveList_reaction_testUtils.executeReactionTransaction(
            subjectReactiveList,
            slottedInputStimulation = sourceReactiveSet.revokingChange(
                tag = SourceReactiveCollectionTag,
                temporaryChangeDescription = TestInputReactiveSet.ChangeDescription(
                    addedElements = setOf(
                        ".70" withSortKey 70.5,
                        ".80" withSortKey 80.9,
                    ),
                ),
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectElementTransition = ReactiveList_expectations_testUtils.expectNoContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedContent = listOf(
                    "^0",
                    "#10",
                    "$20",
                    "?30",
                    ".50",
                    "!60",
                ),
            ),
        )
    }

    @Test
    fun test_sourceCollectionChangesRevoked_removalsOnly() {
        slottedStimulationScenarioBank_sourceCollectionChangesRevoked.forEach {
            test_sourceCollectionChangesRevoked_removalsOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceCollectionChangesRevoked_removalsOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveSet = TestInputReactiveSet(
            initialElements = setOf(
                "#10" withSortKey 10.8,
                "^0" withSortKey 0.3,
                "?30" withSortKey 30.1,
                "$20" withSortKey 20.6,
                ".50" withSortKey 50.4,
                "!60" withSortKey 60.7,
            ),
        )

        val subjectReactiveList = sourceReactiveSet.sortedUniquely()

        ReactiveList_reaction_testUtils.executeReactionTransaction(
            subjectReactiveList,
            slottedInputStimulation = sourceReactiveSet.revokingChange(
                tag = SourceReactiveCollectionTag,
                temporaryChangeDescription = TestInputReactiveSet.ChangeDescription(
                    removedElements = setOf(
                        "#10" withSortKey 10.8,
                        "$20" withSortKey 20.6,
                        "?30" withSortKey 30.1,
                        ".50" withSortKey 50.4,
                    ),
                ),
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectElementTransition = ReactiveList_expectations_testUtils.expectNoContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedContent = listOf(
                    "^0",
                    "#10",
                    "$20",
                    "?30",
                    ".50",
                    "!60",
                ),
            ),
        )
    }

    @Test
    fun test_sourceCollectionChangesRevoked_mixed() {
        slottedStimulationScenarioBank_sourceCollectionChangesRevoked.forEach {
            test_sourceCollectionChangesRevoked_mixed(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceCollectionChangesRevoked_mixed(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveSet = TestInputReactiveSet(
            initialElements = setOf(
                "#10" withSortKey 10.8,
                "^0" withSortKey 0.3,
                "?30" withSortKey 30.1,
                "$20" withSortKey 20.6,
                ".50" withSortKey 50.4,
                "!60" withSortKey 60.7,
            ),
        )

        val subjectReactiveList = sourceReactiveSet.sortedUniquely()

        ReactiveList_reaction_testUtils.executeReactionTransaction(
            subjectReactiveList,
            slottedInputStimulation = sourceReactiveSet.revokingChange(
                tag = SourceReactiveCollectionTag,
                temporaryChangeDescription = TestInputReactiveSet.ChangeDescription(
                    addedElements = setOf(
                        ".70" withSortKey 70.5,
                        ".80" withSortKey 80.9,
                    ),
                    removedElements = setOf(
                        "$20" withSortKey 20.6,
                        "?30" withSortKey 30.1,
                        ".50" withSortKey 50.4,
                    ),
                ),
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectElementTransition = ReactiveList_expectations_testUtils.expectNoContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedContent = listOf(
                    "^0",
                    "#10",
                    "$20",
                    "?30",
                    ".50",
                    "!60",
                ),
            ),
        )
    }

    @Test
    fun test_sourceCollectionChangesCorrected_additionsOnly() {
        slottedStimulationScenarioBank_sourceCollectionChangesCorrected.forEach {
            test_sourceCollectionChangesCorrected_additionsOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceCollectionChangesCorrected_additionsOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveSet = TestInputReactiveSet(
            initialElements = setOf(
                "#10" withSortKey 10.8,
                "^0" withSortKey 0.3,
                "?30" withSortKey 30.1,
                "$20" withSortKey 20.6,
                ".50" withSortKey 50.4,
                "!60" withSortKey 60.7,
            ),
        )

        val subjectReactiveList = sourceReactiveSet.sortedUniquely()

        ReactiveList_reaction_testUtils.executeReactionTransaction(
            subjectReactiveList,
            slottedInputStimulation = sourceReactiveSet.correctingChange(
                tag = SourceReactiveCollectionTag,
                intermediateChangeDescription = TestInputReactiveSet.ChangeDescription(
                    addedElements = setOf(
                        ".25" withSortKey 25.3, // not corrected
                        ".26" withSortKey 26.7, // corrected: added differently
                        ".70" withSortKey 70.5, // corrected: not added
                    ),
                ),
                correctedChangeDescription = TestInputReactiveSet.ChangeDescription(
                    addedElements = setOf(
                        ".25" withSortKey 25.3,
                        ".26" withSortKey 26.7,
                        ".27" withSortKey 27.4, // (not mentioned before)
                    ),
                ),
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectElementTransition = ReactiveList_expectations_testUtils.expectContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldContent = listOf(
                    "^0",
                    "#10",
                    "$20",
                    "?30",
                    ".50",
                    "!60",
                ),
                expectedNewContent = listOf(
                    "^0",
                    "#10",
                    "$20",
                    ".25",
                    ".26",
                    ".27",
                    "?30",
                    ".50",
                    "!60",
                ),
            ),
        )
    }

    @Test
    fun test_sourceCollectionChangesCorrected_removalsOnly() {
        slottedStimulationScenarioBank_sourceCollectionChangesCorrected.forEach {
            test_sourceCollectionChangesCorrected_removalsOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceCollectionChangesCorrected_removalsOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveSet = TestInputReactiveSet(
            initialElements = setOf(
                "#10" withSortKey 10.8,
                "^0" withSortKey 0.3,
                "?30" withSortKey 30.1,
                "$20" withSortKey 20.6,
                ".50" withSortKey 50.4,
                "!60" withSortKey 60.7,
            ),
        )

        val subjectReactiveList = sourceReactiveSet.sortedUniquely()

        ReactiveList_reaction_testUtils.executeReactionTransaction(
            subjectReactiveList,
            slottedInputStimulation = sourceReactiveSet.correctingChange(
                tag = SourceReactiveCollectionTag,
                intermediateChangeDescription = TestInputReactiveSet.ChangeDescription(
                    removedElements = setOf(
                        "#10" withSortKey 10.8, // corrected: not removed
                        "$20" withSortKey 20.6, // not corrected
                        "?30" withSortKey 30.1, // not corrected
                        "!60" withSortKey 60.7, // corrected: not removed
                        ".50" withSortKey 50.4, // not corrected
                    ),
                ),
                correctedChangeDescription = TestInputReactiveSet.ChangeDescription(
                    removedElements = setOf(
                        "$20" withSortKey 20.6,
                        "?30" withSortKey 30.1,
                        ".50" withSortKey 50.4,
                        "#10" withSortKey 10.8, // (not mentioned before)
                    ),
                ),
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectElementTransition = ReactiveList_expectations_testUtils.expectContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldContent = listOf(
                    "^0",
                    "#10",
                    "$20",
                    "?30",
                    ".50",
                    "!60",
                ),
                expectedNewContent = listOf(
                    "^0",
                    "!60",
                ),
            ),
        )
    }

    @Test
    fun test_sourceCollectionChangesCorrected_mixed() {
        slottedStimulationScenarioBank_sourceCollectionChangesCorrected.forEach {
            test_sourceCollectionChangesCorrected_mixed(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceCollectionChangesCorrected_mixed(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveSet = TestInputReactiveSet(
            initialElements = setOf(
                "#10" withSortKey 10.8,
                "^0" withSortKey 0.3,
                "?30" withSortKey 30.1,
                "$20" withSortKey 20.6,
                ".50" withSortKey 50.4,
                "!60" withSortKey 60.7,
            ),
        )

        val subjectReactiveList = sourceReactiveSet.sortedUniquely()

        ReactiveList_reaction_testUtils.executeReactionTransaction(
            subjectReactiveList,
            slottedInputStimulation = sourceReactiveSet.correctingChange(
                tag = SourceReactiveCollectionTag,
                intermediateChangeDescription = TestInputReactiveSet.ChangeDescription(
                    addedElements = setOf(
                        ".5" withSortKey 5.2, // not corrected
                        ".70" withSortKey 70.5, // corrected: not added
                    ),
                    removedElements = setOf(
                        "$20" withSortKey 20.6,
                        ".50" withSortKey 50.4,
                    ),
                ),
                correctedChangeDescription = TestInputReactiveSet.ChangeDescription(
                    addedElements = setOf(
                        ".5" withSortKey 5.2,
                    ),
                    removedElements = setOf(
                        "$20" withSortKey 20.6,
                        "?30" withSortKey 30.1,
                        "!60" withSortKey 60.7, // (not mentioned before)
                        ".50" withSortKey 50.4,
                    ),
                ),
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectElementTransition = ReactiveList_expectations_testUtils.expectContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldContent = listOf(
                    "^0",
                    "#10",
                    "$20",
                    "?30",
                    ".50",
                    "!60",
                ),
                expectedNewContent = listOf(
                    "^0",
                    ".5",
                    "#10",
                ),
            ),
        )
    }
}
