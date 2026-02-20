package dev.azide.core.collections.reactive_list

import dev.azide.core.collections.ReactiveList
import dev.azide.core.collections.helpers.SortableValue
import dev.azide.core.collections.helpers.withSortKey
import dev.azide.core.collections.sampleContentExternally
import dev.azide.core.collections.sampleTaggedElementsExternally
import dev.azide.core.impl.collections.reactive_list.ListChange
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.assertIsInactive
import dev.azide.core.test_utils.collections.reactive_bag.TestInputReactiveBag
import dev.azide.core.test_utils.collections.reactive_list.ExpectedReactiveListContentTransition
import dev.azide.core.test_utils.collections.reactive_list.ReactiveList_expectations_testUtils
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.generic.ReactiveListObservationTrait
import dev.azide.core.test_utils.generic.generic_reaction_testUtils
import dev.azide.core.test_utils.generic.generic_reaction_testUtils.TestSubjectHealthCheckStrategy

@Suppress("ClassName")
object ReactiveList_sortedUniquely_testUtils {
    enum class SortableValueEntryTag {
        Tag1, Tag2, Tag3, Tag4, Tag5, Tag6, Tag7,
    }

    private enum class ExtraSortableValueEntryTag {
        ExtraTag1, ExtraTag2,
    }

    private class SortedUniquelyHealthChecker(
        private val inputReactiveBag: TestInputReactiveBag<SortableValue<String, Double>>,
    ) : generic_reaction_testUtils.ReactiveListHealthChecker<String> {
        override fun verifyInputsInactive() {
            assertIsInactive(
                testInputEntity = inputReactiveBag,
                inputEntityLabel = "input reactive bag",
            )
        }

        override fun prepareHealthCheck(
            subject: ReactiveList<String>,
        ): generic_reaction_testUtils.TestSubjectHealthChecker.HealthCheckDescription<ReactiveList<String>, ListChange<String>> {
            val preHealthCheckSortedContent = subject.sampleContentExternally()

            // Two extra entries added during the health check, with sort keys placing them last
            val extraEntry1 = "Extra#1" withSortKey Double.MAX_VALUE / 2
            val extraEntry2 = "Extra#2" withSortKey Double.MAX_VALUE

            // Optionally remove the first tag currently in the bag to prove removal health
            val currentTaggedElements = inputReactiveBag.sampleTaggedElementsExternally()

            val removedTags = when {
                currentTaggedElements.isEmpty() -> emptySet()
                else -> setOf(currentTaggedElements.elementByTag.keys.first())
            }

            // Compute the expected post-health-check sorted content
            val removedValues: Set<String> = removedTags
                .mapNotNull { tag -> currentTaggedElements.elementByTag[tag]?.value }
                .toSet()

            val remainingSortableValues = currentTaggedElements.elementByTag.values
                .filter { it.value !in removedValues }

            val expectedNewSortedContent: List<String> =
                (remainingSortableValues + extraEntry1 + extraEntry2)
                    .sortedBy { it.sortKey }
                    .map { it.value }

            return generic_reaction_testUtils.TestSubjectHealthChecker.HealthCheckDescription(
                inputStimulation = TestStimulation.combine(
                    inputReactiveBag.change(
                        changeDescription = TestInputReactiveBag.ChangeDescription(
                            addedElementByTag = mapOf(
                                ExtraSortableValueEntryTag.ExtraTag1 to extraEntry1,
                                ExtraSortableValueEntryTag.ExtraTag2 to extraEntry2,
                            ),
                            removedTags = removedTags,
                        ),
                    ),
                ),
                expectedSubjectTransition = ReactiveList_expectations_testUtils.expectContentTransition(
                    intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                    expectedOldContent = preHealthCheckSortedContent,
                    expectedNewContent = expectedNewSortedContent,
                ),
            )
        }
    }

    fun executeReactionTransaction(
        inputReactiveBag: TestInputReactiveBag<SortableValue<String, Double>>,
        inputStimulationPlan: generic_reaction_testUtils.InputStimulationPlan,
        subjectReactiveList: ReactiveList<String>,
        expectedSubjectContentTransition: ExpectedReactiveListContentTransition<String>,
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        generic_reaction_testUtils.executeReactionTransaction(
            trait = ReactiveListObservationTrait(),
            subject = subjectReactiveList,
            inputStimulationPlan = inputStimulationPlan,
            expectedSubjectTransition = expectedSubjectContentTransition,
            subjectHealthChecker = SortedUniquelyHealthChecker(
                inputReactiveBag = inputReactiveBag,
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }
}
