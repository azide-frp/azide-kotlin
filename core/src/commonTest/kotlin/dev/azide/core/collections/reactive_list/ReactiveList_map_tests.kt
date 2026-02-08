package dev.azide.core.collections.reactive_list

import dev.azide.core.collections.map
import dev.azide.core.test_utils.collections.reactive_list.ReactiveList_expectations_testUtils
import dev.azide.core.test_utils.collections.reactive_list.ReactiveList_reaction_testUtils
import dev.azide.core.test_utils.collections.reactive_list.TestInputReactiveList
import dev.azide.core.test_utils.collections.reactive_list.TestInputReactiveList.ChangeDescription
import dev.azide.core.test_utils.collections.reactive_list.changing
import dev.azide.core.test_utils.collections.reactive_list.correctingChange
import dev.azide.core.test_utils.collections.reactive_list.revokingChange
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulationScenario
import dev.azide.core.test_utils.stimulation_combinatorics.bind
import kotlin.test.Test

@Suppress("ClassName", "PrivatePropertyName")
class ReactiveList_map_tests {
    private typealias SuitableSlotCount = TestSlotCount.Count2

    private typealias SuitableTestSlottedStimulationScenario = TestSlottedStimulationScenario<SuitableSlotCount>

    private val slottedStimulationBank_sourceListChanges =
        ReactiveList_generic_testUtils.stimulationBank_sourceListChanges.distribute(slotCount = SuitableSlotCount)

    private val slottedStimulationBank_sourceListChangesRevoked =
        ReactiveList_generic_testUtils.stimulationBank_sourceListChangesRevoked.distribute(slotCount = SuitableSlotCount)

    private val slottedStimulationBank_sourceListChangesCorrected =
        ReactiveList_generic_testUtils.stimulationBank_sourceListChangesCorrected.distribute(slotCount = SuitableSlotCount)

