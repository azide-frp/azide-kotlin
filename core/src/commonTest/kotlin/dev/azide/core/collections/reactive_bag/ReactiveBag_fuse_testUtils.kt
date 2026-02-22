package dev.azide.core.collections.reactive_bag

import dev.azide.core.collections.ReactiveBag
import dev.azide.core.collections.sampleTaggedElementsExternally
import dev.azide.core.impl.collections.reactive_bag.TaggedBagChange
import dev.azide.core.impl.collections.reactive_bag.mapKeepingTags
import dev.azide.core.impl.collections.reactive_bag.toMutableBag
import dev.azide.core.sampleExternally
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.assertIsInactive
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.collections.reactive_bag.ExpectedReactiveBagContentTransition
import dev.azide.core.test_utils.collections.reactive_bag.ReactiveBag_expectations_testUtils
import dev.azide.core.test_utils.collections.reactive_bag.TestInputReactiveBag
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.generic.ReactiveBagObservationTrait
import dev.azide.core.test_utils.generic.TestSubjectHealthCheckStrategy
import dev.azide.core.test_utils.generic.TestSubjectHealthChecker
import dev.azide.core.test_utils.generic.generic_reaction_testUtils

@Suppress("ClassName")
object ReactiveBag_fuse_testUtils {
    enum class FuseEntryTag {
        Tag1, Tag2, Tag3, Tag4, Tag5, Tag6, Tag7,
    }

    private enum class ExtraFuseEntryTag {
        ExtraTag1, ExtraTag2,
    }

    private class FuseHealthChecker(
        private val inputReactiveBag: TestInputReactiveBag<TestInputCell<String>>,
        private val inputCellByLabel: Map<String, TestInputCell<String>>,
    ) : generic_reaction_testUtils.ReactiveBagHealthChecker<String> {
        override fun verifyInputsInactive() {
            assertIsInactive(
                testInputEntity = inputReactiveBag,
                inputEntityLabel = "input reactive bag",
            )

            inputCellByLabel.forEach { (label, inputCell) ->
                assertIsInactive(
                    testInputEntity = inputCell,
                    inputEntityLabel = "input cell [$label]",
                )
            }
        }

        override fun prepareHealthCheck(
            subject: ReactiveBag<String>,
        ): TestSubjectHealthChecker.HealthCheckDescription<ReactiveBag<String>, TaggedBagChange<String>> {
            val preHealthCheckInputCells = inputReactiveBag.sampleTaggedElementsExternally()

            val newInputCellByTag: Map<ReactiveBag.Tag, TestInputCell<String>> = mapOf(
                ExtraFuseEntryTag.ExtraTag1 to TestInputCell(initialValue = "Extra#1"),
                ExtraFuseEntryTag.ExtraTag2 to TestInputCell(initialValue = "Extra#2"),
            )

            val expectedPreHealthCheckTaggedElements = preHealthCheckInputCells.mapKeepingTags {
                it.sampleExternally()
            }

            val mutableExpectedPostHealthCheckTaggedElements =
                expectedPreHealthCheckTaggedElements.mapKeepingTags { preHealthCheckValue ->
                    "$preHealthCheckValue-postHealthCheck"
                }.toMutableBag().apply {
                    addByTag(ExtraFuseEntryTag.ExtraTag1, "Extra#1")
                    addByTag(ExtraFuseEntryTag.ExtraTag2, "Extra#2")
                }

            val removedTags = when {
                preHealthCheckInputCells.isEmpty() -> emptySet()
                else -> setOf(preHealthCheckInputCells.elementByTag.keys.first())
            }

            if (removedTags.isNotEmpty()) {
                removedTags.forEach { removedTag ->
                    mutableExpectedPostHealthCheckTaggedElements.removeByTag(removedTag)
                }
            }

            // Stimulate all input cells (including the ones not exposed in the subject bag anymore) to prove the health
            // of the input cells observation
            val inputCellsStimulation = TestStimulation.combine(
                inputCellByLabel.values.map { inputCell ->
                    val preHealthCheckValue = inputCell.sampleExternally()

                    inputCell.update("$preHealthCheckValue-postHealthCheck")
                },
            )

            return TestSubjectHealthChecker.HealthCheckDescription(
                inputStimulation = TestStimulation.combine(
                    inputCellsStimulation,
                    inputReactiveBag.change(
                        // Add some new cells to prove the health of the subject bag observation
                        changeDescription = TestInputReactiveBag.ChangeDescription(
                            addedElementByTag = newInputCellByTag,
                            removedTags = removedTags,
                        ),
                    ),
                ),
                expectedSubjectTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                    intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                    expectedOldTaggedElements = expectedPreHealthCheckTaggedElements,
                    expectedNewTaggedElements = mutableExpectedPostHealthCheckTaggedElements,
                ),
            )
        }
    }

    fun testReaction(
        inputReactiveBag: TestInputReactiveBag<TestInputCell<String>>,
        inputCellByLabel: Map<String, TestInputCell<String>>,
        inputStimulationPlan: generic_reaction_testUtils.InputStimulationPlan,
        subjectReactiveBag: ReactiveBag<String>,
        expectedSubjectContentTransition: ExpectedReactiveBagContentTransition<String>,
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        generic_reaction_testUtils.testReaction(
            trait = ReactiveBagObservationTrait(),
            subject = subjectReactiveBag,
            inputStimulationPlan = inputStimulationPlan,
            expectedSubjectTransition = expectedSubjectContentTransition,
            subjectHealthChecker = FuseHealthChecker(
                inputReactiveBag = inputReactiveBag,
                inputCellByLabel = inputCellByLabel,
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }
}
