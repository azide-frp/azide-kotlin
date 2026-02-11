package dev.azide.core.collections.reactive_list

import dev.azide.core.collections.helpers.withSortKey
import dev.azide.core.collections.sortedUniquely
import dev.azide.core.test_utils.collections.reactive_list.ReactiveList_expectations_testUtils
import dev.azide.core.test_utils.collections.reactive_list.ReactiveList_reaction_testUtils
import dev.azide.core.test_utils.collections.reactive_set.ReactiveSet_generic_testUtils
import dev.azide.core.test_utils.collections.reactive_set.ReactiveSet_generic_testUtils.SourceReactiveSetTag
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

    private val slottedStimulationBank_sourceSetChanges =
        ReactiveSet_generic_testUtils.stimulationBank_sourceSetChanges.distribute(slotCount = SuitableSlotCount)

    private val slottedStimulationBank_sourceSetChangesRevoked =
        ReactiveSet_generic_testUtils.stimulationBank_sourceSetChangesRevoked.distribute(slotCount = SuitableSlotCount)

    private val slottedStimulationBank_sourceSetChangesCorrected =
        ReactiveSet_generic_testUtils.stimulationBank_sourceSetChangesCorrected.distribute(slotCount = SuitableSlotCount)

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
                "#10" withSortKey 10,
                "^0" withSortKey 0,
                "?30" withSortKey 30,
                "$20" withSortKey 20,
                ".50" withSortKey 50,
            ),
        )

        val subjectReactiveList = sourceReactiveSet.sortedUniquely()

        ReactiveList_reaction_testUtils.executeReactionTransaction(
            subjectReactiveList,
            slottedInputStimulation = sourceReactiveSet.changing(
                tag = SourceReactiveSetTag,
                changeDescription = TestInputReactiveSet.ChangeDescription(
                    addedElements = setOf(
                        ".11" withSortKey 11,
                        "!21" withSortKey 21,
                        ".60" withSortKey 60,
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
                "#10" withSortKey 10,
                "^0" withSortKey 0,
                "?30" withSortKey 30,
                "$20" withSortKey 20,
                ".50" withSortKey 50,
            ),
        )

        val subjectReactiveList = sourceReactiveSet.sortedUniquely()

        ReactiveList_reaction_testUtils.executeReactionTransaction(
            subjectReactiveList,
            slottedInputStimulation = sourceReactiveSet.changing(
                tag = SourceReactiveSetTag,
                changeDescription = TestInputReactiveSet.ChangeDescription(
                    removedElements = setOf(
                        "#10" withSortKey 10,
                        "?30" withSortKey 30,
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
                "#10" withSortKey 10,
                "^0" withSortKey 0,
                "?30" withSortKey 30,
                "$20" withSortKey 20,
                ".50" withSortKey 50,
                "!60" withSortKey 60,
            ),
        )

        val subjectReactiveList = sourceReactiveSet.sortedUniquely()

        ReactiveList_reaction_testUtils.executeReactionTransaction(
            subjectReactiveList,
            slottedInputStimulation = sourceReactiveSet.changing(
                tag = SourceReactiveSetTag,
                changeDescription = TestInputReactiveSet.ChangeDescription(
                    addedElements = setOf(
                        ".15" withSortKey 15,
                        ".16" withSortKey 16,
                    ),
                    removedElements = setOf(
                        "$20" withSortKey 20,
                        "?30" withSortKey 30,
                        ".50" withSortKey 50,
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
    fun test_sourceSetChangesRevoked_additionsOnly() {
        slottedStimulationBank_sourceSetChangesRevoked.forEach {
            test_sourceSetChangesRevoked_additionsOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceSetChangesRevoked_additionsOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveSet = TestInputReactiveSet(
            initialElements = setOf(
                "#10" withSortKey 10,
                "^0" withSortKey 0,
                "?30" withSortKey 30,
                "$20" withSortKey 20,
                ".50" withSortKey 50,
                "!60" withSortKey 60,
            ),
        )

        val subjectReactiveList = sourceReactiveSet.sortedUniquely()

        ReactiveList_reaction_testUtils.executeReactionTransaction(
            subjectReactiveList,
            slottedInputStimulation = sourceReactiveSet.revokingChange(
                tag = SourceReactiveSetTag,
                temporaryChangeDescription = TestInputReactiveSet.ChangeDescription(
                    addedElements = setOf(
                        ".70" withSortKey 70,
                        ".80" withSortKey 80,
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
    fun test_sourceSetChangesRevoked_removalsOnly() {
        slottedStimulationBank_sourceSetChangesRevoked.forEach {
            test_sourceSetChangesRevoked_removalsOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceSetChangesRevoked_removalsOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveSet = TestInputReactiveSet(
            initialElements = setOf(
                "#10" withSortKey 10,
                "^0" withSortKey 0,
                "?30" withSortKey 30,
                "$20" withSortKey 20,
                ".50" withSortKey 50,
                "!60" withSortKey 60,
            ),
        )

        val subjectReactiveList = sourceReactiveSet.sortedUniquely()

        ReactiveList_reaction_testUtils.executeReactionTransaction(
            subjectReactiveList,
            slottedInputStimulation = sourceReactiveSet.revokingChange(
                tag = SourceReactiveSetTag,
                temporaryChangeDescription = TestInputReactiveSet.ChangeDescription(
                    removedElements = setOf(
                        "#10" withSortKey 10,
                        "$20" withSortKey 20,
                        "?30" withSortKey 30,
                        ".50" withSortKey 50,
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
    fun test_sourceSetChangesRevoked_mixed() {
        slottedStimulationBank_sourceSetChangesRevoked.forEach {
            test_sourceSetChangesRevoked_mixed(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceSetChangesRevoked_mixed(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveSet = TestInputReactiveSet(
            initialElements = setOf(
                "#10" withSortKey 10,
                "^0" withSortKey 0,
                "?30" withSortKey 30,
                "$20" withSortKey 20,
                ".50" withSortKey 50,
                "!60" withSortKey 60,
            ),
        )

        val subjectReactiveList = sourceReactiveSet.sortedUniquely()

        ReactiveList_reaction_testUtils.executeReactionTransaction(
            subjectReactiveList,
            slottedInputStimulation = sourceReactiveSet.revokingChange(
                tag = SourceReactiveSetTag,
                temporaryChangeDescription = TestInputReactiveSet.ChangeDescription(
                    addedElements = setOf(
                        ".70" withSortKey 70,
                        ".80" withSortKey 80,
                    ),
                    removedElements = setOf(
                        "$20" withSortKey 20,
                        "?30" withSortKey 30,
                        ".50" withSortKey 50,
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
    fun test_sourceSetChangesCorrected_additionsOnly() {
        slottedStimulationBank_sourceSetChangesCorrected.forEach {
            test_sourceSetChangesCorrected_additionsOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceSetChangesCorrected_additionsOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveSet = TestInputReactiveSet(
            initialElements = setOf(
                "#10" withSortKey 10,
                "^0" withSortKey 0,
                "?30" withSortKey 30,
                "$20" withSortKey 20,
                ".50" withSortKey 50,
                "!60" withSortKey 60,
            ),
        )

        val subjectReactiveList = sourceReactiveSet.sortedUniquely()

        ReactiveList_reaction_testUtils.executeReactionTransaction(
            subjectReactiveList,
            slottedInputStimulation = sourceReactiveSet.correctingChange(
                tag = SourceReactiveSetTag,
                intermediateChangeDescription = TestInputReactiveSet.ChangeDescription(
                    addedElements = setOf(
                        ".25" withSortKey 25, // not corrected
                        ".26" withSortKey 26, // corrected: added differently
                        ".70" withSortKey 70, // corrected: not added
                    ),
                ),
                correctedChangeDescription = TestInputReactiveSet.ChangeDescription(
                    addedElements = setOf(
                        ".25" withSortKey 25,
                        ".26" withSortKey 26,
                        ".27" withSortKey 27, // (not mentioned before)
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
    fun test_sourceSetChangesCorrected_removalsOnly() {
        slottedStimulationBank_sourceSetChangesCorrected.forEach {
            test_sourceSetChangesCorrected_removalsOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceSetChangesCorrected_removalsOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveSet = TestInputReactiveSet(
            initialElements = setOf(
                "#10" withSortKey 10,
                "^0" withSortKey 0,
                "?30" withSortKey 30,
                "$20" withSortKey 20,
                ".50" withSortKey 50,
                "!60" withSortKey 60,
            ),
        )

        val subjectReactiveList = sourceReactiveSet.sortedUniquely()

        ReactiveList_reaction_testUtils.executeReactionTransaction(
            subjectReactiveList,
            slottedInputStimulation = sourceReactiveSet.correctingChange(
                tag = SourceReactiveSetTag,
                intermediateChangeDescription = TestInputReactiveSet.ChangeDescription(
                    removedElements = setOf(
                        "#10" withSortKey 10, // corrected: not removed
                        "$20" withSortKey 20, // not corrected
                        "?30" withSortKey 30, // not corrected
                        "!60" withSortKey 60, // corrected: not removed
                        ".50" withSortKey 50, // not corrected
                    ),
                ),
                correctedChangeDescription = TestInputReactiveSet.ChangeDescription(
                    removedElements = setOf(
                        "$20" withSortKey 20,
                        "?30" withSortKey 30,
                        ".50" withSortKey 50,
                        "#10" withSortKey 10, // (not mentioned before)
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
    fun test_sourceSetChangesCorrected_mixed() {
        slottedStimulationBank_sourceSetChangesCorrected.forEach {
            test_sourceSetChangesCorrected_mixed(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceSetChangesCorrected_mixed(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveSet = TestInputReactiveSet(
            initialElements = setOf(
                "#10" withSortKey 10,
                "^0" withSortKey 0,
                "?30" withSortKey 30,
                "$20" withSortKey 20,
                ".50" withSortKey 50,
                "!60" withSortKey 60,
            ),
        )

        val subjectReactiveList = sourceReactiveSet.sortedUniquely()

        ReactiveList_reaction_testUtils.executeReactionTransaction(
            subjectReactiveList,
            slottedInputStimulation = sourceReactiveSet.correctingChange(
                tag = SourceReactiveSetTag,
                intermediateChangeDescription = TestInputReactiveSet.ChangeDescription(
                    addedElements = setOf(
                        ".5" withSortKey 5, // not corrected
                        ".70" withSortKey 70, // corrected: not added
                    ),
                    removedElements = setOf(
                        "$20" withSortKey 20,
                        ".50" withSortKey 50,
                    ),
                ),
                correctedChangeDescription = TestInputReactiveSet.ChangeDescription(
                    addedElements = setOf(
                        ".5" withSortKey 5,
                    ),
                    removedElements = setOf(
                        "$20" withSortKey 20,
                        "?30" withSortKey 30,
                        "!60" withSortKey 60, // (not mentioned before)
                        ".50" withSortKey 50,
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
