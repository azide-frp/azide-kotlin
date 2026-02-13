package dev.azide.core.collections.reactive_bag

import dev.azide.core.collections.fuse
import dev.azide.core.impl.collections.reactive_bag.TaggedBag
import dev.azide.core.impl.collections.reactive_bag.taggedBagOf
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.cell.TestInputCellStimulationTag
import dev.azide.core.test_utils.cell.TestInputCellTag
import dev.azide.core.test_utils.cell.TestInputReactiveCollectionTag
import dev.azide.core.test_utils.cell.correctingUpdate
import dev.azide.core.test_utils.cell.revokingUpdate
import dev.azide.core.test_utils.cell.updating
import dev.azide.core.test_utils.collections.reactive_bag.ReactiveBag_expectations_testUtils
import dev.azide.core.test_utils.collections.reactive_bag.ReactiveBag_reaction_testUtils
import dev.azide.core.test_utils.collections.reactive_bag.TestInputReactiveBag
import dev.azide.core.test_utils.collections.reactive_bag.changing
import dev.azide.core.test_utils.collections.reactive_bag.correctingChange
import dev.azide.core.test_utils.collections.reactive_bag.revokingChange
import dev.azide.core.test_utils.collections.reactive_list.ReactiveBag_sampling_testUtils
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlotCount
import dev.azide.core.test_utils.stimulation_combinatorics.TestSlottedStimulationScenario
import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationScenarioBank
import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationMap
import dev.azide.core.test_utils.stimulation_combinatorics.TestStimulationScenario
import dev.azide.core.test_utils.stimulation_combinatorics.bind
import kotlin.test.Test

@Suppress("ClassName")
class ReactiveBag_fuse_tests {
    private typealias SuitableSlotCount = TestSlotCount.Count2

    private typealias SuitableTestSlottedStimulationScenario = TestSlottedStimulationScenario<SuitableSlotCount>

    private data object OuterSourceReactiveBagTag : TestInputReactiveCollectionTag

    private enum class InnerCellBagTag {
        InnerCell1, InnerCell2, InnerCell3, InnerCell4, InnerCell5, InnerCell6, InnerCell7, InnerCell8
    }

    private enum class DynamicCellStimulationTag : TestInputCellTag {
        DynamicCellX, DynamicCellY, DynamicCellZ, DynamicCellW,
    }

    private interface InnerCellMapping {
        val sourceDynamicCellX: TestInputCell<String>
        val sourceDynamicCellXLabel: String

        val sourceDynamicCellY: TestInputCell<String>
        val sourceDynamicCellYLabel: String

        val sourceDynamicCellZ: TestInputCell<String>
        val sourceDynamicCellZLabel: String

        val sourceDynamicCellW: TestInputCell<String>
        val sourceDynamicCellWLabel: String
    }

    private sealed interface TestInput : InnerCellMapping {
        val sourceReactiveBagInitialContent: TaggedBag<TestInputCell<String>>

        val intermediateSourceBagChangeDescription: TestInputReactiveBag.ChangeDescription<TestInputCell<String>>
        val finalSourceBagChangeDescription: TestInputReactiveBag.ChangeDescription<TestInputCell<String>>

        val expectedSubjectOldTaggedElements: TaggedBag<String>

        fun buildExpectedSubjectNewTaggedElements(
            dynamicCellStimulationScenario: DynamicCellStimulationScenario,
        ): TaggedBag<String>

        fun buildExpectedSubjectNewTaggedElementsIfRevoked(
            dynamicCellStimulationScenario: DynamicCellStimulationScenario,
        ): TaggedBag<String>
    }

    private sealed interface SourceBagStimulationScenario {
        data object ChangesEffectively : SourceBagStimulationScenario {
            override val outerSourceBagStimulationScenario = TestInputReactiveCollectionTag.changeScenario(
                inputReactiveCollectionTag = OuterSourceReactiveBagTag,
            )

            override fun test(
                sourceBagChangeVariety: SourceBagChangeVariety,
                dynamicCellStimulationScenario: DynamicCellStimulationScenario,
                slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
            ) {
                val input = sourceBagChangeVariety.buildInput()

                val sourceReactiveBag = TestInputReactiveBag(
                    initialTaggedElements = input.sourceReactiveBagInitialContent,
                )

                val subjectReactiveBag = sourceReactiveBag.fuse()

                val sourceReactiveBagChangeDescription = input.finalSourceBagChangeDescription

                val dynamicCellStimulationMap = dynamicCellStimulationScenario.buildTestStimulationMap(
                    innerCellMapping = input,
                )

                val expectedOldTaggedElements = input.expectedSubjectOldTaggedElements

                val expectedNewTaggedElements = input.buildExpectedSubjectNewTaggedElements(
                    dynamicCellStimulationScenario = dynamicCellStimulationScenario,
                )

                ReactiveBag_reaction_testUtils.executeReactionTransaction(
                    subjectReactiveBag = subjectReactiveBag,
                    slottedInputStimulation = TestStimulationMap.union(
                        sourceReactiveBag.changing(
                            tag = OuterSourceReactiveBagTag,
                            changeDescription = sourceReactiveBagChangeDescription,
                        ),
                        dynamicCellStimulationMap,
                    ).bind(slottedStimulationScenario),
                    expectedSubjectElementTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                        intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                        expectedOldTaggedElements = expectedOldTaggedElements,
                        expectedNewTaggedElements = expectedNewTaggedElements,
                    )
                )
            }
        }

        data object ChangesRevoked : SourceBagStimulationScenario {
            override val outerSourceBagStimulationScenario = TestInputReactiveCollectionTag.revokedChangeScenario(
                inputReactiveCollectionTag = OuterSourceReactiveBagTag,
            )

            override fun test(
                sourceBagChangeVariety: SourceBagChangeVariety,
                dynamicCellStimulationScenario: DynamicCellStimulationScenario,
                slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
            ) {
                val input = sourceBagChangeVariety.buildInput()

                val sourceReactiveBag = TestInputReactiveBag(
                    initialTaggedElements = input.sourceReactiveBagInitialContent,
                )

                val subjectReactiveBag = sourceReactiveBag.fuse()

                val temporarySourceOuterBagChangeDescription = input.finalSourceBagChangeDescription

                val dynamicCellStimulationMap = dynamicCellStimulationScenario.buildTestStimulationMap(
                    innerCellMapping = input,
                )

                val expectedSubjectOldTaggedElements = input.expectedSubjectOldTaggedElements

                val expectedSubjectNewTaggedContent = input.buildExpectedSubjectNewTaggedElementsIfRevoked(
                    dynamicCellStimulationScenario = dynamicCellStimulationScenario,
                )

                ReactiveBag_reaction_testUtils.executeReactionTransaction(
                    subjectReactiveBag = subjectReactiveBag,
                    slottedInputStimulation = TestStimulationMap.union(
                        sourceReactiveBag.revokingChange(
                            tag = OuterSourceReactiveBagTag,
                            temporaryChangeDescription = temporarySourceOuterBagChangeDescription,
                        ),
                        dynamicCellStimulationMap,
                    ).bind(slottedStimulationScenario),
                    expectedSubjectElementTransition = ReactiveBag_expectations_testUtils.expectPotentialTaggedContentTransition(
                        intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                        expectedOldTaggedElements = expectedSubjectOldTaggedElements,
                        expectedNewTaggedElements = expectedSubjectNewTaggedContent,
                    ),
                )
            }
        }

        data object ChangesCorrected : SourceBagStimulationScenario {
            override val outerSourceBagStimulationScenario = TestInputReactiveCollectionTag.correctedChangeScenario(
                inputReactiveCollectionTag = OuterSourceReactiveBagTag,
            )

