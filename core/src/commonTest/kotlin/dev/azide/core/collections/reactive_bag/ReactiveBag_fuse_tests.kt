package dev.azide.core.collections.reactive_bag

import dev.azide.core.collections.fuse
import dev.azide.core.collections.reactive_bag.ReactiveBag_fuse_testUtils.FuseEntryTag
import dev.azide.core.impl.collections.reactive_bag.taggedBagOf
import dev.azide.core.test_utils.TestSequentialStimulationSet
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.cell.TestInputCell
import dev.azide.core.test_utils.collections.reactive_bag.ReactiveBag_expectations_testUtils
import dev.azide.core.test_utils.collections.reactive_bag.TestInputReactiveBag
import dev.azide.core.test_utils.collections.reactive_list.ReactiveBag_sampling_testUtils
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.generic.TestSubjectHealthCheckStrategy
import dev.azide.core.test_utils.generic.generic_reaction_testUtils
import kotlin.test.Test

@Suppress("ClassName")
class ReactiveBag_fuse_tests {
    @Test
    fun test_passiveSampling() {
        val initialInputCell1 = TestInputCell(initialValue = "#1a")
        val initialInputCell2 = TestInputCell(initialValue = "#2")
        val initialInputCell3 = TestInputCell(initialValue = "#3a")

        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = taggedBagOf(
                FuseEntryTag.Tag1 to initialInputCell1,
                FuseEntryTag.Tag2 to initialInputCell2,
                FuseEntryTag.Tag3 to initialInputCell3,
            ),
        )

        val subjectReactiveBag = inputReactiveBag.fuse()