    @Test
    fun test_sourceListChanges_insertionsOnly() {
        slottedStimulationBank_sourceListChanges.forEach {
            test_sourceListChanges_insertionsOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceListChanges_insertionsOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(0, 10, 20, 30, 40, 50, 60),
        )

        val subjectReactiveList = sourceReactiveList.map { it.toString() }

        ReactiveList_reaction_testUtils.executeReactionTransaction(
            subjectReactiveList,
            slottedInputStimulation = sourceReactiveList.changing(
                tag = ReactiveList_generic_testUtils.SourceReactiveListTag,
                description = ChangeDescription.of(
                    ChangeDescription.Part.Insertion(
                        index = 3,
                        newElements = listOf(21, 22, 23),
                    ),
                    ChangeDescription.Part.Insertion(
                        index = 6,
                        newElements = listOf(51, 52),
                    ),
                ),
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectElementTransition = ReactiveList_expectations_testUtils.expectContentTransition(
                expectedOldContent = listOf("0", "10", "20", "30", "40", "50", "60"),
                expectedNewContent = listOf("0", "10", "20", "21", "22", "23", "30", "40", "50", "51", "52", "60"),
            ),
        )
    }

    @Test
    fun test_sourceChanges_removalsOnly() {
        slottedStimulationBank_sourceListChanges.forEach {
            test_sourceChanges_removalsOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceChanges_removalsOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(0, 10, 20, 30, 40, 50, 60),
        )

        val subjectReactiveList = sourceReactiveList.map { it.toString() }

        ReactiveList_reaction_testUtils.executeReactionTransaction(
            subjectReactiveList,
            slottedInputStimulation = sourceReactiveList.changing(
                tag = ReactiveList_generic_testUtils.SourceReactiveListTag,
                description = ChangeDescription.of(
                    ChangeDescription.Part.Removal(
                        indexRange = 1..<3,
                    ),
                    ChangeDescription.Part.Removal(
                        indexRange = 4..<6,
                    ),
                ),
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectElementTransition = ReactiveList_expectations_testUtils.expectContentTransition(
                expectedOldContent = listOf("0", "10", "20", "30", "40", "50", "60"),
                expectedNewContent = listOf("0", "30", "60"),
            ),
        )
    }

    @Test
    fun test_sourceChanges_replacementsOnly() {
        slottedStimulationBank_sourceListChanges.forEach {
            test_sourceChanges_replacementsOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceChanges_replacementsOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(0, 10, 20, 30, 40, 50, 60),
        )

        val subjectReactiveList = sourceReactiveList.map { it.toString() }

        ReactiveList_reaction_testUtils.executeReactionTransaction(
            subjectReactiveList,
            slottedInputStimulation = sourceReactiveList.changing(
                tag = ReactiveList_generic_testUtils.SourceReactiveListTag,
                description = ChangeDescription.of(
                    ChangeDescription.Part.Replacement(
                        indexRange = 1..<3,
                        replacedElements = listOf(11, 12),
                    ),
                    ChangeDescription.Part.Replacement(
                        indexRange = 4..<6,
                        replacedElements = listOf(41, 42, 43),
                    ),
                ),
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectElementTransition = ReactiveList_expectations_testUtils.expectContentTransition(
                expectedOldContent = listOf("0", "10", "20", "30", "40", "50", "60"),
                expectedNewContent = listOf("0", "11", "12", "30", "41", "42", "43", "60"),
            ),
        )
    }

    @Test
    fun test_sourceChanges_mixed() {
        slottedStimulationBank_sourceListChanges.forEach {
            test_sourceChanges_mixed(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceChanges_mixed(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(0, 10, 20, 30, 40, 50, 60),
        )

        val subjectReactiveList = sourceReactiveList.map { it.toString() }

        ReactiveList_reaction_testUtils.executeReactionTransaction(
            subjectReactiveList,
            slottedInputStimulation = sourceReactiveList.changing(
                tag = ReactiveList_generic_testUtils.SourceReactiveListTag,
                description = ChangeDescription.of(
                    ChangeDescription.Part.Insertion(
                        index = 2,
                        newElements = listOf(15, 16),
                    ),
                    ChangeDescription.Part.Removal(
                        indexRange = 4..<5,
                    ),
                    ChangeDescription.Part.Replacement(
                        indexRange = 6..<7,
                        replacedElements = listOf(200),
                    ),
                ),
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectElementTransition = ReactiveList_expectations_testUtils.expectContentTransition(
                expectedOldContent = listOf("0", "10", "20", "30", "40", "50", "60"),
                expectedNewContent = listOf("0", "10", "15", "16", "20", "30", "50", "200"),
            ),
        )
    }

    @Test
    fun test_sourceChangesRevoked_insertionsOnly() {
        slottedStimulationBank_sourceListChangesRevoked.forEach {
            test_sourceChangesRevoked_insertionsOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceChangesRevoked_insertionsOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(0, 10, 20, 30, 40, 50, 60),
        )

        val subjectReactiveList = sourceReactiveList.map { it.toString() }

        ReactiveList_reaction_testUtils.executeReactionTransaction(
            subjectReactiveList,
            slottedInputStimulation = sourceReactiveList.revokingChange(
                tag = ReactiveList_generic_testUtils.SourceReactiveListTag,
                intermediateDescription = ChangeDescription.of(
                    ChangeDescription.Part.Insertion(
                        index = 3,
                        newElements = listOf(21, 22, 23),
                    ),
                    ChangeDescription.Part.Insertion(
                        index = 6,
                        newElements = listOf(51, 52),
                    ),
                ),
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectElementTransition = ReactiveList_expectations_testUtils.expectNoContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedContent = listOf("0", "10", "20", "30", "40", "50", "60"),
            ),
        )
    }

    @Test
    fun test_sourceChangesRevoked_removalsOnly() {
        slottedStimulationBank_sourceListChangesRevoked.forEach {
            test_sourceChangesRevoked_removalsOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceChangesRevoked_removalsOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(0, 10, 20, 30, 40, 50, 60),
        )

        val subjectReactiveList = sourceReactiveList.map { it.toString() }

        ReactiveList_reaction_testUtils.executeReactionTransaction(
            subjectReactiveList,
            slottedInputStimulation = sourceReactiveList.revokingChange(
                tag = ReactiveList_generic_testUtils.SourceReactiveListTag,
                intermediateDescription = ChangeDescription.of(
                    ChangeDescription.Part.Removal(
                        indexRange = 1..<3,
                    ),
                    ChangeDescription.Part.Removal(
                        indexRange = 4..<6,
                    ),
                ),
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectElementTransition = ReactiveList_expectations_testUtils.expectNoContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedContent = listOf("0", "10", "20", "30", "40", "50", "60"),
            ),
        )
    }

    @Test
    fun test_sourceChangesRevoked_replacementsOnly() {
        slottedStimulationBank_sourceListChangesRevoked.forEach {
            test_sourceChangesRevoked_replacementsOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceChangesRevoked_replacementsOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(0, 10, 20, 30, 40, 50, 60),
        )

        val subjectReactiveList = sourceReactiveList.map { it.toString() }

        ReactiveList_reaction_testUtils.executeReactionTransaction(
            subjectReactiveList,
            slottedInputStimulation = sourceReactiveList.revokingChange(
                tag = ReactiveList_generic_testUtils.SourceReactiveListTag,
                intermediateDescription = ChangeDescription.of(
                    ChangeDescription.Part.Replacement(
                        indexRange = 1..<3,
                        replacedElements = listOf(100, 110),
                    ),
                    ChangeDescription.Part.Replacement(
                        indexRange = 4..<6,
                        replacedElements = listOf(140, 150),
                    ),
                ),
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectElementTransition = ReactiveList_expectations_testUtils.expectNoContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedContent = listOf("0", "10", "20", "30", "40", "50", "60"),
            ),
        )
    }

    @Test
    fun test_sourceChangesRevoked_mixed() {
        slottedStimulationBank_sourceListChangesRevoked.forEach {
            test_sourceChangesRevoked_mixed(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceChangesRevoked_mixed(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(0, 10, 20, 30, 40, 50, 60),
        )

        val subjectReactiveList = sourceReactiveList.map { it.toString() }

        ReactiveList_reaction_testUtils.executeReactionTransaction(
            subjectReactiveList,
            slottedInputStimulation = sourceReactiveList.revokingChange(
                tag = ReactiveList_generic_testUtils.SourceReactiveListTag,
                intermediateDescription = ChangeDescription.of(
                    ChangeDescription.Part.Insertion(
                        index = 2,
                        newElements = listOf(15, 16),
                    ),
                    ChangeDescription.Part.Removal(
                        indexRange = 4..<5,
                    ),
                    ChangeDescription.Part.Replacement(
                        indexRange = 6..<7,
                        replacedElements = listOf(200),
                    ),
                ),
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectElementTransition = ReactiveList_expectations_testUtils.expectNoContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedContent = listOf("0", "10", "20", "30", "40", "50", "60"),
            ),
        )
    }

    @Test
    fun test_sourceChangesCorrected_insertionsOnly() {
        slottedStimulationBank_sourceListChangesCorrected.forEach {
            test_sourceChangesCorrected_insertionsOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceChangesCorrected_insertionsOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(0, 10, 20, 30, 40, 50, 60),
        )

        val subjectReactiveList = sourceReactiveList.map { it.toString() }

        ReactiveList_reaction_testUtils.executeReactionTransaction(
            subjectReactiveList,
            slottedInputStimulation = sourceReactiveList.correctingChange(
                tag = ReactiveList_generic_testUtils.SourceReactiveListTag,
                intermediateDescription = ChangeDescription.of(
                    ChangeDescription.Part.Insertion(
                        index = 3,
                        newElements = listOf(21, 22, 23),
                    ),
                    ChangeDescription.Part.Insertion(
                        index = 6,
                        newElements = listOf(51, 52),
                    ),
                ),
                correctedDescription = ChangeDescription.of(
                    ChangeDescription.Part.Insertion(
                        index = 3,
                        newElements = listOf(25, 26),
                    ),
                ),
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectElementTransition = ReactiveList_expectations_testUtils.expectContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldContent = listOf("0", "10", "20", "30", "40", "50", "60"),
                expectedNewContent = listOf("0", "10", "20", "25", "26", "30", "40", "50", "60"),
            ),
        )
    }

    @Test
    fun test_sourceChangesCorrected_removalsOnly() {
        slottedStimulationBank_sourceListChangesCorrected.forEach {
            test_sourceChangesCorrected_removalsOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceChangesCorrected_removalsOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(0, 10, 20, 30, 40, 50, 60),
        )

        val subjectReactiveList = sourceReactiveList.map { it.toString() }

        ReactiveList_reaction_testUtils.executeReactionTransaction(
            subjectReactiveList,
            slottedInputStimulation = sourceReactiveList.correctingChange(
                tag = ReactiveList_generic_testUtils.SourceReactiveListTag,
                intermediateDescription = ChangeDescription.of(
                    ChangeDescription.Part.Removal(
                        indexRange = 1..<3,
                    ),
                    ChangeDescription.Part.Removal(
                        indexRange = 4..<6,
                    ),
                ),
                correctedDescription = ChangeDescription.of(
                    ChangeDescription.Part.Removal(
                        indexRange = 2..<4,
                    ),
                ),
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectElementTransition = ReactiveList_expectations_testUtils.expectContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldContent = listOf("0", "10", "20", "30", "40", "50", "60"),
                expectedNewContent = listOf("0", "10", "40", "50", "60"),
            ),
        )
    }

    @Test
    fun test_sourceChangesCorrected_replacementsOnly() {
        slottedStimulationBank_sourceListChangesCorrected.forEach {
            test_sourceChangesCorrected_replacementsOnly(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceChangesCorrected_replacementsOnly(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(0, 10, 20, 30, 40, 50, 60),
        )

        val subjectReactiveList = sourceReactiveList.map { it.toString() }

        ReactiveList_reaction_testUtils.executeReactionTransaction(
            subjectReactiveList,
            slottedInputStimulation = sourceReactiveList.correctingChange(
                tag = ReactiveList_generic_testUtils.SourceReactiveListTag,
                intermediateDescription = ChangeDescription.of(
                    ChangeDescription.Part.Replacement(
                        indexRange = 1..<3,
                        replacedElements = listOf(11, 21),
                    ),
                    ChangeDescription.Part.Replacement(
                        indexRange = 4..<6,
                        replacedElements = listOf(140, 150),
                    ),
                ),
                correctedDescription = ChangeDescription.of(
                    ChangeDescription.Part.Replacement(
                        indexRange = 1..<4,
                        replacedElements = listOf(11, 12, 13),
                    ),
                ),
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectElementTransition = ReactiveList_expectations_testUtils.expectContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldContent = listOf("0", "10", "20", "30", "40", "50", "60"),
                expectedNewContent = listOf("0", "11", "12", "13", "40", "50", "60"),
            ),
        )
    }

    @Test
    fun test_sourceChangesCorrected_mixed() {
        slottedStimulationBank_sourceListChangesCorrected.forEach {
            test_sourceChangesCorrected_mixed(
                slottedStimulationScenario = it,
            )
        }
    }

    private fun test_sourceChangesCorrected_mixed(
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceReactiveList = TestInputReactiveList(
            initialElements = listOf(0, 10, 20, 30, 40, 50, 60),
        )

        val subjectReactiveList = sourceReactiveList.map { it.toString() }

        ReactiveList_reaction_testUtils.executeReactionTransaction(
            subjectReactiveList,
            slottedInputStimulation = sourceReactiveList.correctingChange(
                tag = ReactiveList_generic_testUtils.SourceReactiveListTag,
                intermediateDescription = ChangeDescription.of(
                    ChangeDescription.Part.Insertion(
                        index = 2,
                        newElements = listOf(15, 16),
                    ),
                    ChangeDescription.Part.Removal(
                        indexRange = 4..<5,
                    ),
                    ChangeDescription.Part.Replacement(
                        indexRange = 6..<7,
                        replacedElements = listOf(200),
                    ),
                ),
                correctedDescription = ChangeDescription.of(
                    ChangeDescription.Part.Insertion(
                        index = 1,
                        newElements = listOf(5),
                    ),
                    ChangeDescription.Part.Removal(
                        indexRange = 5..<6,
                    ),
                ),
            ).bind(
                slottedStimulationScenario,
            ),
            expectedSubjectElementTransition = ReactiveList_expectations_testUtils.expectContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldContent = listOf("0", "10", "20", "30", "40", "50", "60"),
                expectedNewContent = listOf("0", "5", "10", "20", "30", "40", "60"),
            ),
        )
    }
}