            override fun test(
                sourceBagChangeVariety: SourceBagChangeVariety,
                dynamicCellStimulationScenario: DynamicCellStimulationScenario,
                slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
            ) {
                val input = sourceBagChangeVariety.buildInput()

                val sourceReactiveBag = TestInputReactiveBag(
                    initialTaggedElements = input.sourceReactiveBagInitialContent,
                )

                val subjectReactiveBag = sourceReactiveBag.fuse()

                val intermediateSourceBagChangeDescription = input.intermediateSourceBagChangeDescription
                val correctedSourceBagChangeDescription = input.finalSourceBagChangeDescription

                val dynamicCellStimulationMap = dynamicCellStimulationScenario.buildTestStimulationMap(
                    innerCellMapping = input,
                )

                val expectedSubjectOldTaggedElements = input.expectedSubjectOldTaggedElements

                val expectedSubjectNewTaggedElements = input.buildExpectedSubjectNewTaggedElements(
                    dynamicCellStimulationScenario = dynamicCellStimulationScenario,
                )

                ReactiveBag_reaction_testUtils.executeReactionTransaction(
                    subjectReactiveBag = subjectReactiveBag,
                    slottedInputStimulation = TestStimulationMap.union(
                        sourceReactiveBag.correctingChange(
                            tag = OuterSourceReactiveBagTag,
                            intermediateChangeDescription = intermediateSourceBagChangeDescription,
                            correctedChangeDescription = correctedSourceBagChangeDescription,
                        ),
                        dynamicCellStimulationMap,
                    ).bind(slottedStimulationScenario),
                    expectedSubjectElementTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                        intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                        expectedOldTaggedElements = expectedSubjectOldTaggedElements,
                        expectedNewTaggedElements = expectedSubjectNewTaggedElements,
                    )
                )
            }
        }

        val outerSourceBagStimulationScenario: TestStimulationScenario

        fun test(
            sourceBagChangeVariety: SourceBagChangeVariety,
            dynamicCellStimulationScenario: DynamicCellStimulationScenario,
            slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
        )
    }

    private sealed interface SourceBagChangeVariety {
        data object AdditionsOnly : SourceBagChangeVariety {
            class AdditionsOnlyTestInput : TestInput {
                // Initial cells in the bag (never replaced, no suffix)
                private val sourceCell1 = TestInputCell(initialValue = "1!")
                private val sourceCell2 = TestInputCell(initialValue = "2!")
                private val sourceCell3 = TestInputCell(initialValue = "3!")

                // Cells for intermediate additions (will be corrected)
                private val sourceCell4 = TestInputCell(initialValue = "4!") // Corrected: not added
                private val sourceCell5a = TestInputCell(initialValue = "5a!") // Has replacement
                private val sourceCell6a = TestInputCell(initialValue = "6a!") // Has replacement

                // Cells for final/corrected additions
                private val sourceCell5B = TestInputCell(initialValue = "5B!")
                private val sourceCell6B = TestInputCell(initialValue = "6B!")
                private val sourceCell7 = TestInputCell(initialValue = "7!") // No replacement

                override val sourceDynamicCellX = sourceCell3
                override val sourceDynamicCellXLabel = "3"

                override val sourceDynamicCellY = sourceCell5a
                override val sourceDynamicCellYLabel = "5a"

                override val sourceDynamicCellZ = sourceCell5B
                override val sourceDynamicCellZLabel = "5B"

                override val sourceDynamicCellW = sourceCell6B
                override val sourceDynamicCellWLabel = "6B"

                override val sourceReactiveBagInitialContent: TaggedBag<TestInputCell<String>>
                    get() = taggedBagOf(
                        InnerCellBagTag.InnerCell1 to sourceCell1,
                        InnerCellBagTag.InnerCell2 to sourceCell2,
                        InnerCellBagTag.InnerCell3 to sourceCell3,
                    )

                override val intermediateSourceBagChangeDescription: TestInputReactiveBag.ChangeDescription<TestInputCell<String>>
                    get() = TestInputReactiveBag.ChangeDescription(
                        addedElementByTag = mapOf(
                            InnerCellBagTag.InnerCell4 to sourceCell4, // Corrected: not added
                            InnerCellBagTag.InnerCell5 to sourceCell5a, // Corrected: added differently
                            InnerCellBagTag.InnerCell6 to sourceCell6a, // Corrected: added differently
                        ),
                    )

                override val finalSourceBagChangeDescription: TestInputReactiveBag.ChangeDescription<TestInputCell<String>>
                    get() = TestInputReactiveBag.ChangeDescription(
                        addedElementByTag = mapOf(
                            InnerCellBagTag.InnerCell5 to sourceCell5B,
                            InnerCellBagTag.InnerCell6 to sourceCell6B,
                            InnerCellBagTag.InnerCell7 to sourceCell7, // (not mentioned before)
                        ),
                    )

                override val expectedSubjectOldTaggedElements: TaggedBag<String>
                    get() = taggedBagOf(
                        InnerCellBagTag.InnerCell1 to "1!",
                        InnerCellBagTag.InnerCell2 to "2!",
                        InnerCellBagTag.InnerCell3 to "3!",
                    )

                override fun buildExpectedSubjectNewTaggedElements(
                    dynamicCellStimulationScenario: DynamicCellStimulationScenario,
                ): TaggedBag<String> = taggedBagOf(
                    InnerCellBagTag.InnerCell1 to "1!",
                    InnerCellBagTag.InnerCell2 to "2!",
                    InnerCellBagTag.InnerCell3 to "3${dynamicCellStimulationScenario.expectedNewSymbolX}",
                    InnerCellBagTag.InnerCell5 to "5B${dynamicCellStimulationScenario.expectedNewSymbolZ}",
                    InnerCellBagTag.InnerCell6 to "6B${dynamicCellStimulationScenario.expectedNewSymbolW}",
                    InnerCellBagTag.InnerCell7 to "7!",
                )

                override fun buildExpectedSubjectNewTaggedElementsIfRevoked(
                    dynamicCellStimulationScenario: DynamicCellStimulationScenario,
                ): TaggedBag<String> = taggedBagOf(
                    InnerCellBagTag.InnerCell1 to "1!",
                    InnerCellBagTag.InnerCell2 to "2!",
                    InnerCellBagTag.InnerCell3 to "3${dynamicCellStimulationScenario.expectedNewSymbolX}",
                )
            }

            override fun buildInput() = AdditionsOnlyTestInput()
        }

        data object RemovalsOnly : SourceBagChangeVariety {
            class RemovalsOnlyTestInput : TestInput {
                // Initial cells in the bag (none are replaced, no suffix)
                private val sourceCell1 = TestInputCell(initialValue = "1!")
                private val sourceCell2 = TestInputCell(initialValue = "2!")
                private val sourceCell3 = TestInputCell(initialValue = "3!")
                private val sourceCell4 = TestInputCell(initialValue = "4!")
                private val sourceCell5 = TestInputCell(initialValue = "5!")
                private val sourceCell6 = TestInputCell(initialValue = "6!")

                override val sourceDynamicCellX = sourceCell4
                override val sourceDynamicCellXLabel = "4"

                override val sourceDynamicCellY = sourceCell5
                override val sourceDynamicCellYLabel = "5"

                override val sourceDynamicCellZ = sourceCell6
                override val sourceDynamicCellZLabel = "6"

                override val sourceDynamicCellW = sourceCell1
                override val sourceDynamicCellWLabel = "1"

                override val sourceReactiveBagInitialContent: TaggedBag<TestInputCell<String>>
                    get() = taggedBagOf(
                        InnerCellBagTag.InnerCell1 to sourceCell1,
                        InnerCellBagTag.InnerCell2 to sourceCell2,
                        InnerCellBagTag.InnerCell3 to sourceCell3,
                        InnerCellBagTag.InnerCell4 to sourceCell4,
                        InnerCellBagTag.InnerCell5 to sourceCell5,
                        InnerCellBagTag.InnerCell6 to sourceCell6,
                    )

                override val intermediateSourceBagChangeDescription: TestInputReactiveBag.ChangeDescription<TestInputCell<String>>
                    get() = TestInputReactiveBag.ChangeDescription(
                        removedTags = setOf(
                            InnerCellBagTag.InnerCell4, // Corrected: not removed
                            InnerCellBagTag.InnerCell5, // Not corrected
                        ),
                    )

                override val finalSourceBagChangeDescription: TestInputReactiveBag.ChangeDescription<TestInputCell<String>>
                    get() = TestInputReactiveBag.ChangeDescription(
                        removedTags = setOf(
                            InnerCellBagTag.InnerCell5, // (not mentioned before)
                            InnerCellBagTag.InnerCell6,
                        ),
                    )

                override val expectedSubjectOldTaggedElements: TaggedBag<String>
                    get() = taggedBagOf(
                        InnerCellBagTag.InnerCell1 to "1!",
                        InnerCellBagTag.InnerCell2 to "2!",
                        InnerCellBagTag.InnerCell3 to "3!",
                        InnerCellBagTag.InnerCell4 to "4!",
                        InnerCellBagTag.InnerCell5 to "5!",
                        InnerCellBagTag.InnerCell6 to "6!",
                    )

                override fun buildExpectedSubjectNewTaggedElements(
                    dynamicCellStimulationScenario: DynamicCellStimulationScenario,
                ): TaggedBag<String> = taggedBagOf(
                    InnerCellBagTag.InnerCell1 to "1${dynamicCellStimulationScenario.expectedNewSymbolW}",
                    InnerCellBagTag.InnerCell2 to "2!",
                    InnerCellBagTag.InnerCell3 to "3!",
                    InnerCellBagTag.InnerCell4 to "4${dynamicCellStimulationScenario.expectedNewSymbolX}",
                )

                override fun buildExpectedSubjectNewTaggedElementsIfRevoked(
                    dynamicCellStimulationScenario: DynamicCellStimulationScenario,
                ): TaggedBag<String> = taggedBagOf(
                    InnerCellBagTag.InnerCell1 to "1${dynamicCellStimulationScenario.expectedNewSymbolW}",
                    InnerCellBagTag.InnerCell2 to "2!",
                    InnerCellBagTag.InnerCell3 to "3!",
                    InnerCellBagTag.InnerCell4 to "4${dynamicCellStimulationScenario.expectedNewSymbolX}",
                    InnerCellBagTag.InnerCell5 to "5${dynamicCellStimulationScenario.expectedNewSymbolY}",
                    InnerCellBagTag.InnerCell6 to "6${dynamicCellStimulationScenario.expectedNewSymbolZ}",
                )
            }

            override fun buildInput() = RemovalsOnlyTestInput()
        }

        data object ReplacementsOnly : SourceBagChangeVariety {
            class ReplacementsOnlyTestInput : TestInput {
                // Initial cells in the bag
                private val sourceCell1 = TestInputCell(initialValue = "1!") // Never replaced
                private val sourceCell2a = TestInputCell(initialValue = "2a!") // Will be replaced
                private val sourceCell3a = TestInputCell(initialValue = "3a!") // Will be replaced
                private val sourceCell4a = TestInputCell(initialValue = "4a!") // Intermediate replacement only

                // Replacement cells for intermediate change (lowercase b)
                private val sourceCell2b = TestInputCell(initialValue = "2b!")
                private val sourceCell3b = TestInputCell(initialValue = "3b!")
                private val sourceCell4b = TestInputCell(initialValue = "4b!")

                // Replacement cells for final/corrected change (uppercase B)
                private val sourceCell2B = TestInputCell(initialValue = "2B!")
                private val sourceCell3B = TestInputCell(initialValue = "3B!")

                override val sourceDynamicCellX = sourceCell4a
                override val sourceDynamicCellXLabel = "4a"

                override val sourceDynamicCellY = sourceCell2b
                override val sourceDynamicCellYLabel = "2b"

                override val sourceDynamicCellZ = sourceCell3B
                override val sourceDynamicCellZLabel = "3B"

                override val sourceDynamicCellW = sourceCell3a
                override val sourceDynamicCellWLabel = "3a"

                override val sourceReactiveBagInitialContent: TaggedBag<TestInputCell<String>>
                    get() = taggedBagOf(
                        InnerCellBagTag.InnerCell1 to sourceCell1,
                        InnerCellBagTag.InnerCell2 to sourceCell2a,
                        InnerCellBagTag.InnerCell3 to sourceCell3a,
                        InnerCellBagTag.InnerCell4 to sourceCell4a,
                    )

                override val intermediateSourceBagChangeDescription: TestInputReactiveBag.ChangeDescription<TestInputCell<String>>
                    get() = TestInputReactiveBag.ChangeDescription(
                        replacedElementByTag = mapOf(
                            InnerCellBagTag.InnerCell2 to sourceCell2b, // Corrected: replaced differently
                            InnerCellBagTag.InnerCell3 to sourceCell3b, // Corrected: replaced differently
                            InnerCellBagTag.InnerCell4 to sourceCell4b, // Corrected: not replaced
                        ),
                    )

                override val finalSourceBagChangeDescription: TestInputReactiveBag.ChangeDescription<TestInputCell<String>>
                    get() = TestInputReactiveBag.ChangeDescription(
                        replacedElementByTag = mapOf(
                            InnerCellBagTag.InnerCell2 to sourceCell2B,
                            InnerCellBagTag.InnerCell3 to sourceCell3B,
                        ),
                    )

                override val expectedSubjectOldTaggedElements: TaggedBag<String>
                    get() = taggedBagOf(
                        InnerCellBagTag.InnerCell1 to "1!",
                        InnerCellBagTag.InnerCell2 to "2a!",
                        InnerCellBagTag.InnerCell3 to "3a!",
                        InnerCellBagTag.InnerCell4 to "4a!",
                    )

                override fun buildExpectedSubjectNewTaggedElements(
                    dynamicCellStimulationScenario: DynamicCellStimulationScenario,
                ): TaggedBag<String> = taggedBagOf(
                    InnerCellBagTag.InnerCell1 to "1!",
                    InnerCellBagTag.InnerCell2 to "2B!",
                    InnerCellBagTag.InnerCell3 to "3B${dynamicCellStimulationScenario.expectedNewSymbolZ}",
                    InnerCellBagTag.InnerCell4 to "4a${dynamicCellStimulationScenario.expectedNewSymbolX}",
                )

                override fun buildExpectedSubjectNewTaggedElementsIfRevoked(
                    dynamicCellStimulationScenario: DynamicCellStimulationScenario,
                ): TaggedBag<String> = taggedBagOf(
                    InnerCellBagTag.InnerCell1 to "1!",
                    InnerCellBagTag.InnerCell2 to "2a!",
                    InnerCellBagTag.InnerCell3 to "3a${dynamicCellStimulationScenario.expectedNewSymbolW}",
                    InnerCellBagTag.InnerCell4 to "4a${dynamicCellStimulationScenario.expectedNewSymbolX}",
                )
            }

            override fun buildInput() = ReplacementsOnlyTestInput()
        }

        data object Mixed : SourceBagChangeVariety {
            class MixedTestInput : TestInput {
                // Initial cells in the bag
                private val sourceCell1 = TestInputCell(initialValue = "1!") // Never replaced
                private val sourceCell2a = TestInputCell(initialValue = "2a!") // Will be replaced
                private val sourceCell3 = TestInputCell(initialValue = "3!") // Never replaced
                private val sourceCell4 = TestInputCell(initialValue = "4!") // Never replaced (removed)
                private val sourceCell5 = TestInputCell(initialValue = "5!") // Never replaced (removed)

                // Cells for intermediate additions
                private val sourceCell6a = TestInputCell(initialValue = "6a!") // Will be replaced
                private val sourceCell7 = TestInputCell(initialValue = "7!") // Intermediate only, not replaced

                // Cells for final/corrected additions
                private val sourceCell6B = TestInputCell(initialValue = "6B!")
                private val sourceCell8 = TestInputCell(initialValue = "8!") // New addition, not replaced

                // Replacement cells - intermediate (lowercase b) and final/corrected (uppercase B)
                private val sourceCell2b = TestInputCell(initialValue = "2b!")
                private val sourceCell2B = TestInputCell(initialValue = "2B!")

                override val sourceDynamicCellX = sourceCell1
                override val sourceDynamicCellXLabel = "1"

                override val sourceDynamicCellY = sourceCell6a
                override val sourceDynamicCellYLabel = "6a"

                override val sourceDynamicCellZ = sourceCell2B
                override val sourceDynamicCellZLabel = "2B"

                override val sourceDynamicCellW = sourceCell3
                override val sourceDynamicCellWLabel = "3"

                override val sourceReactiveBagInitialContent: TaggedBag<TestInputCell<String>>
                    get() = taggedBagOf(
                        InnerCellBagTag.InnerCell1 to sourceCell1,
                        InnerCellBagTag.InnerCell2 to sourceCell2a,
                        InnerCellBagTag.InnerCell3 to sourceCell3,
                        InnerCellBagTag.InnerCell4 to sourceCell4,
                        InnerCellBagTag.InnerCell5 to sourceCell5,
                    )

                override val intermediateSourceBagChangeDescription: TestInputReactiveBag.ChangeDescription<TestInputCell<String>>
                    get() = TestInputReactiveBag.ChangeDescription(
                        addedElementByTag = mapOf(
                            InnerCellBagTag.InnerCell6 to sourceCell6a, // Corrected: added differently
                            InnerCellBagTag.InnerCell7 to sourceCell7, // Corrected: not added
                        ),
                        replacedElementByTag = mapOf(
                            InnerCellBagTag.InnerCell2 to sourceCell2b, // Corrected: replaced differently
                        ),
                        removedTags = setOf(
                            InnerCellBagTag.InnerCell3, // Corrected: not removed
                            InnerCellBagTag.InnerCell4, // Not corrected
                        ),
                    )

                override val finalSourceBagChangeDescription: TestInputReactiveBag.ChangeDescription<TestInputCell<String>>
                    get() = TestInputReactiveBag.ChangeDescription(
                        addedElementByTag = mapOf(
                            InnerCellBagTag.InnerCell6 to sourceCell6B, // Add different cell
                            InnerCellBagTag.InnerCell8 to sourceCell8, // New addition
                        ),
                        replacedElementByTag = mapOf(
                            InnerCellBagTag.InnerCell2 to sourceCell2B, // Replace with different cell
                        ),
                        removedTags = setOf(
                            InnerCellBagTag.InnerCell4, // Remove
                            InnerCellBagTag.InnerCell5, // Remove (not mentioned before)
                        ),
                    )

                override val expectedSubjectOldTaggedElements: TaggedBag<String>
                    get() = taggedBagOf(
                        InnerCellBagTag.InnerCell1 to "1!",
                        InnerCellBagTag.InnerCell2 to "2a!",
                        InnerCellBagTag.InnerCell3 to "3!",
                        InnerCellBagTag.InnerCell4 to "4!",
                        InnerCellBagTag.InnerCell5 to "5!",
                    )

                override fun buildExpectedSubjectNewTaggedElements(
                    dynamicCellStimulationScenario: DynamicCellStimulationScenario,
                ): TaggedBag<String> = taggedBagOf(
                    InnerCellBagTag.InnerCell1 to "1${dynamicCellStimulationScenario.expectedNewSymbolX}",
                    InnerCellBagTag.InnerCell2 to "2B${dynamicCellStimulationScenario.expectedNewSymbolZ}",
                    InnerCellBagTag.InnerCell3 to "3${dynamicCellStimulationScenario.expectedNewSymbolW}",
                    InnerCellBagTag.InnerCell6 to "6B!",
                    InnerCellBagTag.InnerCell8 to "8!",
                )

                override fun buildExpectedSubjectNewTaggedElementsIfRevoked(
                    dynamicCellStimulationScenario: DynamicCellStimulationScenario,
                ): TaggedBag<String> = taggedBagOf(
                    InnerCellBagTag.InnerCell1 to "1${dynamicCellStimulationScenario.expectedNewSymbolX}",
                    InnerCellBagTag.InnerCell2 to "2a!",
                    InnerCellBagTag.InnerCell3 to "3${dynamicCellStimulationScenario.expectedNewSymbolW}",
                    InnerCellBagTag.InnerCell4 to "4!",
                    InnerCellBagTag.InnerCell5 to "5!",
                )
            }

            override fun buildInput() = MixedTestInput()
        }

        fun buildInput(): TestInput
    }

    private sealed class DynamicCellStimulationScenario {
        sealed class DynamicInnerSourceCellStimulationKind {
            data object None : DynamicInnerSourceCellStimulationKind() {
                override val expectedNewSymbol: Char = InitialSymbol

                override fun toStimulationScenario(
                    tag: DynamicCellStimulationTag,
                ): TestStimulationScenario = TestStimulationScenario.Empty

                override fun buildTestStimulationMap(
                    sourceCell: TestInputCell<String>,
                    tag: DynamicCellStimulationTag,
                    label: String,
                ): TestStimulationMap = TestStimulationMap.Empty
            }

            data object UpdatesEffectively : DynamicInnerSourceCellStimulationKind() {
                override val expectedNewSymbol: Char = FinalSymbol

                override fun toStimulationScenario(
                    tag: DynamicCellStimulationTag,
                ): TestStimulationScenario = TestInputCellTag.updateScenario(
                    inputCellTag = tag,
                )

                override fun buildTestStimulationMap(
                    sourceCell: TestInputCell<String>,
                    tag: DynamicCellStimulationTag,
                    label: String,
                ): TestStimulationMap = sourceCell.updating(
                    tag = tag,
                    newValue = "$label$FinalSymbol",
                )
            }

            data object UpdatesRevoked : DynamicInnerSourceCellStimulationKind() {
                override val expectedNewSymbol: Char = InitialSymbol

                override fun toStimulationScenario(
                    tag: DynamicCellStimulationTag,
                ): TestStimulationScenario = TestInputCellTag.revokedUpdateScenario(
                    inputCellTag = tag,
                )

                override fun buildTestStimulationMap(
                    sourceCell: TestInputCell<String>,
                    tag: DynamicCellStimulationTag,
                    label: String,
                ): TestStimulationMap = sourceCell.revokingUpdate(
                    tag = tag, newValue = "${label}$IntermediateSymbol"
                )
            }

            data object UpdatesCorrected : DynamicInnerSourceCellStimulationKind() {
                override val expectedNewSymbol: Char = FinalSymbol

                override fun toStimulationScenario(
                    tag: DynamicCellStimulationTag,
                ): TestStimulationScenario = TestInputCellTag.correctedUpdateScenario(
                    inputCellTag = tag,
                )

                override fun buildTestStimulationMap(
                    sourceCell: TestInputCell<String>,
                    tag: DynamicCellStimulationTag,
                    label: String,
                ): TestStimulationMap = sourceCell.correctingUpdate(
                    tag = tag,
                    intermediateNewValue = "$label$IntermediateSymbol",
                    correctedNewValue = "$label$FinalSymbol",
                )
            }

            abstract val expectedNewSymbol: Char

            abstract fun toStimulationScenario(
                tag: DynamicCellStimulationTag,
            ): TestStimulationScenario

            abstract fun buildTestStimulationMap(
                sourceCell: TestInputCell<String>,
                tag: DynamicCellStimulationTag,
                label: String,
            ): TestStimulationMap
        }

        /**
         * The dynamic inner source cells don't update.
         */
        data object None : DynamicCellStimulationScenario() {
            override val dynamicCellXStimulationKind = DynamicInnerSourceCellStimulationKind.None
            override val dynamicCellYStimulationKind = DynamicInnerSourceCellStimulationKind.None
            override val dynamicCellZStimulationKind = DynamicInnerSourceCellStimulationKind.None
            override val dynamicCellWStimulationKind = DynamicInnerSourceCellStimulationKind.None
        }

        /**
         * The dynamic inner source cells update, all of these updates are effective.
         */
        data object Update : DynamicCellStimulationScenario() {
            override val dynamicCellXStimulationKind = DynamicInnerSourceCellStimulationKind.UpdatesEffectively
            override val dynamicCellYStimulationKind = DynamicInnerSourceCellStimulationKind.UpdatesEffectively
            override val dynamicCellZStimulationKind = DynamicInnerSourceCellStimulationKind.UpdatesEffectively
            override val dynamicCellWStimulationKind = DynamicInnerSourceCellStimulationKind.UpdatesEffectively
        }

        /**
         * The dynamic inner source cells update, some (but not all) of these updates are revoked.
         */
        data object UpdateSomeRevoked : DynamicCellStimulationScenario() {
            private val updateX = TestInputCellStimulationTag.Update(
                inputTag = DynamicCellStimulationTag.DynamicCellX,
            )

            private val updateY = TestInputCellStimulationTag.Update(
                inputTag = DynamicCellStimulationTag.DynamicCellY,
            )

            private val updateRevocationY = TestInputCellStimulationTag.UpdateRevocation(
                inputTag = DynamicCellStimulationTag.DynamicCellY,
            )

            private val updateZ = TestInputCellStimulationTag.Update(
                inputTag = DynamicCellStimulationTag.DynamicCellZ,
            )

            private val updateRevocationZ = TestInputCellStimulationTag.UpdateRevocation(
                inputTag = DynamicCellStimulationTag.DynamicCellZ,
            )

            private val updateW = TestInputCellStimulationTag.Update(
                inputTag = DynamicCellStimulationTag.DynamicCellW,
            )

            private val updateRevocationW = TestInputCellStimulationTag.UpdateRevocation(
                inputTag = DynamicCellStimulationTag.DynamicCellW,
            )

            override val dynamicCellXStimulationKind = DynamicInnerSourceCellStimulationKind.UpdatesEffectively
            override val dynamicCellYStimulationKind = DynamicInnerSourceCellStimulationKind.UpdatesRevoked
            override val dynamicCellZStimulationKind = DynamicInnerSourceCellStimulationKind.UpdatesRevoked
            override val dynamicCellWStimulationKind = DynamicInnerSourceCellStimulationKind.UpdatesRevoked

            override val dynamicCellStimulationScenarioBank: TestStimulationScenarioBank
                get() = TestStimulationScenarioBank.of(
                    // Each revoked immediately (in order, X effective)
                    TestStimulationScenario.of(
                        updateW,
                        updateRevocationW,
                        updateX,
                        updateY,
                        updateRevocationY,
                        updateZ,
                        updateRevocationZ,
                    ),
                    // Each revoked immediately (in reversed order, X effective)
                    TestStimulationScenario.of(
                        updateZ,
                        updateRevocationZ,
                        updateY,
                        updateRevocationY,
                        updateX,
                        updateW,
                        updateRevocationW,
                    ),
                    // Each revoked immediately (shuffled 1)
                    TestStimulationScenario.of(
                        updateY,
                        updateRevocationY,
                        updateZ,
                        updateRevocationZ,
                        updateX,
                        updateW,
                        updateRevocationW,
                    ),
                    // Each revoked immediately (shuffled 2)
                    TestStimulationScenario.of(
                        updateX,
                        updateW,
                        updateRevocationW,
                        updateZ,
                        updateRevocationZ,
                        updateY,
                        updateRevocationY,
                    ),
                    // Each revoked immediately (shuffled 3)
                    TestStimulationScenario.of(
                        updateW,
                        updateRevocationW,
                        updateZ,
                        updateRevocationZ,
                        updateY,
                        updateRevocationY,
                        updateX,
                    ),
                    // All updates, then all revoked (in order)
                    TestStimulationScenario.of(
                        updateW,
                        updateX,
                        updateY,
                        updateZ,
                        updateRevocationW,
                        updateRevocationY,
                        updateRevocationZ,
                    ),
                    // All updates, then all revoked (in reversed order)
                    TestStimulationScenario.of(
                        updateZ,
                        updateY,
                        updateX,
                        updateW,
                        updateRevocationZ,
                        updateRevocationY,
                        updateRevocationW,
                    ),
                    // All updates, then all revoked (shuffle 1)
                    TestStimulationScenario.of(
                        updateY,
                        updateZ,
                        updateW,
                        updateX,
                        updateRevocationY,
                        updateRevocationZ,
                        updateRevocationW,
                    ),
                    // All updates, then all revoked (shuffle 2)
                    TestStimulationScenario.of(
                        updateX,
                        updateW,
                        updateZ,
                        updateY,
                        updateRevocationW,
                        updateRevocationZ,
                        updateRevocationY,
                    ),
                    // All updates, then all revoked (shuffle 3)
                    TestStimulationScenario.of(
                        updateZ,
                        updateX,
                        updateW,
                        updateY,
                        updateRevocationZ,
                        updateRevocationW,
                        updateRevocationY,
                    ),
                    // Interleaved (shuffle 1)
                    TestStimulationScenario.of(
                        updateW,
                        updateX,
                        updateY,
                        updateRevocationW,
                        updateZ,
                        updateRevocationY,
                        updateRevocationZ,
                    ),
                    // Interleaved (shuffle 2)
                    TestStimulationScenario.of(
                        updateY,
                        updateRevocationY,
                        updateW,
                        updateZ,
                        updateRevocationZ,
                        updateX,
                        updateRevocationW,
                    ),
                    // Interleaved (shuffle 3)
                    TestStimulationScenario.of(
                        updateZ,
                        updateW,
                        updateRevocationW,
                        updateX,
                        updateRevocationZ,
                        updateY,
                        updateRevocationY,
                    ),
                    // Interleaved (shuffle 4)
                    TestStimulationScenario.of(
                        updateX,
                        updateY,
                        updateRevocationY,
                        updateZ,
                        updateW,
                        updateRevocationZ,
                        updateRevocationW,
                    ),
                    // Interleaved (shuffle 5)
                    TestStimulationScenario.of(
                        updateW,
                        updateZ,
                        updateRevocationZ,
                        updateRevocationW,
                        updateX,
                        updateY,
                        updateRevocationY,
                    ),
                )
        }

        /**
         * The dynamic inner source cells update, all of these updates are revoked.
         */
        data object UpdateAllRevoked : DynamicCellStimulationScenario() {
            private val updateX = TestInputCellStimulationTag.Update(
                inputTag = DynamicCellStimulationTag.DynamicCellX,
            )

            private val updateRevocationX = TestInputCellStimulationTag.UpdateRevocation(
                inputTag = DynamicCellStimulationTag.DynamicCellX,
            )

            private val updateY = TestInputCellStimulationTag.Update(
                inputTag = DynamicCellStimulationTag.DynamicCellY,
            )

            private val updateRevocationY = TestInputCellStimulationTag.UpdateRevocation(
                inputTag = DynamicCellStimulationTag.DynamicCellY,
            )

            private val updateZ = TestInputCellStimulationTag.Update(
                inputTag = DynamicCellStimulationTag.DynamicCellZ,
            )

            private val updateRevocationZ = TestInputCellStimulationTag.UpdateRevocation(
                inputTag = DynamicCellStimulationTag.DynamicCellZ,
            )

            private val updateW = TestInputCellStimulationTag.Update(
                inputTag = DynamicCellStimulationTag.DynamicCellW,
            )

            private val updateRevocationW = TestInputCellStimulationTag.UpdateRevocation(
                inputTag = DynamicCellStimulationTag.DynamicCellW,
            )

            override val dynamicCellXStimulationKind = DynamicInnerSourceCellStimulationKind.UpdatesRevoked
            override val dynamicCellYStimulationKind = DynamicInnerSourceCellStimulationKind.UpdatesRevoked
            override val dynamicCellZStimulationKind = DynamicInnerSourceCellStimulationKind.UpdatesRevoked
            override val dynamicCellWStimulationKind = DynamicInnerSourceCellStimulationKind.UpdatesRevoked

            override val dynamicCellStimulationScenarioBank: TestStimulationScenarioBank
                get() = TestStimulationScenarioBank.of(
                    // Each revoked immediately (in order)
                    TestStimulationScenario.of(
                        updateW,
                        updateRevocationW,
                        updateX,
                        updateRevocationX,
                        updateY,
                        updateRevocationY,
                        updateZ,
                        updateRevocationZ,
                    ),
                    // Each revoked immediately (in reversed order)
                    TestStimulationScenario.of(
                        updateZ,
                        updateRevocationZ,
                        updateY,
                        updateRevocationY,
                        updateX,
                        updateRevocationX,
                        updateW,
                        updateRevocationW,
                    ),
                    // Each revoked immediately (shuffled 1)
                    TestStimulationScenario.of(
                        updateY,
                        updateRevocationY,
                        updateZ,
                        updateRevocationZ,
                        updateX,
                        updateRevocationX,
                        updateW,
                        updateRevocationW,
                    ),
                    // Each revoked immediately (shuffled 2)
                    TestStimulationScenario.of(
                        updateX,
                        updateRevocationX,
                        updateW,
                        updateRevocationW,
                        updateZ,
                        updateRevocationZ,
                        updateY,
                        updateRevocationY,
                    ),
                    // Each revoked immediately (shuffled 3)
                    TestStimulationScenario.of(
                        updateW,
                        updateRevocationW,
                        updateZ,
                        updateRevocationZ,
                        updateY,
                        updateRevocationY,
                        updateX,
                        updateRevocationX,
                    ),
                    // All updates, then all revoked (in order)
                    TestStimulationScenario.of(
                        updateW,
                        updateX,
                        updateY,
                        updateZ,
                        updateRevocationW,
                        updateRevocationX,
                        updateRevocationY,
                        updateRevocationZ,
                    ),
                    // All updates, then all revoked (in reversed order)
                    TestStimulationScenario.of(
                        updateZ,
                        updateY,
                        updateX,
                        updateW,
                        updateRevocationZ,
                        updateRevocationY,
                        updateRevocationX,
                        updateRevocationW,
                    ),
                    // All updates, then all revoked (shuffle 1)
                    TestStimulationScenario.of(
                        updateY,
                        updateZ,
                        updateW,
                        updateX,
                        updateRevocationY,
                        updateRevocationZ,
                        updateRevocationW,
                        updateRevocationX,
                    ),
                    // All updates, then all revoked (shuffle 2)
                    TestStimulationScenario.of(
                        updateX,
                        updateW,
                        updateZ,
                        updateY,
                        updateRevocationX,
                        updateRevocationW,
                        updateRevocationZ,
                        updateRevocationY,
                    ),
                    // All updates, then all revoked (shuffle 3)
                    TestStimulationScenario.of(
                        updateZ,
                        updateX,
                        updateW,
                        updateY,
                        updateRevocationZ,
                        updateRevocationX,
                        updateRevocationW,
                        updateRevocationY,
                    ),
                    // Interleaved (shuffle 1)
                    TestStimulationScenario.of(
                        updateW,
                        updateX,
                        updateRevocationX,
                        updateY,
                        updateRevocationW,
                        updateZ,
                        updateRevocationY,
                        updateRevocationZ,
                    ),
                    // Interleaved (shuffle 2)
                    TestStimulationScenario.of(
                        updateY,
                        updateRevocationY,
                        updateW,
                        updateZ,
                        updateRevocationZ,
                        updateX,
                        updateRevocationW,
                        updateRevocationX,
                    ),
                    // Interleaved (shuffle 3)
                    TestStimulationScenario.of(
                        updateZ,
                        updateW,
                        updateRevocationW,
                        updateX,
                        updateRevocationZ,
                        updateY,
                        updateRevocationX,
                        updateRevocationY,
                    ),
                    // Interleaved (shuffle 4)
                    TestStimulationScenario.of(
                        updateX,
                        updateY,
                        updateRevocationY,
                        updateZ,
                        updateRevocationX,
                        updateW,
                        updateRevocationZ,
                        updateRevocationW,
                    ),
                    // Interleaved (shuffle 5)
                    TestStimulationScenario.of(
                        updateW,
                        updateZ,
                        updateRevocationZ,
                        updateRevocationW,
                        updateX,
                        updateY,
                        updateRevocationY,
                        updateRevocationX,
                    ),
                )
        }

        /**
         * The dynamic inner source cells update, some (but not all) of these updates are corrected.
         */
        data object UpdateSomeCorrected : DynamicCellStimulationScenario() {
            private val updateX = TestInputCellStimulationTag.Update(
                inputTag = DynamicCellStimulationTag.DynamicCellX,
            )

            private val updateCorrectionX = TestInputCellStimulationTag.UpdateCorrection(
                inputTag = DynamicCellStimulationTag.DynamicCellX,
            )

            private val updateY = TestInputCellStimulationTag.Update(
                inputTag = DynamicCellStimulationTag.DynamicCellY,
            )

            private val updateZ = TestInputCellStimulationTag.Update(
                inputTag = DynamicCellStimulationTag.DynamicCellZ,
            )

            private val updateCorrectionZ = TestInputCellStimulationTag.UpdateCorrection(
                inputTag = DynamicCellStimulationTag.DynamicCellZ,
            )

            private val updateW = TestInputCellStimulationTag.Update(
                inputTag = DynamicCellStimulationTag.DynamicCellW,
            )

            override val dynamicCellXStimulationKind = DynamicInnerSourceCellStimulationKind.UpdatesCorrected
            override val dynamicCellYStimulationKind = DynamicInnerSourceCellStimulationKind.UpdatesEffectively
            override val dynamicCellZStimulationKind = DynamicInnerSourceCellStimulationKind.UpdatesCorrected
            override val dynamicCellWStimulationKind = DynamicInnerSourceCellStimulationKind.UpdatesEffectively

            override val dynamicCellStimulationScenarioBank: TestStimulationScenarioBank
                get() = TestStimulationScenarioBank.of(
                    // Each corrected immediately (in order, Y and W effective)
                    TestStimulationScenario.of(
                        updateW,
                        updateX,
                        updateCorrectionX,
                        updateY,
                        updateZ,
                        updateCorrectionZ,
                    ),
                    // Each corrected immediately (in reversed order, Y and W effective)
                    TestStimulationScenario.of(
                        updateZ,
                        updateCorrectionZ,
                        updateY,
                        updateX,
                        updateCorrectionX,
                        updateW,
                    ),
                    // Each corrected immediately (shuffled 1)
                    TestStimulationScenario.of(
                        updateY,
                        updateZ,
                        updateCorrectionZ,
                        updateX,
                        updateCorrectionX,
                        updateW,
                    ),
                    // Each corrected immediately (shuffled 2)
                    TestStimulationScenario.of(
                        updateX,
                        updateCorrectionX,
                        updateW,
                        updateZ,
                        updateCorrectionZ,
                        updateY,
                    ),
                    // Each corrected immediately (shuffled 3)
                    TestStimulationScenario.of(
                        updateW,
                        updateZ,
                        updateCorrectionZ,
                        updateY,
                        updateX,
                        updateCorrectionX,
                    ),
                    // All updates, then all corrected (in order)
                    TestStimulationScenario.of(
                        updateW,
                        updateX,
                        updateY,
                        updateZ,
                        updateCorrectionX,
                        updateCorrectionZ,
                    ),
                    // All updates, then all corrected (in reversed order)
                    TestStimulationScenario.of(
                        updateZ,
                        updateY,
                        updateX,
                        updateW,
                        updateCorrectionZ,
                        updateCorrectionX,
                    ),
                    // All updates, then all corrected (shuffle 1)
                    TestStimulationScenario.of(
                        updateY,
                        updateZ,
                        updateW,
                        updateX,
                        updateCorrectionZ,
                        updateCorrectionX,
                    ),
                    // All updates, then all corrected (shuffle 2)
                    TestStimulationScenario.of(
                        updateX,
                        updateW,
                        updateZ,
                        updateY,
                        updateCorrectionX,
                        updateCorrectionZ,
                    ),
                    // All updates, then all corrected (shuffle 3)
                    TestStimulationScenario.of(
                        updateZ,
                        updateX,
                        updateW,
                        updateY,
                        updateCorrectionZ,
                        updateCorrectionX,
                    ),
                    // Interleaved (shuffle 1)
                    TestStimulationScenario.of(
                        updateW,
                        updateX,
                        updateY,
                        updateZ,
                        updateCorrectionX,
                        updateCorrectionZ,
                    ),
                    // Interleaved (shuffle 2)
                    TestStimulationScenario.of(
                        updateY,
                        updateW,
                        updateZ,
                        updateCorrectionZ,
                        updateX,
                        updateCorrectionX,
                    ),
                    // Interleaved (shuffle 3)
                    TestStimulationScenario.of(
                        updateZ,
                        updateW,
                        updateX,
                        updateCorrectionZ,
                        updateY,
                        updateCorrectionX,
                    ),
                    // Interleaved (shuffle 4)
                    TestStimulationScenario.of(
                        updateX,
                        updateY,
                        updateZ,
                        updateCorrectionX,
                        updateW,
                        updateCorrectionZ,
                    ),
                    // Interleaved (shuffle 5)
                    TestStimulationScenario.of(
                        updateW,
                        updateZ,
                        updateCorrectionZ,
                        updateX,
                        updateY,
                        updateCorrectionX,
                    ),
                )
        }

        /**
         * The dynamic inner source cells update, all of these updates are corrected.
         */
        data object UpdateAllCorrected : DynamicCellStimulationScenario() {
            private val updateX = TestInputCellStimulationTag.Update(
                inputTag = DynamicCellStimulationTag.DynamicCellX,
            )

            private val updateCorrectionX = TestInputCellStimulationTag.UpdateCorrection(
                inputTag = DynamicCellStimulationTag.DynamicCellX,
            )

            private val updateY = TestInputCellStimulationTag.Update(
                inputTag = DynamicCellStimulationTag.DynamicCellY,
            )

            private val updateCorrectionY = TestInputCellStimulationTag.UpdateCorrection(
                inputTag = DynamicCellStimulationTag.DynamicCellY,
            )

            private val updateZ = TestInputCellStimulationTag.Update(
                inputTag = DynamicCellStimulationTag.DynamicCellZ,
            )

            private val updateCorrectionZ = TestInputCellStimulationTag.UpdateCorrection(
                inputTag = DynamicCellStimulationTag.DynamicCellZ,
            )

            private val updateW = TestInputCellStimulationTag.Update(
                inputTag = DynamicCellStimulationTag.DynamicCellW,
            )

            private val updateCorrectionW = TestInputCellStimulationTag.UpdateCorrection(
                inputTag = DynamicCellStimulationTag.DynamicCellW,
            )

            override val dynamicCellXStimulationKind = DynamicInnerSourceCellStimulationKind.UpdatesCorrected
            override val dynamicCellYStimulationKind = DynamicInnerSourceCellStimulationKind.UpdatesCorrected
            override val dynamicCellZStimulationKind = DynamicInnerSourceCellStimulationKind.UpdatesCorrected
            override val dynamicCellWStimulationKind = DynamicInnerSourceCellStimulationKind.UpdatesCorrected

            override val dynamicCellStimulationScenarioBank: TestStimulationScenarioBank
                get() = TestStimulationScenarioBank.of(
                    // Each corrected immediately (in order)
                    TestStimulationScenario.of(
                        updateW,
                        updateCorrectionW,
                        updateX,
                        updateCorrectionX,
                        updateY,
                        updateCorrectionY,
                        updateZ,
                        updateCorrectionZ,
                    ),
                    // Each corrected immediately (in reversed order)
                    TestStimulationScenario.of(
                        updateZ,
                        updateCorrectionZ,
                        updateY,
                        updateCorrectionY,
                        updateX,
                        updateCorrectionX,
                        updateW,
                        updateCorrectionW,
                    ),
                    // Each corrected immediately (shuffled 1)
                    TestStimulationScenario.of(
                        updateY,
                        updateCorrectionY,
                        updateZ,
                        updateCorrectionZ,
                        updateX,
                        updateCorrectionX,
                        updateW,
                        updateCorrectionW,
                    ),
                    // Each corrected immediately (shuffled 2)
                    TestStimulationScenario.of(
                        updateX,
                        updateCorrectionX,
                        updateW,
                        updateCorrectionW,
                        updateZ,
                        updateCorrectionZ,
                        updateY,
                        updateCorrectionY,
                    ),
                    // Each corrected immediately (shuffled 3)
                    TestStimulationScenario.of(
                        updateW,
                        updateCorrectionW,
                        updateZ,
                        updateCorrectionZ,
                        updateY,
                        updateCorrectionY,
                        updateX,
                        updateCorrectionX,
                    ),
                    // All updates, then all corrected (in order)
                    TestStimulationScenario.of(
                        updateW,
                        updateX,
                        updateY,
                        updateZ,
                        updateCorrectionW,
                        updateCorrectionX,
                        updateCorrectionY,
                        updateCorrectionZ,
                    ),
                    // All updates, then all corrected (in reversed order)
                    TestStimulationScenario.of(
                        updateZ,
                        updateY,
                        updateX,
                        updateW,
                        updateCorrectionZ,
                        updateCorrectionY,
                        updateCorrectionX,
                        updateCorrectionW,
                    ),
                    // All updates, then all corrected (shuffle 1)
                    TestStimulationScenario.of(
                        updateY,
                        updateZ,
                        updateW,
                        updateX,
                        updateCorrectionY,
                        updateCorrectionZ,
                        updateCorrectionW,
                        updateCorrectionX,
                    ),
                    // All updates, then all corrected (shuffle 2)
                    TestStimulationScenario.of(
                        updateX,
                        updateW,
                        updateZ,
                        updateY,
                        updateCorrectionX,
                        updateCorrectionW,
                        updateCorrectionZ,
                        updateCorrectionY,
                    ),
                    // All updates, then all corrected (shuffle 3)
                    TestStimulationScenario.of(
                        updateZ,
                        updateX,
                        updateW,
                        updateY,
                        updateCorrectionZ,
                        updateCorrectionX,
                        updateCorrectionW,
                        updateCorrectionY,
                    ),
                    // Interleaved (shuffle 1)
                    TestStimulationScenario.of(
                        updateW,
                        updateX,
                        updateCorrectionX,
                        updateY,
                        updateCorrectionW,
                        updateZ,
                        updateCorrectionY,
                        updateCorrectionZ,
                    ),
                    // Interleaved (shuffle 2)
                    TestStimulationScenario.of(
                        updateY,
                        updateCorrectionY,
                        updateW,
                        updateZ,
                        updateCorrectionZ,
                        updateX,
                        updateCorrectionW,
                        updateCorrectionX,
                    ),
                    // Interleaved (shuffle 3)
                    TestStimulationScenario.of(
                        updateZ,
                        updateW,
                        updateCorrectionW,
                        updateX,
                        updateCorrectionZ,
                        updateY,
                        updateCorrectionX,
                        updateCorrectionY,
                    ),
                    // Interleaved (shuffle 4)
                    TestStimulationScenario.of(
                        updateX,
                        updateY,
                        updateCorrectionY,
                        updateZ,
                        updateCorrectionX,
                        updateW,
                        updateCorrectionZ,
                        updateCorrectionW,
                    ),
                    // Interleaved (shuffle 5)
                    TestStimulationScenario.of(
                        updateW,
                        updateZ,
                        updateCorrectionZ,
                        updateCorrectionW,
                        updateX,
                        updateY,
                        updateCorrectionY,
                        updateCorrectionX,
                    ),
                )
        }

        /**
         * The dynamic inner source cells update, some of these updates are corrected and some are revoked.
         */
        data object UpdateSomeCorrectedSomeRevoked : DynamicCellStimulationScenario() {
            private val updateX = TestInputCellStimulationTag.Update(
                inputTag = DynamicCellStimulationTag.DynamicCellX,
            )

            private val updateY = TestInputCellStimulationTag.Update(
                inputTag = DynamicCellStimulationTag.DynamicCellY,
            )

            private val updateRevocationY = TestInputCellStimulationTag.UpdateRevocation(
                inputTag = DynamicCellStimulationTag.DynamicCellY,
            )

            private val updateZ = TestInputCellStimulationTag.Update(
                inputTag = DynamicCellStimulationTag.DynamicCellZ,
            )

            private val updateCorrectionZ = TestInputCellStimulationTag.UpdateCorrection(
                inputTag = DynamicCellStimulationTag.DynamicCellZ,
            )

            private val updateW = TestInputCellStimulationTag.Update(
                inputTag = DynamicCellStimulationTag.DynamicCellW,
            )

            private val updateCorrectionW = TestInputCellStimulationTag.UpdateCorrection(
                inputTag = DynamicCellStimulationTag.DynamicCellW,
            )

            override val dynamicCellXStimulationKind = DynamicInnerSourceCellStimulationKind.UpdatesEffectively
            override val dynamicCellYStimulationKind = DynamicInnerSourceCellStimulationKind.UpdatesRevoked
            override val dynamicCellZStimulationKind = DynamicInnerSourceCellStimulationKind.UpdatesCorrected
            override val dynamicCellWStimulationKind = DynamicInnerSourceCellStimulationKind.UpdatesCorrected

            override val dynamicCellStimulationScenarioBank: TestStimulationScenarioBank
                get() = TestStimulationScenarioBank.of(
                    // Each revoked/corrected immediately (in order, X effective)
                    TestStimulationScenario.of(
                        updateW,
                        updateCorrectionW,
                        updateX,
                        updateY,
                        updateRevocationY,
                        updateZ,
                        updateCorrectionZ,
                    ),
                    // Each revoked/corrected immediately (in reversed order, X effective)
                    TestStimulationScenario.of(
                        updateZ,
                        updateCorrectionZ,
                        updateY,
                        updateRevocationY,
                        updateX,
                        updateW,
                        updateCorrectionW,
                    ),
                    // Each revoked/corrected immediately (shuffled 1)
                    TestStimulationScenario.of(
                        updateY,
                        updateRevocationY,
                        updateZ,
                        updateCorrectionZ,
                        updateX,
                        updateW,
                        updateCorrectionW,
                    ),
                    // Each revoked/corrected immediately (shuffled 2)
                    TestStimulationScenario.of(
                        updateX,
                        updateW,
                        updateCorrectionW,
                        updateZ,
                        updateCorrectionZ,
                        updateY,
                        updateRevocationY,
                    ),
                    // Each revoked/corrected immediately (shuffled 3)
                    TestStimulationScenario.of(
                        updateW,
                        updateCorrectionW,
                        updateZ,
                        updateCorrectionZ,
                        updateY,
                        updateRevocationY,
                        updateX,
                    ),
                    // All updates, then all revoked/corrected (in order)
                    TestStimulationScenario.of(
                        updateW,
                        updateX,
                        updateY,
                        updateZ,
                        updateCorrectionW,
                        updateRevocationY,
                        updateCorrectionZ,
                    ),
                    // All updates, then all revoked/corrected (in reversed order)
                    TestStimulationScenario.of(
                        updateZ,
                        updateY,
                        updateX,
                        updateW,
                        updateCorrectionZ,
                        updateRevocationY,
                        updateCorrectionW,
                    ),
                    // All updates, then all revoked/corrected (shuffle 1)
                    TestStimulationScenario.of(
                        updateY,
                        updateZ,
                        updateW,
                        updateX,
                        updateRevocationY,
                        updateCorrectionZ,
                        updateCorrectionW,
                    ),
                    // All updates, then all revoked/corrected (shuffle 2)
                    TestStimulationScenario.of(
                        updateX,
                        updateW,
                        updateZ,
                        updateY,
                        updateCorrectionW,
                        updateCorrectionZ,
                        updateRevocationY,
                    ),
                    // All updates, then all revoked/corrected (shuffle 3)
                    TestStimulationScenario.of(
                        updateZ,
                        updateX,
                        updateW,
                        updateY,
                        updateCorrectionZ,
                        updateCorrectionW,
                        updateRevocationY,
                    ),
                    // Interleaved (shuffle 1)
                    TestStimulationScenario.of(
                        updateW,
                        updateX,
                        updateY,
                        updateCorrectionW,
                        updateZ,
                        updateRevocationY,
                        updateCorrectionZ,
                    ),
                    // Interleaved (shuffle 2)
                    TestStimulationScenario.of(
                        updateY,
                        updateRevocationY,
                        updateW,
                        updateZ,
                        updateCorrectionZ,
                        updateX,
                        updateCorrectionW,
                    ),
                    // Interleaved (shuffle 3)
                    TestStimulationScenario.of(
                        updateZ,
                        updateW,
                        updateCorrectionW,
                        updateX,
                        updateCorrectionZ,
                        updateY,
                        updateRevocationY,
                    ),
                    // Interleaved (shuffle 4)
                    TestStimulationScenario.of(
                        updateX,
                        updateY,
                        updateRevocationY,
                        updateZ,
                        updateW,
                        updateCorrectionZ,
                        updateCorrectionW,
                    ),
                    // Interleaved (shuffle 5)
                    TestStimulationScenario.of(
                        updateW,
                        updateZ,
                        updateCorrectionZ,
                        updateCorrectionW,
                        updateX,
                        updateY,
                        updateRevocationY,
                    ),
                )
        }

        companion object {
            const val InitialSymbol = '!'
            const val IntermediateSymbol = '@'
            const val FinalSymbol = '#'

            val allEffective: List<DynamicCellStimulationScenario> = listOf(
                Update,
                UpdateSomeRevoked,
                UpdateAllRevoked,
                UpdateSomeCorrected,
                UpdateAllCorrected,
                UpdateSomeCorrectedSomeRevoked,
            )
        }

        val expectedNewSymbolX: Char
            get() = dynamicCellXStimulationKind.expectedNewSymbol

        val expectedNewSymbolY: Char
            get() = dynamicCellYStimulationKind.expectedNewSymbol

        val expectedNewSymbolZ: Char
            get() = dynamicCellZStimulationKind.expectedNewSymbol

        val expectedNewSymbolW: Char
            get() = dynamicCellWStimulationKind.expectedNewSymbol

        fun buildTestStimulationMap(
            innerCellMapping: InnerCellMapping,
        ): TestStimulationMap = TestStimulationMap.union(
            dynamicCellXStimulationKind.buildTestStimulationMap(
                sourceCell = innerCellMapping.sourceDynamicCellX,
                tag = DynamicCellStimulationTag.DynamicCellX,
                label = innerCellMapping.sourceDynamicCellXLabel,
            ),
            dynamicCellYStimulationKind.buildTestStimulationMap(
                sourceCell = innerCellMapping.sourceDynamicCellY,
                tag = DynamicCellStimulationTag.DynamicCellY,
                label = innerCellMapping.sourceDynamicCellYLabel,
            ),
            dynamicCellZStimulationKind.buildTestStimulationMap(
                sourceCell = innerCellMapping.sourceDynamicCellZ,
                tag = DynamicCellStimulationTag.DynamicCellZ,
                label = innerCellMapping.sourceDynamicCellZLabel,
            ),
            dynamicCellWStimulationKind.buildTestStimulationMap(
                sourceCell = innerCellMapping.sourceDynamicCellW,
                tag = DynamicCellStimulationTag.DynamicCellW,
                label = innerCellMapping.sourceDynamicCellWLabel,
            ),
        )

        abstract val dynamicCellXStimulationKind: DynamicInnerSourceCellStimulationKind
        abstract val dynamicCellYStimulationKind: DynamicInnerSourceCellStimulationKind
        abstract val dynamicCellZStimulationKind: DynamicInnerSourceCellStimulationKind
        abstract val dynamicCellWStimulationKind: DynamicInnerSourceCellStimulationKind

        open val dynamicCellStimulationScenarioBank: TestStimulationScenarioBank
            // By default, build all combinations of the dynamic cell stimulations
            get() = TestStimulationScenarioBank.mixAll(
                dynamicCellXStimulationKind.toStimulationScenario(
                    tag = DynamicCellStimulationTag.DynamicCellX,
                ),
                dynamicCellYStimulationKind.toStimulationScenario(
                    tag = DynamicCellStimulationTag.DynamicCellY,
                ),
                dynamicCellZStimulationKind.toStimulationScenario(
                    tag = DynamicCellStimulationTag.DynamicCellZ,
                ),
                dynamicCellWStimulationKind.toStimulationScenario(
                    tag = DynamicCellStimulationTag.DynamicCellW,
                ),
            )
    }

    @Test
    fun test_passiveSample() {
        val sourceCell1 = TestInputCell(initialValue = "1!")
        val sourceCell2 = TestInputCell(initialValue = "2!")
        val sourceCell3 = TestInputCell(initialValue = "3!")

        val sourceReactiveBag = TestInputReactiveBag(
            initialTaggedElements = taggedBagOf(
                InnerCellBagTag.InnerCell1 to sourceCell1,
                InnerCellBagTag.InnerCell2 to sourceCell2,
                InnerCellBagTag.InnerCell3 to sourceCell3,
            ),
        )

        val subjectReactiveBag = sourceReactiveBag.fuse()

        ReactiveBag_sampling_testUtils.executeSamplingTransaction(
            subjectReactiveBag = subjectReactiveBag,
            expectedSubjectContent = ReactiveBag_expectations_testUtils.expectStableTaggedContent(
                expectedTaggedElements = taggedBagOf(
                    InnerCellBagTag.InnerCell1 to "1!",
                    InnerCellBagTag.InnerCell2 to "2!",
                    InnerCellBagTag.InnerCell3 to "3!",
                ),
            ),
        )
    }

    @Test
    fun test_sourceBagChangesEffectively_additionsOnly_only() {
        test_sourceBagChanges_only_matrix(
            sourceBagStimulationScenario = SourceBagStimulationScenario.ChangesEffectively,
            sourceBagChangeVariety = SourceBagChangeVariety.AdditionsOnly,
        )
    }

    @Test
    fun test_sourceBagChangesEffectively_removalsOnly_only() {
        test_sourceBagChanges_only_matrix(
            sourceBagStimulationScenario = SourceBagStimulationScenario.ChangesEffectively,
            sourceBagChangeVariety = SourceBagChangeVariety.RemovalsOnly,
        )
    }

    @Test
    fun test_sourceBagChangesEffectively_replacementsOnly_only() {
        test_sourceBagChanges_only_matrix(
            sourceBagStimulationScenario = SourceBagStimulationScenario.ChangesEffectively,
            sourceBagChangeVariety = SourceBagChangeVariety.ReplacementsOnly,
        )
    }

    @Test
    fun test_sourceBagChangesEffectively_mixed_only() {
        test_sourceBagChanges_only_matrix(
            sourceBagStimulationScenario = SourceBagStimulationScenario.ChangesEffectively,
            sourceBagChangeVariety = SourceBagChangeVariety.Mixed,
        )
    }

    private fun test_sourceBagChanges_only_matrix(
        sourceBagStimulationScenario: SourceBagStimulationScenario,
        sourceBagChangeVariety: SourceBagChangeVariety,
    ) {
        val stimulationScenarioBank = TestStimulationScenarioBank.mixAll(
            sourceBagStimulationScenario.outerSourceBagStimulationScenario,
        )

        stimulationScenarioBank.distribute(SuitableSlotCount).forEach { slottedStimulationScenario ->
            sourceBagStimulationScenario.test(
                sourceBagChangeVariety = sourceBagChangeVariety,
                dynamicCellStimulationScenario = DynamicCellStimulationScenario.None,
                slottedStimulationScenario = slottedStimulationScenario,
            )
        }
    }

    @Test
    fun test_dynamicCellsUpdate_only_matrix() {
        DynamicCellStimulationScenario.allEffective.forEach { dynamicSourceCellStimulationStrategy ->
            val stimulationScenarioBank = dynamicSourceCellStimulationStrategy.dynamicCellStimulationScenarioBank

            val foo = stimulationScenarioBank.stimulationScenarios.count()

            stimulationScenarioBank.distribute(SuitableSlotCount).forEach { slottedStimulationScenario ->
                test_dynamicCellsUpdate_only(
                    dynamicCellStimulationScenario = dynamicSourceCellStimulationStrategy,
                    slottedStimulationScenario = slottedStimulationScenario,
                )
            }
        }
    }

    private fun test_dynamicCellsUpdate_only(
        dynamicCellStimulationScenario: DynamicCellStimulationScenario,
        slottedStimulationScenario: SuitableTestSlottedStimulationScenario,
    ) {
        val sourceInputCell1 = TestInputCell(initialValue = "1!")
        val sourceInputCell2 = TestInputCell(initialValue = "2!")
        val sourceInputCell3 = TestInputCell(initialValue = "3!")
        val sourceInputCell4 = TestInputCell(initialValue = "4!")
        val sourceInputCell5 = TestInputCell(initialValue = "5!")

        val sourceReactiveBag = TestInputReactiveBag(
            initialTaggedElements = taggedBagOf(
                InnerCellBagTag.InnerCell1 to sourceInputCell1,
                InnerCellBagTag.InnerCell2 to sourceInputCell2,
                InnerCellBagTag.InnerCell3 to sourceInputCell3,
                InnerCellBagTag.InnerCell4 to sourceInputCell4,
                InnerCellBagTag.InnerCell5 to sourceInputCell5,
            ),
        )

        val subjectReactiveBag = sourceReactiveBag.fuse()

        val dynamicCellStimulationMap = dynamicCellStimulationScenario.buildTestStimulationMap(
            innerCellMapping = object : InnerCellMapping {
                override val sourceDynamicCellX = sourceInputCell1
                override val sourceDynamicCellXLabel = "1"

                override val sourceDynamicCellY = sourceInputCell2
                override val sourceDynamicCellYLabel = "2"

                override val sourceDynamicCellZ = sourceInputCell4
                override val sourceDynamicCellZLabel = "4"

                override val sourceDynamicCellW = sourceInputCell5
                override val sourceDynamicCellWLabel = "5"
            },
        )

        val expectedOldTaggedElements = taggedBagOf(
            InnerCellBagTag.InnerCell1 to "1!",
            InnerCellBagTag.InnerCell2 to "2!",
            InnerCellBagTag.InnerCell3 to "3!",
            InnerCellBagTag.InnerCell4 to "4!",
            InnerCellBagTag.InnerCell5 to "5!",
        )

        val expectedNewTaggedElements = taggedBagOf(
            InnerCellBagTag.InnerCell1 to "1${dynamicCellStimulationScenario.expectedNewSymbolX}",
            InnerCellBagTag.InnerCell2 to "2${dynamicCellStimulationScenario.expectedNewSymbolY}",
            InnerCellBagTag.InnerCell3 to "3!",
            InnerCellBagTag.InnerCell4 to "4${dynamicCellStimulationScenario.expectedNewSymbolZ}",
            InnerCellBagTag.InnerCell5 to "5${dynamicCellStimulationScenario.expectedNewSymbolW}",
        )

        ReactiveBag_reaction_testUtils.executeReactionTransaction(
            subjectReactiveBag = subjectReactiveBag,
            slottedInputStimulation = dynamicCellStimulationMap.bind(slottedStimulationScenario),
            expectedSubjectElementTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = expectedOldTaggedElements,
                expectedNewTaggedElements = expectedNewTaggedElements,
            )
        )
    }

    @Test
    fun test_sourceBagChangesEffectively_additionsOnly_dynamicCellsUpdate() {
        test_sourceBagChanges_dynamicCellsUpdate_matrix(
            sourceBagStimulationScenario = SourceBagStimulationScenario.ChangesEffectively,
            sourceBagChangeVariety = SourceBagChangeVariety.AdditionsOnly,
        )
    }

    @Test
    fun test_sourceBagChangesEffectively_removalsOnly_dynamicCellsUpdate() {
        test_sourceBagChanges_dynamicCellsUpdate_matrix(
            sourceBagStimulationScenario = SourceBagStimulationScenario.ChangesEffectively,
            sourceBagChangeVariety = SourceBagChangeVariety.RemovalsOnly,
        )
    }

    @Test
    fun test_sourceBagChangesEffectively_replacementsOnly_dynamicCellsUpdate() {
        test_sourceBagChanges_dynamicCellsUpdate_matrix(
            sourceBagStimulationScenario = SourceBagStimulationScenario.ChangesEffectively,
            sourceBagChangeVariety = SourceBagChangeVariety.ReplacementsOnly,
        )
    }

    @Test
    fun test_sourceBagChangesEffectively_mixed_dynamicCellsUpdate() {
        test_sourceBagChanges_dynamicCellsUpdate_matrix(
            sourceBagStimulationScenario = SourceBagStimulationScenario.ChangesEffectively,
            sourceBagChangeVariety = SourceBagChangeVariety.Mixed,
        )
    }

    @Test
    fun test_sourceBagChangesRevoked_additionsOnly_dynamicCellsUpdate() {
        test_sourceBagChanges_dynamicCellsUpdate_matrix(
            sourceBagStimulationScenario = SourceBagStimulationScenario.ChangesRevoked,
            sourceBagChangeVariety = SourceBagChangeVariety.AdditionsOnly,
        )
    }

    @Test
    fun test_sourceBagChangesRevoked_removalsOnly_dynamicCellsUpdate() {
        test_sourceBagChanges_dynamicCellsUpdate_matrix(
            sourceBagStimulationScenario = SourceBagStimulationScenario.ChangesRevoked,
            sourceBagChangeVariety = SourceBagChangeVariety.RemovalsOnly,
        )
    }

    @Test
    fun test_sourceBagChangesRevoked_replacementsOnly_dynamicCellsUpdate() {
        test_sourceBagChanges_dynamicCellsUpdate_matrix(
            sourceBagStimulationScenario = SourceBagStimulationScenario.ChangesRevoked,
            sourceBagChangeVariety = SourceBagChangeVariety.ReplacementsOnly,
        )
    }

    @Test
    fun test_sourceBagChangesRevoked_mixed_dynamicCellsUpdate() {
        test_sourceBagChanges_dynamicCellsUpdate_matrix(
            sourceBagStimulationScenario = SourceBagStimulationScenario.ChangesRevoked,
            sourceBagChangeVariety = SourceBagChangeVariety.Mixed,
        )
    }

    @Test
    fun test_sourceBagChangesCorrected_additionsOnly_dynamicCellsUpdate() {
        test_sourceBagChanges_dynamicCellsUpdate_matrix(
            sourceBagStimulationScenario = SourceBagStimulationScenario.ChangesCorrected,
            sourceBagChangeVariety = SourceBagChangeVariety.AdditionsOnly,
        )
    }

    @Test
    fun test_sourceBagChangesCorrected_removalsOnly_dynamicCellsUpdate() {
        test_sourceBagChanges_dynamicCellsUpdate_matrix(
            sourceBagStimulationScenario = SourceBagStimulationScenario.ChangesCorrected,
            sourceBagChangeVariety = SourceBagChangeVariety.RemovalsOnly,
        )
    }

    @Test
    fun test_sourceBagChangesCorrected_replacementsOnly_dynamicCellsUpdate() {
        test_sourceBagChanges_dynamicCellsUpdate_matrix(
            sourceBagStimulationScenario = SourceBagStimulationScenario.ChangesCorrected,
            sourceBagChangeVariety = SourceBagChangeVariety.ReplacementsOnly,
        )
    }

    @Test
    fun test_sourceBagChangesCorrected_mixed_dynamicCellsUpdate() {
        test_sourceBagChanges_dynamicCellsUpdate_matrix(
            sourceBagStimulationScenario = SourceBagStimulationScenario.ChangesCorrected,
            sourceBagChangeVariety = SourceBagChangeVariety.Mixed,
        )
    }

    private fun test_sourceBagChanges_dynamicCellsUpdate_matrix(
        sourceBagStimulationScenario: SourceBagStimulationScenario,
        sourceBagChangeVariety: SourceBagChangeVariety,
    ) {
        DynamicCellStimulationScenario.allEffective.forEach { dynamicSourceCellStimulationStrategy ->
            val outerSourceBagStimulationScenario = sourceBagStimulationScenario.outerSourceBagStimulationScenario
            val dynamicCellStimulationScenarioBank = dynamicSourceCellStimulationStrategy.dynamicCellStimulationScenarioBank

            val stimulationScenarioBank = outerSourceBagStimulationScenario.toBank().mixWith(dynamicCellStimulationScenarioBank)
            val slottedStimulationScenarioBank = stimulationScenarioBank.distribute(SuitableSlotCount)

            slottedStimulationScenarioBank.forEach { slottedStimulationScenario ->
                sourceBagStimulationScenario.test(
                    sourceBagChangeVariety = sourceBagChangeVariety,
                    dynamicCellStimulationScenario = dynamicSourceCellStimulationStrategy,
                    slottedStimulationScenario = slottedStimulationScenario,
                )
            }
        }
    }
}