        ReactiveBag_sampling_testUtils.testPassiveSampling(
            subjectReactiveBag = subjectReactiveBag,
            expectedSubjectContent = ReactiveBag_expectations_testUtils.expectStableTaggedContent(
                expectedTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1a",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "#3a",
                ),
            ),
        )
    }

    @Test
    fun test_initialCellsUpdate_deactivated() {
        test_initialCellsUpdate(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_initialCellsUpdate_keptAlive() {
        test_initialCellsUpdate(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * Some of the cells initially present in the input bag update.
     */
    private fun test_initialCellsUpdate(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val initialInputCell1 = TestInputCell(initialValue = "#1a")
        val initialInputCell2 = TestInputCell(initialValue = "#2")
        val initialInputCell3 = TestInputCell(initialValue = "#3a")

        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = taggedBagOf(
                FuseEntryTag.Tag1 to initialInputCell1,
                FuseEntryTag.Tag2 to initialInputCell2,
                FuseEntryTag.Tag3 to initialInputCell3,
            ),
        )

        val subjectReactiveBag = inputReactiveBag.fuse()

        ReactiveBag_fuse_testUtils.testReaction(
            inputReactiveBag = inputReactiveBag,
            inputCellByLabel = mapOf(
                "initial 1" to initialInputCell1,
                "initial 2" to initialInputCell2,
                "initial 3" to initialInputCell3,
            ),
            subjectReactiveBag = subjectReactiveBag,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combineInArbitraryOrder(
                    setOf(
                        initialInputCell1.update(newValue = "#1b"),
                        initialInputCell3.update(newValue = "#3b"),
                    ),
                ),
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1a",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "#3a",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1b",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "#3b",
                ),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    @Test
    fun test_initialCellsUpdate_duplicatesIncluded_deactivated() {
        test_initialCellsUpdate_duplicatesIncluded(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_initialCellsUpdate_duplicatesIncluded_keptAlive() {
        test_initialCellsUpdate_duplicatesIncluded(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * Some of the cells initially present in the input bag update. Some of the updated input cells share a single cell
     * object instance.
     */
    private fun test_initialCellsUpdate_duplicatesIncluded(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val initialInputCell1 = TestInputCell(initialValue = "#1a")
        val initialInputCell2 = TestInputCell(initialValue = "#2")
        val initialInputCell3 = TestInputCell(initialValue = "#3a")

        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = taggedBagOf(
                FuseEntryTag.Tag1 to initialInputCell1,
                FuseEntryTag.Tag2 to initialInputCell2,
                FuseEntryTag.Tag3 to initialInputCell3,
                FuseEntryTag.Tag4 to initialInputCell1, // Duplicate
            ),
        )

        val subjectReactiveBag = inputReactiveBag.fuse()

        ReactiveBag_fuse_testUtils.testReaction(
            inputReactiveBag = inputReactiveBag,
            inputCellByLabel = mapOf(
                "initial 1" to initialInputCell1,
                "initial 2" to initialInputCell2,
                "initial 3" to initialInputCell3,
            ),
            subjectReactiveBag = subjectReactiveBag,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combineInArbitraryOrder(
                    setOf(
                        initialInputCell1.update(newValue = "#1b"),
                        initialInputCell3.update(newValue = "#3b"),
                    ),
                ),
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1a",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "#3a",
                    FuseEntryTag.Tag4 to "#1a",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1b",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "#3b",
                    FuseEntryTag.Tag4 to "#1b",
                ),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    @Test
    fun test_bagChanges_addedCells_deactivated() {
        test_bagChanges_addedCells(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChanges_addedCells_keptAlive() {
        test_bagChanges_addedCells(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The input bag changes. Some tags/cells are added.
     */
    private fun test_bagChanges_addedCells(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val initialInputCell1 = TestInputCell(initialValue = "#1")
        val initialInputCell2 = TestInputCell(initialValue = "#2")

        val addedInputCell1 = TestInputCell(initialValue = "+1")
        val addedInputCell2 = TestInputCell(initialValue = "+2")

        val initialTaggedInputCells = taggedBagOf(
            FuseEntryTag.Tag1 to initialInputCell1,
            FuseEntryTag.Tag2 to initialInputCell2,
        )

        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = initialTaggedInputCells,
        )

        val subjectReactiveBag = inputReactiveBag.fuse()

        ReactiveBag_fuse_testUtils.testReaction(
            inputReactiveBag = inputReactiveBag,
            inputCellByLabel = mapOf(
                "initial 1" to initialInputCell1,
                "initial 2" to initialInputCell2,
                "added 1" to addedInputCell1,
                "added 2" to addedInputCell2,
            ),
            subjectReactiveBag = subjectReactiveBag,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = inputReactiveBag.change(
                    TestInputReactiveBag.ChangeDescription(
                        addedElementByTag = mapOf(
                            FuseEntryTag.Tag3 to addedInputCell1,
                            FuseEntryTag.Tag4 to addedInputCell2,
                        ),
                    ),
                ),
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "+1",
                    FuseEntryTag.Tag4 to "+2",
                ),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    @Test
    fun test_bagChanges_addedCells_addedDuplicates_deactivated() {
        test_bagChanges_addedCells_addedDuplicates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChanges_addedCells_addedDuplicates_keptAlive() {
        test_bagChanges_addedCells_addedDuplicates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The input bag changes. Some tags/cells are added. Some of the added cells share a single cell object instance with
     * the initial cells.
     */
    private fun test_bagChanges_addedCells_addedDuplicates(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val initialInputCell1 = TestInputCell(initialValue = "#1")
        val initialInputCell2 = TestInputCell(initialValue = "#2")
        val sharedCell = TestInputCell(initialValue = "shared")

        val initialTaggedInputCells = taggedBagOf(
            FuseEntryTag.Tag1 to initialInputCell1,
            FuseEntryTag.Tag2 to initialInputCell2,
            FuseEntryTag.Tag3 to sharedCell,
        )

        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = initialTaggedInputCells,
        )

        val subjectReactiveBag = inputReactiveBag.fuse()

        ReactiveBag_fuse_testUtils.testReaction(
            inputReactiveBag = inputReactiveBag,
            inputCellByLabel = mapOf(
                "initial 1" to initialInputCell1,
                "initial 2" to initialInputCell2,
                "shared" to sharedCell,
            ),
            subjectReactiveBag = subjectReactiveBag,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = inputReactiveBag.change(
                    TestInputReactiveBag.ChangeDescription(
                        addedElementByTag = mapOf(
                            FuseEntryTag.Tag4 to sharedCell,
                        ),
                    ),
                ),
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "shared",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "shared",
                    FuseEntryTag.Tag4 to "shared",
                ),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    @Test
    fun test_bagChanges_addedCells_addedCellsUpdate_deactivated() {
        test_bagChanges_addedCells_addedCellsUpdate(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChanges_addedCells_addedCellsUpdate_keptAlive() {
        test_bagChanges_addedCells_addedCellsUpdate(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The input bag changes. Some tags/cells are added. Some of them update.
     */
    private fun test_bagChanges_addedCells_addedCellsUpdate(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val initialInputCell1 = TestInputCell(initialValue = "#1")
        val initialInputCell2 = TestInputCell(initialValue = "#2")

        val addedInputCell1 = TestInputCell(initialValue = "+1a")
        val addedInputCell2 = TestInputCell(initialValue = "+2a")

        val initialTaggedInputCells = taggedBagOf(
            FuseEntryTag.Tag1 to initialInputCell1,
            FuseEntryTag.Tag2 to initialInputCell2,
        )

        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = initialTaggedInputCells,
        )

        val subjectReactiveBag = inputReactiveBag.fuse()

        ReactiveBag_fuse_testUtils.testReaction(
            inputReactiveBag = inputReactiveBag,
            inputCellByLabel = mapOf(
                "initial 1" to initialInputCell1,
                "initial 2" to initialInputCell2,
                "added 1" to addedInputCell1,
                "added 2" to addedInputCell2,
            ),
            subjectReactiveBag = subjectReactiveBag,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combineInArbitraryOrder(
                    setOf(
                        inputReactiveBag.change(
                            TestInputReactiveBag.ChangeDescription(
                                addedElementByTag = mapOf(
                                    FuseEntryTag.Tag3 to addedInputCell1,
                                    FuseEntryTag.Tag4 to addedInputCell2,
                                ),
                            ),
                        ),
                        addedInputCell1.update(newValue = "+1b"),
                        addedInputCell2.update(newValue = "+2b"),
                    ),
                ),
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "+1b",
                    FuseEntryTag.Tag4 to "+2b",
                ),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    @Test
    fun test_bagChanges_addedCells_addedDuplicates_addedCellsUpdate_deactivated() {
        test_bagChanges_addedCells_addedDuplicates_addedCellsUpdate(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChanges_addedCells_addedDuplicates_addedCellsUpdate_keptAlive() {
        test_bagChanges_addedCells_addedDuplicates_addedCellsUpdate(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The input bag changes. Some tags/cells are added. Some of the added cells share a single cell object instance
     * with the initial cells. Some of them update.
     */
    private fun test_bagChanges_addedCells_addedDuplicates_addedCellsUpdate(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val initialInputCell1 = TestInputCell(initialValue = "#1")
        val initialInputCell2 = TestInputCell(initialValue = "#2")
        val sharedCell = TestInputCell(initialValue = "shared-a")

        val initialTaggedInputCells = taggedBagOf(
            FuseEntryTag.Tag1 to initialInputCell1,
            FuseEntryTag.Tag2 to initialInputCell2,
            FuseEntryTag.Tag3 to sharedCell,
        )

        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = initialTaggedInputCells,
        )

        val subjectReactiveBag = inputReactiveBag.fuse()

        ReactiveBag_fuse_testUtils.testReaction(
            inputReactiveBag = inputReactiveBag,
            inputCellByLabel = mapOf(
                "initial 1" to initialInputCell1,
                "initial 2" to initialInputCell2,
                "shared" to sharedCell,
            ),
            subjectReactiveBag = subjectReactiveBag,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combineInArbitraryOrder(
                    setOf(
                        inputReactiveBag.change(
                            TestInputReactiveBag.ChangeDescription(
                                addedElementByTag = mapOf(
                                    FuseEntryTag.Tag4 to sharedCell,
                                ),
                            ),
                        ),
                        sharedCell.update(newValue = "shared-b"),
                    ),
                ),
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "shared-a",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "shared-b",
                    FuseEntryTag.Tag4 to "shared-b",
                ),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    @Test
    fun test_bagChanges_addedCells_bagChangeRevoked_addedCellUpdateRevoked_deactivated() {
        test_bagChanges_addedCells_bagChangeRevoked_addedCellUpdateRevoked(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChanges_addedCells_bagChangeRevoked_addedCellUpdateRevoked_keptAlive() {
        test_bagChanges_addedCells_bagChangeRevoked_addedCellUpdateRevoked(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The input bag changes. Some tags/cells are added. The bag change is revoked; after the change is revoked,
     * one of the added cells revokes its updates.
     */
    private fun test_bagChanges_addedCells_bagChangeRevoked_addedCellUpdateRevoked(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val initialInputCell1 = TestInputCell(initialValue = "#1")
        val initialInputCell2 = TestInputCell(initialValue = "#2")

        val addedInputCell1 = TestInputCell(initialValue = "+1a")
        val addedInputCell2 = TestInputCell(initialValue = "+2")

        val initialTaggedInputCells = taggedBagOf(
            FuseEntryTag.Tag1 to initialInputCell1,
            FuseEntryTag.Tag2 to initialInputCell2,
        )

        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = initialTaggedInputCells,
        )

        val subjectReactiveBag = inputReactiveBag.fuse()

        ReactiveBag_fuse_testUtils.testReaction(
            inputReactiveBag = inputReactiveBag,
            inputCellByLabel = mapOf(
                "initial 1" to initialInputCell1,
                "initial 2" to initialInputCell2,
                "added 1" to addedInputCell1,
                "added 2" to addedInputCell2,
            ),
            subjectReactiveBag = subjectReactiveBag,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestSequentialStimulationSet(
                    setOf(
                        TestStimulation.combineInProvidedOrder(
                            addedInputCell1.update(newValue = "+1b"),
                            addedInputCell1.revokeUpdate(),
                        ),
                        TestStimulation.combineInProvidedOrder(
                            inputReactiveBag.change(
                                TestInputReactiveBag.ChangeDescription(
                                    addedElementByTag = mapOf(
                                        FuseEntryTag.Tag3 to addedInputCell1,
                                        FuseEntryTag.Tag4 to addedInputCell2,
                                    ),
                                ),
                            ),
                            inputReactiveBag.revokeChange(),
                        ),
                    ),
                ).determinizeArbitrarily(),
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectNoTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                ),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    @Test
    fun test_bagChanges_addedCells_bagChangeRevoked_initialCellUpdate_deactivated() {
        test_bagChanges_addedCells_bagChangeRevoked_initialCellUpdate(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChanges_addedCells_bagChangeRevoked_initialCellUpdate_keptAlive() {
        test_bagChanges_addedCells_bagChangeRevoked_initialCellUpdate(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The input bag changes. Some tags/cells are added. The bag change is revoked; after the change is revoked,
     * one of the initial cells updates.
     */
    private fun test_bagChanges_addedCells_bagChangeRevoked_initialCellUpdate(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val initialInputCell1 = TestInputCell(initialValue = "#1a")
        val initialInputCell2 = TestInputCell(initialValue = "#2")

        val addedInputCell1 = TestInputCell(initialValue = "+1")
        val addedInputCell2 = TestInputCell(initialValue = "+2")

        val initialTaggedInputCells = taggedBagOf(
            FuseEntryTag.Tag1 to initialInputCell1,
            FuseEntryTag.Tag2 to initialInputCell2,
        )

        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = initialTaggedInputCells,
        )

        val subjectReactiveBag = inputReactiveBag.fuse()

        ReactiveBag_fuse_testUtils.testReaction(
            inputReactiveBag = inputReactiveBag,
            inputCellByLabel = mapOf(
                "initial 1" to initialInputCell1,
                "initial 2" to initialInputCell2,
                "added 1" to addedInputCell1,
                "added 2" to addedInputCell2,
            ),
            subjectReactiveBag = subjectReactiveBag,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    inputReactiveBag.change(
                        TestInputReactiveBag.ChangeDescription(
                            addedElementByTag = mapOf(
                                FuseEntryTag.Tag3 to addedInputCell1,
                                FuseEntryTag.Tag4 to addedInputCell2,
                            ),
                        ),
                    ),
                    inputReactiveBag.revokeChange(),
                    initialInputCell1.update(newValue = "#1b"),
                ),
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1a",
                    FuseEntryTag.Tag2 to "#2",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1b",
                    FuseEntryTag.Tag2 to "#2",
                ),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    @Test
    fun test_bagChanges_addedCells_bagChangeCorrected_someCellsUnadded_deactivated() {
        test_bagChanges_addedCells_bagChangeCorrected_someCellsUnadded(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChanges_addedCells_bagChangeCorrected_someCellsUnadded_keptAlive() {
        test_bagChanges_addedCells_bagChangeCorrected_someCellsUnadded(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The input bag changes. Some tags/cells are added. The input bag change is corrected. Some of the previously
     * added tags/cells are not part of the change anymore.
     */
    private fun test_bagChanges_addedCells_bagChangeCorrected_someCellsUnadded(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val initialInputCell1 = TestInputCell(initialValue = "#1")
        val initialInputCell2 = TestInputCell(initialValue = "#2")

        val addedInputCell1 = TestInputCell(initialValue = "+1")
        val temporarilyAddedInputCell2 = TestInputCell(initialValue = "+2~")

        val initialTaggedInputCells = taggedBagOf(
            FuseEntryTag.Tag1 to initialInputCell1,
            FuseEntryTag.Tag2 to initialInputCell2,
        )

        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = initialTaggedInputCells,
        )

        val subjectReactiveBag = inputReactiveBag.fuse()

        ReactiveBag_fuse_testUtils.testReaction(
            inputReactiveBag = inputReactiveBag,
            inputCellByLabel = mapOf(
                "initial 1" to initialInputCell1,
                "initial 2" to initialInputCell2,
                "temporarily added 1" to addedInputCell1,
                "temporarily added 2" to temporarilyAddedInputCell2,
            ),
            subjectReactiveBag = subjectReactiveBag,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    inputReactiveBag.change(
                        TestInputReactiveBag.ChangeDescription(
                            addedElementByTag = mapOf(
                                FuseEntryTag.Tag3 to addedInputCell1,
                                FuseEntryTag.Tag4 to temporarilyAddedInputCell2,
                            ),
                        ),
                    ),
                    inputReactiveBag.correctChange(
                        TestInputReactiveBag.ChangeDescription(
                            addedElementByTag = mapOf(
                                FuseEntryTag.Tag3 to addedInputCell1,
                            ),
                        ),
                    ),
                ),
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "+1",
                ),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    @Test
    fun test_bagChanges_addedCells_bagChangeCorrected_someCellsUnadded_previouslyAddedCellUpdates_deactivated() {
        test_bagChanges_addedCells_bagChangeCorrected_someCellsUnadded_previouslyAddedCellUpdates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChanges_addedCells_bagChangeCorrected_someCellsUnadded_previouslyAddedCellUpdates_keptAlive() {
        test_bagChanges_addedCells_bagChangeCorrected_someCellsUnadded_previouslyAddedCellUpdates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The input bag changes. Some tags/cells are added. The input bag change is corrected. Some of the previously
     * added tags/cells are not part of the change anymore. One of the previously added cells updates after the
     * correction.
     */
    private fun test_bagChanges_addedCells_bagChangeCorrected_someCellsUnadded_previouslyAddedCellUpdates(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val initialInputCell1 = TestInputCell(initialValue = "#1")
        val initialInputCell2 = TestInputCell(initialValue = "#2")

        val temporarilyAddedInputCell1 = TestInputCell(initialValue = "+1~a")
        val temporarilyAddedInputCell2 = TestInputCell(initialValue = "+2~")
        val addedInputCell3 = TestInputCell(initialValue = "+3")

        val initialTaggedInputCells = taggedBagOf(
            FuseEntryTag.Tag1 to initialInputCell1,
            FuseEntryTag.Tag2 to initialInputCell2,
        )

        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = initialTaggedInputCells,
        )

        val subjectReactiveBag = inputReactiveBag.fuse()

        ReactiveBag_fuse_testUtils.testReaction(
            inputReactiveBag = inputReactiveBag,
            inputCellByLabel = mapOf(
                "initial 1" to initialInputCell1,
                "initial 2" to initialInputCell2,
                "temporarily added 1" to temporarilyAddedInputCell1,
                "temporarily added 2" to temporarilyAddedInputCell2,
                "added 3" to addedInputCell3,
            ),
            subjectReactiveBag = subjectReactiveBag,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    inputReactiveBag.change(
                        TestInputReactiveBag.ChangeDescription(
                            addedElementByTag = mapOf(
                                FuseEntryTag.Tag3 to temporarilyAddedInputCell1,
                                FuseEntryTag.Tag4 to temporarilyAddedInputCell2,
                                FuseEntryTag.Tag5 to addedInputCell3,
                            ),
                        ),
                    ),
                    // The previously added cells updates _after_ the change
                    temporarilyAddedInputCell1.update(newValue = "+1~b"),
                    inputReactiveBag.correctChange(
                        TestInputReactiveBag.ChangeDescription(
                            addedElementByTag = mapOf(
                                FuseEntryTag.Tag5 to addedInputCell3,
                            ),
                        ),
                    ),
                ),
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag5 to "+3",
                ),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    @Test
    fun test_bagChanges_addedCells_bagChangeCorrected_someCellsAddedDifferently_deactivated() {
        test_bagChanges_addedCells_bagChangeCorrected_someCellsAddedDifferently(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChanges_addedCells_bagChangeCorrected_someCellsAddedDifferently_keptAlive() {
        test_bagChanges_addedCells_bagChangeCorrected_someCellsAddedDifferently(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The input bag changes. Some tags/cells are added. The input bag change is corrected. Some of the previously
     * added tags are now added with different cells.
     */
    private fun test_bagChanges_addedCells_bagChangeCorrected_someCellsAddedDifferently(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val initialInputCell1 = TestInputCell(initialValue = "#1")
        val initialInputCell2 = TestInputCell(initialValue = "#2")

        val temporaryAddedInputCell1 = TestInputCell(initialValue = "+1~")
        val temporaryAddedInputCell2 = TestInputCell(initialValue = "+2~")
        val finalAddedInputCell1 = TestInputCell(initialValue = "+1!")
        val finalAddedInputCell2 = TestInputCell(initialValue = "+2!")

        val initialTaggedInputCells = taggedBagOf(
            FuseEntryTag.Tag1 to initialInputCell1,
            FuseEntryTag.Tag2 to initialInputCell2,
        )

        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = initialTaggedInputCells,
        )

        val subjectReactiveBag = inputReactiveBag.fuse()

        ReactiveBag_fuse_testUtils.testReaction(
            inputReactiveBag = inputReactiveBag,
            inputCellByLabel = mapOf(
                "initial 1" to initialInputCell1,
                "initial 2" to initialInputCell2,
                "temporary added 1" to temporaryAddedInputCell1,
                "temporary added 2" to temporaryAddedInputCell2,
                "final added 1" to finalAddedInputCell1,
                "final added 2" to finalAddedInputCell2,
            ),
            subjectReactiveBag = subjectReactiveBag,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    inputReactiveBag.change(
                        TestInputReactiveBag.ChangeDescription(
                            addedElementByTag = mapOf(
                                FuseEntryTag.Tag3 to temporaryAddedInputCell1,
                                FuseEntryTag.Tag4 to temporaryAddedInputCell2,
                            ),
                        ),
                    ),
                    inputReactiveBag.correctChange(
                        TestInputReactiveBag.ChangeDescription(
                            addedElementByTag = mapOf(
                                FuseEntryTag.Tag3 to finalAddedInputCell1,
                                FuseEntryTag.Tag4 to finalAddedInputCell2,
                            ),
                        ),
                    ),
                ),
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "+1!",
                    FuseEntryTag.Tag4 to "+2!",
                ),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    @Test
    fun test_bagChanges_addedCells_bagChangeCorrected_someCellsAddedDifferently_previouslyAddedCellUpdates_deactivated() {
        test_bagChanges_addedCells_bagChangeCorrected_someCellsAddedDifferently_previouslyAddedCellUpdates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChanges_addedCells_bagChangeCorrected_someCellsAddedDifferently_previouslyAddedCellUpdates_keptAlive() {
        test_bagChanges_addedCells_bagChangeCorrected_someCellsAddedDifferently_previouslyAddedCellUpdates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The input bag changes. Some tags/cells are added. The input bag change is corrected. Some of the previously
     * added tags are now added with different cells. One of the previously added cells updates after the correction.
     */
    private fun test_bagChanges_addedCells_bagChangeCorrected_someCellsAddedDifferently_previouslyAddedCellUpdates(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val initialInputCell1 = TestInputCell(initialValue = "#1")
        val initialInputCell2 = TestInputCell(initialValue = "#2")

        val temporaryAddedInputCell1 = TestInputCell(initialValue = "+1~a")
        val temporaryAddedInputCell2 = TestInputCell(initialValue = "+2~")
        val finalAddedInputCell1 = TestInputCell(initialValue = "+1!")
        val finalAddedInputCell2 = TestInputCell(initialValue = "+2!")

        val initialTaggedInputCells = taggedBagOf(
            FuseEntryTag.Tag1 to initialInputCell1,
            FuseEntryTag.Tag2 to initialInputCell2,
        )

        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = initialTaggedInputCells,
        )

        val subjectReactiveBag = inputReactiveBag.fuse()

        ReactiveBag_fuse_testUtils.testReaction(
            inputReactiveBag = inputReactiveBag,
            inputCellByLabel = mapOf(
                "initial 1" to initialInputCell1,
                "initial 2" to initialInputCell2,
                "temporary added 1" to temporaryAddedInputCell1,
                "temporary added 2" to temporaryAddedInputCell2,
                "final added 1" to finalAddedInputCell1,
                "final added 2" to finalAddedInputCell2,
            ),
            subjectReactiveBag = subjectReactiveBag,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    inputReactiveBag.change(
                        TestInputReactiveBag.ChangeDescription(
                            addedElementByTag = mapOf(
                                FuseEntryTag.Tag3 to temporaryAddedInputCell1,
                                FuseEntryTag.Tag4 to temporaryAddedInputCell2,
                            ),
                        ),
                    ),
                    // The previously added cells updates _after_ the change
                    temporaryAddedInputCell1.update(newValue = "+1~b"),
                    inputReactiveBag.correctChange(
                        TestInputReactiveBag.ChangeDescription(
                            addedElementByTag = mapOf(
                                FuseEntryTag.Tag3 to finalAddedInputCell1,
                                FuseEntryTag.Tag4 to finalAddedInputCell2,
                            ),
                        ),
                    ),
                ),
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "+1!",
                    FuseEntryTag.Tag4 to "+2!",
                ),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    @Test
    fun test_bagChanges_addedCells_bagChangeCorrected_someCellsAddedDifferently_newlyAddedCellUpdates_deactivated() {
        test_bagChanges_addedCells_bagChangeCorrected_someCellsAddedDifferently_newlyAddedCellUpdates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChanges_addedCells_bagChangeCorrected_someCellsAddedDifferently_newlyAddedCellUpdates_keptAlive() {
        test_bagChanges_addedCells_bagChangeCorrected_someCellsAddedDifferently_newlyAddedCellUpdates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The input bag changes. Some tags/cells are added. The input bag change is corrected. Some of the previously
     * added tags are now added with different cells. One of the newly added cells updates after the correction.
     */
    private fun test_bagChanges_addedCells_bagChangeCorrected_someCellsAddedDifferently_newlyAddedCellUpdates(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val initialInputCell1 = TestInputCell(initialValue = "#1")
        val initialInputCell2 = TestInputCell(initialValue = "#2")

        val temporaryAddedInputCell1 = TestInputCell(initialValue = "+1~")
        val temporaryAddedInputCell2 = TestInputCell(initialValue = "+2~")
        val finalAddedInputCell1 = TestInputCell(initialValue = "+1!a")
        val finalAddedInputCell2 = TestInputCell(initialValue = "+2!")

        val initialTaggedInputCells = taggedBagOf(
            FuseEntryTag.Tag1 to initialInputCell1,
            FuseEntryTag.Tag2 to initialInputCell2,
        )

        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = initialTaggedInputCells,
        )

        val subjectReactiveBag = inputReactiveBag.fuse()

        ReactiveBag_fuse_testUtils.testReaction(
            inputReactiveBag = inputReactiveBag,
            inputCellByLabel = mapOf(
                "initial 1" to initialInputCell1,
                "initial 2" to initialInputCell2,
                "temporary added 1" to temporaryAddedInputCell1,
                "temporary added 2" to temporaryAddedInputCell2,
                "final added 1" to finalAddedInputCell1,
                "final added 2" to finalAddedInputCell2,
            ),
            subjectReactiveBag = subjectReactiveBag,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    inputReactiveBag.change(
                        TestInputReactiveBag.ChangeDescription(
                            addedElementByTag = mapOf(
                                FuseEntryTag.Tag3 to temporaryAddedInputCell1,
                                FuseEntryTag.Tag4 to temporaryAddedInputCell2,
                            ),
                        ),
                    ),
                    inputReactiveBag.correctChange(
                        TestInputReactiveBag.ChangeDescription(
                            addedElementByTag = mapOf(
                                FuseEntryTag.Tag3 to finalAddedInputCell1,
                                FuseEntryTag.Tag4 to finalAddedInputCell2,
                            ),
                        ),
                    ),
                    // The newly added cell updates _after_ the change correction
                    finalAddedInputCell1.update(newValue = "+1!b"),
                ),
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "+1!b",
                    FuseEntryTag.Tag4 to "+2!",
                ),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    @Test
    fun test_bagChanges_removedCells_deactivated() {
        test_bagChanges_removedCells(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChanges_removedCells_keptAlive() {
        test_bagChanges_removedCells(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The input bag changes. Some tags/cells are removed.
     */
    private fun test_bagChanges_removedCells(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val initialInputCell1 = TestInputCell(initialValue = "#1")
        val initialInputCell2 = TestInputCell(initialValue = "#2")
        val removedInputCell1 = TestInputCell(initialValue = "-1")
        val removedInputCell2 = TestInputCell(initialValue = "-2")

        val initialTaggedInputCells = taggedBagOf(
            FuseEntryTag.Tag1 to initialInputCell1,
            FuseEntryTag.Tag2 to initialInputCell2,
            FuseEntryTag.Tag3 to removedInputCell1,
            FuseEntryTag.Tag4 to removedInputCell2,
        )

        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = initialTaggedInputCells,
        )

        val subjectReactiveBag = inputReactiveBag.fuse()

        ReactiveBag_fuse_testUtils.testReaction(
            inputReactiveBag = inputReactiveBag,
            inputCellByLabel = mapOf(
                "initial 1" to initialInputCell1,
                "initial 2" to initialInputCell2,
                "removed 1" to removedInputCell1,
                "removed 2" to removedInputCell2,
            ),
            subjectReactiveBag = subjectReactiveBag,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = inputReactiveBag.change(
                    TestInputReactiveBag.ChangeDescription(
                        removedTags = setOf(
                            FuseEntryTag.Tag3,
                            FuseEntryTag.Tag4,
                        ),
                    ),
                ),
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "-1",
                    FuseEntryTag.Tag4 to "-2",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                ),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    @Test
    fun test_bagChanges_removedCells_removedDuplicates_deactivated() {
        test_bagChanges_removedCells_removedDuplicates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChanges_removedCells_removedDuplicates_keptAlive() {
        test_bagChanges_removedCells_removedDuplicates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The input bag changes. Some tags/cells are removed. Some of the removed cells share a single cell object instance
     * with non-removed cells.
     */
    private fun test_bagChanges_removedCells_removedDuplicates(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val initialInputCell1 = TestInputCell(initialValue = "#1")
        val initialInputCell2 = TestInputCell(initialValue = "#2")
        val sharedCell = TestInputCell(initialValue = "shared")

        val initialTaggedInputCells = taggedBagOf(
            FuseEntryTag.Tag1 to initialInputCell1,
            FuseEntryTag.Tag2 to initialInputCell2,
            FuseEntryTag.Tag3 to sharedCell,
            FuseEntryTag.Tag4 to sharedCell,
        )

        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = initialTaggedInputCells,
        )

        val subjectReactiveBag = inputReactiveBag.fuse()

        ReactiveBag_fuse_testUtils.testReaction(
            inputReactiveBag = inputReactiveBag,
            inputCellByLabel = mapOf(
                "initial 1" to initialInputCell1,
                "initial 2" to initialInputCell2,
                "shared" to sharedCell,
            ),
            subjectReactiveBag = subjectReactiveBag,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = inputReactiveBag.change(
                    TestInputReactiveBag.ChangeDescription(
                        removedTags = setOf(
                            FuseEntryTag.Tag4,
                        ),
                    ),
                ),
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "shared",
                    FuseEntryTag.Tag4 to "shared",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "shared",
                ),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    @Test
    fun test_bagChanges_removedCellsUpdate_deactivated() {
        test_bagChanges_removedCellsUpdate(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChanges_removedCellsUpdate_keptAlive() {
        test_bagChanges_removedCellsUpdate(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The input bag changes. Some tags/cells are removed. Some of them update.
     */
    private fun test_bagChanges_removedCellsUpdate(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val initialInputCell1 = TestInputCell(initialValue = "#1")
        val initialInputCell2 = TestInputCell(initialValue = "#2")
        val removedInputCell1 = TestInputCell(initialValue = "-1a")
        val removedInputCell2 = TestInputCell(initialValue = "-2a")

        val initialTaggedInputCells = taggedBagOf(
            FuseEntryTag.Tag1 to initialInputCell1,
            FuseEntryTag.Tag2 to initialInputCell2,
            FuseEntryTag.Tag3 to removedInputCell1,
            FuseEntryTag.Tag4 to removedInputCell2,
        )

        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = initialTaggedInputCells,
        )

        val subjectReactiveBag = inputReactiveBag.fuse()

        ReactiveBag_fuse_testUtils.testReaction(
            inputReactiveBag = inputReactiveBag,
            inputCellByLabel = mapOf(
                "initial 1" to initialInputCell1,
                "initial 2" to initialInputCell2,
                "removed 1" to removedInputCell1,
                "removed 2" to removedInputCell2,
            ),
            subjectReactiveBag = subjectReactiveBag,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combineInArbitraryOrder(
                    setOf(
                        inputReactiveBag.change(
                            TestInputReactiveBag.ChangeDescription(
                                removedTags = setOf(
                                    FuseEntryTag.Tag3,
                                    FuseEntryTag.Tag4,
                                ),
                            ),
                        ),
                        removedInputCell1.update(newValue = "-1b"),
                        removedInputCell2.update(newValue = "-2b"),
                    ),
                ),
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "-1a",
                    FuseEntryTag.Tag4 to "-2a",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                ),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    @Test
    fun test_bagChanges_removedCells_bagChangeRevoked_removedCellUpdates_deactivated() {
        test_bagChanges_removedCells_bagChangeRevoked_removedCellUpdates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChanges_removedCells_bagChangeRevoked_removedCellUpdates_keptAlive() {
        test_bagChanges_removedCells_bagChangeRevoked_removedCellUpdates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The input bag changes. Some tags/cells are removed. The bag change is revoked; after the change is revoked,
     * one of the removed cells updates.
     */
    private fun test_bagChanges_removedCells_bagChangeRevoked_removedCellUpdates(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val initialInputCell1 = TestInputCell(initialValue = "#1")
        val initialInputCell2 = TestInputCell(initialValue = "#2")
        val removedInputCell1 = TestInputCell(initialValue = "-1a")
        val removedInputCell2 = TestInputCell(initialValue = "-2")

        val initialTaggedInputCells = taggedBagOf(
            FuseEntryTag.Tag1 to initialInputCell1,
            FuseEntryTag.Tag2 to initialInputCell2,
            FuseEntryTag.Tag3 to removedInputCell1,
            FuseEntryTag.Tag4 to removedInputCell2,
        )

        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = initialTaggedInputCells,
        )

        val subjectReactiveBag = inputReactiveBag.fuse()

        ReactiveBag_fuse_testUtils.testReaction(
            inputReactiveBag = inputReactiveBag,
            inputCellByLabel = mapOf(
                "initial 1" to initialInputCell1,
                "initial 2" to initialInputCell2,
                "removed 1" to removedInputCell1,
                "removed 2" to removedInputCell2,
            ),
            subjectReactiveBag = subjectReactiveBag,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    inputReactiveBag.change(
                        TestInputReactiveBag.ChangeDescription(
                            removedTags = setOf(
                                FuseEntryTag.Tag3,
                                FuseEntryTag.Tag4,
                            ),
                        ),
                    ),
                    inputReactiveBag.revokeChange(),
                    // The previously removed cell updates _after_ the change is revoked
                    removedInputCell1.update(newValue = "-1b"),
                ),
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "-1a",
                    FuseEntryTag.Tag4 to "-2",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "-1b",
                    FuseEntryTag.Tag4 to "-2",
                ),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    @Test
    fun test_bagChanges_removedCells_bagChangeRevoked_initialCellUpdates_deactivated() {
        test_bagChanges_removedCells_bagChangeRevoked_initialCellUpdates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChanges_removedCells_bagChangeRevoked_initialCellUpdates_keptAlive() {
        test_bagChanges_removedCells_bagChangeRevoked_initialCellUpdates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The input bag changes. Some tags/cells are removed. The bag change is revoked; after the change is revoked,
     * one of the initial cells updates.
     */
    private fun test_bagChanges_removedCells_bagChangeRevoked_initialCellUpdates(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val initialInputCell1 = TestInputCell(initialValue = "#1a")
        val initialInputCell2 = TestInputCell(initialValue = "#2")
        val removedInputCell1 = TestInputCell(initialValue = "-1")
        val removedInputCell2 = TestInputCell(initialValue = "-2")

        val initialTaggedInputCells = taggedBagOf(
            FuseEntryTag.Tag1 to initialInputCell1,
            FuseEntryTag.Tag2 to initialInputCell2,
            FuseEntryTag.Tag3 to removedInputCell1,
            FuseEntryTag.Tag4 to removedInputCell2,
        )

        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = initialTaggedInputCells,
        )

        val subjectReactiveBag = inputReactiveBag.fuse()

        ReactiveBag_fuse_testUtils.testReaction(
            inputReactiveBag = inputReactiveBag,
            inputCellByLabel = mapOf(
                "initial 1" to initialInputCell1,
                "initial 2" to initialInputCell2,
                "removed 1" to removedInputCell1,
                "removed 2" to removedInputCell2,
            ),
            subjectReactiveBag = subjectReactiveBag,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    inputReactiveBag.change(
                        TestInputReactiveBag.ChangeDescription(
                            removedTags = setOf(
                                FuseEntryTag.Tag3,
                                FuseEntryTag.Tag4,
                            ),
                        ),
                    ),
                    inputReactiveBag.revokeChange(),
                    // One of the initial cells updates _after_ the change is revoked
                    initialInputCell1.update(newValue = "#1b"),
                ),
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1a",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "-1",
                    FuseEntryTag.Tag4 to "-2",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1b",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "-1",
                    FuseEntryTag.Tag4 to "-2",
                ),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    @Test
    fun test_bagChanges_removedCells_bagChangeCorrected_someCellsUnremoved_deactivated() {
        test_bagChanges_removedCells_bagChangeCorrected_someCellsUnremoved(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChanges_removedCells_bagChangeCorrected_someCellsUnremoved_keptAlive() {
        test_bagChanges_removedCells_bagChangeCorrected_someCellsUnremoved(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The input bag changes. Some tags/cells are removed. The input bag change is corrected. Some of the previously
     * removed tags/cells are not part of the change anymore.
     */
    private fun test_bagChanges_removedCells_bagChangeCorrected_someCellsUnremoved(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val initialInputCell1 = TestInputCell(initialValue = "#1")
        val initialInputCell2 = TestInputCell(initialValue = "#2")
        val temporarilyRemovedInputCell1 = TestInputCell(initialValue = "-1~")
        val temporarilyRemovedInputCell2 = TestInputCell(initialValue = "-2~")
        val removedInputCell3 = TestInputCell(initialValue = "-3")

        val initialTaggedInputCells = taggedBagOf(
            FuseEntryTag.Tag1 to initialInputCell1,
            FuseEntryTag.Tag2 to initialInputCell2,
            FuseEntryTag.Tag3 to temporarilyRemovedInputCell1,
            FuseEntryTag.Tag4 to temporarilyRemovedInputCell2,
            FuseEntryTag.Tag5 to removedInputCell3,
        )

        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = initialTaggedInputCells,
        )

        val subjectReactiveBag = inputReactiveBag.fuse()

        ReactiveBag_fuse_testUtils.testReaction(
            inputReactiveBag = inputReactiveBag,
            inputCellByLabel = mapOf(
                "initial 1" to initialInputCell1,
                "initial 2" to initialInputCell2,
                "temporarily removed 1" to temporarilyRemovedInputCell1,
                "temporarily removed 2" to temporarilyRemovedInputCell2,
                "removed 3" to removedInputCell3,
            ),
            subjectReactiveBag = subjectReactiveBag,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    inputReactiveBag.change(
                        TestInputReactiveBag.ChangeDescription(
                            removedTags = setOf(
                                FuseEntryTag.Tag3,
                                FuseEntryTag.Tag4,
                                FuseEntryTag.Tag5,
                            ),
                        ),
                    ),
                    inputReactiveBag.correctChange(
                        TestInputReactiveBag.ChangeDescription(
                            removedTags = setOf(
                                FuseEntryTag.Tag5,
                            ),
                        ),
                    ),
                ),
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "-1~",
                    FuseEntryTag.Tag4 to "-2~",
                    FuseEntryTag.Tag5 to "-3",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "-1~",
                    FuseEntryTag.Tag4 to "-2~",
                ),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    @Test
    fun test_bagChanges_removedCells_bagChangeCorrected_someCellsUnremoved_temporarilyRemovedCellUpdates_deactivated() {
        test_bagChanges_removedCells_bagChangeCorrected_someCellsUnremoved_temporarilyRemovedCellUpdates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChanges_removedCells_bagChangeCorrected_someCellsUnremoved_temporarilyRemovedCellUpdates_keptAlive() {
        test_bagChanges_removedCells_bagChangeCorrected_someCellsUnremoved_temporarilyRemovedCellUpdates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The input bag changes. Some tags/cells are removed. The input bag change is corrected. Some of the previously
     * removed tags/cells are not part of the change anymore. One of the temporarily removed cells updates after the
     * correction.
     */
    private fun test_bagChanges_removedCells_bagChangeCorrected_someCellsUnremoved_temporarilyRemovedCellUpdates(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val initialInputCell1 = TestInputCell(initialValue = "#1")
        val initialInputCell2 = TestInputCell(initialValue = "#2")
        val temporarilyRemovedInputCell1 = TestInputCell(initialValue = "-1~")
        val temporarilyRemovedInputCell2 = TestInputCell(initialValue = "-2~")
        val removedInputCell3 = TestInputCell(initialValue = "-3")

        val initialTaggedInputCells = taggedBagOf(
            FuseEntryTag.Tag1 to initialInputCell1,
            FuseEntryTag.Tag2 to initialInputCell2,
            FuseEntryTag.Tag3 to temporarilyRemovedInputCell1,
            FuseEntryTag.Tag4 to temporarilyRemovedInputCell2,
            FuseEntryTag.Tag5 to removedInputCell3,
        )

        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = initialTaggedInputCells,
        )

        val subjectReactiveBag = inputReactiveBag.fuse()

        ReactiveBag_fuse_testUtils.testReaction(
            inputReactiveBag = inputReactiveBag,
            inputCellByLabel = mapOf(
                "initial 1" to initialInputCell1,
                "initial 2" to initialInputCell2,
                "temporarily removed 1" to temporarilyRemovedInputCell1,
                "temporarily removed 2" to temporarilyRemovedInputCell2,
                "removed 3" to removedInputCell3,
            ),
            subjectReactiveBag = subjectReactiveBag,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    inputReactiveBag.change(
                        TestInputReactiveBag.ChangeDescription(
                            removedTags = setOf(
                                FuseEntryTag.Tag3,
                                FuseEntryTag.Tag4,
                                FuseEntryTag.Tag5,
                            ),
                        ),
                    ),
                    inputReactiveBag.correctChange(
                        TestInputReactiveBag.ChangeDescription(
                            removedTags = setOf(
                                FuseEntryTag.Tag5,
                            ),
                        ),
                    ),
                    // One of the temporarily removed cells updates _after_ the change correction
                    temporarilyRemovedInputCell2.update(newValue = "-2~~"),
                ),
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "-1~",
                    FuseEntryTag.Tag4 to "-2~",
                    FuseEntryTag.Tag5 to "-3",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "-1~",
                    FuseEntryTag.Tag4 to "-2~~",
                ),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    @Test
    fun test_bagChanges_removedCells_bagChangeCorrected_someRemovedCellsNowReplaced_deactivated() {
        test_bagChanges_removedCells_bagChangeCorrected_someRemovedCellsNowReplaced(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChanges_removedCells_bagChangeCorrected_someRemovedCellsNowReplaced_keptAlive() {
        test_bagChanges_removedCells_bagChangeCorrected_someRemovedCellsNowReplaced(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The input bag changes. Some tags/cells are removed. The input bag change is corrected. Some of the previously
     * removed tags/cells are replaced with other cells in the change correction.
     */
    private fun test_bagChanges_removedCells_bagChangeCorrected_someRemovedCellsNowReplaced(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val initialInputCell1 = TestInputCell(initialValue = "#1")
        val initialInputCell2 = TestInputCell(initialValue = "#2")
        val temporarilyRemovedInputCell1 = TestInputCell(initialValue = "-1~")
        val temporarilyRemovedInputCell2 = TestInputCell(initialValue = "-2~")
        val replacementInputCell1 = TestInputCell(initialValue = "~1")
        val replacementInputCell2 = TestInputCell(initialValue = "~2")

        val initialTaggedInputCells = taggedBagOf(
            FuseEntryTag.Tag1 to initialInputCell1,
            FuseEntryTag.Tag2 to initialInputCell2,
            FuseEntryTag.Tag3 to temporarilyRemovedInputCell1,
            FuseEntryTag.Tag4 to temporarilyRemovedInputCell2,
        )

        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = initialTaggedInputCells,
        )

        val subjectReactiveBag = inputReactiveBag.fuse()

        ReactiveBag_fuse_testUtils.testReaction(
            inputReactiveBag = inputReactiveBag,
            inputCellByLabel = mapOf(
                "initial 1" to initialInputCell1,
                "initial 2" to initialInputCell2,
                "temporarily removed 1" to temporarilyRemovedInputCell1,
                "temporarily removed 2" to temporarilyRemovedInputCell2,
                "replacement 1" to replacementInputCell1,
                "replacement 2" to replacementInputCell2,
            ),
            subjectReactiveBag = subjectReactiveBag,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    inputReactiveBag.change(
                        TestInputReactiveBag.ChangeDescription(
                            removedTags = setOf(
                                FuseEntryTag.Tag3,
                                FuseEntryTag.Tag4,
                            ),
                        ),
                    ),
                    inputReactiveBag.correctChange(
                        TestInputReactiveBag.ChangeDescription(
                            replacedElementByTag = mapOf(
                                FuseEntryTag.Tag3 to replacementInputCell1,
                                FuseEntryTag.Tag4 to replacementInputCell2,
                            ),
                        ),
                    ),
                ),
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "-1~",
                    FuseEntryTag.Tag4 to "-2~",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "~1",
                    FuseEntryTag.Tag4 to "~2",
                ),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    @Test
    fun test_bagChanges_removedCells_bagChangeCorrected_someRemovedCellsNowReplaced_temporarilyRemovedCellUpdates_deactivated() {
        test_bagChanges_removedCells_bagChangeCorrected_someRemovedCellsNowReplaced_temporarilyRemovedCellUpdates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChanges_removedCells_bagChangeCorrected_someRemovedCellsNowReplaced_temporarilyRemovedCellUpdates_keptAlive() {
        test_bagChanges_removedCells_bagChangeCorrected_someRemovedCellsNowReplaced_temporarilyRemovedCellUpdates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The input bag changes. Some tags/cells are removed. The input bag change is corrected. Some of the previously
     * removed tags/cells are replaced with other cells in the change correction. One of the temporarily removed cells
     * updates after the correction.
     */
    private fun test_bagChanges_removedCells_bagChangeCorrected_someRemovedCellsNowReplaced_temporarilyRemovedCellUpdates(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val initialInputCell1 = TestInputCell(initialValue = "#1")
        val initialInputCell2 = TestInputCell(initialValue = "#2")
        val temporarilyRemovedInputCell1 = TestInputCell(initialValue = "-1~a")
        val temporarilyRemovedInputCell2 = TestInputCell(initialValue = "-2~")
        val replacementInputCell1 = TestInputCell(initialValue = "~1")
        val replacementInputCell2 = TestInputCell(initialValue = "~2")

        val initialTaggedInputCells = taggedBagOf(
            FuseEntryTag.Tag1 to initialInputCell1,
            FuseEntryTag.Tag2 to initialInputCell2,
            FuseEntryTag.Tag3 to temporarilyRemovedInputCell1,
            FuseEntryTag.Tag4 to temporarilyRemovedInputCell2,
        )

        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = initialTaggedInputCells,
        )

        val subjectReactiveBag = inputReactiveBag.fuse()

        ReactiveBag_fuse_testUtils.testReaction(
            inputReactiveBag = inputReactiveBag,
            inputCellByLabel = mapOf(
                "initial 1" to initialInputCell1,
                "initial 2" to initialInputCell2,
                "temporarily removed 1" to temporarilyRemovedInputCell1,
                "temporarily removed 2" to temporarilyRemovedInputCell2,
                "replacement 1" to replacementInputCell1,
                "replacement 2" to replacementInputCell2,
            ),
            subjectReactiveBag = subjectReactiveBag,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    inputReactiveBag.change(
                        TestInputReactiveBag.ChangeDescription(
                            removedTags = setOf(
                                FuseEntryTag.Tag3,
                                FuseEntryTag.Tag4,
                            ),
                        ),
                    ),
                    inputReactiveBag.correctChange(
                        TestInputReactiveBag.ChangeDescription(
                            replacedElementByTag = mapOf(
                                FuseEntryTag.Tag3 to replacementInputCell1,
                                FuseEntryTag.Tag4 to replacementInputCell2,
                            ),
                        ),
                    ),
                    // One of the temporarily removed cells updates _after_ the change correction
                    temporarilyRemovedInputCell1.update(newValue = "-1~b"),
                ),
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "-1~a",
                    FuseEntryTag.Tag4 to "-2~",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "~1",
                    FuseEntryTag.Tag4 to "~2",
                ),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    @Test
    fun test_bagChanges_removedCells_bagChangeCorrected_someRemovedCellsNowReplaced_replacementCellUpdates_deactivated() {
        test_bagChanges_removedCells_bagChangeCorrected_someRemovedCellsNowReplaced_replacementCellUpdates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChanges_removedCells_bagChangeCorrected_someRemovedCellsNowReplaced_replacementCellUpdates_keptAlive() {
        test_bagChanges_removedCells_bagChangeCorrected_someRemovedCellsNowReplaced_replacementCellUpdates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The input bag changes. Some tags/cells are removed. The input bag change is corrected. Some of the previously
     * removed tags/cells are replaced with other cells in the change correction. One of the replacement cells updates
     * after the correction.
     */
    private fun test_bagChanges_removedCells_bagChangeCorrected_someRemovedCellsNowReplaced_replacementCellUpdates(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val initialInputCell1 = TestInputCell(initialValue = "#1")
        val initialInputCell2 = TestInputCell(initialValue = "#2")
        val temporarilyRemovedInputCell1 = TestInputCell(initialValue = "-1~")
        val temporarilyRemovedInputCell2 = TestInputCell(initialValue = "-2~")
        val replacementInputCell1 = TestInputCell(initialValue = "~1a")
        val replacementInputCell2 = TestInputCell(initialValue = "~2")

        val initialTaggedInputCells = taggedBagOf(
            FuseEntryTag.Tag1 to initialInputCell1,
            FuseEntryTag.Tag2 to initialInputCell2,
            FuseEntryTag.Tag3 to temporarilyRemovedInputCell1,
            FuseEntryTag.Tag4 to temporarilyRemovedInputCell2,
        )

        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = initialTaggedInputCells,
        )

        val subjectReactiveBag = inputReactiveBag.fuse()

        ReactiveBag_fuse_testUtils.testReaction(
            inputReactiveBag = inputReactiveBag,
            inputCellByLabel = mapOf(
                "initial 1" to initialInputCell1,
                "initial 2" to initialInputCell2,
                "temporarily removed 1" to temporarilyRemovedInputCell1,
                "temporarily removed 2" to temporarilyRemovedInputCell2,
                "replacement 1" to replacementInputCell1,
                "replacement 2" to replacementInputCell2,
            ),
            subjectReactiveBag = subjectReactiveBag,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestSequentialStimulationSet(
                    includedStimulations = setOf(
                        TestStimulation.combineInProvidedOrder(
                            inputReactiveBag.change(
                                TestInputReactiveBag.ChangeDescription(
                                    removedTags = setOf(
                                        FuseEntryTag.Tag3,
                                        FuseEntryTag.Tag4,
                                    ),
                                ),
                            ),
                            inputReactiveBag.correctChange(
                                TestInputReactiveBag.ChangeDescription(
                                    replacedElementByTag = mapOf(
                                        FuseEntryTag.Tag3 to replacementInputCell1,
                                        FuseEntryTag.Tag4 to replacementInputCell2,
                                    ),
                                ),
                            ),
                        ),
                        TestStimulation.combineInProvidedOrder(
                            replacementInputCell1.update(newValue = "~1b"),
                        ),
                    ),
                ).determinizeArbitrarily(),
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "-1~",
                    FuseEntryTag.Tag4 to "-2~",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "~1b",
                    FuseEntryTag.Tag4 to "~2",
                ),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    @Test
    fun test_bagChanges_replacedCells_deactivated() {
        test_bagChanges_replacedCells(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChanges_replacedCells_keptAlive() {
        test_bagChanges_replacedCells(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The input bag changes. Some tags/cells are replaced.
     */
    private fun test_bagChanges_replacedCells(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val initialInputCell1 = TestInputCell(initialValue = "#1")
        val initialInputCell2 = TestInputCell(initialValue = "#2")
        val replacedInputCell1 = TestInputCell(initialValue = "~1a")
        val replacedInputCell2 = TestInputCell(initialValue = "~2a")
        val replacementInputCell1 = TestInputCell(initialValue = "~1b")
        val replacementInputCell2 = TestInputCell(initialValue = "~2b")

        val initialTaggedInputCells = taggedBagOf(
            FuseEntryTag.Tag1 to initialInputCell1,
            FuseEntryTag.Tag2 to initialInputCell2,
            FuseEntryTag.Tag3 to replacedInputCell1,
            FuseEntryTag.Tag4 to replacedInputCell2,
        )

        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = initialTaggedInputCells,
        )

        val subjectReactiveBag = inputReactiveBag.fuse()

        ReactiveBag_fuse_testUtils.testReaction(
            inputReactiveBag = inputReactiveBag,
            inputCellByLabel = mapOf(
                "initial 1" to initialInputCell1,
                "initial 2" to initialInputCell2,
                "replaced 1" to replacedInputCell1,
                "replaced 2" to replacedInputCell2,
                "replacement 1" to replacementInputCell1,
                "replacement 2" to replacementInputCell2,
            ),
            subjectReactiveBag = subjectReactiveBag,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = inputReactiveBag.change(
                    TestInputReactiveBag.ChangeDescription(
                        replacedElementByTag = mapOf(
                            FuseEntryTag.Tag3 to replacementInputCell1,
                            FuseEntryTag.Tag4 to replacementInputCell2,
                        ),
                    ),
                ),
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "~1a",
                    FuseEntryTag.Tag4 to "~2a",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "~1b",
                    FuseEntryTag.Tag4 to "~2b",
                ),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    @Test
    fun test_bagChanges_replacedCells_replacedWithDuplicates_deactivated() {
        test_bagChanges_replacedCells_replacedWithDuplicates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChanges_replacedCells_replacedWithDuplicates_keptAlive() {
        test_bagChanges_replacedCells_replacedWithDuplicates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The input bag changes. Some tags/cells are replaced. Some of the replacement cells share a single cell object
     * instance with non-replaced cells.
     */
    private fun test_bagChanges_replacedCells_replacedWithDuplicates(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val initialInputCell1 = TestInputCell(initialValue = "#1")
        val initialInputCell2 = TestInputCell(initialValue = "#2")
        val sharedCell = TestInputCell(initialValue = "shared")
        val replacedInputCell = TestInputCell(initialValue = "~1")

        val initialTaggedInputCells = taggedBagOf(
            FuseEntryTag.Tag1 to initialInputCell1,
            FuseEntryTag.Tag2 to initialInputCell2,
            FuseEntryTag.Tag3 to sharedCell,
            FuseEntryTag.Tag4 to replacedInputCell,
        )

        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = initialTaggedInputCells,
        )

        val subjectReactiveBag = inputReactiveBag.fuse()

        ReactiveBag_fuse_testUtils.testReaction(
            inputReactiveBag = inputReactiveBag,
            inputCellByLabel = mapOf(
                "initial 1" to initialInputCell1,
                "initial 2" to initialInputCell2,
                "shared" to sharedCell,
                "replaced" to replacedInputCell,
            ),
            subjectReactiveBag = subjectReactiveBag,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = inputReactiveBag.change(
                    TestInputReactiveBag.ChangeDescription(
                        replacedElementByTag = mapOf(
                            FuseEntryTag.Tag4 to sharedCell,
                        ),
                    ),
                ),
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "shared",
                    FuseEntryTag.Tag4 to "~1",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "shared",
                    FuseEntryTag.Tag4 to "shared",
                ),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    @Test
    fun test_bagChanges_replacedCells_replacementCellsUpdate_deactivated() {
        test_bagChanges_replacedCells_replacementCellsUpdate(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChanges_replacedCells_replacementCellsUpdate_keptAlive() {
        test_bagChanges_replacedCells_replacementCellsUpdate(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The input bag changes. Some tags/cells are replaced. Some of the replacement cells update.
     */
    private fun test_bagChanges_replacedCells_replacementCellsUpdate(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val initialInputCell1 = TestInputCell(initialValue = "#1")
        val initialInputCell2 = TestInputCell(initialValue = "#2")
        val replacedInputCell1 = TestInputCell(initialValue = "~1a")
        val replacedInputCell2 = TestInputCell(initialValue = "~2a")
        val replacementInputCell1 = TestInputCell(initialValue = "~1b")
        val replacementInputCell2 = TestInputCell(initialValue = "~2b")

        val initialTaggedInputCells = taggedBagOf(
            FuseEntryTag.Tag1 to initialInputCell1,
            FuseEntryTag.Tag2 to initialInputCell2,
            FuseEntryTag.Tag3 to replacedInputCell1,
            FuseEntryTag.Tag4 to replacedInputCell2,
        )

        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = initialTaggedInputCells,
        )

        val subjectReactiveBag = inputReactiveBag.fuse()

        ReactiveBag_fuse_testUtils.testReaction(
            inputReactiveBag = inputReactiveBag,
            inputCellByLabel = mapOf(
                "initial 1" to initialInputCell1,
                "initial 2" to initialInputCell2,
                "replaced 1" to replacedInputCell1,
                "replaced 2" to replacedInputCell2,
                "replacement 1" to replacementInputCell1,
                "replacement 2" to replacementInputCell2,
            ),
            subjectReactiveBag = subjectReactiveBag,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combineInArbitraryOrder(
                    setOf(
                        inputReactiveBag.change(
                            TestInputReactiveBag.ChangeDescription(
                                replacedElementByTag = mapOf(
                                    FuseEntryTag.Tag3 to replacementInputCell1,
                                    FuseEntryTag.Tag4 to replacementInputCell2,
                                ),
                            ),
                        ),
                        replacementInputCell1.update(newValue = "~1c"),
                        replacementInputCell2.update(newValue = "~2c"),
                    ),
                ),
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "~1a",
                    FuseEntryTag.Tag4 to "~2a",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "~1c",
                    FuseEntryTag.Tag4 to "~2c",
                ),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    @Test
    fun test_bagChanges_replacedCells_replacedWithDuplicates_replacementCellsUpdate_deactivated() {
        test_bagChanges_replacedCells_replacedWithDuplicates_replacementCellsUpdate(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChanges_replacedCells_replacedWithDuplicates_replacementCellsUpdate_keptAlive() {
        test_bagChanges_replacedCells_replacedWithDuplicates_replacementCellsUpdate(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The input bag changes. Some tags/cells are replaced. Some of the replacement cells update.
     */
    private fun test_bagChanges_replacedCells_replacedWithDuplicates_replacementCellsUpdate(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val initialInputCell1 = TestInputCell(initialValue = "#1")
        val initialInputCell2 = TestInputCell(initialValue = "#2")
        val sharedCell = TestInputCell(initialValue = "shared-a")
        val replacedInputCell = TestInputCell(initialValue = "~1")

        val initialTaggedInputCells = taggedBagOf(
            FuseEntryTag.Tag1 to initialInputCell1,
            FuseEntryTag.Tag2 to initialInputCell2,
            FuseEntryTag.Tag3 to sharedCell,
            FuseEntryTag.Tag4 to replacedInputCell,
        )

        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = initialTaggedInputCells,
        )

        val subjectReactiveBag = inputReactiveBag.fuse()

        ReactiveBag_fuse_testUtils.testReaction(
            inputReactiveBag = inputReactiveBag,
            inputCellByLabel = mapOf(
                "initial 1" to initialInputCell1,
                "initial 2" to initialInputCell2,
                "shared" to sharedCell,
                "replaced" to replacedInputCell,
            ),
            subjectReactiveBag = subjectReactiveBag,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combineInArbitraryOrder(
                    setOf(
                        inputReactiveBag.change(
                            TestInputReactiveBag.ChangeDescription(
                                replacedElementByTag = mapOf(
                                    FuseEntryTag.Tag4 to sharedCell,
                                ),
                            ),
                        ),
                        sharedCell.update(newValue = "shared-b"),
                    ),
                ),
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "shared-a",
                    FuseEntryTag.Tag4 to "~1",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "shared-b",
                    FuseEntryTag.Tag4 to "shared-b",
                ),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    @Test
    fun test_bagChanges_replacedCells_replacedCellsUpdate_deactivated() {
        test_bagChanges_replacedCells_replacedCellsUpdate(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChanges_replacedCells_replacedCellsUpdate_keptAlive() {
        test_bagChanges_replacedCells_replacedCellsUpdate(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The input bag changes. Some tags/cells are replaced. Some of the replaced cells update.
     */
    private fun test_bagChanges_replacedCells_replacedCellsUpdate(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val initialInputCell1 = TestInputCell(initialValue = "#1")
        val initialInputCell2 = TestInputCell(initialValue = "#2")
        val replacedInputCell1 = TestInputCell(initialValue = "~1a")
        val replacedInputCell2 = TestInputCell(initialValue = "~2a")
        val replacementInputCell1 = TestInputCell(initialValue = "~1b")
        val replacementInputCell2 = TestInputCell(initialValue = "~2b")

        val initialTaggedInputCells = taggedBagOf(
            FuseEntryTag.Tag1 to initialInputCell1,
            FuseEntryTag.Tag2 to initialInputCell2,
            FuseEntryTag.Tag3 to replacedInputCell1,
            FuseEntryTag.Tag4 to replacedInputCell2,
        )

        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = initialTaggedInputCells,
        )

        val subjectReactiveBag = inputReactiveBag.fuse()

        ReactiveBag_fuse_testUtils.testReaction(
            inputReactiveBag = inputReactiveBag,
            inputCellByLabel = mapOf(
                "initial 1" to initialInputCell1,
                "initial 2" to initialInputCell2,
                "replaced 1" to replacedInputCell1,
                "replaced 2" to replacedInputCell2,
                "replacement 1" to replacementInputCell1,
                "replacement 2" to replacementInputCell2,
            ),
            subjectReactiveBag = subjectReactiveBag,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combineInArbitraryOrder(
                    setOf(
                        inputReactiveBag.change(
                            TestInputReactiveBag.ChangeDescription(
                                replacedElementByTag = mapOf(
                                    FuseEntryTag.Tag3 to replacementInputCell1,
                                    FuseEntryTag.Tag4 to replacementInputCell2,
                                ),
                            ),
                        ),
                        replacedInputCell1.update(newValue = "~1c"),
                        replacedInputCell2.update(newValue = "~2c"),
                    ),
                ),
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "~1a",
                    FuseEntryTag.Tag4 to "~2a",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "~1b",
                    FuseEntryTag.Tag4 to "~2b",
                ),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    @Test
    fun test_bagChanges_replacedCells_replacedWithDuplicates_replacedCellsUpdate_deactivated() {
        test_bagChanges_replacedCells_replacedWithDuplicates_replacedCellsUpdate(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChanges_replacedCells_replacedWithDuplicates_replacedCellsUpdate_keptAlive() {
        test_bagChanges_replacedCells_replacedWithDuplicates_replacedCellsUpdate(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The input bag changes. Some tags/cells are replaced. Some of the replacement cells share a single cell object
     * instance with non-replaced cells. Some of the replaced cells update.
     */
    private fun test_bagChanges_replacedCells_replacedWithDuplicates_replacedCellsUpdate(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val initialInputCell1 = TestInputCell(initialValue = "#1")
        val initialInputCell2 = TestInputCell(initialValue = "#2")
        val sharedCell = TestInputCell(initialValue = "shared")
        val replacedInputCell = TestInputCell(initialValue = "~1a")

        val initialTaggedInputCells = taggedBagOf(
            FuseEntryTag.Tag1 to initialInputCell1,
            FuseEntryTag.Tag2 to initialInputCell2,
            FuseEntryTag.Tag3 to sharedCell,
            FuseEntryTag.Tag4 to replacedInputCell,
        )

        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = initialTaggedInputCells,
        )

        val subjectReactiveBag = inputReactiveBag.fuse()

        ReactiveBag_fuse_testUtils.testReaction(
            inputReactiveBag = inputReactiveBag,
            inputCellByLabel = mapOf(
                "initial 1" to initialInputCell1,
                "initial 2" to initialInputCell2,
                "shared" to sharedCell,
                "replaced" to replacedInputCell,
            ),
            subjectReactiveBag = subjectReactiveBag,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combineInArbitraryOrder(
                    setOf(
                        inputReactiveBag.change(
                            TestInputReactiveBag.ChangeDescription(
                                replacedElementByTag = mapOf(
                                    FuseEntryTag.Tag4 to sharedCell,
                                ),
                            ),
                        ),
                        replacedInputCell.update(newValue = "~1b"),
                    ),
                ),
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "shared",
                    FuseEntryTag.Tag4 to "~1a",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "shared",
                    FuseEntryTag.Tag4 to "shared",
                ),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    @Test
    fun test_bagChanges_replacedCells_bagChangeRevoked_replacedCellUpdates_deactivated() {
        test_bagChanges_replacedCells_bagChangeRevoked_replacedCellUpdates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChanges_replacedCells_bagChangeRevoked_replacedCellUpdates_keptAlive() {
        test_bagChanges_replacedCells_bagChangeRevoked_replacedCellUpdates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The input bag changes. Some tags/cells are replaced. The bag change is revoked; after the change is revoked,
     * one of the replaced cells updates.
     */
    private fun test_bagChanges_replacedCells_bagChangeRevoked_replacedCellUpdates(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val initialInputCell1 = TestInputCell(initialValue = "#1")
        val initialInputCell2 = TestInputCell(initialValue = "#2")
        val replacedInputCell1 = TestInputCell(initialValue = "~1a")
        val replacedInputCell2 = TestInputCell(initialValue = "~2")
        val replacementInputCell1 = TestInputCell(initialValue = "~1b")
        val replacementInputCell2 = TestInputCell(initialValue = "~2b")

        val initialTaggedInputCells = taggedBagOf(
            FuseEntryTag.Tag1 to initialInputCell1,
            FuseEntryTag.Tag2 to initialInputCell2,
            FuseEntryTag.Tag3 to replacedInputCell1,
            FuseEntryTag.Tag4 to replacedInputCell2,
        )

        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = initialTaggedInputCells,
        )

        val subjectReactiveBag = inputReactiveBag.fuse()

        ReactiveBag_fuse_testUtils.testReaction(
            inputReactiveBag = inputReactiveBag,
            inputCellByLabel = mapOf(
                "initial 1" to initialInputCell1,
                "initial 2" to initialInputCell2,
                "replaced 1" to replacedInputCell1,
                "replaced 2" to replacedInputCell2,
                "replacement 1" to replacementInputCell1,
                "replacement 2" to replacementInputCell2,
            ),
            subjectReactiveBag = subjectReactiveBag,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestSequentialStimulationSet(
                    includedStimulations = setOf(
                        TestStimulation.combineInProvidedOrder(
                            inputReactiveBag.change(
                                TestInputReactiveBag.ChangeDescription(
                                    replacedElementByTag = mapOf(
                                        FuseEntryTag.Tag3 to replacementInputCell1,
                                        FuseEntryTag.Tag4 to replacementInputCell2,
                                    ),
                                ),
                            ),
                            inputReactiveBag.revokeChange(),
                        ),
                        TestStimulation.combineInProvidedOrder(
                            replacedInputCell1.update(newValue = "~1c"),
                        ),
                    ),
                ).determinizeArbitrarily(),
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "~1a",
                    FuseEntryTag.Tag4 to "~2",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "~1c",
                    FuseEntryTag.Tag4 to "~2",
                ),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    @Test
    fun test_bagChanges_replacedCells_bagChangeRevoked_initialCellUpdates_deactivated() {
        test_bagChanges_replacedCells_bagChangeRevoked_initialCellUpdates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChanges_replacedCells_bagChangeRevoked_initialCellUpdates_keptAlive() {
        test_bagChanges_replacedCells_bagChangeRevoked_initialCellUpdates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The input bag changes. Some tags/cells are replaced. The bag change is revoked; after the change is revoked,
     * one of the initial cells updates.
     */
    private fun test_bagChanges_replacedCells_bagChangeRevoked_initialCellUpdates(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val initialInputCell1 = TestInputCell(initialValue = "#1a")
        val initialInputCell2 = TestInputCell(initialValue = "#2")
        val replacedInputCell1 = TestInputCell(initialValue = "~1a")
        val replacedInputCell2 = TestInputCell(initialValue = "~2a")
        val replacementInputCell1 = TestInputCell(initialValue = "~1b")
        val replacementInputCell2 = TestInputCell(initialValue = "~2b")

        val initialTaggedInputCells = taggedBagOf(
            FuseEntryTag.Tag1 to initialInputCell1,
            FuseEntryTag.Tag2 to initialInputCell2,
            FuseEntryTag.Tag3 to replacedInputCell1,
            FuseEntryTag.Tag4 to replacedInputCell2,
        )

        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = initialTaggedInputCells,
        )

        val subjectReactiveBag = inputReactiveBag.fuse()

        ReactiveBag_fuse_testUtils.testReaction(
            inputReactiveBag = inputReactiveBag,
            inputCellByLabel = mapOf(
                "initial 1" to initialInputCell1,
                "initial 2" to initialInputCell2,
                "replaced 1" to replacedInputCell1,
                "replaced 2" to replacedInputCell2,
                "replacement 1" to replacementInputCell1,
                "replacement 2" to replacementInputCell2,
            ),
            subjectReactiveBag = subjectReactiveBag,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    inputReactiveBag.change(
                        TestInputReactiveBag.ChangeDescription(
                            replacedElementByTag = mapOf(
                                FuseEntryTag.Tag3 to replacementInputCell1,
                                FuseEntryTag.Tag4 to replacementInputCell2,
                            ),
                        ),
                    ),
                    inputReactiveBag.revokeChange(),
                    // One of the initial cells updates _after_ the change is revoked
                    initialInputCell1.update(newValue = "#1b"),
                ),
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1a",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "~1a",
                    FuseEntryTag.Tag4 to "~2a",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1b",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "~1a",
                    FuseEntryTag.Tag4 to "~2a",
                ),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    @Test
    fun test_bagChanges_replacedCells_bagChangeCorrected_someCellsUnreplaced_deactivated() {
        test_bagChanges_replacedCells_bagChangeCorrected_someCellsUnreplaced(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChanges_replacedCells_bagChangeCorrected_someCellsUnreplaced_keptAlive() {
        test_bagChanges_replacedCells_bagChangeCorrected_someCellsUnreplaced(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The input bag changes. Some tags/cells are replaced. The input bag change is corrected. Some of the previously
     * replaced tags/cells are not part of the change anymore.
     */
    private fun test_bagChanges_replacedCells_bagChangeCorrected_someCellsUnreplaced(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val initialInputCell1 = TestInputCell(initialValue = "#1")
        val initialInputCell2 = TestInputCell(initialValue = "#2")
        val replacedInputCell1 = TestInputCell(initialValue = "~1")
        val replacedInputCell2 = TestInputCell(initialValue = "~2")
        val temporaryReplacementInputCell1 = TestInputCell(initialValue = "~1~")
        val temporaryReplacementInputCell2 = TestInputCell(initialValue = "~2~")
        val finalReplacementInputCell1 = TestInputCell(initialValue = "~1!")

        val initialTaggedInputCells = taggedBagOf(
            FuseEntryTag.Tag1 to initialInputCell1,
            FuseEntryTag.Tag2 to initialInputCell2,
            FuseEntryTag.Tag3 to replacedInputCell1,
            FuseEntryTag.Tag4 to replacedInputCell2,
        )

        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = initialTaggedInputCells,
        )

        val subjectReactiveBag = inputReactiveBag.fuse()

        ReactiveBag_fuse_testUtils.testReaction(
            inputReactiveBag = inputReactiveBag,
            inputCellByLabel = mapOf(
                "initial 1" to initialInputCell1,
                "initial 2" to initialInputCell2,
                "replaced 1" to replacedInputCell1,
                "replaced 2" to replacedInputCell2,
                "temporary replacement 1" to temporaryReplacementInputCell1,
                "temporary replacement 2" to temporaryReplacementInputCell2,
                "final replacement 1" to finalReplacementInputCell1,
            ),
            subjectReactiveBag = subjectReactiveBag,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    inputReactiveBag.change(
                        TestInputReactiveBag.ChangeDescription(
                            replacedElementByTag = mapOf(
                                FuseEntryTag.Tag3 to temporaryReplacementInputCell1,
                                FuseEntryTag.Tag4 to temporaryReplacementInputCell2,
                            ),
                        ),
                    ),
                    inputReactiveBag.correctChange(
                        TestInputReactiveBag.ChangeDescription(
                            replacedElementByTag = mapOf(
                                FuseEntryTag.Tag3 to finalReplacementInputCell1,
                            ),
                        ),
                    ),
                ),
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "~1",
                    FuseEntryTag.Tag4 to "~2",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "~1!",
                    FuseEntryTag.Tag4 to "~2",
                ),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    @Test
    fun test_bagChanges_replacedCells_bagChangeCorrected_someCellsUnreplaced_temporaryReplacementCellUpdates_deactivated() {
        test_bagChanges_replacedCells_bagChangeCorrected_someCellsUnreplaced_temporaryReplacementCellUpdates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChanges_replacedCells_bagChangeCorrected_someCellsUnreplaced_temporaryReplacementCellUpdates_keptAlive() {
        test_bagChanges_replacedCells_bagChangeCorrected_someCellsUnreplaced_temporaryReplacementCellUpdates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The input bag changes. Some tags/cells are replaced. The input bag change is corrected. Some of the previously
     * replaced tags/cells are not part of the change anymore. One of the temporary replacement cells updates after the
     * correction.
     */
    private fun test_bagChanges_replacedCells_bagChangeCorrected_someCellsUnreplaced_temporaryReplacementCellUpdates(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val initialInputCell1 = TestInputCell(initialValue = "#1")
        val initialInputCell2 = TestInputCell(initialValue = "#2")
        val replacedInputCell1 = TestInputCell(initialValue = "~1")
        val replacedInputCell2 = TestInputCell(initialValue = "~2")
        val temporaryReplacementInputCell1 = TestInputCell(initialValue = "~1~a")
        val temporaryReplacementInputCell2 = TestInputCell(initialValue = "~2~")
        val finalReplacementInputCell1 = TestInputCell(initialValue = "~1!")

        val initialTaggedInputCells = taggedBagOf(
            FuseEntryTag.Tag1 to initialInputCell1,
            FuseEntryTag.Tag2 to initialInputCell2,
            FuseEntryTag.Tag3 to replacedInputCell1,
            FuseEntryTag.Tag4 to replacedInputCell2,
        )

        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = initialTaggedInputCells,
        )

        val subjectReactiveBag = inputReactiveBag.fuse()

        ReactiveBag_fuse_testUtils.testReaction(
            inputReactiveBag = inputReactiveBag,
            inputCellByLabel = mapOf(
                "initial 1" to initialInputCell1,
                "initial 2" to initialInputCell2,
                "replaced 1" to replacedInputCell1,
                "replaced 2" to replacedInputCell2,
                "temporary replacement 1" to temporaryReplacementInputCell1,
                "temporary replacement 2" to temporaryReplacementInputCell2,
                "final replacement 1" to finalReplacementInputCell1,
            ),
            subjectReactiveBag = subjectReactiveBag,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    inputReactiveBag.change(
                        TestInputReactiveBag.ChangeDescription(
                            replacedElementByTag = mapOf(
                                FuseEntryTag.Tag3 to temporaryReplacementInputCell1,
                                FuseEntryTag.Tag4 to temporaryReplacementInputCell2,
                            ),
                        ),
                    ),
                    inputReactiveBag.correctChange(
                        TestInputReactiveBag.ChangeDescription(
                            replacedElementByTag = mapOf(
                                FuseEntryTag.Tag3 to finalReplacementInputCell1,
                            ),
                        ),
                    ),
                    // One of the temporary replacement cells updates _after_ the change is corrected
                    temporaryReplacementInputCell1.update(newValue = "~1~b"),
                ),
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "~1",
                    FuseEntryTag.Tag4 to "~2",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "~1!",
                    FuseEntryTag.Tag4 to "~2",
                ),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    @Test
    fun test_bagChanges_replacedCells_bagChangeCorrected_someCellsUnreplaced_finalReplacementCellUpdates_deactivated() {
        test_bagChanges_replacedCells_bagChangeCorrected_someCellsUnreplaced_finalReplacementCellUpdates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChanges_replacedCells_bagChangeCorrected_someCellsUnreplaced_finalReplacementCellUpdates_keptAlive() {
        test_bagChanges_replacedCells_bagChangeCorrected_someCellsUnreplaced_finalReplacementCellUpdates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The input bag changes. Some tags/cells are replaced. The input bag change is corrected. Some of the previously
     * replaced tags/cells are not part of the change anymore. One of the final replacement cells updates after the
     * correction.
     */
    private fun test_bagChanges_replacedCells_bagChangeCorrected_someCellsUnreplaced_finalReplacementCellUpdates(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val initialInputCell1 = TestInputCell(initialValue = "#1")
        val initialInputCell2 = TestInputCell(initialValue = "#2")
        val replacedInputCell1 = TestInputCell(initialValue = "~1")
        val replacedInputCell2 = TestInputCell(initialValue = "~2")
        val temporaryReplacementInputCell1 = TestInputCell(initialValue = "~1~")
        val temporaryReplacementInputCell2 = TestInputCell(initialValue = "~2~")
        val finalReplacementInputCell1 = TestInputCell(initialValue = "~1!a")

        val initialTaggedInputCells = taggedBagOf(
            FuseEntryTag.Tag1 to initialInputCell1,
            FuseEntryTag.Tag2 to initialInputCell2,
            FuseEntryTag.Tag3 to replacedInputCell1,
            FuseEntryTag.Tag4 to replacedInputCell2,
        )

        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = initialTaggedInputCells,
        )

        val subjectReactiveBag = inputReactiveBag.fuse()

        ReactiveBag_fuse_testUtils.testReaction(
            inputReactiveBag = inputReactiveBag,
            inputCellByLabel = mapOf(
                "initial 1" to initialInputCell1,
                "initial 2" to initialInputCell2,
                "replaced 1" to replacedInputCell1,
                "replaced 2" to replacedInputCell2,
                "temporary replacement 1" to temporaryReplacementInputCell1,
                "temporary replacement 2" to temporaryReplacementInputCell2,
                "final replacement 1" to finalReplacementInputCell1,
            ),
            subjectReactiveBag = subjectReactiveBag,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    inputReactiveBag.change(
                        TestInputReactiveBag.ChangeDescription(
                            replacedElementByTag = mapOf(
                                FuseEntryTag.Tag3 to temporaryReplacementInputCell1,
                                FuseEntryTag.Tag4 to temporaryReplacementInputCell2,
                            ),
                        ),
                    ),
                    inputReactiveBag.correctChange(
                        TestInputReactiveBag.ChangeDescription(
                            replacedElementByTag = mapOf(
                                FuseEntryTag.Tag3 to finalReplacementInputCell1,
                            ),
                        ),
                    ),
                    // One of the final replacement cells updates _after_ the change is corrected
                    finalReplacementInputCell1.update(newValue = "~1!b"),
                ),
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "~1",
                    FuseEntryTag.Tag4 to "~2",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "~1!b",
                    FuseEntryTag.Tag4 to "~2",
                ),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    @Test
    fun test_bagChanges_replacedCells_bagChangeCorrected_someCellsReplacedDifferently_deactivated() {
        test_bagChanges_replacedCells_bagChangeCorrected_someCellsReplacedDifferently(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChanges_replacedCells_bagChangeCorrected_someCellsReplacedDifferently_keptAlive() {
        test_bagChanges_replacedCells_bagChangeCorrected_someCellsReplacedDifferently(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The input bag changes. Some tags/cells are replaced. The input bag change is corrected. Some of the previously
     * replaced tags/cells are now replaced to a different cell.
     */
    private fun test_bagChanges_replacedCells_bagChangeCorrected_someCellsReplacedDifferently(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val initialInputCell1 = TestInputCell(initialValue = "#1")
        val initialInputCell2 = TestInputCell(initialValue = "#2")
        val replacedInputCell1 = TestInputCell(initialValue = "~1")
        val replacedInputCell2 = TestInputCell(initialValue = "~2")
        val temporaryReplacementInputCell1 = TestInputCell(initialValue = "~1~")
        val temporaryReplacementInputCell2 = TestInputCell(initialValue = "~2~")
        val finalReplacementInputCell1 = TestInputCell(initialValue = "~1!")
        val finalReplacementInputCell2 = TestInputCell(initialValue = "~2!")

        val initialTaggedInputCells = taggedBagOf(
            FuseEntryTag.Tag1 to initialInputCell1,
            FuseEntryTag.Tag2 to initialInputCell2,
            FuseEntryTag.Tag3 to replacedInputCell1,
            FuseEntryTag.Tag4 to replacedInputCell2,
        )

        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = initialTaggedInputCells,
        )

        val subjectReactiveBag = inputReactiveBag.fuse()

        ReactiveBag_fuse_testUtils.testReaction(
            inputReactiveBag = inputReactiveBag,
            inputCellByLabel = mapOf(
                "initial 1" to initialInputCell1,
                "initial 2" to initialInputCell2,
                "replaced 1" to replacedInputCell1,
                "replaced 2" to replacedInputCell2,
                "temporary replacement 1" to temporaryReplacementInputCell1,
                "temporary replacement 2" to temporaryReplacementInputCell2,
                "final replacement 1" to finalReplacementInputCell1,
                "final replacement 2" to finalReplacementInputCell2,
            ),
            subjectReactiveBag = subjectReactiveBag,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    inputReactiveBag.change(
                        TestInputReactiveBag.ChangeDescription(
                            replacedElementByTag = mapOf(
                                FuseEntryTag.Tag3 to temporaryReplacementInputCell1,
                                FuseEntryTag.Tag4 to temporaryReplacementInputCell2,
                            ),
                        ),
                    ),
                    inputReactiveBag.correctChange(
                        TestInputReactiveBag.ChangeDescription(
                            replacedElementByTag = mapOf(
                                FuseEntryTag.Tag3 to finalReplacementInputCell1,
                                FuseEntryTag.Tag4 to finalReplacementInputCell2,
                            ),
                        ),
                    ),
                ),
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "~1",
                    FuseEntryTag.Tag4 to "~2",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "~1!",
                    FuseEntryTag.Tag4 to "~2!",
                ),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    @Test
    fun test_bagChanges_replacedCells_bagChangeCorrected_someCellsReplacedDifferently_temporaryReplacementCellUpdates_deactivated() {
        test_bagChanges_replacedCells_bagChangeCorrected_someCellsReplacedDifferently_temporaryReplacementCellUpdates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChanges_replacedCells_bagChangeCorrected_someCellsReplacedDifferently_temporaryReplacementCellUpdates_keptAlive() {
        test_bagChanges_replacedCells_bagChangeCorrected_someCellsReplacedDifferently_temporaryReplacementCellUpdates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The input bag changes. Some tags/cells are replaced. The input bag change is corrected. Some of the previously
     * replaced tags/cells are now replaced to a different cell. One of the temporarily replaced cells updates after the
     * correction.
     */
    private fun test_bagChanges_replacedCells_bagChangeCorrected_someCellsReplacedDifferently_temporaryReplacementCellUpdates(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val initialInputCell1 = TestInputCell(initialValue = "#1")
        val initialInputCell2 = TestInputCell(initialValue = "#2")
        val replacedInputCell1 = TestInputCell(initialValue = "~1")
        val replacedInputCell2 = TestInputCell(initialValue = "~2")
        val temporaryReplacementInputCell1 = TestInputCell(initialValue = "~1~a")
        val temporaryReplacementInputCell2 = TestInputCell(initialValue = "~2~")
        val finalReplacementInputCell1 = TestInputCell(initialValue = "~1!")
        val finalReplacementInputCell2 = TestInputCell(initialValue = "~2!")

        val initialTaggedInputCells = taggedBagOf(
            FuseEntryTag.Tag1 to initialInputCell1,
            FuseEntryTag.Tag2 to initialInputCell2,
            FuseEntryTag.Tag3 to replacedInputCell1,
            FuseEntryTag.Tag4 to replacedInputCell2,
        )

        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = initialTaggedInputCells,
        )

        val subjectReactiveBag = inputReactiveBag.fuse()

        ReactiveBag_fuse_testUtils.testReaction(
            inputReactiveBag = inputReactiveBag,
            inputCellByLabel = mapOf(
                "initial 1" to initialInputCell1,
                "initial 2" to initialInputCell2,
                "replaced 1" to replacedInputCell1,
                "replaced 2" to replacedInputCell2,
                "temporary replacement 1" to temporaryReplacementInputCell1,
                "temporary replacement 2" to temporaryReplacementInputCell2,
                "final replacement 1" to finalReplacementInputCell1,
                "final replacement 2" to finalReplacementInputCell2,
            ),
            subjectReactiveBag = subjectReactiveBag,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    inputReactiveBag.change(
                        TestInputReactiveBag.ChangeDescription(
                            replacedElementByTag = mapOf(
                                FuseEntryTag.Tag3 to temporaryReplacementInputCell1,
                                FuseEntryTag.Tag4 to temporaryReplacementInputCell2,
                            ),
                        ),
                    ),
                    inputReactiveBag.correctChange(
                        TestInputReactiveBag.ChangeDescription(
                            replacedElementByTag = mapOf(
                                FuseEntryTag.Tag3 to finalReplacementInputCell1,
                                FuseEntryTag.Tag4 to finalReplacementInputCell2,
                            ),
                        ),
                    ),
                    temporaryReplacementInputCell1.update(newValue = "~1~b"),
                ),
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "~1",
                    FuseEntryTag.Tag4 to "~2",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "~1!",
                    FuseEntryTag.Tag4 to "~2!",
                ),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    @Test
    fun test_bagChanges_replacedCells_bagChangeCorrected_someCellsReplacedDifferently_finalReplacementCellUpdates_deactivated() {
        test_bagChanges_replacedCells_bagChangeCorrected_someCellsReplacedDifferently_finalReplacementCellUpdates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChanges_replacedCells_bagChangeCorrected_someCellsReplacedDifferently_finalReplacementCellUpdates_keptAlive() {
        test_bagChanges_replacedCells_bagChangeCorrected_someCellsReplacedDifferently_finalReplacementCellUpdates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The input bag changes. Some tags/cells are replaced. The input bag change is corrected. Some of the previously
     * replaced tags/cells are now replaced to a different cell. One of the final replacement cells updates after the
     * correction.
     */
    private fun test_bagChanges_replacedCells_bagChangeCorrected_someCellsReplacedDifferently_finalReplacementCellUpdates(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val initialInputCell1 = TestInputCell(initialValue = "#1")
        val initialInputCell2 = TestInputCell(initialValue = "#2")
        val replacedInputCell1 = TestInputCell(initialValue = "~1")
        val replacedInputCell2 = TestInputCell(initialValue = "~2")
        val temporaryReplacementInputCell1 = TestInputCell(initialValue = "~1~")
        val temporaryReplacementInputCell2 = TestInputCell(initialValue = "~2~")
        val finalReplacementInputCell1 = TestInputCell(initialValue = "~1!a")
        val finalReplacementInputCell2 = TestInputCell(initialValue = "~2!")

        val initialTaggedInputCells = taggedBagOf(
            FuseEntryTag.Tag1 to initialInputCell1,
            FuseEntryTag.Tag2 to initialInputCell2,
            FuseEntryTag.Tag3 to replacedInputCell1,
            FuseEntryTag.Tag4 to replacedInputCell2,
        )

        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = initialTaggedInputCells,
        )

        val subjectReactiveBag = inputReactiveBag.fuse()

        ReactiveBag_fuse_testUtils.testReaction(
            inputReactiveBag = inputReactiveBag,
            inputCellByLabel = mapOf(
                "initial 1" to initialInputCell1,
                "initial 2" to initialInputCell2,
                "replaced 1" to replacedInputCell1,
                "replaced 2" to replacedInputCell2,
                "temporary replacement 1" to temporaryReplacementInputCell1,
                "temporary replacement 2" to temporaryReplacementInputCell2,
                "final replacement 1" to finalReplacementInputCell1,
                "final replacement 2" to finalReplacementInputCell2,
            ),
            subjectReactiveBag = subjectReactiveBag,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestSequentialStimulationSet(
                    includedStimulations = setOf(
                        TestStimulation.combineInProvidedOrder(
                            inputReactiveBag.change(
                                TestInputReactiveBag.ChangeDescription(
                                    replacedElementByTag = mapOf(
                                        FuseEntryTag.Tag3 to temporaryReplacementInputCell1,
                                        FuseEntryTag.Tag4 to temporaryReplacementInputCell2,
                                    ),
                                ),
                            ),
                            inputReactiveBag.correctChange(
                                TestInputReactiveBag.ChangeDescription(
                                    replacedElementByTag = mapOf(
                                        FuseEntryTag.Tag3 to finalReplacementInputCell1,
                                        FuseEntryTag.Tag4 to finalReplacementInputCell2,
                                    ),
                                ),
                            ),
                        ),
                        TestStimulation.combineInProvidedOrder(
                            finalReplacementInputCell1.update(newValue = "~1!b"),
                        ),
                    ),
                ).determinizeArbitrarily(),
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "~1",
                    FuseEntryTag.Tag4 to "~2",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "~1!b",
                    FuseEntryTag.Tag4 to "~2!",
                ),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    @Test
    fun test_bagChanges_replacedCells_bagChangeCorrected_someReplacedCellsNowRemoved_deactivated() {
        test_bagChanges_replacedCells_bagChangeCorrected_someReplacedCellsNowRemoved(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChanges_replacedCells_bagChangeCorrected_someReplacedCellsNowRemoved_keptAlive() {
        test_bagChanges_replacedCells_bagChangeCorrected_someReplacedCellsNowRemoved(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The input bag changes. Some tags/cells are replaced. The input bag change is corrected. Some of the previously
     * replaced tags/cells are now removed.
     */
    private fun test_bagChanges_replacedCells_bagChangeCorrected_someReplacedCellsNowRemoved(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val initialInputCell1 = TestInputCell(initialValue = "#1")
        val initialInputCell2 = TestInputCell(initialValue = "#2")
        val replacedInputCell1 = TestInputCell(initialValue = "~1")
        val replacedInputCell2 = TestInputCell(initialValue = "~2")
        val temporaryReplacementInputCell1 = TestInputCell(initialValue = "~1~")
        val temporaryReplacementInputCell2 = TestInputCell(initialValue = "~2~")

        val initialTaggedInputCells = taggedBagOf(
            FuseEntryTag.Tag1 to initialInputCell1,
            FuseEntryTag.Tag2 to initialInputCell2,
            FuseEntryTag.Tag3 to replacedInputCell1,
            FuseEntryTag.Tag4 to replacedInputCell2,
        )

        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = initialTaggedInputCells,
        )

        val subjectReactiveBag = inputReactiveBag.fuse()

        ReactiveBag_fuse_testUtils.testReaction(
            inputReactiveBag = inputReactiveBag,
            inputCellByLabel = mapOf(
                "initial 1" to initialInputCell1,
                "initial 2" to initialInputCell2,
                "replaced 1" to replacedInputCell1,
                "replaced 2" to replacedInputCell2,
                "temporary replacement 1" to temporaryReplacementInputCell1,
                "temporary replacement 2" to temporaryReplacementInputCell2,
            ),
            subjectReactiveBag = subjectReactiveBag,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    inputReactiveBag.change(
                        TestInputReactiveBag.ChangeDescription(
                            replacedElementByTag = mapOf(
                                FuseEntryTag.Tag3 to temporaryReplacementInputCell1,
                                FuseEntryTag.Tag4 to temporaryReplacementInputCell2,
                            ),
                        ),
                    ),
                    inputReactiveBag.correctChange(
                        TestInputReactiveBag.ChangeDescription(
                            removedTags = setOf(
                                FuseEntryTag.Tag3,
                                FuseEntryTag.Tag4,
                            ),
                        ),
                    ),
                ),
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "~1",
                    FuseEntryTag.Tag4 to "~2",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                ),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    @Test
    fun test_bagChanges_replacedCells_bagChangeCorrected_someReplacedCellsNowRemoved_temporaryReplacementCellUpdates_deactivated() {
        test_bagChanges_replacedCells_bagChangeCorrected_someReplacedCellsNowRemoved_temporaryReplacementCellUpdates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChanges_replacedCells_bagChangeCorrected_someReplacedCellsNowRemoved_temporaryReplacementCellUpdates_keptAlive() {
        test_bagChanges_replacedCells_bagChangeCorrected_someReplacedCellsNowRemoved_temporaryReplacementCellUpdates(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The input bag changes. Some tags/cells are replaced. The input bag change is corrected. Some of the previously
     * replaced tags/cells are now removed. One of the temporary replacement cells updates after the correction.
     */
    private fun test_bagChanges_replacedCells_bagChangeCorrected_someReplacedCellsNowRemoved_temporaryReplacementCellUpdates(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val initialInputCell1 = TestInputCell(initialValue = "#1")
        val initialInputCell2 = TestInputCell(initialValue = "#2")
        val replacedInputCell1 = TestInputCell(initialValue = "~1")
        val replacedInputCell2 = TestInputCell(initialValue = "~2")
        val temporaryReplacementInputCell1 = TestInputCell(initialValue = "~1~a")
        val temporaryReplacementInputCell2 = TestInputCell(initialValue = "~2~")

        val initialTaggedInputCells = taggedBagOf(
            FuseEntryTag.Tag1 to initialInputCell1,
            FuseEntryTag.Tag2 to initialInputCell2,
            FuseEntryTag.Tag3 to replacedInputCell1,
            FuseEntryTag.Tag4 to replacedInputCell2,
        )

        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = initialTaggedInputCells,
        )

        val subjectReactiveBag = inputReactiveBag.fuse()

        ReactiveBag_fuse_testUtils.testReaction(
            inputReactiveBag = inputReactiveBag,
            inputCellByLabel = mapOf(
                "initial 1" to initialInputCell1,
                "initial 2" to initialInputCell2,
                "replaced 1" to replacedInputCell1,
                "replaced 2" to replacedInputCell2,
                "temporary replacement 1" to temporaryReplacementInputCell1,
                "temporary replacement 2" to temporaryReplacementInputCell2,
            ),
            subjectReactiveBag = subjectReactiveBag,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combineInProvidedOrder(
                    inputReactiveBag.change(
                        TestInputReactiveBag.ChangeDescription(
                            replacedElementByTag = mapOf(
                                FuseEntryTag.Tag3 to temporaryReplacementInputCell1,
                                FuseEntryTag.Tag4 to temporaryReplacementInputCell2,
                            ),
                        ),
                    ),
                    inputReactiveBag.correctChange(
                        TestInputReactiveBag.ChangeDescription(
                            removedTags = setOf(
                                FuseEntryTag.Tag3,
                                FuseEntryTag.Tag4,
                            ),
                        ),
                    ),
                    temporaryReplacementInputCell1.update(newValue = "~1~b"),
                ),
            ),
            expectedSubjectContentTransition = ReactiveBag_expectations_testUtils.expectTaggedContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                    FuseEntryTag.Tag3 to "~1",
                    FuseEntryTag.Tag4 to "~2",
                ),
                expectedNewTaggedElements = taggedBagOf(
                    FuseEntryTag.Tag1 to "#1",
                    FuseEntryTag.Tag2 to "#2",
                ),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    @Test
    fun test_offlineActivation() {
        // TODO
    }
}
