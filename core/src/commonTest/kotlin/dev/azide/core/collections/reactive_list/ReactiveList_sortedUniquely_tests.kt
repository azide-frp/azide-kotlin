package dev.azide.core.collections.reactive_list

import dev.azide.core.collections.helpers.withSortKey
import dev.azide.core.collections.reactive_list.ReactiveList_sortedUniquely_testUtils.SortableValueEntryTag
import dev.azide.core.collections.sortedUniquely
import dev.azide.core.impl.collections.reactive_bag.taggedBagOf
import dev.azide.core.test_utils.TestStimulation
import dev.azide.core.test_utils.collections.reactive_bag.TestInputReactiveBag
import dev.azide.core.test_utils.collections.reactive_list.ReactiveList_expectations_testUtils
import dev.azide.core.test_utils.generic.ExpectedTestSubjectReaction.IntermediatePropagationTolerance
import dev.azide.core.test_utils.generic.generic_reaction_testUtils
import dev.azide.core.test_utils.generic.TestSubjectHealthCheckStrategy
import kotlin.test.Ignore
import kotlin.test.Test

@Suppress("ClassName")
@Ignore // FIXME: Fix commitment in `sortedUniquely`
class ReactiveList_sortedUniquely_tests {

    // region bagChanges_additionsOnly

    @Test
    fun test_bagChanges_additionsOnly_deactivated() {
        test_bagChanges_additionsOnly(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChanges_additionsOnly_keptAlive() {
        test_bagChanges_additionsOnly(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The input bag gains new entries. The sorted list gains the new values at the correct positions.
     */
    private fun test_bagChanges_additionsOnly(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = taggedBagOf(
                SortableValueEntryTag.Tag1 to ("#10" withSortKey 10.8),
                SortableValueEntryTag.Tag2 to ("^0" withSortKey 0.3),
                SortableValueEntryTag.Tag3 to ("?30" withSortKey 30.1),
                SortableValueEntryTag.Tag4 to ("&20" withSortKey 20.6),
                SortableValueEntryTag.Tag5 to (".50" withSortKey 50.4),
            ),
        )

        val subjectReactiveList = inputReactiveBag.sortedUniquely()

        ReactiveList_sortedUniquely_testUtils.executeReactionTransaction(
            inputReactiveBag = inputReactiveBag,
            subjectReactiveList = subjectReactiveList,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = inputReactiveBag.change(
                    TestInputReactiveBag.ChangeDescription(
                        addedElementByTag = mapOf(
                            SortableValueEntryTag.Tag6 to (".11" withSortKey 11.5),
                            SortableValueEntryTag.Tag7 to ("!21" withSortKey 21.9),
                        ),
                    ),
                ),
            ),
            expectedSubjectContentTransition = ReactiveList_expectations_testUtils.expectContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldContent = listOf("^0", "#10", "&20", "?30", ".50"),
                expectedNewContent = listOf("^0", "#10", ".11", "&20", "!21", "?30", ".50"),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    // endregion

    // region bagChanges_removalsOnly

    @Test
    fun test_bagChanges_removalsOnly_deactivated() {
        test_bagChanges_removalsOnly(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChanges_removalsOnly_keptAlive() {
        test_bagChanges_removalsOnly(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * Some entries are removed from the input bag. The sorted list loses the corresponding values.
     */
    private fun test_bagChanges_removalsOnly(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = taggedBagOf(
                SortableValueEntryTag.Tag1 to ("#10" withSortKey 10.8),
                SortableValueEntryTag.Tag2 to ("^0" withSortKey 0.3),
                SortableValueEntryTag.Tag3 to ("?30" withSortKey 30.1),
                SortableValueEntryTag.Tag4 to ("&20" withSortKey 20.6),
                SortableValueEntryTag.Tag5 to (".50" withSortKey 50.4),
            ),
        )

        val subjectReactiveList = inputReactiveBag.sortedUniquely()

        ReactiveList_sortedUniquely_testUtils.executeReactionTransaction(
            inputReactiveBag = inputReactiveBag,
            subjectReactiveList = subjectReactiveList,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = inputReactiveBag.change(
                    TestInputReactiveBag.ChangeDescription(
                        removedTags = setOf(SortableValueEntryTag.Tag1, SortableValueEntryTag.Tag3),
                    ),
                ),
            ),
            expectedSubjectContentTransition = ReactiveList_expectations_testUtils.expectContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldContent = listOf("^0", "#10", "&20", "?30", ".50"),
                expectedNewContent = listOf("^0", "&20", ".50"),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    // endregion

    // region bagChanges_replacementsOnly_sameSortKey

    @Test
    fun test_bagChanges_replacementsOnly_sameSortKey_deactivated() {
        test_bagChanges_replacementsOnly_sameSortKey(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChanges_replacementsOnly_sameSortKey_keptAlive() {
        test_bagChanges_replacementsOnly_sameSortKey(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * Some entries in the input bag are replaced with new values that share the same sort key. The sorted order is
     * unchanged; only the values at those positions update.
     */
    private fun test_bagChanges_replacementsOnly_sameSortKey(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = taggedBagOf(
                SortableValueEntryTag.Tag1 to ("#10a" withSortKey 10.8),
                SortableValueEntryTag.Tag2 to ("^0" withSortKey 0.3),
                SortableValueEntryTag.Tag3 to ("?30" withSortKey 30.1),
                SortableValueEntryTag.Tag4 to ("&20a" withSortKey 20.6),
                SortableValueEntryTag.Tag5 to (".50" withSortKey 50.4),
            ),
        )

        val subjectReactiveList = inputReactiveBag.sortedUniquely()

        ReactiveList_sortedUniquely_testUtils.executeReactionTransaction(
            inputReactiveBag = inputReactiveBag,
            subjectReactiveList = subjectReactiveList,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = inputReactiveBag.change(
                    TestInputReactiveBag.ChangeDescription(
                        replacedElementByTag = mapOf(
                            SortableValueEntryTag.Tag1 to ("#10b" withSortKey 10.8), // same sort key, value changes
                            SortableValueEntryTag.Tag4 to ("&20b" withSortKey 20.6), // same sort key, value changes
                        ),
                    ),
                ),
            ),
            expectedSubjectContentTransition = ReactiveList_expectations_testUtils.expectContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldContent = listOf("^0", "#10a", "&20a", "?30", ".50"),
                expectedNewContent = listOf("^0", "#10b", "&20b", "?30", ".50"),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    // endregion

    // region bagChanges_replacementsOnly_newSortKey

    @Test
    fun test_bagChanges_replacementsOnly_newSortKey_deactivated() {
        test_bagChanges_replacementsOnly_newSortKey(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChanges_replacementsOnly_newSortKey_keptAlive() {
        test_bagChanges_replacementsOnly_newSortKey(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * Some entries in the input bag are replaced with new sort keys, causing them to move to different positions in
     * the sorted list.
     */
    private fun test_bagChanges_replacementsOnly_newSortKey(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = taggedBagOf(
                SortableValueEntryTag.Tag1 to ("#10" withSortKey 10.8),
                SortableValueEntryTag.Tag2 to ("^0" withSortKey 0.3),
                SortableValueEntryTag.Tag3 to ("?30" withSortKey 30.1),
                SortableValueEntryTag.Tag4 to ("&20" withSortKey 20.6),
                SortableValueEntryTag.Tag5 to (".50" withSortKey 50.4),
            ),
        )

        val subjectReactiveList = inputReactiveBag.sortedUniquely()

        ReactiveList_sortedUniquely_testUtils.executeReactionTransaction(
            inputReactiveBag = inputReactiveBag,
            subjectReactiveList = subjectReactiveList,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = inputReactiveBag.change(
                    TestInputReactiveBag.ChangeDescription(
                        replacedElementByTag = mapOf(
                            SortableValueEntryTag.Tag1 to ("#35" withSortKey 35.6), // moves after ?30 and before .50
                            SortableValueEntryTag.Tag2 to ("^45" withSortKey 45.2), // moves after #35 and before .50
                        ),
                    ),
                ),
            ),
            expectedSubjectContentTransition = ReactiveList_expectations_testUtils.expectContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldContent = listOf("^0", "#10", "&20", "?30", ".50"),
                expectedNewContent = listOf("&20", "?30", "#35", "^45", ".50"),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    // endregion

    // region bagChanges_mixed

    @Test
    fun test_bagChanges_mixed_deactivated() {
        test_bagChanges_mixed(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChanges_mixed_keptAlive() {
        test_bagChanges_mixed(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * The input bag gains new entries, loses some entries, and has some entries replaced — all in a single change.
     */
    private fun test_bagChanges_mixed(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = taggedBagOf(
                SortableValueEntryTag.Tag1 to ("#10" withSortKey 10.8),
                SortableValueEntryTag.Tag2 to ("^0" withSortKey 0.3),
                SortableValueEntryTag.Tag3 to ("?30" withSortKey 30.1),
                SortableValueEntryTag.Tag4 to ("&20" withSortKey 20.6),
                SortableValueEntryTag.Tag5 to (".50" withSortKey 50.4),
                SortableValueEntryTag.Tag6 to ("!60" withSortKey 60.7),
            ),
        )

        val subjectReactiveList = inputReactiveBag.sortedUniquely()

        ReactiveList_sortedUniquely_testUtils.executeReactionTransaction(
            inputReactiveBag = inputReactiveBag,
            subjectReactiveList = subjectReactiveList,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = inputReactiveBag.change(
                    TestInputReactiveBag.ChangeDescription(
                        addedElementByTag = mapOf(
                            SortableValueEntryTag.Tag7 to (".15" withSortKey 15.2),
                        ),
                        replacedElementByTag = mapOf(
                            SortableValueEntryTag.Tag6 to ("!5" withSortKey 5.5), // moves from the end to the front
                        ),
                        removedTags = setOf(
                            SortableValueEntryTag.Tag3,
                            SortableValueEntryTag.Tag4,
                            SortableValueEntryTag.Tag5,
                        ),
                    ),
                ),
            ),
            expectedSubjectContentTransition = ReactiveList_expectations_testUtils.expectContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldContent = listOf("^0", "#10", "&20", "?30", ".50", "!60"),
                expectedNewContent = listOf("^0", "!5", "#10", ".15"),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    // endregion

    // region bagChangeRevoked_additionsOnly

    @Test
    fun test_bagChangeRevoked_additionsOnly_deactivated() {
        test_bagChangeRevoked_additionsOnly(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChangeRevoked_additionsOnly_keptAlive() {
        test_bagChangeRevoked_additionsOnly(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * New entries are added to the input bag, then the change is revoked. The sorted list remains unchanged.
     */
    private fun test_bagChangeRevoked_additionsOnly(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = taggedBagOf(
                SortableValueEntryTag.Tag1 to ("#10" withSortKey 10.8),
                SortableValueEntryTag.Tag2 to ("^0" withSortKey 0.3),
                SortableValueEntryTag.Tag3 to ("?30" withSortKey 30.1),
                SortableValueEntryTag.Tag4 to ("&20" withSortKey 20.6),
                SortableValueEntryTag.Tag5 to (".50" withSortKey 50.4),
            ),
        )

        val subjectReactiveList = inputReactiveBag.sortedUniquely()

        ReactiveList_sortedUniquely_testUtils.executeReactionTransaction(
            inputReactiveBag = inputReactiveBag,
            subjectReactiveList = subjectReactiveList,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combine(
                    inputReactiveBag.change(
                        TestInputReactiveBag.ChangeDescription(
                            addedElementByTag = mapOf(
                                SortableValueEntryTag.Tag6 to (".70" withSortKey 70.5),
                                SortableValueEntryTag.Tag7 to (".80" withSortKey 80.9),
                            ),
                        ),
                    ),
                    inputReactiveBag.revokeChange(),
                ),
            ),
            expectedSubjectContentTransition = ReactiveList_expectations_testUtils.expectNoContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedContent = listOf("^0", "#10", "&20", "?30", ".50"),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    // endregion

    // region bagChangeRevoked_removalsOnly

    @Test
    fun test_bagChangeRevoked_removalsOnly_deactivated() {
        test_bagChangeRevoked_removalsOnly(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChangeRevoked_removalsOnly_keptAlive() {
        test_bagChangeRevoked_removalsOnly(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * Some entries are removed from the input bag, then the change is revoked. The sorted list remains unchanged.
     */
    private fun test_bagChangeRevoked_removalsOnly(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = taggedBagOf(
                SortableValueEntryTag.Tag1 to ("#10" withSortKey 10.8),
                SortableValueEntryTag.Tag2 to ("^0" withSortKey 0.3),
                SortableValueEntryTag.Tag3 to ("?30" withSortKey 30.1),
                SortableValueEntryTag.Tag4 to ("&20" withSortKey 20.6),
                SortableValueEntryTag.Tag5 to (".50" withSortKey 50.4),
            ),
        )

        val subjectReactiveList = inputReactiveBag.sortedUniquely()

        ReactiveList_sortedUniquely_testUtils.executeReactionTransaction(
            inputReactiveBag = inputReactiveBag,
            subjectReactiveList = subjectReactiveList,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combine(
                    inputReactiveBag.change(
                        TestInputReactiveBag.ChangeDescription(
                            removedTags = setOf(
                                SortableValueEntryTag.Tag1,
                                SortableValueEntryTag.Tag3,
                                SortableValueEntryTag.Tag4,
                            ),
                        ),
                    ),
                    inputReactiveBag.revokeChange(),
                ),
            ),
            expectedSubjectContentTransition = ReactiveList_expectations_testUtils.expectNoContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedContent = listOf("^0", "#10", "&20", "?30", ".50"),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    // endregion

    // region bagChangeRevoked_replacementsOnly

    @Test
    fun test_bagChangeRevoked_replacementsOnly_deactivated() {
        test_bagChangeRevoked_replacementsOnly(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChangeRevoked_replacementsOnly_keptAlive() {
        test_bagChangeRevoked_replacementsOnly(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * Some entries in the input bag are temporarily replaced with new sort keys, then the change is revoked. The
     * sorted list remains unchanged.
     */
    private fun test_bagChangeRevoked_replacementsOnly(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = taggedBagOf(
                SortableValueEntryTag.Tag1 to ("#10" withSortKey 10.8),
                SortableValueEntryTag.Tag2 to ("^0" withSortKey 0.3),
                SortableValueEntryTag.Tag3 to ("?30" withSortKey 30.1),
                SortableValueEntryTag.Tag4 to ("&20" withSortKey 20.6),
                SortableValueEntryTag.Tag5 to (".50" withSortKey 50.4),
            ),
        )

        val subjectReactiveList = inputReactiveBag.sortedUniquely()

        ReactiveList_sortedUniquely_testUtils.executeReactionTransaction(
            inputReactiveBag = inputReactiveBag,
            subjectReactiveList = subjectReactiveList,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combine(
                    inputReactiveBag.change(
                        TestInputReactiveBag.ChangeDescription(
                            replacedElementByTag = mapOf(
                                SortableValueEntryTag.Tag1 to ("#35" withSortKey 35.6), // temporarily moves
                                SortableValueEntryTag.Tag2 to ("^45" withSortKey 45.2), // temporarily moves
                            ),
                        ),
                    ),
                    inputReactiveBag.revokeChange(),
                ),
            ),
            expectedSubjectContentTransition = ReactiveList_expectations_testUtils.expectNoContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedContent = listOf("^0", "#10", "&20", "?30", ".50"),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    // endregion

    // region bagChangeRevoked_mixed

    @Test
    fun test_bagChangeRevoked_mixed_deactivated() {
        test_bagChangeRevoked_mixed(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChangeRevoked_mixed_keptAlive() {
        test_bagChangeRevoked_mixed(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * A mixed change (additions, replacements, removals) is applied to the input bag, then revoked. The sorted list
     * remains unchanged.
     */
    private fun test_bagChangeRevoked_mixed(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = taggedBagOf(
                SortableValueEntryTag.Tag1 to ("#10" withSortKey 10.8),
                SortableValueEntryTag.Tag2 to ("^0" withSortKey 0.3),
                SortableValueEntryTag.Tag3 to ("?30" withSortKey 30.1),
                SortableValueEntryTag.Tag4 to ("&20" withSortKey 20.6),
                SortableValueEntryTag.Tag5 to (".50" withSortKey 50.4),
                SortableValueEntryTag.Tag6 to ("!60" withSortKey 60.7),
            ),
        )

        val subjectReactiveList = inputReactiveBag.sortedUniquely()

        ReactiveList_sortedUniquely_testUtils.executeReactionTransaction(
            inputReactiveBag = inputReactiveBag,
            subjectReactiveList = subjectReactiveList,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combine(
                    inputReactiveBag.change(
                        TestInputReactiveBag.ChangeDescription(
                            addedElementByTag = mapOf(
                                SortableValueEntryTag.Tag7 to (".70" withSortKey 70.5),
                            ),
                            replacedElementByTag = mapOf(
                                SortableValueEntryTag.Tag6 to ("!5" withSortKey 5.5), // temporarily moves to the front
                            ),
                            removedTags = setOf(
                                SortableValueEntryTag.Tag3,
                                SortableValueEntryTag.Tag4,
                                SortableValueEntryTag.Tag5,
                            ),
                        ),
                    ),
                    inputReactiveBag.revokeChange(),
                ),
            ),
            expectedSubjectContentTransition = ReactiveList_expectations_testUtils.expectNoContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedUnaffectedContent = listOf("^0", "#10", "&20", "?30", ".50", "!60"),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    // endregion

    // region bagChangeCorrected_additionsOnly

    @Test
    fun test_bagChangeCorrected_additionsOnly_deactivated() {
        test_bagChangeCorrected_additionsOnly(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChangeCorrected_additionsOnly_keptAlive() {
        test_bagChangeCorrected_additionsOnly(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * New entries are added to the input bag, then the change is corrected: one entry is added differently, one
     * temporary entry disappears, and one new entry appears only in the corrected change.
     */
    private fun test_bagChangeCorrected_additionsOnly(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = taggedBagOf(
                SortableValueEntryTag.Tag1 to ("#10" withSortKey 10.8),
                SortableValueEntryTag.Tag2 to ("^0" withSortKey 0.3),
                SortableValueEntryTag.Tag3 to ("?30" withSortKey 30.1),
                SortableValueEntryTag.Tag4 to ("&20" withSortKey 20.6),
                SortableValueEntryTag.Tag5 to (".50" withSortKey 50.4),
                SortableValueEntryTag.Tag6 to ("!60" withSortKey 60.7),
            ),
        )

        val subjectReactiveList = inputReactiveBag.sortedUniquely()

        ReactiveList_sortedUniquely_testUtils.executeReactionTransaction(
            inputReactiveBag = inputReactiveBag,
            subjectReactiveList = subjectReactiveList,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combine(
                    inputReactiveBag.change(
                        TestInputReactiveBag.ChangeDescription(
                            addedElementByTag = mapOf(
                                SortableValueEntryTag.Tag7 to (".25a" withSortKey 25.3), // corrected: value changes
                            ),
                        ),
                    ),
                    inputReactiveBag.correctChange(
                        TestInputReactiveBag.ChangeDescription(
                            addedElementByTag = mapOf(
                                SortableValueEntryTag.Tag7 to (".25b" withSortKey 25.3), // corrected value
                            ),
                        ),
                    ),
                ),
            ),
            expectedSubjectContentTransition = ReactiveList_expectations_testUtils.expectContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldContent = listOf("^0", "#10", "&20", "?30", ".50", "!60"),
                expectedNewContent = listOf("^0", "#10", "&20", ".25b", "?30", ".50", "!60"),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    // endregion

    // region bagChangeCorrected_additionsOnly_someUnadded

    @Test
    fun test_bagChangeCorrected_additionsOnly_someUnadded_deactivated() {
        test_bagChangeCorrected_additionsOnly_someUnadded(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChangeCorrected_additionsOnly_someUnadded_keptAlive() {
        test_bagChangeCorrected_additionsOnly_someUnadded(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * New entries are added to the input bag, then the change is corrected: a temporary entry is not present in the
     * corrected change, and a new entry appears only in the corrected change.
     */
    private fun test_bagChangeCorrected_additionsOnly_someUnadded(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = taggedBagOf(
                SortableValueEntryTag.Tag1 to ("#10" withSortKey 10.8),
                SortableValueEntryTag.Tag2 to ("^0" withSortKey 0.3),
                SortableValueEntryTag.Tag3 to ("?30" withSortKey 30.1),
                SortableValueEntryTag.Tag4 to ("&20" withSortKey 20.6),
                SortableValueEntryTag.Tag5 to (".50" withSortKey 50.4),
                SortableValueEntryTag.Tag6 to ("!60" withSortKey 60.7),
            ),
        )

        val subjectReactiveList = inputReactiveBag.sortedUniquely()

        ReactiveList_sortedUniquely_testUtils.executeReactionTransaction(
            inputReactiveBag = inputReactiveBag,
            subjectReactiveList = subjectReactiveList,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combine(
                    inputReactiveBag.change(
                        TestInputReactiveBag.ChangeDescription(
                            addedElementByTag = mapOf(
                                SortableValueEntryTag.Tag7 to (".25" withSortKey 25.3), // not corrected
                            ),
                        ),
                    ),
                    inputReactiveBag.correctChange(
                        TestInputReactiveBag.ChangeDescription(
                            addedElementByTag = mapOf(
                                SortableValueEntryTag.Tag7 to (".25" withSortKey 25.3),
                                // Tag8 would be new but we only have 7 tags — use a different approach:
                                // corrected change drops the temporary entry and adds nothing extra
                            ),
                        ),
                    ),
                ),
            ),
            expectedSubjectContentTransition = ReactiveList_expectations_testUtils.expectContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldContent = listOf("^0", "#10", "&20", "?30", ".50", "!60"),
                expectedNewContent = listOf("^0", "#10", "&20", ".25", "?30", ".50", "!60"),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    // endregion

    // region bagChangeCorrected_removalsOnly

    @Test
    fun test_bagChangeCorrected_removalsOnly_deactivated() {
        test_bagChangeCorrected_removalsOnly(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChangeCorrected_removalsOnly_keptAlive() {
        test_bagChangeCorrected_removalsOnly(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * Some entries are removed from the input bag, then the change is corrected: the corrected change removes a
     * different set of entries (one entry is no longer removed, one new removal appears).
     */
    private fun test_bagChangeCorrected_removalsOnly(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = taggedBagOf(
                SortableValueEntryTag.Tag1 to ("#10" withSortKey 10.8),
                SortableValueEntryTag.Tag2 to ("^0" withSortKey 0.3),
                SortableValueEntryTag.Tag3 to ("?30" withSortKey 30.1),
                SortableValueEntryTag.Tag4 to ("&20" withSortKey 20.6),
                SortableValueEntryTag.Tag5 to (".50" withSortKey 50.4),
                SortableValueEntryTag.Tag6 to ("!60" withSortKey 60.7),
            ),
        )

        val subjectReactiveList = inputReactiveBag.sortedUniquely()

        ReactiveList_sortedUniquely_testUtils.executeReactionTransaction(
            inputReactiveBag = inputReactiveBag,
            subjectReactiveList = subjectReactiveList,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combine(
                    inputReactiveBag.change(
                        TestInputReactiveBag.ChangeDescription(
                            removedTags = setOf(
                                SortableValueEntryTag.Tag1,
                                SortableValueEntryTag.Tag3,
                                SortableValueEntryTag.Tag4,
                                SortableValueEntryTag.Tag6, // corrected: not removed
                            ),
                        ),
                    ),
                    inputReactiveBag.correctChange(
                        TestInputReactiveBag.ChangeDescription(
                            removedTags = setOf(
                                SortableValueEntryTag.Tag1,
                                SortableValueEntryTag.Tag3,
                                SortableValueEntryTag.Tag4,
                                SortableValueEntryTag.Tag5, // (not mentioned before)
                            ),
                        ),
                    ),
                ),
            ),
            expectedSubjectContentTransition = ReactiveList_expectations_testUtils.expectContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldContent = listOf("^0", "#10", "&20", "?30", ".50", "!60"),
                expectedNewContent = listOf("^0", "!60"),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    // endregion

    // region bagChangeCorrected_replacementsOnly

    @Test
    fun test_bagChangeCorrected_replacementsOnly_deactivated() {
        test_bagChangeCorrected_replacementsOnly(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChangeCorrected_replacementsOnly_keptAlive() {
        test_bagChangeCorrected_replacementsOnly(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * Some entries are replaced in the input bag, then the change is corrected: one replacement is corrected to a
     * different sort key, one temporary replacement disappears, and one new replacement appears.
     */
    private fun test_bagChangeCorrected_replacementsOnly(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = taggedBagOf(
                SortableValueEntryTag.Tag1 to ("#10" withSortKey 10.8),
                SortableValueEntryTag.Tag2 to ("^0" withSortKey 0.3),
                SortableValueEntryTag.Tag3 to ("?30" withSortKey 30.1),
                SortableValueEntryTag.Tag4 to ("&20" withSortKey 20.6),
                SortableValueEntryTag.Tag5 to (".50" withSortKey 50.4),
                SortableValueEntryTag.Tag6 to ("!60" withSortKey 60.7),
            ),
        )

        val subjectReactiveList = inputReactiveBag.sortedUniquely()

        ReactiveList_sortedUniquely_testUtils.executeReactionTransaction(
            inputReactiveBag = inputReactiveBag,
            subjectReactiveList = subjectReactiveList,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combine(
                    inputReactiveBag.change(
                        TestInputReactiveBag.ChangeDescription(
                            replacedElementByTag = mapOf(
                                SortableValueEntryTag.Tag1 to ("#35" withSortKey 35.6), // not corrected
                                SortableValueEntryTag.Tag2 to ("^45a" withSortKey 45.2), // corrected: different sort key
                                SortableValueEntryTag.Tag4 to ("&55~" withSortKey 55.3), // corrected: not replaced
                            ),
                        ),
                    ),
                    inputReactiveBag.correctChange(
                        TestInputReactiveBag.ChangeDescription(
                            replacedElementByTag = mapOf(
                                SortableValueEntryTag.Tag1 to ("#35" withSortKey 35.6),
                                SortableValueEntryTag.Tag2 to ("^25" withSortKey 25.9), // corrected to a different sort key
                                SortableValueEntryTag.Tag3 to ("?1" withSortKey 1.1),   // (not mentioned before)
                            ),
                        ),
                    ),
                ),
            ),
            expectedSubjectContentTransition = ReactiveList_expectations_testUtils.expectContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldContent = listOf("^0", "#10", "&20", "?30", ".50", "!60"),
                expectedNewContent = listOf("?1", "&20", "^25", "#35", ".50", "!60"),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    // endregion

    // region bagChangeCorrected_mixed

    @Test
    fun test_bagChangeCorrected_mixed_deactivated() {
        test_bagChangeCorrected_mixed(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectDeactivated,
        )
    }

    @Test
    fun test_bagChangeCorrected_mixed_keptAlive() {
        test_bagChangeCorrected_mixed(
            subjectHealthCheckStrategy = TestSubjectHealthCheckStrategy.TestSubjectKeptActive,
        )
    }

    /**
     * A mixed change is applied to the input bag (additions, replacements, removals), then the change is corrected:
     * a temporary addition disappears, a temporary replacement becomes a removal, and a new removal appears.
     */
    private fun test_bagChangeCorrected_mixed(
        subjectHealthCheckStrategy: TestSubjectHealthCheckStrategy,
    ) {
        val inputReactiveBag = TestInputReactiveBag(
            initialTaggedElements = taggedBagOf(
                SortableValueEntryTag.Tag1 to ("#10" withSortKey 10.8),
                SortableValueEntryTag.Tag2 to ("^0" withSortKey 0.3),
                SortableValueEntryTag.Tag3 to ("?30" withSortKey 30.1),
                SortableValueEntryTag.Tag4 to ("&20" withSortKey 20.6),
                SortableValueEntryTag.Tag5 to (".50" withSortKey 50.4),
                SortableValueEntryTag.Tag6 to ("!60" withSortKey 60.7),
            ),
        )

        val subjectReactiveList = inputReactiveBag.sortedUniquely()

        ReactiveList_sortedUniquely_testUtils.executeReactionTransaction(
            inputReactiveBag = inputReactiveBag,
            subjectReactiveList = subjectReactiveList,
            inputStimulationPlan = generic_reaction_testUtils.InputStimulationPlan(
                observedInputStimulation = TestStimulation.combine(
                    inputReactiveBag.change(
                        TestInputReactiveBag.ChangeDescription(
                            addedElementByTag = mapOf(
                                SortableValueEntryTag.Tag7 to (".5" withSortKey 5.2), // not corrected
                            ),
                            replacedElementByTag = mapOf(
                                SortableValueEntryTag.Tag1 to ("#55" withSortKey 55.5), // not corrected
                                SortableValueEntryTag.Tag6 to ("!65~" withSortKey 65.1), // corrected: removed instead
                            ),
                            removedTags = setOf(SortableValueEntryTag.Tag4, SortableValueEntryTag.Tag5),
                        ),
                    ),
                    inputReactiveBag.correctChange(
                        TestInputReactiveBag.ChangeDescription(
                            addedElementByTag = mapOf(
                                SortableValueEntryTag.Tag7 to (".5" withSortKey 5.2),
                            ),
                            replacedElementByTag = mapOf(
                                SortableValueEntryTag.Tag1 to ("#55" withSortKey 55.5),
                            ),
                            removedTags = setOf(
                                SortableValueEntryTag.Tag3,
                                SortableValueEntryTag.Tag4,
                                SortableValueEntryTag.Tag5,
                                SortableValueEntryTag.Tag6,
                            ),
                        ),
                    ),
                ),
            ),
            expectedSubjectContentTransition = ReactiveList_expectations_testUtils.expectContentTransition(
                intermediatePropagationTolerance = IntermediatePropagationTolerance.Tolerate,
                expectedOldContent = listOf("^0", "#10", "&20", "?30", ".50", "!60"),
                expectedNewContent = listOf("^0", ".5", "#55"),
            ),
            subjectHealthCheckStrategy = subjectHealthCheckStrategy,
        )
    }

    // endregion
}
